import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SignUpView extends StackPane {
    public SignUpView(Stage primaryStage) {
        setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #1c1c1c, #3a3a52);");

        VBox mainBox = new VBox(18);
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setPadding(new Insets(30, 40, 30, 40));
        mainBox.setMaxWidth(600);
        mainBox.setStyle("-fx-background-radius: 20; -fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #232136, #45475a); -fx-effect: dropshadow(gaussian, #00000055, 20, 0.2, 0, 4);");

        Label title = new Label("Sign Up");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setAlignment(Pos.CENTER);
        title.getStyleClass().add("section-title");
        title.setTextFill(Color.web("#e5e7eb"));
        Label subtitle = new Label("Create your account");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        subtitle.setAlignment(Pos.CENTER);
        subtitle.getStyleClass().add("info-label");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Họ và tên");
        fullNameField.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        fullNameField.setMaxWidth(300);
        fullNameField.setStyle("-fx-background-radius: 10; -fx-background-color: #e5e7eb; -fx-border-radius: 10; -fx-border-color: #a5b4fc; -fx-border-width: 1.2; -fx-padding: 8 12 8 12; -fx-text-fill: #232136;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        usernameField.setMaxWidth(300);
        usernameField.setStyle("-fx-background-radius: 10; -fx-background-color: #e5e7eb; -fx-border-radius: 10; -fx-border-color: #a5b4fc; -fx-border-width: 1.2; -fx-padding: 8 12 8 12; -fx-text-fill: #232136;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        emailField.setMaxWidth(300);
        emailField.setStyle("-fx-background-radius: 10; -fx-background-color: #e5e7eb; -fx-border-radius: 10; -fx-border-color: #a5b4fc; -fx-border-width: 1.2; -fx-padding: 8 12 8 12; -fx-text-fill: #232136;");

        TextField addressField = new TextField();
        addressField.setPromptText("Địa chỉ");
        addressField.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        addressField.setMaxWidth(300);
        addressField.setStyle("-fx-background-radius: 10; -fx-background-color: #e5e7eb; -fx-border-radius: 10; -fx-border-color: #a5b4fc; -fx-border-width: 1.2; -fx-padding: 8 12 8 12; -fx-text-fill: #232136;");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Số điện thoại");
        phoneField.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        phoneField.setMaxWidth(300);
        phoneField.setStyle("-fx-background-radius: 10; -fx-background-color: #e5e7eb; -fx-border-radius: 10; -fx-border-color: #a5b4fc; -fx-border-width: 1.2; -fx-padding: 8 12 8 12; -fx-text-fill: #232136;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        passwordField.setMaxWidth(300);
        passwordField.setStyle("-fx-background-radius: 10; -fx-background-color: #e5e7eb; -fx-border-radius: 10; -fx-border-color: #a5b4fc; -fx-border-width: 1.2; -fx-padding: 8 12 8 12; -fx-text-fill: #232136;");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        confirmPasswordField.setMaxWidth(300);
        confirmPasswordField.setStyle("-fx-background-radius: 10; -fx-background-color: #e5e7eb; -fx-border-radius: 10; -fx-border-color: #a5b4fc; -fx-border-width: 1.2; -fx-padding: 8 12 8 12; -fx-text-fill: #232136;");

        Button signUpBtn = new Button("Sign Up");
        signUpBtn.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        signUpBtn.setStyle("-fx-background-radius: 10; -fx-background-color: #6d28d9; -fx-text-fill: white; -fx-cursor: hand;");
        signUpBtn.setMaxWidth(300);
        signUpBtn.setOnMouseEntered(e -> signUpBtn.setStyle("-fx-background-radius: 10; -fx-background-color: #a5b4fc; -fx-text-fill: #232136; -fx-cursor: hand;"));
        signUpBtn.setOnMouseExited(e -> signUpBtn.setStyle("-fx-background-radius: 10; -fx-background-color: #6d28d9; -fx-text-fill: white; -fx-cursor: hand;"));

        Label errorLabel = new Label();
        errorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        signUpBtn.setOnAction(e -> {
            String user = usernameField.getText();
            String pass = passwordField.getText();
            String confirm = confirmPasswordField.getText();
            String email = emailField.getText();
            String address = addressField.getText();
            String phone = phoneField.getText();
            String fullName = fullNameField.getText();

            if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty() || email.isEmpty() || address.isEmpty() || phone.isEmpty() || fullName.isEmpty()) {
                errorLabel.setText("Vui lòng nhập đầy đủ thông tin!");
            } else if (!pass.equals(confirm)) {
                errorLabel.setText("Mật khẩu xác nhận không khớp!");
            } else {

                if (DatabaseManager.addUser(user, pass, email, address, phone, fullName)) {
                    errorLabel.setTextFill(Color.web("#4CAF50"));
                    errorLabel.setText("Đăng ký thành công!");
                    Stage currentStage = (Stage) getScene().getWindow();
                    LoginView loginView = new LoginView(currentStage);
                    Scene loginScene = new Scene(loginView, 400, 500);
                    currentStage.setScene(loginScene);
                    currentStage.setTitle("Login");
                    currentStage.show();
                    javafx.application.Platform.runLater(() -> currentStage.centerOnScreen());
                } else {
                    errorLabel.setTextFill(Color.web("#DC143C"));
                    errorLabel.setText("Đăng ký thất bại. Tên đăng nhập có thể đã tồn tại.");
                }
            }
        });

        Button backBtn = new Button("← Back to Login");
        backBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        backBtn.setStyle("-fx-background-radius: 8; -fx-background-color: #ede9fe; -fx-text-fill: #6d28d9; -fx-cursor: hand;");
        backBtn.setOnMouseEntered(e -> backBtn.setStyle("-fx-background-radius: 8; -fx-background-color: #c7d2fe; -fx-text-fill: #232136; -fx-cursor: hand;"));
        backBtn.setOnMouseExited(e -> backBtn.setStyle("-fx-background-radius: 8; -fx-background-color: #ede9fe; -fx-text-fill: #6d28d9; -fx-cursor: hand;"));
        backBtn.setOnAction(e -> {
            Stage currentStage = (Stage) getScene().getWindow();
            LoginView loginView = new LoginView(currentStage);
            Scene loginScene = new Scene(loginView, 400, 500);
            currentStage.setScene(loginScene);
            currentStage.setTitle("Login");
            currentStage.show();
            javafx.application.Platform.runLater(() -> currentStage.centerOnScreen());
        });

        mainBox.getChildren().addAll(title, subtitle, fullNameField, usernameField, emailField, addressField, phoneField, passwordField, confirmPasswordField, signUpBtn, errorLabel, backBtn);
        setAlignment(Pos.CENTER);
        getChildren().add(mainBox);

        mainBox.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(600), mainBox);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }
}