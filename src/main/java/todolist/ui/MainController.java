package todolist.ui;

import java.io.IOException;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import todolist.model.Task;
import todolist.service.TaskService;

public class MainController {
    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> titleColumn, priorityColumn;
    @FXML private TableColumn<Task, Boolean> completedColumn;
    @FXML private TableColumn<Task, Void> actionsColumn;
    @FXML private Button newButton, clearCompletedButton, refreshButton;
    @FXML private Label statusLabel;
    private final DatabaseTasks jobs = new DatabaseTasks();
    private TaskService service;
    private boolean loaded;

    @FXML private void initialize() {
        titleColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTitle()));
        priorityColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getPriority().toString()));
        taskTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        completedColumn.setCellValueFactory(c -> new ReadOnlyBooleanWrapper(c.getValue().isCompleted()));
        completedColumn.setCellFactory(column -> new TableCell<>() {
            private final CheckBox box = new CheckBox();
            {
                setAlignment(Pos.CENTER);
                box.setOnAction(event -> {
                    Task task = getTableRow().getItem();
                    if (task == null) return;
                    boolean selected = box.isSelected();
                    // El modelo solo cambia después de confirmar la escritura.
                    box.setSelected(task.isCompleted());
                    mutate(() -> service.setTaskCompleted(task.getId(), selected));
                });
            }
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null) box.setSelected(item);
                setGraphic(empty || item == null ? null : box);
            }
        });
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button edit = new Button("Actualizar");
            private final Button duplicate = new Button("Duplicar");
            private final Button delete = new Button("Borrar");
            private final HBox buttons = new HBox(6, edit, duplicate, delete);
            {
                buttons.setAlignment(Pos.CENTER);
                edit.setOnAction(e -> { Task t = getTableRow().getItem(); if (t != null) openTaskForm(t); });
                duplicate.setOnAction(e -> { Task t = getTableRow().getItem();
                    if (t != null) mutate(() -> service.duplicateTask(t.getId())); });
                delete.setOnAction(e -> { Task t = getTableRow().getItem();
                    if (t != null && confirm("¿Borrar esta tarea?", t.getTitle()))
                        mutate(() -> service.deleteTask(t.getId())); });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
        jobs.busyProperty().addListener((obs, old, busy) -> updateControls());
        updateControls();
    }

    public void setTaskService(TaskService service) { this.service = service; loadTasks(); }
    public boolean isBusy() { return jobs.isBusy(); }

    private void updateControls() {
        boolean disabled = jobs.isBusy() || !loaded;
        taskTable.setDisable(disabled);
        newButton.setDisable(disabled);
        clearCompletedButton.setDisable(disabled);
        refreshButton.setDisable(jobs.isBusy());
    }

    @FXML private void loadTasks() {
        statusLabel.setText("Cargando tareas...");
        jobs.run(service::getAllTasks, tasks -> {
            taskTable.getItems().setAll(tasks);
            taskTable.sort();
            taskTable.setPlaceholder(new Label("No hay tareas. Pulsa + Nuevo para crear una."));
            loaded = true;
            statusLabel.setText(tasks.size() + " tareas");
            updateControls();
        }, error -> failed("No se pudieron cargar las tareas", error));
    }

    private void failed(String header, Throwable error) {
        loaded = false;
        statusLabel.setText("Error. La lista puede estar desactualizada. Pulsa Actualizar lista.");
        taskTable.setPlaceholder(new Label("No se pudieron cargar los datos."));
        updateControls();
        Errors.show(taskTable.getScene().getWindow(), header, error);
    }

    @FunctionalInterface private interface Change { void run() throws Exception; }
    private void mutate(Change change) {
        statusLabel.setText("Guardando cambios...");
        jobs.run(() -> { change.run(); return null; }, ignored -> loadTasks(),
                error -> failed("No se pudo confirmar el cambio", error));
    }

    @FXML private void openNewTaskForm() { openTaskForm(null); }

    private void openTaskForm(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/todolist/ui/Formulario.fxml"));
            Parent root = loader.load();
            TaskFormController controller = loader.getController();
            controller.configure(service, jobs, task);
            Stage dialog = new Stage();
            dialog.setTitle(task == null ? "Nueva tarea" : "Actualizar tarea");
            dialog.initOwner(taskTable.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setScene(new Scene(root));
            dialog.setOnCloseRequest(event -> { if (jobs.isBusy()) event.consume(); });
            dialog.showAndWait();
            // También refrescamos tras errores de escritura: su resultado puede ser incierto.
            if (controller.wasAttempted()) loadTasks();
        } catch (IOException e) {
            Errors.show(taskTable.getScene().getWindow(), "No se pudo abrir el formulario", e);
        }
    }

    @FXML private void deleteAllCompleted() {
        if (taskTable.getItems().stream().noneMatch(Task::isCompleted)) {
            statusLabel.setText("No hay tareas completadas en la lista. Actualízala si es necesario.");
            return;
        }
        if (confirm("¿Borrar todas las tareas completadas?", "Esta acción no se puede deshacer."))
            mutate(() -> service.deleteCompletedTasks());
    }

    private boolean confirm(String header, String detail) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, detail, ButtonType.OK, ButtonType.CANCEL);
        alert.initOwner(taskTable.getScene().getWindow());
        alert.setHeaderText(header);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}
