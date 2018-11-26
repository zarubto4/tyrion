package utilities.synchronization;

import common.Identifiable;

public interface Task extends Identifiable {
    void start();
    void stop();
}
