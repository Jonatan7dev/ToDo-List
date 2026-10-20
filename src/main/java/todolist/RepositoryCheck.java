package todolist;

import todolist.model.Task;
import todolist.repository.TaskRepository;

import java.sql.SQLException;
import java.util.List;

public class RepositoryCheck {

    public static void main(String[] args) throws SQLException {
        TaskRepository repository = new TaskRepository();
        String testTitle = "Tarea de prueba3";

        // Crear una tarea
        Task newTask = new Task(
                0,
                testTitle,
                "Descripción de prueba6",
                "Low",
                false,
                null
        );

        repository.updateTask(newTask, 1);

       
       for (Task task : repository.getAllTasks()) {
        System.out.println(
                task.getId() + " | " +
                task.getTitle() + " | " +
                task.getPriority() + " | " +
                task.isCompleted()
            );
        }
    }
}