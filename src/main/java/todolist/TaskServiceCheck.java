
package todolist;

import todolist.model.Task;
import todolist.model.Priority;
import todolist.repository.TaskRepository;
import todolist.service.TaskService;

public class TaskServiceCheck {

    public static void main(String[] args) {

        TaskRepository repository = new TaskRepository();
        TaskService service = new TaskService(repository);

        int testId = -1;

        try {
            // 1. Crear una tarea
            System.out.println("=== CREAR TAREA ===");

            service.createTask(
                "Tarea de prueba",
                "Probando TaskService",
                Priority.HIGH
            );

            // Buscar la tarea creada
            for (Task task : repository.getAllTasks()) {
                if (task.getTitle().equals("Tarea de prueba")) {
                    testId = task.getId();
                }
            }

            if (testId == -1) {
                throw new IllegalStateException(
                    "No se encontró la tarea creada."
                );
            }

            System.out.println("Tarea creada. ID: " + testId);


            // 2. Actualizar la tarea
            System.out.println("\n=== ACTUALIZAR TAREA ===");

            service.updateTask(
                testId,
                "Tarea actualizada",
                "Descripción modificada",
                Priority.MEDIUM
            );

            System.out.println("Tarea actualizada correctamente.");


            // 3. Marcar como completada
            System.out.println("\n=== COMPLETAR TAREA ===");

            service.completeTask(testId);

            Task completedTask = repository.getTaskById(testId);

            System.out.println(
                "¿Está completada? " + completedTask.isCompleted()
            );


            // 4. Duplicar la tarea
            System.out.println("\n=== DUPLICAR TAREA ===");

            service.duplicateTask(testId);

            System.out.println("Tarea duplicada correctamente.");


            // 5. Mostrar todas las tareas
            System.out.println("\n=== LISTA DE TAREAS ===");

            for (Task task : repository.getAllTasks()) {
                System.out.println(
                    task.getId() + " | " +
                    task.getTitle() + " | " +
                    task.getPriority() + " | " +
                    task.isCompleted()
                );
            }


            // 6. Eliminar todas las tareas completadas
            System.out.println("\n=== ELIMINAR COMPLETADAS ===");

            int deleted = service.deleteCompletedTasks();

            System.out.println(
                "Tareas completadas eliminadas: " + deleted
            );


            // 7. Eliminar la tarea original si todavía existe
            System.out.println("\n=== ELIMINAR TAREA ===");

            Task remainingTask = repository.getTaskById(testId);

            if (remainingTask != null) {
                service.deleteTask(testId);
                System.out.println("Tarea original eliminada.");
            } else {
                System.out.println(
                    "La tarea original ya había sido eliminada."
                );
            }

            System.out.println("\nPruebas finalizadas.");

        } catch (Exception e) {
            System.err.println(
                "Error durante las pruebas: " + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}
