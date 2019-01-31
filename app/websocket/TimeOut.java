package websocket;

import com.google.inject.Singleton;

import java.util.concurrent.*;

@Singleton
public class TimeOut {

    public final ScheduledExecutorService scheduler;

    public TimeOut() {
        this.scheduler = Executors.newScheduledThreadPool(2);
    }

    /**
     * Returns a stage that is completed exceptionally with the {@link TimeoutException} after the provided timeout.
     * @param timeout after which should the stage complete
     * @param unit of timeout value
     * @param <T> return type of the stage, no effect in this case
     * @return {@link CompletionStage}
     */
    public <T> CompletionStage<T> after(long timeout, TimeUnit unit) {
        CompletableFuture<T> future = new CompletableFuture<>();
        this.scheduler.schedule(() -> future.completeExceptionally(new TimeoutException("Operation was timed out after " + timeout + " " + unit.toString())), timeout, unit);
        return future;
    }
}
