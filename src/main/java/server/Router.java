package server;

import command.CommandRequest;
import command.Commander;
import dao.to.UserTo;
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

    public void accept(UserTo user) {
        sessions.add(user);
    }

    public void close(String userName) {
        UserTo user = sessions.get(userName);
        if (user == null) {
            return;
        }

        sessions.remove(userName);
        if (user.hasRoom()) {
            broker.unsubscribe(user.getRoom().getName(), userName);
        }
    }

    public void receiveMessage(String userName, CommandRequest cmd) {
        UserTo user = sessions.get(userName);
        if (user == null) {
            return;
        }

        if (!commandExecutor.contains(cmd.getCmd())) {
            user.send("[SERVER] - " + COMMAND_NOT_FOUND + "\r");
            return;
        }

        // Delegate command execution to its own thread pool
        executor.execute(() -> {
            try {
                commandExecutor.execute(user, cmd);
            } catch (Exception e) {
                user.send("[SERVER] - " + UNEXPECTED_ERROR + "\r");
                e.printStackTrace();
            }
        });
    }

    public void joinLastChannel(String userName) {
        executor.execute(() -> {
            UserTo user = sessions.get(userName);
            if (user.getRoom() == null)
                return;
            try {
                String[] args = {user.getRoom().getName()};
                commandExecutor.execute(user, new CommandRequest("join", args));
            } catch (Exception e) {
                user.send("[SERVER] - Unable to join last channel\r");
                e.printStackTrace();
            }
        });
    }
}