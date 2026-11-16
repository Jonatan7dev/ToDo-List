package todolist.database;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {
    private DatabaseConnection() { }

    // Ruta estable: no depende del IDE ni de dónde se abra el EXE.
    public static Path configPath() {
        return Path.of(System.getProperty("user.home"), ".todolist", "database.properties");
    }

    public static Connection getConnection() throws SQLException {
        Properties config = new Properties();
        Path path = configPath();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                config.load(reader);
            } catch (IOException | IllegalArgumentException e) {
                throw new SQLException("No se puede leer " + path, e);
            }
        }
        Properties options = new Properties();
        options.setProperty("user", config.getProperty("user", "root"));
        options.setProperty("password", config.getProperty("password", ""));
        options.setProperty("connectTimeout", "3000");
        options.setProperty("socketTimeout", "5000");
        // Contar filas encontradas: guardar sin cambios también es válido.
        options.setProperty("useAffectedRows", "false");
        return DriverManager.getConnection(config.getProperty("url",
                "jdbc:mysql://127.0.0.1:3306/todolist"), options);
    }
}
