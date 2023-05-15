package command;

import dao.to.UserTo;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class CommandExecutor implements Commander {

    private Map<String, BiConsumer<UserTo, String[]>> commands = new HashMap<>();

    public CommandExecutor() {
    }

    @Override
    public void register(CommandHandler commandHandler) {
        for (Map.Entry<String, BiConsumer<UserTo, String[]>> cmd : commandHandler.handlers().entrySet()) {
            commands.put(cmd.getKey(), cmd.getValue());
        }
    }

    public void execute(UserTo user, CommandRequest cmd) {
        if (!commands.containsKey(cmd.getCmd())) {
            return;
        }

        commands.get(cmd.getCmd()).accept(user, cmd.getArguments());
    }

    public boolean contains(String cmdType) {
        return commands.containsKey(cmdType);
    }
}