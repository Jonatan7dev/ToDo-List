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

public class TaskRepository {

    //Obtener las taras
    public List<Task> getAllTasks() throws SQLException{

        String sql = "SELECT * FROM tasks ORDER BY id";
        List<Task> tasks = new ArrayList<>();

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){

                while (rs.next()) {
                    Timestamp createdAt = rs.getTimestamp("created_at");

                    Task task = new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("priority"),
                        rs.getBoolean("completed"),
                        createdAt == null ? null : createdAt.toLocalDateTime()
                    );
                    
                    tasks.add(task);
                }
            }
        return tasks;
    }

    public List<Task> getTasksByID(int id) throws SQLException {

        String sql = "SELECT * FROM tasks WHERE id = ?";
        List<Task> tasksByID = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp createdAt = rs.getTimestamp("created_at");

                    Task task = new Task(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("priority"),
                            rs.getBoolean("completed"),
                            createdAt == null ? null : createdAt.toLocalDateTime()
                    );

                    tasksByID.add(task);
                }
            }
        }

        return tasksByID;
    }

    public boolean createTask(Task task) throws SQLException{

        String sql = "INSERT INTO tasks (title, description, priority) VALUES (?, ?, ?)";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

                ps.setString(1, task.getTitle());
                ps.setString(2, task.getDescription());
                ps.setString(3, task.getPriority());

                int rowsInserted = ps.executeUpdate();
                return rowsInserted > 0;
            }
    }
}
