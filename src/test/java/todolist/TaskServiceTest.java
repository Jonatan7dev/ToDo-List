package todolist;

import java.sql.*;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import todolist.model.*;
import todolist.repository.TaskRepository;
import todolist.service.TaskService;

class TaskServiceTest {
    private TaskRepository repository;
    private TaskService service;
    @BeforeEach void setup() throws Exception {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        repository = new TaskRepository(() -> DriverManager.getConnection(url));
        service = new TaskService(repository);
        try (Connection c = DriverManager.getConnection(url); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE tasks(id INT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(255) NOT NULL,"
                    + "description TEXT, priority VARCHAR(10) NOT NULL, completed BOOLEAN DEFAULT FALSE NOT NULL,"
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL)");
        }
    }
    @Test void createNormalizesAndPersistsAcrossServiceInstances() throws Exception {
        service.createTask("  Estudiar  ", "Español: ñ á", Priority.HIGH);
        Task t = new TaskService(repository).getAllTasks().getFirst();
        assertEquals("Estudiar", t.getTitle()); assertEquals("Español: ñ á", t.getDescription());
        assertFalse(t.isCompleted()); assertNotNull(t.getCreatedAt()); assertTrue(t.getId() > 0);
    }
    @Test void rejectsInvalidInputsWithoutInserts() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> service.createTask(" ", "", Priority.LOW));
        assertThrows(IllegalArgumentException.class, () -> service.createTask(null, "", Priority.LOW));
        assertThrows(IllegalArgumentException.class, () -> service.createTask("x".repeat(256), "", Priority.LOW));
        assertThrows(IllegalArgumentException.class, () -> service.createTask("a", "", null));
        assertTrue(service.getAllTasks().isEmpty());
    }
    @Test void boundaryTitleAndOptionalDescription() throws Exception {
        service.createTask("x".repeat(255), null, Priority.LOW);
        assertNull(service.getAllTasks().getFirst().getDescription());
    }
    @Test void editStateAndSaveWithoutChanges() throws Exception {
        service.createTask("A", "", Priority.LOW); int id = service.getAllTasks().getFirst().getId();
        service.updateTask(id, " B ", "detalle", Priority.HIGH, true);
        service.updateTask(id, " B ", "detalle", Priority.HIGH, true);
        Task t = repository.getTaskById(id);
        assertEquals("B", t.getTitle()); assertTrue(t.isCompleted()); assertEquals(Priority.HIGH, t.getPriority());
    }
    @Test void completionOnlyChangesStateAndCanBeReversed() throws Exception {
        service.createTask("A", "detalle", Priority.MEDIUM); int id = service.getAllTasks().getFirst().getId();
        service.setTaskCompleted(id, true); service.setTaskCompleted(id, true);
        assertTrue(repository.getTaskById(id).isCompleted());
        service.setTaskCompleted(id, false);
        Task t = repository.getTaskById(id); assertFalse(t.isCompleted()); assertEquals("detalle", t.getDescription());
    }
    @Test void duplicateIsIndependentAndPending() throws Exception {
        service.createTask("A", "detalle", Priority.HIGH); int id = service.getAllTasks().getFirst().getId();
        service.setTaskCompleted(id, true); service.duplicateTask(id);
        var tasks = service.getAllTasks(); assertEquals(2, tasks.size());
        assertNotEquals(id, tasks.get(1).getId()); assertFalse(tasks.get(1).isCompleted());
        assertTrue(tasks.getFirst().isCompleted()); assertNotNull(tasks.get(1).getCreatedAt());
        service.updateTask(tasks.get(1).getId(), "copia", "otro", Priority.LOW, false);
        assertEquals("A", repository.getTaskById(id).getTitle());
    }
    @Test void deletesOnlyCompletedThenIndividual() throws Exception {
        service.createTask("A", "", Priority.LOW); service.createTask("B", "", Priority.LOW);
        service.setTaskCompleted(service.getAllTasks().getFirst().getId(), true);
        assertEquals(1, service.deleteCompletedTasks()); assertEquals(0, service.deleteCompletedTasks());
        assertEquals("B", service.getAllTasks().getFirst().getTitle());
        service.deleteTask(service.getAllTasks().getFirst().getId()); assertTrue(service.getAllTasks().isEmpty());
    }
    @Test void missingTaskAndInvalidIdAreReported() {
        assertThrows(IllegalArgumentException.class, () -> service.deleteTask(0));
        assertThrows(IllegalArgumentException.class, () -> service.deleteTask(999));
        assertThrows(IllegalArgumentException.class, () -> service.duplicateTask(999));
        assertThrows(IllegalArgumentException.class, () -> service.setTaskCompleted(999, true));
        assertThrows(IllegalArgumentException.class, () -> service.updateTask(999,"a","",Priority.LOW,false));
    }
    @Test void databaseFailureIsNotAnEmptyList() {
        TaskService broken = new TaskService(new TaskRepository(() -> { throw new SQLException("offline"); }));
        assertThrows(SQLException.class, broken::getAllTasks);
        assertThrows(SQLException.class, () -> broken.createTask("A", "", Priority.HIGH));
    }
}
