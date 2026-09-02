package harold.gui;

import java.util.Objects;

import harold.Harold;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Configures and displays Harold's JavaFX window.
 */
public class HaroldApplication extends Application {
    private static final int WINDOW_WIDTH = 720;
    private static final int WINDOW_HEIGHT = 760;

    /**
     * Creates the JavaFX application instance.
     */
    public HaroldApplication() {
    }

    @Override
    public void start(Stage stage) {
        MainWindow root = new MainWindow(new Harold());
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        String stylesheet = Objects.requireNonNull(
                getClass().getResource("harold.css")).toExternalForm();
        scene.getStylesheets().add(stylesheet);

        stage.setTitle("Harold — Frenchie Task Companion");
        stage.setMinWidth(560);
        stage.setMinHeight(620);
        stage.setScene(scene);
        stage.show();
    }
}
