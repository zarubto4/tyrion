package utilities.synchronization;

import com.google.inject.Singleton;
import utilities.logger.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
            task.start();
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public synchronized void cancel(Task task) {
        if (this.tasks.containsKey(task.getId())) {
            this.tasks.get(task.getId()).stop();
        }
    }
}
