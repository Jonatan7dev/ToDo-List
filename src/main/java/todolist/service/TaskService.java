package todolist.service;

import java.sql.SQLException;
import java.util.List;
import todolist.model.Priority;
import todolist.model.Task;
import todolist.repository.TaskRepository;

public class TaskService {
    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public List<Task> getAllTasks() throws SQLException {
        return repository.getAllTasks();
    }

    private void validateId(int id) {
        if (id <= 0) throw new IllegalArgumentException("El ID no es válido.");
    }

    private String validateTitle(String title, Priority priority) {
        if (title == null || title.isBlank())
            throw new IllegalArgumentException("El título es obligatorio.");
        title = title.strip();
        if (title.length() > 255)
            throw new IllegalArgumentException("El título no puede superar 255 caracteres.");
        if (priority == null)
            throw new IllegalArgumentException("La prioridad es obligatoria.");
        return title;
    }

    private Task requireTask(int id) throws SQLException {
        validateId(id);
        Task task = repository.getTaskById(id);
        if (task == null) throw new IllegalArgumentException("La tarea ya no existe. Actualiza la lista.");
        return task;
    }

    public void createTask(String title, String description, Priority priority) throws SQLException {
        Task task = new Task(validateTitle(title, priority), description, priority);
        if (!repository.createTask(task)) throw new SQLException("No se insertó la tarea.");
    }

    public void updateTask(int id, String title, String description, Priority priority,
                           boolean completed) throws SQLException {
        validateId(id);
        Task task = new Task(validateTitle(title, priority), description, priority);
        task.setCompleted(completed);
        if (!repository.updateTask(task, id))
            throw new IllegalArgumentException("La tarea ya no existe. Actualiza la lista.");
    }

    public void setTaskCompleted(int id, boolean completed) throws SQLException {
        validateId(id);
        if (!repository.setCompleted(id, completed))
            throw new IllegalArgumentException("La tarea ya no existe. Actualiza la lista.");
    }

    public void deleteTask(int id) throws SQLException {
        validateId(id);
        if (!repository.removeTask(id))
            throw new IllegalArgumentException("La tarea ya no existe. Actualiza la lista.");
    }

    public void duplicateTask(int id) throws SQLException {
        Task original = requireTask(id);
        createTask(original.getTitle(), original.getDescription(), original.getPriority());
    }

    public int deleteCompletedTasks() throws SQLException {
        return repository.removeCompletedTasks();
    }
}
