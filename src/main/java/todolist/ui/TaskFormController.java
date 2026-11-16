package todolist.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import todolist.model.Priority;
import todolist.model.Task;
import todolist.service.TaskService;

public class TaskFormController {
    @FXML private VBox formRoot;
    @FXML private Label headingLabel;
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<Priority> priorityCombo;
    @FXML private CheckBox completedCheck;
    @FXML private Button saveButton;
    private TaskService service;
    private DatabaseTasks jobs;
    private Task taskToEdit;
    private boolean attempted;

    @FXML private void initialize() {
        priorityCombo.getItems().setAll(Priority.values());
        priorityCombo.setValue(Priority.MEDIUM);
    }

    public void configure(TaskService service, DatabaseTasks jobs, Task task) {
        this.service = service;
        this.jobs = jobs;
        taskToEdit = task;
        formRoot.disableProperty().bind(jobs.busyProperty());
        if (task != null) {
            headingLabel.setText("Actualizar tarea");
            titleField.setText(task.getTitle());
            descriptionField.setText(task.getDescription());
            priorityCombo.setValue(task.getPriority());
            completedCheck.setVisible(true);
            completedCheck.setManaged(true);
            completedCheck.setSelected(task.isCompleted());
        }
    }

    public boolean wasAttempted() { return attempted; }

    @FXML private void handleSave() {
        // Capturamos los controles en JavaFX antes de pasar el trabajo al otro hilo.
        String title = titleField.getText();
        String description = descriptionField.getText();
        Priority priority = priorityCombo.getValue();
        boolean completed = completedCheck.isSelected();
        Integer id = taskToEdit == null ? null : taskToEdit.getId();
        attempted = true;
        headingLabel.setText("Guardando...");
        jobs.run(() -> {
            if (id == null) service.createTask(title, description, priority);
            else service.updateTask(id, title, description, priority, completed);
            return null;
        }, ignored -> closeWindow(), error -> {
            headingLabel.setText(id == null ? "Nueva tarea" : "Actualizar tarea");
            // No repetir automáticamente una escritura cuyo resultado es incierto.
            if (!(error instanceof IllegalArgumentException)) saveButton.setDisable(true);
            Errors.show(titleField.getScene().getWindow(), "No se pudo guardar la tarea", error);
        });
    }

    @FXML private void handleCancel() { if (!jobs.isBusy()) closeWindow(); }
    private void closeWindow() {
        formRoot.disableProperty().unbind();
        ((Stage) titleField.getScene().getWindow()).close();
    }
}
