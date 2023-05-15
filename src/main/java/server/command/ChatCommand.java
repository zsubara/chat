package server.command;

import command.CommandHandler;
import dao.to.RoomTo;
import dao.to.UserTo;
import io.netty.channel.ChannelHandlerContext;
import server.chat.Chat;
import server.chat.Entry;
import server.session.SessionRepository;
import util.Constants;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ChatCommand implements CommandHandler {

    private Chat broker;
    private SessionRepository repository;

    public ChatCommand(Chat broker, SessionRepository repository) {
        this.broker = broker;
        this.repository = repository;
    }

    public Map<String, BiConsumer<UserTo, String[]>> handlers() throws RuntimeException {
        Map<String, BiConsumer<UserTo, String[]>> handlers = new HashMap<>();
        handlers.put(Constants.COMMAND_DISCONNECT, this::disconnect);
        handlers.put(Constants.COMMAND_JOIN, this::join);
        handlers.put(Constants.COMMAND_LEAVE, this::leave);
        handlers.put(Constants.COMMAND_PUBLISH, this::publish);
        handlers.put(Constants.COMMAND_LIST, this::list);
        handlers.put(Constants.COMMAND_USERS, this::users);

        return handlers;
    }

    private void join(UserTo user, String[] arguments) {
        if (arguments.length != 1) {
            throw new RuntimeException("Unexpected command arguments size");
        }
        String roomName = arguments[0];

        if (broker.getSubscribers(roomName).contains(user.getName()) && !user.isNewJoiner()) {
            if (user.getRoom().getName().equals(roomName)) {
                user.sendMeOnly("[SERVER] - you are already in the same room\r");
                return;
            }
        }

        if (!broker.getSubscribers(roomName).contains(user.getName()))
            for (String userName : broker.getSubscribers(roomName)) {
                if (userName.equals(user.getName())) {
                    continue;
                }
                sendJoinNotification(userName, user.getName());
            }

        if (user.hasRoom()) {
            broker.unsubscribe(user.getRoom().getName(), user.getName());
            if (!user.getRoom().getName().equals(roomName))
                for (String userName : broker.getSubscribers(user.getRoom().getName())) {
                    UserTo usr = repository.get(userName);
                    usr.send(String.format("[SERVER] - %s has disconnected\r", user.getName()));
                }
        }

        try {
            broker.subscribe(roomName, user.getName());
            user.setNewJoiner(false);
        } catch (Exception e) { //@TODO: Throw Specific Exception
            user.sendMeOnly("[SERVER] - " + e.getMessage() + "\r");
            return;
        }

        user.setRoom(broker.getRoom(roomName));

        user.sendMeOnly(String.format("[SERVER] - Joined room %s\r", roomName));

        for (Entry msg : broker.getHistory(roomName)) {
            user.sendMeOnly(String.format("%s [%s]: %s\r", msg.getTime(), msg.getUserName(), msg.getMessage()));
        }
    }

    private void leave(UserTo user, String[] arguments) {

        if (!user.hasRoom()) {
            user.send("[SERVER] - " + Constants.NO_ACTIVE_CHANNEL + "\r");
            return;
        }

        broker.unsubscribe(user.getRoom().getName(), user.getName());
        for (String userName : broker.getSubscribers(user.getRoom().getName())) {
            UserTo usr = repository.get(userName);
            usr.send(String.format("[SERVER] - %s has disconnected\r", user.getName()));
        }
        user.setRoom(null);
    }

    private void disconnect(UserTo user, String[] arguments) {
        user.terminate();
    }

    private void users(UserTo user, String[] arguments) {

        if (!user.hasRoom()) {
            user.send("[SERVER] - " + Constants.NO_ACTIVE_CHANNEL + "\r");
            return;
        }

        user.send(String.format("[SERVER] - Users in channel %s\r", user.getRoom().getName()));
        for (String userName : broker.getSubscribers(user.getRoom().getName())) {
            user.send(String.format("%s\r", userName));
        }
    }

    private void list(UserTo user, String[] arguments) {

        user.send("[SERVER] - Active channels:" + "\r");
        for (RoomTo room : broker.getRooms()) {
            user.send(String.format("%s\r", room.getName()));
        }
    }

    private void publish(UserTo user, String[] arguments) {
        if (arguments.length != 1) {
            throw new RuntimeException("Unexpected command arguments size");
        }

        if (!user.hasRoom()) {
            user.send("[SERVER] - " + Constants.NO_ACTIVE_CHANNEL + "\r");
            return;
        }

        String message = arguments[0];
        user.sendToSameUser(message + "\r"); // send message to all logged devices
        for (String userName : broker.getSubscribers(user.getRoom().getName())) {
            if (userName.equals(user.getName())) {
                continue;
            }
            send(userName, message, user.getName());
        }
        broker.addToHistory(user.getName(), user.getRoom().getName(), message);
    }

    private void send(String userName, String msg, String sender) {
        UserTo user = repository.get(userName);
        if (user == null) {
            return;
        }

        user.send(String.format("%s [%s]: %s\r", LocalTime.now(), sender, msg));
    }

    private void sendJoinNotification(String userName, String sender) {
        UserTo user = repository.get(userName);
        if (user == null) {
            return;
        }

        user.send(String.format("[SERVER] - %s joined your channel\r", sender));
    }
}
