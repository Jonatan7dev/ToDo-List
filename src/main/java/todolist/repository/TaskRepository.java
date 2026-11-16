package todolist.repository;

import todolist.database.DatabaseConnection;
import todolist.model.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import todolist.model.Priority;


public class TaskRepository {
    @FunctionalInterface
    public interface ConnectionFactory { Connection open() throws SQLException; }
    private final ConnectionFactory connections;

    public TaskRepository() { this(DatabaseConnection::getConnection); }
    // Permite probar SQL en una base aislada sin tocar tus tareas reales.
    public TaskRepository(ConnectionFactory connections) { this.connections = connections; }


    public List<Task> getAllTasks() throws SQLException {
        String sql = "SELECT * FROM tasks ORDER BY id";
        List<Task> tasks = new ArrayList<>();
        try (Connection conn = connections.open();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) tasks.add(mapTask(rs));
        }
        return tasks;
    }

    public Task getTaskById(int id) throws SQLException {
        try (Connection conn = connections.open();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM tasks WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapTask(rs) : null;
            }
        }
    }

    public boolean createTask(Task task) throws SQLException {

        String sql = "INSERT INTO tasks (title, description, priority) VALUES (?, ?, ?)";

        try (Connection conn = connections.open();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getPriority().name());

            int rowsInserted = ps.executeUpdate();
            return rowsInserted > 0;
        }
    }

    public boolean updateTask(Task task, int id) throws SQLException {

        String sql = "UPDATE tasks SET title = ?, description = ?, priority =?, completed = ? WHERE id = ?";
        
        try (Connection conn = connections.open();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getPriority().name());
            ps.setBoolean(4, task.isCompleted());
            ps.setInt(5, id);

            int rowUpdated = ps.executeUpdate();
            return rowUpdated > 0;                
        }
    }

    public boolean removeTask(int id) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = connections.open();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int rowsRemoved = ps.executeUpdate();

            return rowsRemoved > 0;
        }
    }

    public int removeCompletedTasks() throws SQLException {
        String sql = "DELETE FROM tasks WHERE completed = TRUE";

        try (Connection conn = connections.open();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            return ps.executeUpdate();
        }
    }
    public boolean setCompleted(int id, boolean completed) throws SQLException {
        try (Connection conn = connections.open();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE tasks SET completed = ? WHERE id = ?")) {
            ps.setBoolean(1, completed);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Task mapTask(ResultSet rs) throws SQLException {
        Priority priority;
        try {
            priority = Priority.valueOf(rs.getString("priority"));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new SQLException("Prioridad inválida en la tarea ID " + rs.getInt("id")
                    + ". Debe ser HIGH, MEDIUM o LOW.", e);
        }
        Task task = new Task(rs.getString("title"), rs.getString("description"), priority);
        task.setId(rs.getInt("id"));
        task.setCompleted(rs.getBoolean("completed"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        task.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        return task;
    }
}
