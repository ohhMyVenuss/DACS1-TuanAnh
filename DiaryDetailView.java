import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;

public class DiaryDetailView extends Stage {

    public DiaryDetailView(DiaryEntry entry) {
        this.initModality(Modality.APPLICATION_MODAL);
        this.setTitle("Chi tiết nhật ký");

        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 100%, #e0e7ff, #c7d2fe, #a5b4fc);");

        Label titleLabel = new Label(entry.getTitle());
        titleLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 28));
        titleLabel.setTextFill(Color.web("#232136"));
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setWrapText(true);

        Label dateLabel = new Label(entry.getDate());
        dateLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        dateLabel.setTextFill(Color.web("#45475a"));
        dateLabel.setAlignment(Pos.CENTER_RIGHT);
        dateLabel.setMaxWidth(Double.MAX_VALUE);

        ImageView imageView = null;
        if (entry.getImagePath() != null && !entry.getImagePath().isEmpty()) {
            Image image = new Image(entry.getImagePath());
            imageView = new ImageView(image);
            imageView.setFitWidth(600); // Adjust width as needed
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setStyle("-fx-background-radius: 10;");
        }

        Label contentLabel = new Label(entry.getContent());
        contentLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 17));
        contentLabel.setTextFill(Color.web("#45475a"));
        contentLabel.setWrapText(true);

        VBox contentBox = new VBox(10);
        contentBox.getChildren().add(titleLabel);
        contentBox.getChildren().add(dateLabel);

        if (imageView != null) {
            contentBox.getChildren().add(imageView);
        }
        contentBox.getChildren().add(contentLabel);

        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        root.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 700, 600);
        this.setScene(scene);
    }
} 