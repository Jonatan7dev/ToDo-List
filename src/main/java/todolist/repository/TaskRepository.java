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

    //Obtener las taras
    public List<Task> getAllTasks() throws SQLException {

    String sql = "SELECT * FROM tasks ORDER BY id";
    List<Task> tasks = new ArrayList<>();

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            Task task = new Task(
                rs.getString("title"),
                rs.getString("description"),
                Priority.valueOf(rs.getString("priority"))
            );

            task.setId(rs.getInt("id"));
            task.setCompleted(rs.getBoolean("completed"));

            Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) {
                task.setCreatedAt(createdAt.toLocalDateTime());
            }

            tasks.add(task);
        }
    }

    return tasks;
}

    public Task getTaskById(int id) throws SQLException {
    String sql = "SELECT * FROM tasks WHERE id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, id);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                Task task = new Task(
                    rs.getString("title"),
                    rs.getString("description"),
                    Priority.valueOf(rs.getString("priority"))
                );

                task.setId(rs.getInt("id"));
                task.setCompleted(rs.getBoolean("completed"));

                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    task.setCreatedAt(createdAt.toLocalDateTime());
                }

                return task;
            }
        }
    }

    return null; // No se encontró una tarea con ese ID.
}

    public boolean createTask(Task task) throws SQLException{

        String sql = "INSERT INTO tasks (title, description, priority) VALUES (?, ?, ?)";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

                ps.setString(1, task.getTitle());
                ps.setString(2, task.getDescription());
                ps.setString(3, task.getPriority().name());

                int rowsInserted = ps.executeUpdate();
                return rowsInserted > 0;
        }
    }

    public boolean updateTask(Task task, int id) throws SQLException{

        String sql = "UPDATE tasks SET title = ?, description = ?, priority =?, completed = ? WHERE id = ?";
        
        String newTitle = task.getTitle();
        String newDescription = task.getDescription();
        String newPriority = task.getPriority().name();
        boolean newCompleted = task.isCompleted();
        

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

                ps.setString(1, newTitle);
                ps.setString(2, newDescription);
                ps.setString(3, newPriority);
                ps.setBoolean(4, newCompleted);
                ps.setInt(5, id);

                int rowUpdated = ps.executeUpdate();
                return rowUpdated > 0;                
        }
    }

    public boolean removeTask(int id) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int rowsRemoved = ps.executeUpdate();

            return rowsRemoved > 0;
        }
    }

    public int removeCompletedTasks() throws SQLException {
        String sql = "DELETE FROM tasks WHERE completed = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            return ps.executeUpdate();
        }
    }
}
