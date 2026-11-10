package todolist.ui;

import java.io.IOException;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import todolist.model.Priority;
import todolist.model.Task;
import todolist.service.TaskService;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;

public class MainController {

    @FXML
    private TableView<Task> taskTable;
    @FXML
    private TableColumn<Task, String> titleColumn;
    @FXML
    private TableColumn<Task, String> priorityColumn;
    @FXML
    private TableColumn<Task, Boolean> completedColumn;
    
    private TaskService taskService;

    @FXML
    private void initialize() {
        titleColumn.setCellValueFactory(cell ->
            new ReadOnlyStringWrapper(cell.getValue().getTitle())
        );

        priorityColumn.setCellValueFactory(cell ->
            new ReadOnlyStringWrapper(priorityLabel(cell.getValue().getPriority()))
        );

        completedColumn.setCellFactory(column -> new TableCell<Task, Boolean>() {

            private final CheckBox checkBox = new CheckBox();

            {
                setStyle("-fx-alignment: CENTER;");

                checkBox.setOnAction(event -> {
                    Task task = getTableRow().getItem();

                    if (task == null) {
                        return;
                    }

                    boolean completed = checkBox.isSelected();

                    try {
                        taskService.setTaskCompleted(task.getId(), completed);
                        loadTasks();
                    } catch (RuntimeException e) {
                        // Recupera el estado anterior si falla el guardado.
                        checkBox.setSelected(task.isCompleted());

                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setHeaderText("No se pudo cambiar el estado");
                        alert.setContentText(e.getMessage());
                        alert.showAndWait();
                    }
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                Task task = getTableRow() == null
                    ? null
                    : getTableRow().getItem();

                if (empty || task == null) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(task.isCompleted());
                    setGraphic(checkBox);
                }
            }
        });
    }

    private String priorityLabel(Priority priority) {
        return switch (priority) {
            case HIGH -> "Alta";
            case MEDIUM -> "Media";
            case LOW -> "Baja";
        };
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
        loadTasks();
    }

    private void loadTasks() {
        try {
            taskTable.getItems().setAll(taskService.getAllTasks());
        } catch (RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("No se pudo cargar la lista de tareas");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void openNewTaskForm() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/todolist/ui/Formulario.fxml")
            );
            Parent form = loader.load();

            TaskFormController formController = loader.getController();
            formController.setTaskService(taskService);

            Stage dialog = new Stage();
            dialog.setTitle("Nueva tarea");
            dialog.initOwner(taskTable.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setScene(new Scene(form));
            dialog.showAndWait();
            loadTasks();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("No se pudo abrir el formulario");
            alert.setContentText("Comprueba la ruta y el contenido de Formulario.fxml.");
            alert.showAndWait();
        }
    }

    @FXML
    private void deleteAllCompleted() {
    try {
        taskService.deleteCompletedTasks();
        loadTasks(); // Actualiza la tabla después de borrar
    } catch (RuntimeException e) {
        e.printStackTrace();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("No se pudieron borrar las tareas completadas");
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}
    
}
