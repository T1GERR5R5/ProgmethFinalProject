package application;

import application.scene.StartScene;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public static Stage  window;

    @Override
    public void start(Stage stage) {
        window = stage;
        window.setTitle("AnimalWizard");
        window.setScene(new StartScene().build());
        window.show();
    }
    public static void main(String[] args) { launch(); }
}
