package todolist;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import todolist.ui.*;
import todolist.model.*;
import todolist.repository.TaskRepository;
import todolist.service.TaskService;

class JavaFxTest {
    @BeforeAll static void startToolkit() throws Exception {
        CountDownLatch ready = new CountDownLatch(1);
        Platform.startup(() -> { Platform.setImplicitExit(false); ready.countDown(); });
        assertTrue(ready.await(10, TimeUnit.SECONDS));
    }
    static <T> T fx(Callable<T> action) throws Exception {
        FutureTask<T> future = new FutureTask<>(action); Platform.runLater(future);
        return future.get(10, TimeUnit.SECONDS);
    }
    @Test void fxmlMainAndFormLoadAndEditFieldsArePopulated() throws Exception {
        fx(() -> {
            Parent main = FXMLLoader.load(Main.class.getResource("/todolist/ui/Principal.fxml"));
            new Scene(main); main.applyCss(); main.layout();
            assertNotNull(main.lookup("#refreshButton"));
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/todolist/ui/Formulario.fxml"));
            Parent form = loader.load(); new Scene(form);
            Task task = new Task("Texto", "Descripción", Priority.HIGH); task.setId(1); task.setCompleted(true);
            TaskFormController controller = loader.getController();
            controller.configure(new TaskService(new TaskRepository()), new DatabaseTasks(), task);
            assertEquals("Texto", ((TextField) form.lookup("#titleField")).getText());
            CheckBox box = (CheckBox) form.lookup("#completedCheck");
            assertTrue(box.isVisible()); assertTrue(box.isManaged()); assertTrue(box.isSelected());
            assertEquals(Priority.HIGH, ((ComboBox<?>) form.lookup("#priorityCombo")).getValue());
            return null;
        });
    }
    @Test void slowWorkDoesNotBlockFxAndSuccessRunsOnFxThread() throws Exception {
        DatabaseTasks jobs = fx(DatabaseTasks::new);
        CountDownLatch started = new CountDownLatch(1), release = new CountDownLatch(1), done = new CountDownLatch(1);
        AtomicBoolean background = new AtomicBoolean(), callback = new AtomicBoolean();
        fx(() -> { jobs.run(() -> { background.set(!Platform.isFxApplicationThread()); started.countDown();
            release.await(5,TimeUnit.SECONDS); return 7; }, value -> {
                callback.set(Platform.isFxApplicationThread() && value == 7 && !jobs.isBusy()); done.countDown();
            }, error -> done.countDown()); return null; });
        assertTrue(started.await(3,TimeUnit.SECONDS));
        try { assertTrue(fx(jobs::isBusy)); } finally { release.countDown(); }
        assertTrue(done.await(5, TimeUnit.SECONDS)); assertTrue(background.get()); assertTrue(callback.get());
    }
    @Test void failureReleasesBusyOnFxThread() throws Exception {
        DatabaseTasks jobs = fx(DatabaseTasks::new);
        CountDownLatch done = new CountDownLatch(1); AtomicBoolean valid = new AtomicBoolean();
        fx(() -> { jobs.run(() -> { throw new java.sql.SQLException("offline"); }, ignored -> done.countDown(), error -> {
            valid.set(error instanceof java.sql.SQLException && Platform.isFxApplicationThread() && !jobs.isBusy());
            done.countDown();
        }); return null; });
        assertTrue(done.await(5,TimeUnit.SECONDS)); assertTrue(valid.get());
    }
    @Test void mainDisplaysLoadedTasksAndRendersActions() throws Exception {
        CountDownLatch loaded = new CountDownLatch(1);
        javafx.stage.Stage stage = fx(() -> {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/todolist/ui/Principal.fxml"));
            Parent root = loader.load();
            javafx.stage.Stage window = new javafx.stage.Stage();
            window.setScene(new Scene(root)); window.show();
            TableView<?> table = (TableView<?>) root.lookup("#taskTable");
            table.getItems().addListener((javafx.collections.ListChangeListener<Object>) change -> {
                if (table.getItems().size() == 2) loaded.countDown();
            });
            Task first = new Task("Preparar presentación", "Revisar diapositivas", Priority.HIGH); first.setId(1);
            Task second = new Task("Repasar JavaFX", "Práctica de interfaces", Priority.MEDIUM);
            second.setId(2); second.setCompleted(true);
            ((MainController) loader.getController()).setTaskService(new TaskService(new TaskRepository()) {
                @Override public java.util.List<Task> getAllTasks() { return java.util.List.of(first, second); }
            });
            return window;
        });
        try {
            assertTrue(loaded.await(5, TimeUnit.SECONDS));
            fx(() -> {
                Parent root = stage.getScene().getRoot(); root.applyCss(); root.layout();
                TableView<?> table = (TableView<?>) root.lookup("#taskTable");
                assertFalse(table.isDisabled()); assertEquals(2, table.getItems().size());
                assertEquals("2 tareas", ((Label) root.lookup("#statusLabel")).getText());
                assertTrue(root.lookupAll(".button").stream()
                        .anyMatch(n -> n instanceof Button b && "Duplicar".equals(b.getText())));
                javafx.scene.image.WritableImage image = root.snapshot(null, null);
                int width = (int) image.getWidth(), height = (int) image.getHeight();
                java.awt.image.BufferedImage output = new java.awt.image.BufferedImage(width,height,2);
                for (int y=0;y<height;y++) for(int x=0;x<width;x++)
                    output.setRGB(x,y,image.getPixelReader().getArgb(x,y));
                javax.imageio.ImageIO.write(output,"png", new java.io.File("target/ui-preview.png"));
                return null;
            });
        } finally { fx(() -> { stage.close(); return null; }); }
    }
}
