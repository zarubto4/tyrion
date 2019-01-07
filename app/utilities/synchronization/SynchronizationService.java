package utilities.synchronization;

import com.google.inject.Singleton;
import utilities.logger.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CancellationException;

@Singleton
public class SynchronizationService {

    private static final Logger logger = new Logger(SynchronizationService.class);

    private final Map<UUID, Task> tasks = new HashMap<>();

    public synchronized void submit(Task task) {
        if (this.tasks.containsKey(task.getId())) {
            throw new RuntimeException("Synchronization task: " + task.getId() + " is already running.");
        }

        this.tasks.put(task.getId(), task);
        try {

            task.start().handle((result, exception) -> {
                this.tasks.remove(task.getId());
                if (exception != null) {
                    if (exception instanceof CancellationException) {
                        logger.warn("submit - synchronization task {}, id: {} was cancelled", task.getClass().getSimpleName(), task.getId());
                    } else {
                        logger.warn("submit - synchronization task {}, id: {} failed", task.getClass().getSimpleName(), task.getId());
                        logger.internalServerError(exception);
                    }
                } else {
                    logger.info("submit - synchronization task {}, id: {} was successful", task.getClass().getSimpleName(), task.getId());
                }
                return null;
            });
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public synchronized void cancel(Task task) {
        this.cancel(task.getId());
    }

    public synchronized void cancel(UUID id) {
        if (this.tasks.containsKey(id)) {
            this.tasks.get(id).stop();
        }
    }

    public Task getTask(UUID id) {
        return this.tasks.get(id);
    }
}
