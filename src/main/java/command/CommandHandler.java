package command;

import dao.to.UserTo;

import java.util.Map;
import java.util.function.BiConsumer;

public interface CommandHandler {

    Map<String, BiConsumer<UserTo, String[]>> handlers() throws RuntimeException;
}
