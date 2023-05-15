package executor;

import java.util.concurrent.Future;

public interface Executor {
    Future<?> execute(Runnable task);
}
