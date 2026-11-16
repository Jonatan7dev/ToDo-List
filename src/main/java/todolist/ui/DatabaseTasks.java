package todolist.ui;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.concurrent.Task;

/** Ejecuta JDBC fuera del hilo de JavaFX. Los callbacks sí se ejecutan en JavaFX. */
public final class DatabaseTasks {
    private final ReadOnlyBooleanWrapper busy = new ReadOnlyBooleanWrapper(false);

    public ReadOnlyBooleanProperty busyProperty() { return busy.getReadOnlyProperty(); }
    public boolean isBusy() { return busy.get(); }

    public <T> void run(Callable<T> operation, Consumer<T> success, Consumer<Throwable> failure) {
        if (isBusy()) throw new IllegalStateException("Ya hay una operación en curso.");
        busy.set(true);
        Task<T> task = new Task<>() {
            @Override protected T call() throws Exception { return operation.call(); }
        };
        task.setOnSucceeded(event -> {
            busy.set(false);
            success.accept(task.getValue());
        });
        task.setOnFailed(event -> {
            busy.set(false);
            failure.accept(task.getException());
        });
        Thread worker = new Thread(task, "todolist-database");
        worker.setDaemon(true);
        worker.start();
    }
}
