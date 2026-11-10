package todolist.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import todolist.model.Priority;
import todolist.service.TaskService;

public class TaskFormController {

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private ComboBox<String> priorityCombo;

    private TaskService taskService;

    @FXML
    private void initialize() {
        priorityCombo.getItems().setAll("Alta", "Media", "Baja");
        priorityCombo.setValue("Media");
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @FXML
    private void handleSave() {
        String title = titleField.getText().trim();
        String description = descriptionField.getText().trim();
        String selectedPriority = priorityCombo.getValue();

        if (title.isBlank()) {
            showError("Escribe un título para la tarea.");
            return;
        }

        if (selectedPriority == null) {
            showError("Selecciona una prioridad.");
            return;
        }

        Priority priority = switch (selectedPriority) {
            case "Alta" -> Priority.HIGH;
            case "Media" -> Priority.MEDIUM;
            case "Baja" -> Priority.LOW;
            default -> throw new IllegalArgumentException(
                "Prioridad desconocida: " + selectedPriority
            );
        };

        try {
            taskService.createTask(title, description, priority);
            closeWindow();
        } catch (RuntimeException e) {
            showError("No se pudo guardar la tarea: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage window = (Stage) titleField.getScene().getWindow();
        window.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Revisa la tarea");
        alert.setContentText(message);
        alert.showAndWait();
    }
}