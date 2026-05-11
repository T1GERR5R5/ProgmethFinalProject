package application;

import application.scene.StartScene;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX application entry point for AnimalWizard.
 * Holds the shared {@link Stage} reference so any scene can navigate to another.
 */
public class Main extends Application {

    /** The application's primary window; shared across all scenes. */
    public static Stage  window;

    /**
     * JavaFX lifecycle entry point. Initialises the window and shows the start scene.
     * @param stage the primary stage provided by the JavaFX runtime
     */
    @Override
    public void start(Stage stage) {
        window = stage;
        window.setTitle("AnimalWizard");
        window.setScene(new StartScene().build());
        window.show();
    }

    /** Launches the JavaFX runtime. */
    public static void main(String[] args) { launch(); }
}
