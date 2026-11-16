package todolist.ui;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import javafx.scene.control.Alert;
import javafx.stage.Window;
import todolist.database.DatabaseConnection;

public final class Errors {
    private Errors() { }

    public static void show(Window owner, String header, Throwable error) {
        Path log = Path.of(System.getProperty("user.home"), ".todolist", "errors.log");
        StringWriter detail = new StringWriter();
        error.printStackTrace(new PrintWriter(detail));
        boolean logged = false;
        try {
            Files.createDirectories(log.getParent());
            Files.writeString(log, LocalDateTime.now() + " " + header + "\n" + detail + "\n",
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            logged = true;
        } catch (IOException e) { error.printStackTrace(); }
        String message = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
        if (error instanceof SQLException) {
            message = "Revisa que MariaDB esté iniciado, que exista todolist.tasks y la configuración en "
                    + DatabaseConnection.configPath()
                    + ".\nSi falló una escritura, actualiza la lista antes de repetir: podría haberse guardado."
                    + "\n\nDetalle: " + message;
        }
        Alert alert = new Alert(Alert.AlertType.ERROR);
        if (owner != null) alert.initOwner(owner);
        alert.setTitle("ToDoList");
        alert.setHeaderText(header);
        alert.setContentText(message + (logged ? "\n\nRegistro: " + log : ""));
        alert.getDialogPane().setPrefWidth(600);
        alert.showAndWait();
    }
}
