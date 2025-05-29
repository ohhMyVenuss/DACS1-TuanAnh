import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    private WindowManager windowManager;

    @Override
    public void start(Stage primaryStage) {
        windowManager = WindowManager.getInstance();

        windowManager.openNewWindow("main", () -> {
            LoginView loginView = new LoginView(primaryStage);
            Scene scene = new Scene(loginView, 400, 500);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Login");
            primaryStage.show();
            primaryStage.centerOnScreen();
        });
    }

    @Override
    public void stop() {
        if (windowManager != null) {
            windowManager.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}