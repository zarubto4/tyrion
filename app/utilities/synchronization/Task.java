package utilities.synchronization;

import common.Identifiable;

import java.util.concurrent.CompletionStage;

public interface Task extends Identifiable {
    CompletionStage<Void> start();
    void stop();
}
