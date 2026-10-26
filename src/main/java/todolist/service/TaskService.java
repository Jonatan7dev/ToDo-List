
package todolist.service;

import todolist.repository.TaskRepository;
import todolist.model.Priority;
import todolist.model.Task;
import java.sql.SQLException;

public class TaskService {

    private final TaskRepository tr;

    public TaskService(TaskRepository tr) {
        this.tr = tr;
    }

    // Validar ID
    private void validateId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                "El ID no es válido"
            );
        }
    }

    public void createTask(String title, String description, Priority priority) {

        // Validar título
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio");
        }

        title = title.trim();

        if (title.length() > 255) {
            throw new IllegalArgumentException(
                "El título no puede contener más de 255 caracteres"
            );
        }

        // Validar prioridad
        if (priority == null) {
            throw new IllegalArgumentException("La prioridad es obligatoria");
        }

        Task task = new Task(title, description, priority);

        try {
            boolean created = tr.createTask(task);

            if (!created) {
                throw new IllegalStateException(
                    "No se ha podido crear la tarea."
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                "Error al guardar la tarea en la base de datos.", e
            );
        }
    }

    public void updateTask(int id, String title, String description, Priority priority) {

        validateId(id);

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio");
        }

        title = title.trim();

        if (title.length() > 255) {
            throw new IllegalArgumentException(
                "El título no puede contener más de 255 caracteres"
            );
        }

        if (priority == null) {
            throw new IllegalArgumentException("La prioridad es obligatoria");
        }

        try {
            Task task = tr.getTaskById(id);

            if (task == null) {
                throw new IllegalArgumentException(
                    "No existe una tarea con ese ID"
                );
            }

            task.setTitle(title);
            task.setDescription(description);
            task.setPriority(priority);

            boolean updated = tr.updateTask(task, id);

            if (!updated) {
                throw new IllegalStateException(
                    "No se ha podido actualizar la tarea."
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                "Error al actualizar la tarea en la base de datos.", e
            );
        }
    }

    public void deleteTask(int id) {

        validateId(id);

        try {
            Task task = tr.getTaskById(id);

            if (task == null) {
                throw new IllegalArgumentException(
                    "No existe una tarea con ese ID"
                );
            }

            boolean deleted = tr.removeTask(id);

            if (!deleted) {
                throw new IllegalStateException(
                    "No se ha podido eliminar la tarea."
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                "Error al eliminar la tarea de la base de datos.", e
            );
        }
    }

    public void duplicateTask(int id) {

        validateId(id);

        try {
            Task original = tr.getTaskById(id);

            if (original == null) {
                throw new IllegalArgumentException(
                    "No existe una tarea con ese ID"
                );
            }

            Task duplicate = new Task(
                original.getTitle(),
                original.getDescription(),
                original.getPriority()
            );

            boolean created = tr.createTask(duplicate);

            if (!created) {
                throw new IllegalStateException(
                    "No se ha podido duplicar la tarea."
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                "Error al duplicar la tarea en la base de datos.", e
            );
        }
    }

    public void completeTask(int id) {

        validateId(id);

        try {
            Task task = tr.getTaskById(id);

            if (task == null) {
                throw new IllegalArgumentException(
                    "No existe una tarea con ese ID"
                );
            }

            if (task.isCompleted()) {
                throw new IllegalStateException(
                    "La tarea ya está completada."
                );
            }

            task.setCompleted(true);

            boolean updated = tr.updateTask(task, id);

            if (!updated) {
                throw new IllegalStateException(
                    "No se ha podido completar la tarea."
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                "Error al completar la tarea en la base de datos.", e
            );
        }
    }

    public int deleteCompletedTasks() {
        try {
            return tr.removeCompletedTasks();
        } catch (SQLException e) {
            throw new RuntimeException(
                "Error al eliminar las tareas completadas de la base de datos.", e
            );
        }
    }

}

