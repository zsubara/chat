package command;

import dao.to.UserTo;

public interface Commander {

    void register(CommandHandler commandHandler);

    void execute(UserTo user, CommandRequest cmd);

    boolean contains(String cmdType);
}
