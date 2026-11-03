package todolist.ui;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
    
    private TaskService taskService;

    @FXML
    private void initialize() {
        titleColumn.setCellValueFactory(cell ->
            new ReadOnlyStringWrapper(cell.getValue().getTitle())
        );

        priorityColumn.setCellValueFactory(cell ->
            new ReadOnlyStringWrapper(priorityLabel(cell.getValue().getPriority()))
        );
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
    
}
