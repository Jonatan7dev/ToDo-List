package todolist.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/todolist";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos.");
            System.err.println("Código SQL: " + e.getErrorCode());
            System.err.println("Estado SQL: " + e.getSQLState());
            System.err.println("Mensaje: " + e.getMessage());

            throw e;
        }
    }

    public static void main(String[] args) {

        try (Connection connection = getConnection()) {
            System.out.println("Conexión realizada correctamente.");
        } catch (SQLException e) {
            System.out.println("Error al conectar con la base de datos.");
            e.printStackTrace();
        }
    }
}