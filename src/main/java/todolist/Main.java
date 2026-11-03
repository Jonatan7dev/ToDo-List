package todolist;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import todolist.repository.TaskRepository;
import todolist.service.TaskService;
import todolist.ui.MainController;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
            Main.class.getResource("/todolist/ui/Principal.fxml")
        );
        Parent root = loader.load();

        stage.setTitle("Mis tareas");
        stage.setScene(new Scene(root));
        stage.show();

        MainController controller = loader.getController();
        controller.setTaskService(new TaskService(new TaskRepository()));
    }

    public static void main(String[] args) {
        launch(args);
    }
}