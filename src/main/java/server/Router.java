package server;

import command.CommandRequest;
import command.Commander;
import dao.to.UserTo;
import io.netty.channel.ChannelHandlerContext;
import server.chat.Chat;
import executor.Executor;
import server.session.SessionRepository;

public class Router {

    public static String COMMAND_NOT_FOUND = "Command not found!";
    public static String UNEXPECTED_ERROR = "Invalid command arguments";

    private Chat broker;
    private SessionRepository sessions;
    private Executor executor;
    private Commander commandExecutor;

    public Router(Chat broker, Commander cmdExec, Executor executor, SessionRepository sessions) {
        this.broker = broker;
        this.commandExecutor = cmdExec;
        this.executor = executor;
        this.sessions = sessions;
    }

    public void accept(ChannelHandlerContext ctx, String userName, String password) {
        sessions.add(ctx, userName, password);
    }

    public void close(String userName) {
        UserTo user = sessions.get(userName);
        if (user == null) {
            return;
        }

        //user.terminate();
        if (user.stillActive())
            return;
        //sessions.remove(userName);
        if (user.hasRoom()) {
            broker.unsubscribe(user.getRoom().getName(), userName);
            for (String name : broker.getSubscribers(user.getRoom().getName())) {
                UserTo usr = sessions.get(name);
                usr.send(String.format("[SERVER] - %s has disconnected\r", userName));
            }
        }
    }

    public void receiveMessage(String userName, CommandRequest cmd, ChannelHandlerContext ctx) {
        UserTo user = sessions.get(userName);
        user.setSenderContext(ctx);
        if (user == null) {
            return;
        }

        if (!commandExecutor.contains(cmd.getCmd())) {
            user.sendMeOnly("[SERVER] - " + COMMAND_NOT_FOUND + "\r");
            return;
        }

        // Delegate command execution to its own thread pool
        executor.execute(() -> {
            try {
                commandExecutor.execute(user, cmd);
            } catch (Exception e) {
                user.sendMeOnly("[SERVER] - " + UNEXPECTED_ERROR + "\r");
                e.printStackTrace();
            }
        });
    }

    public void joinLastChannel(String userName, ChannelHandlerContext ctx) {
        executor.execute(() -> {
            UserTo user = sessions.get(userName);
            user.setSenderContext(ctx);
            if (user.getRoom() == null)
                return;
            user.setNewJoiner(true);
            try {
                String[] args = {user.getRoom().getName()};
                commandExecutor.execute(user, new CommandRequest("join", args));
            } catch (Exception e) {
                user.sendMeOnly("[SERVER] - Unable to join last channel\r");
                e.printStackTrace();
            }
        });
    }
}