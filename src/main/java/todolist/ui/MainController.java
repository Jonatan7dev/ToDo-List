package todolist.ui;

import java.io.IOException;

import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import todolist.model.Priority;
import todolist.model.Task;
import todolist.service.TaskService;

public class MainController {

    @FXML
    private TableView<Task> taskTable;

    @FXML
    private TableColumn<Task, String> titleColumn;

    @FXML
    private TableColumn<Task, String> priorityColumn;

    @FXML
    private TableColumn<Task, Boolean> completedColumn;

    @FXML
    private TableColumn<Task, Void> actionsColumn;

    private TaskService taskService;

    @FXML
    private void initialize() {
        titleColumn.setCellValueFactory(cell ->
            new ReadOnlyStringWrapper(cell.getValue().getTitle())
        );

        priorityColumn.setCellValueFactory(cell ->
            new ReadOnlyStringWrapper(
                priorityLabel(cell.getValue().getPriority())
            )
        );

        configureCompletedColumn();
        configureActionsColumn();
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
        loadTasks();
    }

    private String priorityLabel(Priority priority) {
        return switch (priority) {
            case HIGH -> "Alta";
            case MEDIUM -> "Media";
            case LOW -> "Baja";
        };
    }

    private void loadTasks() {
        try {
            taskTable.getItems().setAll(taskService.getAllTasks());
        } catch (RuntimeException e) {
            showError("No se pudo cargar la lista de tareas", e);
        }
    }

    private void configureCompletedColumn() {
        completedColumn.setCellValueFactory(cell ->
            new ReadOnlyBooleanWrapper(cell.getValue().isCompleted())
        );

        completedColumn.setCellFactory(column ->
            new TableCell<Task, Boolean>() {

                private final CheckBox checkBox = new CheckBox();

                {
                    setAlignment(Pos.CENTER);

                    checkBox.setOnAction(event -> {
                        Task task = getTableRow().getItem();

                        if (task == null) {
                            return;
                        }

                        boolean previousState = task.isCompleted();
                        boolean completed = checkBox.isSelected();

                        try {
                            taskService.setTaskCompleted(
                                task.getId(), completed
                            );

                            // Actualiza también el objeto de la tabla.
                            task.setCompleted(completed);
                            taskTable.refresh();

                        } catch (RuntimeException e) {
                            checkBox.setSelected(previousState);
                            showError(
                                "No se pudo cambiar el estado de la tarea",
                                e
                            );
                        }
                    });
                }

                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        checkBox.setSelected(item);
                        setGraphic(checkBox);
                    }
                }
            }
        );
    }

    private void configureActionsColumn() {
        actionsColumn.setCellFactory(column ->
            new TableCell<Task, Void>() {

                private final Button editButton =
                    new Button("Actualizar");

                private final Button deleteButton =
                    new Button("Borrar");

                private final HBox buttons =
                    new HBox(8, editButton, deleteButton);

                {
                    buttons.setAlignment(Pos.CENTER);

                    editButton.setOnAction(event -> {
                        Task task = getTableRow().getItem();

                        if (task != null) {
                            openTaskForm(task);
                        }
                    });

                    deleteButton.setOnAction(event -> {
                        Task task = getTableRow().getItem();

                        if (task != null) {
                            deleteTask(task);
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : buttons);
                }
            }
        );
    }

    @FXML
    private void openNewTaskForm() {
        openTaskForm(null);
    }

    private void openTaskForm(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/todolist/ui/Formulario.fxml")
            );

            Parent form = loader.load();

            TaskFormController controller = loader.getController();
            controller.setTaskService(taskService);

            if (task != null) {
                controller.setTaskToEdit(task);
            }

            Stage dialog = new Stage();
            dialog.setTitle(
                task == null ? "Nueva tarea" : "Actualizar tarea"
            );
            dialog.initOwner(taskTable.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setScene(new Scene(form));
            dialog.showAndWait();

            loadTasks();

        } catch (IOException e) {
            showError("No se pudo abrir el formulario", e);
        }
    }

    private void deleteTask(Task task) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Borrar tarea");
        confirmation.setHeaderText("¿Quieres borrar esta tarea?");
        confirmation.setContentText(task.getTitle());

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL)
                != ButtonType.OK) {
            return;
        }

        try {
            taskService.deleteTask(task.getId());
            loadTasks();

        } catch (RuntimeException e) {
            showError("No se pudo borrar la tarea", e);
        }
    }

    @FXML
    private void deleteAllCompleted() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Borrar tareas completadas");
        confirmation.setHeaderText(
            "¿Quieres borrar todas las tareas completadas?"
        );
        confirmation.setContentText("Esta acción no se puede deshacer.");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL)
                != ButtonType.OK) {
            return;
        }

        try {
            taskService.deleteCompletedTasks();
            loadTasks();

        } catch (RuntimeException e) {
            showError("No se pudieron borrar las tareas completadas", e);
        }
    }

    private void showError(String header, Exception e) {
        e.printStackTrace();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}