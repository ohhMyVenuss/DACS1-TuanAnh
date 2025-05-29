import javafx.stage.Stage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WindowManager {
    private static WindowManager instance;
    private final ConcurrentHashMap<String, Stage> windows;
    private final ExecutorService executorService;

    private WindowManager() {
        windows = new ConcurrentHashMap<>();
        executorService = Executors.newCachedThreadPool();
    }

    public static WindowManager getInstance() {
        if (instance == null) {
            instance = new WindowManager();
        }
        return instance;
    }

    public void openNewWindow(String windowId, Runnable windowCreator) {
        executorService.execute(() -> {
            javafx.application.Platform.runLater(() -> {
                Stage stage = new Stage();
                windows.put(windowId, stage);
                windowCreator.run();
            });
        });
    }

    public void closeWindow(String windowId) {
        Stage stage = windows.get(windowId);
        if (stage != null) {
            javafx.application.Platform.runLater(() -> {
                stage.close();
                windows.remove(windowId);
            });
        }
    }

    public Stage getWindow(String windowId) {
        return windows.get(windowId);
    }

    public void shutdown() {
        executorService.shutdown();
        windows.clear();
    }
} 