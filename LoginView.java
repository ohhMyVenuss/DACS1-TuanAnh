import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;

public class LoginView extends StackPane {
    private Stage primaryStage;
    private Label errorLabel;

    public LoginView(Stage primaryStage) {
        this.primaryStage = primaryStage;

        this.primaryStage.setTitle("Diary App");

        setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #1c1c1c, #3a3a52);");
        VBox mainBox = new VBox(18);
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setPadding(new Insets(30, 40, 30, 40));
        mainBox.setMaxWidth(400);
        mainBox.setStyle("-fx-background-radius: 20; -fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #232136, #45475a); -fx-effect: dropshadow(gaussian, #00000055, 20, 0.2, 0, 4);");

        Label title = new Label("Welcome Back");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setAlignment(Pos.CENTER);
        title.getStyleClass().add("section-title");
        title.setTextFill(Color.web("#e5e7eb"));
        Label subtitle = new Label("Sign in to continue");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        subtitle.setAlignment(Pos.CENTER);
        subtitle.getStyleClass().add("info-label");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        usernameField.setMaxWidth(300);
        usernameField.setStyle("-fx-background-radius: 10; -fx-background-color: #e5e7eb; -fx-border-radius: 10; -fx-border-color: #a5b4fc; -fx-border-width: 1.2; -fx-padding: 8 12 8 12; -fx-text-fill: #232136;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        passwordField.setMaxWidth(300);
        passwordField.setStyle("-fx-background-radius: 10; -fx-background-color: #e5e7eb; -fx-border-radius: 10; -fx-border-color: #a5b4fc; -fx-border-width: 1.2; -fx-padding: 8 12 8 12; -fx-text-fill: #232136;");

        HBox optionsBox = new HBox();
        optionsBox.setAlignment(Pos.CENTER_LEFT);
        CheckBox rememberMe = new CheckBox("Remember me");
        rememberMe.setStyle("-fx-cursor: hand; -fx-text-fill: #c7d2fe;");
        Button forgotBtn = new Button("Forgot password?");
        forgotBtn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        forgotBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #c7d2fe; -fx-cursor: hand;");
        forgotBtn.setOnAction(e -> showForgotPasswordDialog());
        optionsBox.getChildren().addAll(rememberMe, new Region(), forgotBtn);
        HBox.setHgrow(optionsBox.getChildren().get(1), Priority.ALWAYS);

        Button loginBtn = new Button("Login");
        loginBtn.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        loginBtn.setStyle("-fx-background-radius: 10; -fx-background-color: #6d28d9; -fx-text-fill: white; -fx-cursor: hand;");
        loginBtn.setMaxWidth(300);
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle("-fx-background-radius: 10; -fx-background-color: #a5b4fc; -fx-text-fill: #232136; -fx-cursor: hand;"));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle("-fx-background-radius: 10; -fx-background-color: #6d28d9; -fx-text-fill: white; -fx-cursor: hand;"));

        errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        loginBtn.setOnAction(e -> {
            String user = usernameField.getText();
            String pass = passwordField.getText();
            handleLogin(user, pass);
        });

        HBox registerBox = new HBox(6);
        registerBox.setAlignment(Pos.CENTER);
        Label noAccount = new Label("Don't have an account?");
        noAccount.getStyleClass().add("info-label");
        noAccount.setTextFill(Color.web("#e5e7eb"));
        Button signUpBtn = new Button("Sign Up");
        signUpBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        signUpBtn.setStyle("-fx-background-radius: 8; -fx-background-color: #ede9fe; -fx-text-fill: #6d28d9; -fx-cursor: hand;");
        signUpBtn.setOnMouseEntered(e -> signUpBtn.setStyle("-fx-background-radius: 8; -fx-background-color: #c7d2fe; -fx-text-fill: #232136; -fx-cursor: hand;"));
        signUpBtn.setOnMouseExited(e -> signUpBtn.setStyle("-fx-background-radius: 8; -fx-background-color: #ede9fe; -fx-text-fill: #6d28d9; -fx-cursor: hand;"));
        signUpBtn.setOnAction(e -> {
            Stage currentStage = (Stage) getScene().getWindow();
            SignUpView signUpView = new SignUpView(currentStage);
            Scene signUpScene = new Scene(signUpView, 400, 650);
            currentStage.setScene(signUpScene);

            currentStage.setTitle("Diary App");
            currentStage.show();
            javafx.application.Platform.runLater(() -> currentStage.centerOnScreen());
        });
        registerBox.getChildren().addAll(noAccount, signUpBtn);

        mainBox.getChildren().addAll(title, subtitle, usernameField, passwordField, optionsBox, loginBtn, errorLabel, registerBox);
        setAlignment(Pos.CENTER);
        getChildren().add(mainBox);

        mainBox.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(600), mainBox);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        Button logoutButton = new Button("Logout");
        logoutButton.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        logoutButton.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 10;");
        logoutButton.setMaxWidth(300);
        logoutButton.setOnMouseEntered(e -> logoutButton.setStyle("-fx-background-color: #a5b4fc; -fx-text-fill: #232136; -fx-background-radius: 10;"));
        logoutButton.setOnMouseExited(e -> logoutButton.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 10;"));
        logoutButton.setOnAction(e -> {

            LoginView loginView = new LoginView(primaryStage);
            Scene loginScene = new Scene(loginView, 400, 500);
            primaryStage.setScene(loginScene);
            // Set the window title back to "Diary App"
            primaryStage.setTitle("Diary App");
            primaryStage.show();
            javafx.application.Platform.runLater(() -> primaryStage.centerOnScreen());
        });
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);

    }

    private void handleLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập tên người dùng và mật khẩu.");
            return;
        }

        if (DatabaseManager.isAdmin(username, password)) {
            System.out.println("Admin Login Successful");

            AdminView adminView = new AdminView();
            Scene adminScene = new Scene(adminView, 1200, 800);
            primaryStage.setScene(adminScene);

            primaryStage.setTitle("Diary App");
            primaryStage.show();
            javafx.application.Platform.runLater(() -> primaryStage.centerOnScreen());

        } else {

            int userId = DatabaseManager.validateUser(username, password);
            if (userId != -1) {

                WindowManager.getInstance().openNewWindow("diary_" + username, () -> {
                    Stage diaryStage = new Stage();
                    DiaryMainView diaryView = new DiaryMainView(diaryStage);
                    Scene scene = new Scene(diaryView, 1200, 800);
                    diaryStage.setScene(scene);

                    diaryStage.setTitle("Diary App");
                    diaryStage.show();
                    javafx.application.Platform.runLater(() -> diaryStage.centerOnScreen());
                });

                Stage currentStage = (Stage) getScene().getWindow();
                currentStage.close();
            } else {

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi đăng nhập");
                alert.setHeaderText(null);
                alert.setContentText("Tên đăng nhập hoặc mật khẩu không đúng!");
                alert.showAndWait();
            }
        }
    }

    private void showForgotPasswordDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Forgot Password");
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        VBox box = new VBox(18);
        box.setPadding(new Insets(28, 32, 24, 32));
        box.setAlignment(Pos.CENTER);
        box.setSpacing(16);
        box.setStyle("-fx-background-radius: 18; -fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #ede9fe, #c7d2fe); -fx-effect: dropshadow(gaussian, #a5b4fc88, 10, 0.2, 0, 2);");
        box.setPickOnBounds(false);

        Label titleLbl = new Label("Quên mật khẩu");
        titleLbl.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 22));
        titleLbl.getStyleClass().add("section-title");
        titleLbl.setTextFill(Color.web("#6d28d9"));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Tên đăng nhập");
        usernameField.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        usernameField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #a5b4fc; -fx-border-width: 1.2; -fx-background-color: #fff; -fx-text-fill: #232136;");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Số điện thoại");
        phoneField.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        phoneField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #a5b4fc; -fx-border-width: 1.2; -fx-background-color: #fff; -fx-text-fill: #232136;");

        Button nextBtn = new Button("Tiếp tục");
        nextBtn.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        nextBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 10;");
        nextBtn.setOnMouseEntered(e -> nextBtn.setStyle("-fx-background-color: #a5b4fc; -fx-text-fill: #232136; -fx-background-radius: 10;"));
        nextBtn.setOnMouseExited(e -> nextBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 10;"));

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));

        nextBtn.setOnAction(ev -> {
            String username = usernameField.getText().trim();
            String phone = phoneField.getText().trim();
            if (username.isEmpty() || phone.isEmpty()) {
                errorLabel.setText("Vui lòng nhập đầy đủ thông tin!");
                return;
            }
            boolean verificationSuccessful = DatabaseManager.verifyUserForPasswordReset(username, phone);
            if (verificationSuccessful) {
                dialog.close();
                showPasswordResetDialog(username);
            } else {
                errorLabel.setText("Tên đăng nhập hoặc số điện thoại không chính xác.");
            }
        });

        box.getChildren().addAll(titleLbl, usernameField, phoneField, errorLabel, nextBtn);
        Scene scene = new Scene(box);
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.showAndWait();
    }

    private void showPasswordResetDialog(String username) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Đặt lại mật khẩu");
        dialog.setHeaderText(null);

        VBox box = new VBox(18);
        box.setPadding(new Insets(28, 32, 24, 32));
        box.setAlignment(Pos.CENTER);
        box.setSpacing(16);
        box.setStyle("-fx-background-radius: 18; -fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #ede9fe, #c7d2fe); -fx-effect: dropshadow(gaussian, #a5b4fc88, 10, 0.2, 0, 2);");

        Label titleLbl = new Label("Đặt lại mật khẩu");
        titleLbl.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 22));
        titleLbl.getStyleClass().add("section-title");
        titleLbl.setTextFill(Color.web("#6d28d9"));

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Mật khẩu mới");
        newPasswordField.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        newPasswordField.setPrefWidth(300);
        newPasswordField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #a5b4fc; -fx-border-width: 1.2; -fx-background-color: #fff; -fx-text-fill: #232136;");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Xác nhận mật khẩu mới");
        confirmPasswordField.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        confirmPasswordField.setPrefWidth(300);
        confirmPasswordField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #a5b4fc; -fx-border-width: 1.2; -fx-background-color: #fff; -fx-text-fill: #232136;");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        errorLabel.setPrefWidth(300);
        errorLabel.setWrapText(true);
        errorLabel.setTextFill(Color.web("#ef4444"));

        Button saveBtn = new Button("Lưu");
        saveBtn.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        saveBtn.setPrefWidth(300);
        saveBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 10;");
        saveBtn.setOnMouseEntered(e -> saveBtn.setStyle("-fx-background-color: #a5b4fc; -fx-text-fill: #232136; -fx-background-radius: 10;"));
        saveBtn.setOnMouseExited(e -> saveBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 10;"));

        box.getChildren().addAll(titleLbl, newPasswordField, confirmPasswordField, errorLabel, saveBtn);

        dialog.getDialogPane().setContent(box);

        ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(saveButtonType);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setVisible(false);

        saveBtn.setOnAction(e -> {
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                errorLabel.setText("Vui lòng nhập đầy đủ thông tin!");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                errorLabel.setText("Mật khẩu mới và xác nhận mật khẩu không khớp!");
                return;
            }

            boolean updateSuccessful = DatabaseManager.updatePassword(username, newPassword);
            if (updateSuccessful) {
                dialog.close();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Mật khẩu đã được đặt lại thành công!");
                alert.showAndWait();
            } else {
                errorLabel.setText("Có lỗi xảy ra khi đặt lại mật khẩu!");
            }
        });

        dialog.showAndWait();
    }
} 