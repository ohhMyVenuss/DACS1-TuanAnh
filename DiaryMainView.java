import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.shape.Circle;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.stage.FileChooser;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javafx.geometry.Orientation;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Timer;
import java.util.TimerTask;
public class DiaryMainView extends StackPane {
    private VBox mainBox;
    private HBox topBar;
    private HBox menuBar;
    private Label headerLabel;
    private VBox diaryListBox;
    private ScrollPane scrollPane;
    private List<DiaryEntry> diaryEntries;
    private List<DiaryEntry> sharedEntries;
    private String currentTab = "Trang chủ";
    private Button fabBtn;
    private List<Button> menuButtons = new ArrayList<>();
    private String currentTheme = "light";
    private UserSettings settings;
    private boolean notificationsEnabled = true;
    private ImageView bellIcon;
    private VBox notificationBox;
    private List<Notification> notifications = new ArrayList<>();
    private Timer notificationTimer;
    private Button bellButton;

    public static class Notification {
        String message;
        String type;
        LocalDateTime timestamp;
        int relatedId;
        
        Notification(String message, String type) {
            this.message = message;
            this.type = type;
            this.timestamp = LocalDateTime.now();
        }

        Notification(String message, String type, int relatedId) {
            this.message = message;
            this.type = type;
            this.timestamp = LocalDateTime.now();
            this.relatedId = relatedId;
        }

        public Notification() {

        }
    }

    public String getCurrentTheme() {
        return currentTheme;
    }

    public DiaryMainView(Stage primaryStage) {
        setPrefSize(1200, 800);
        mainBox = new VBox();
        mainBox.setSpacing(0);
        mainBox.setPrefSize(1200, 800);

        mainBox.setStyle("-fx-background-color: linear-gradient(to right, #e0e7ff, #c7d2fe, #a5b4fc);");

        getStylesheets().add(getClass().getResource("/res/diaryfx.css").toExternalForm());
        
        int currentUserId = DatabaseManager.getCurrentUserId();
        settings = DatabaseManager.getUserSettings(currentUserId);
        if (settings == null) {
            settings = new UserSettings(currentUserId, false, "dark", "vi");
            DatabaseManager.updateUserSettings(settings);
        }
        notificationsEnabled = settings.isNotificationsEnabled();

        currentTheme = settings.getTheme();
        if (currentTheme == null || !currentTheme.equals("dark")) {
            currentTheme = "light";
        }

        createTopBar();
        createMenuBar();
        createHeader();
        createDiaryLists();

        mainBox.getChildren().addAll(topBar, menuBar, headerLabel, scrollPane);

        notificationBox = new VBox(8);
        notificationBox.setPadding(new Insets(12));
        notificationBox.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");
        notificationBox.setVisible(false);
        notificationBox.setMaxWidth(300);

        getChildren().addAll(mainBox, bellButton, notificationBox);

        createFloatingAddButton();
        updateTab("Trang chủ");

        bellIcon.setVisible(notificationsEnabled);
        if (notificationsEnabled) {
            loadExistingNotifications();
        }

        javafx.application.Platform.runLater(() -> {
            Stage stage = (Stage) getScene().getWindow();
            if (stage != null) stage.centerOnScreen();
        });

        StackPane.setAlignment(bellButton, Pos.TOP_RIGHT);
        StackPane.setMargin(bellButton, new Insets(12, 20, 0, 0));

        StackPane.setAlignment(notificationBox, Pos.TOP_RIGHT);
        StackPane.setMargin(notificationBox, new Insets(60, 20, 0, 0));
    }

    private void createTopBar() {
        topBar = new HBox();
        topBar.setPadding(new Insets(12, 32, 12, 32));
        topBar.setSpacing(16);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: transparent;");

        ImageView avatar = new ImageView(new Image("file:D:/dacs1/res/avatar.png"));
        avatar.setFitWidth(36);
        avatar.setFitHeight(36);

        String displayUserName = "User";
        int currentUserId = DatabaseManager.getCurrentUserId();
        if (currentUserId != -1) {
            UserProfile currentUserProfile = DatabaseManager.getUserProfile(currentUserId);
            if (currentUserProfile != null) {

                if (currentUserProfile.getFullName() != null && !currentUserProfile.getFullName().trim().isEmpty()) {
                    displayUserName = currentUserProfile.getFullName();
                } else if (currentUserProfile.getUsername() != null && !currentUserProfile.getUsername().trim().isEmpty()) {
                    displayUserName = currentUserProfile.getUsername();
                }
            }
        }

        MenuButton userMenu = new MenuButton(displayUserName );
        userMenu.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        userMenu.setTextFill(currentTheme.equals("light") ? Color.web("#1f2937") : Color.web("#bfaee0")); // Darken text in light theme
        MenuItem changePass = new MenuItem("Đổi mật khẩu");
        MenuItem logout = new MenuItem("Đăng xuất");
        userMenu.getItems().addAll(changePass, new SeparatorMenuItem(), logout);

        bellIcon = new ImageView(new Image("file:D:/dacs1/res/bell.png"));
        bellIcon.setFitWidth(24);
        bellIcon.setFitHeight(24);

        bellButton = new Button();
        bellButton.setGraphic(bellIcon);
        bellButton.setStyle("-fx-background-color: transparent; -fx-padding: 4; -fx-cursor: hand;");

        bellButton.setOnMouseClicked(e -> {
            if (notificationsEnabled) {
                notificationBox.setVisible(!notificationBox.isVisible());
                if (notificationBox.isVisible()) {

                    DatabaseManager.markNotificationsAsRead(DatabaseManager.getCurrentUserId());
                }
                updateNotificationBox();
            }
        });

        Label forgotPasswordLabel = new Label("Quên mật khẩu?");
        forgotPasswordLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        forgotPasswordLabel.setTextFill(currentTheme.equals("light") ? Color.web("#312e81") : Color.web("#a5b4fc")); // Darken text in light theme
        forgotPasswordLabel.setStyle("-fx-underline: true; -fx-cursor: hand;"); // Underline and hand cursor
        forgotPasswordLabel.setOnMouseClicked(e -> showForgotPasswordDialog()); // Add click handler

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(avatar, userMenu, spacer);

        changePass.setOnAction(e -> {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Đổi mật khẩu");
            dialog.setHeaderText("Nhập mật khẩu mới:");
            PasswordField passField = new PasswordField();
            dialog.getDialogPane().setContent(passField);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.setResultConverter(btn -> btn == ButtonType.OK ? passField.getText() : null);

            dialog.showAndWait().ifPresent(newPassword -> {
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    if (currentUserId != -1) {
                        // TODO: Implement password update logic in DatabaseManager
                        boolean updateSuccessful = DatabaseManager.updatePassword(currentUserId, newPassword);

                        javafx.application.Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle(updateSuccessful ? "Thành công" : "Lỗi");
                            alert.setHeaderText(null);
                            alert.setContentText(updateSuccessful ? "Mật khẩu đã được đổi thành công!" : "Có lỗi xảy ra khi đổi mật khẩu.");
                            alert.showAndWait();
                        });
                    } else {
                        javafx.application.Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Lỗi");
                            alert.setHeaderText(null);
                            alert.setContentText("Không tìm thấy ID người dùng hiện tại.");
                            alert.showAndWait();
                        });
                    }
                } else if (newPassword != null) { // Handle empty input if OK was pressed
                     javafx.application.Platform.runLater(() -> {
                         Alert alert = new Alert(Alert.AlertType.WARNING);
                         alert.setTitle("Cảnh báo");
                         alert.setHeaderText(null);
                         alert.setContentText("Mật khẩu mới không được để trống.");
                         alert.showAndWait();
                     });
                }
            });
        });

        logout.setOnAction(e -> {

            Stage currentStage = (Stage) getScene().getWindow();
            LoginView loginView = new LoginView(currentStage);
            Scene loginScene = new Scene(loginView, 400, 500);
            currentStage.setScene(loginScene);
            currentStage.setTitle("Login");
            currentStage.show();
            javafx.application.Platform.runLater(() -> currentStage.centerOnScreen());
        });
    }

    private void createMenuBar() {
        menuBar = new HBox();
        menuBar.setSpacing(0);
        menuBar.setAlignment(Pos.CENTER_LEFT);
        menuBar.setStyle("-fx-background-color: #ede9fe; -fx-padding: 0 40 0 40;");
        menuBar.setPrefHeight(56);
        String[][] menus = {
            {"Trang chủ", "home.png"},
            {"Nhật ký của tôi", "diary.png"},
            {"Nhật ký được chia sẻ", "shared.png"},
            {"Bạn bè", "friends.png"},
            {"Cài đặt", "settings.png"}
        };
        for (int i = 0; i < menus.length; i++) {
            Button btn = createMenuButton(menus[i][0], menus[i][1]);
            menuButtons.add(btn);
            menuBar.getChildren().add(btn);
            if (i < menus.length - 1) {
                Separator sep = new Separator();
                sep.setOrientation(Orientation.VERTICAL);
                sep.setPrefHeight(32);
                sep.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 100%, #232136, #000000); -fx-padding: 0 0 0 0; -fx-pref-width: 1px;");
                menuBar.getChildren().add(sep);
            }
        }
    }

    private Button createMenuButton(String text, String iconFile) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 17));
        btn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-padding: 0 20 0 20; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 17px;" +
            (currentTheme.equals("light") ? "-fx-text-fill: #232136;" : "-fx-text-fill: #c7d2fe;")
        );
        btn.setPrefHeight(56);
        btn.setAlignment(Pos.CENTER);
        btn.setGraphic(new ImageView(new Image("file:D:/dacs1/res/" + iconFile, 24, 24, true, true)));
        btn.setContentDisplay(ContentDisplay.LEFT);
        btn.setOnAction(e -> updateTab(text));
        return btn;
    }

    private void createHeader() {
        headerLabel = new Label();
        headerLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 34));
        headerLabel.setPadding(new Insets(28, 0, 18, 40));
        headerLabel.setTextFill(currentTheme.equals("light") ? Color.web("#232136") : Color.web("#e5e7eb"));

    }

    private void updateHeaderGradient() {
        if (currentTheme.equals("light")) {

            headerLabel.setTextFill(new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop[]{
                    new Stop(0, Color.web("#232136")),
                    new Stop(1, Color.web("#45475a"))
                }
            ));
        } else {

            headerLabel.setTextFill(new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop[]{
                    new Stop(0, Color.web("#f3f4f6")),
                    new Stop(1, Color.web("#e5e7eb"))
                }
            ));
        }
    }

    private void createDiaryLists() {

        diaryEntries = DatabaseManager.getDiaryEntriesByUserId(DatabaseManager.getCurrentUserId());

        sharedEntries = new ArrayList<>();


        diaryListBox = new VBox(22);
        diaryListBox.setPadding(new Insets(0, 40, 40, 40));
        diaryListBox.setAlignment(Pos.TOP_CENTER);

        scrollPane = new ScrollPane(diaryListBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private Node createDiaryCard(DiaryEntry entry) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(18, 24, 18, 24));
        card.getStyleClass().add("diary-card");
        card.setMaxWidth(600);
        card.setMinWidth(350);
        card.setAlignment(Pos.TOP_LEFT);

        HBox top = new HBox();
        Label title = new Label(entry.getTitle());
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        title.setTextFill(Color.web("#000000")); // Black color

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label date = new Label(entry.getDate());
        date.setFont(Font.font("Arial", FontWeight.NORMAL, 13));

        date.setTextFill(Color.web("#333333")); // Dark grey color

        top.getChildren().addAll(title, spacer, date);

        VBox contentBox = new VBox(8);
        if (entry.getImagePath() != null && !entry.getImagePath().isEmpty()) {
            ImageView img = new ImageView(new Image(entry.getImagePath(), 150, 0, true, true));
            img.setSmooth(true);
            img.setPreserveRatio(true);
            img.setStyle("-fx-background-radius: 8;");
            contentBox.getChildren().add(img);
        }
        Label content = new Label(entry.getContent());
        content.setFont(Font.font("Arial", FontWeight.NORMAL, 15));

        content.setTextFill(Color.web("#333333")); // Dark grey color

        content.setWrapText(true);
        contentBox.getChildren().add(content);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = new Button("Sửa");
        editBtn.setStyle("-fx-background-color: #a5b4fc; -fx-text-fill: #232136; -fx-background-radius: 8;");
        editBtn.setOnMouseEntered(e -> editBtn.setStyle("-fx-background-color: #818cf8; -fx-text-fill: white; -fx-background-radius: 8;"));
        editBtn.setOnMouseExited(e -> editBtn.setStyle("-fx-background-color: #a5b4fc; -fx-text-fill: #232136; -fx-background-radius: 8;"));
        editBtn.setOnAction(e -> {
            System.out.println("Edit button clicked for entry: " + entry.getTitle());
            showEditDiaryDialog(entry);
        });

        Button deleteBtn = new Button("Xóa");
        deleteBtn.setStyle("-fx-background-color: #fca5a5; -fx-text-fill: #444; -fx-background-radius: 8;");
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: #f87171; -fx-text-fill: white; -fx-background-radius: 8;"));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: #fca5a5; -fx-text-fill: #444; -fx-background-radius: 8;"));
        deleteBtn.setOnAction(e -> {
            System.out.println("Delete button clicked for entry: " + entry.getTitle());

            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Xác nhận xóa");
            confirmDialog.setHeaderText("Bạn có chắc chắn muốn xóa nhật ký này?");
            confirmDialog.setContentText("Nhật ký: " + entry.getTitle());

            confirmDialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {

                    if (DatabaseManager.deleteDiaryEntry(entry.getId())) {

                        diaryListBox.getChildren().remove(card);
                        diaryEntries.remove(entry);
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Đã xóa nhật ký thành công!");
                        successAlert.showAndWait();
                    } else {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Lỗi khi xóa nhật ký khỏi cơ sở dữ liệu!");
                        errorAlert.showAndWait();
                    }
                }
            });
        });

        buttonBox.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(top, contentBox, buttonBox);


        card.setOnMouseClicked(event -> {

            Node target = (Node) event.getTarget();
            if (!(target instanceof Button) || (!((Button) target).getText().equals("Sửa") && !((Button) target).getText().equals("Xóa"))) {
                DiaryDetailView detailView = new DiaryDetailView(entry);
                detailView.show();
            }
        });


        card.setOnMouseEntered(e -> {
            card.setStyle(card.getStyle() + (currentTheme.equals("light") ? ";-fx-effect: dropshadow(gaussian, #a5b4fc88, 10, 0.2, 0, 2);" : ";-fx-effect: dropshadow(gaussian, #6d28d9cc, 12, 0.25, 0, 3);")); // Adjusted glow effect
        });
        card.setOnMouseExited(e -> {
            card.setStyle(currentTheme.equals("light")
                ? "-fx-background-color: #e5e7eb; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, #a5b4fc44, 7, 0.12, 0, 2);"
                : "-fx-background-color: radial-gradient(center 50% 50%, radius 100%, #45475a, #232136); -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, #6d28d988, 9, 0.18, 0, 2);");
        });

        return card;
    }

    private void createFloatingAddButton() {
        fabBtn = new Button("+");
        fabBtn.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        fabBtn.setTextFill(Color.WHITE);
        fabBtn.setStyle(currentTheme.equals("light")
            ? "-fx-background-color: #6d28d9; -fx-background-radius: 50%; -fx-min-width: 64px; -fx-min-height: 64px; -fx-max-width: 64px; -fx-max-height: 64px; -fx-effect: dropshadow(gaussian, #a5b4fc88, 12, 0.3, 0, 2);"
            : "-fx-background-color: #a5b4fc; -fx-background-radius: 50%; -fx-min-width: 64px; -fx-min-height: 64px; -fx-max-width: 64px; -fx-max-height: 64px; -fx-effect: dropshadow(gaussian, #6d28d988, 12, 0.3, 0, 2);");
        fabBtn.setOnAction(e -> showAddDiaryDialog());
        fabBtn.setOnMouseEntered(e -> {
        });
        fabBtn.setOnMouseExited(e -> {
        });
        setMargin(fabBtn, new Insets(0, 48, 48, 0));
        StackPane.setAlignment(fabBtn, Pos.BOTTOM_RIGHT);
        getChildren().add(fabBtn);
    }

    private void updateTab(String tab) {
        currentTab = tab;

        for (Button btn : menuButtons) {
            String buttonText = btn.getText();
            boolean isSelected = buttonText.equals(tab);


            String widthStyle;
            if (buttonText.equals("Nhật ký của tôi")) {
                widthStyle = "-fx-min-width: 200px; -fx-pref-width: 200px;";
            } else if (buttonText.equals("Nhật ký được chia sẻ")) {
                widthStyle = "-fx-min-width: 240px; -fx-pref-width: 240px;";
            } else if (buttonText.equals("Trang chủ")) {
                widthStyle = "-fx-min-width: 160px; -fx-pref-width: 160px;";
            } else if (buttonText.equals("Bạn bè")) {
                widthStyle = "-fx-min-width: 180px; -fx-pref-width: 180px;";
            } else if (buttonText.equals("Cài đặt")) {
                widthStyle = "-fx-min-width: 160px; -fx-pref-width: 160px;";
            } else {
                widthStyle = "-fx-min-width: 120px; -fx-pref-width: 120px;";
            }

            btn.setStyle(
                 "-fx-background-color: transparent; " +
                 "-fx-padding: 0 20 0 20; " +
                 "-fx-font-weight: bold; " +
                 "-fx-font-size: 17px;" +
                 widthStyle + ";" +
                 "-fx-min-height: 56px; -fx-pref-height: 56px;"
             );

            if (isSelected) {
                btn.setStyle(btn.getStyle() +
                             "-fx-background-color: #c7d2fe; " +
                             "-fx-text-fill: #6d28d9; " +
                             "-fx-border-color: radial-gradient(center 50% 50%, radius 100%, #232136, #000000);" +
                             "-fx-border-width: 0 0 2 0;"
                 );
                 btn.setOnMouseEntered(null);
                 btn.setOnMouseExited(null);
            } else {
                 btn.setStyle(btn.getStyle() +
                             "-fx-text-fill: " + (currentTheme.equals("light") ? "#232136;" : "#6d28d9;")
                 );
                 btn.setOnMouseEntered(e -> {
                      btn.setStyle(btn.getStyle() +
                                   "-fx-background-color: #c7d2fe; " +
                                   "-fx-text-fill: " + (currentTheme.equals("light") ? "#1f2937;" : "#312e81;")
                      );
                 });
                 btn.setOnMouseExited(e -> {
                      btn.setStyle(btn.getStyle() +
                                   "-fx-background-color: transparent; " +
                                   "-fx-text-fill: " + (currentTheme.equals("light") ? "#232136;" : "#6d28d9;")
                      );
                 });
            }
        }

        if (tab.equals("Trang chủ")) {
            headerLabel.setText("Trang chủ");
            updateHeaderGradient();
            createHomePage();
            fabBtn.setVisible(false);
        } else if (tab.equals("Nhật ký của tôi")) {
            headerLabel.setText("Nhật ký của tôi");
            updateHeaderGradient();
            diaryEntries = DatabaseManager.getDiaryEntriesByUserId(DatabaseManager.getCurrentUserId());
            updateDiaryList(diaryEntries);
            fabBtn.setVisible(true);
        } else if (tab.equals("Nhật ký được chia sẻ")) {
            headerLabel.setText("Nhật ký được chia sẻ");
            updateHeaderGradient();
            sharedEntries = DatabaseManager.getSharedDiaryEntries(DatabaseManager.getCurrentUserId()); // TODO: Server call
            updateDiaryList(sharedEntries);
            fabBtn.setVisible(false);
        } else if (tab.equals("Bạn bè")) {
            headerLabel.setText("Bạn bè");

            VBox friendsContentBox = new VBox(25);
            friendsContentBox.setPadding(new Insets(30, 40, 40, 40));
            friendsContentBox.setAlignment(Pos.TOP_LEFT);

            Label requestsLabel = new Label("Lời mời kết bạn");
            requestsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            requestsLabel.getStyleClass().add("section-title");
            requestsLabel.setPadding(new Insets(0, 0, 5, 0));
            requestsLabel.setTextFill(currentTheme.equals("light") ? Color.web("#1f2937") : Color.web("#f3f4f6"));

            List<FriendRequest> receivedRequests = DatabaseManager.getReceivedFriendRequests(DatabaseManager.getCurrentUserId()); // TODO: Server call

            VBox requestsList = new VBox(15);
            if (receivedRequests.isEmpty()) {
                Label noRequestsLabel = new Label("Không có lời mời kết bạn nào.");
                noRequestsLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
                noRequestsLabel.getStyleClass().add("info-label");
                noRequestsLabel.setTextFill(currentTheme.equals("light") ? Color.web("#4b5563") : Color.web("#a5b4fc"));
                requestsList.getChildren().add(noRequestsLabel);
                requestsLabel.setVisible(false);
            } else {
                for (FriendRequest request : receivedRequests) {
                    HBox requestCard = createFriendRequestCard(request);
                    requestsList.getChildren().add(requestCard);
                }
                 requestsLabel.setVisible(true);
            }

            Separator separator1 = new Separator();
            separator1.setOrientation(Orientation.HORIZONTAL);
            separator1.setStyle("-fx-background-color: #a5b4fc; -fx-pref-height: 1.5px;");

            Label friendsLabel = new Label("Danh sách bạn bè");
            friendsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            friendsLabel.getStyleClass().add("section-title");
            friendsLabel.setPadding(new Insets(0, 0, 5, 0));
            friendsLabel.setTextFill(currentTheme.equals("light") ? Color.web("#1f2937") : Color.web("#f3f4f6"));

            List<Friend> friends = DatabaseManager.getFriendsList(DatabaseManager.getCurrentUserId()); // TODO: Server call

            VBox friendsList = new VBox(15);
            if (friends.isEmpty()) {
                javafx.scene.control.Label noFriendsLabel = new javafx.scene.control.Label("Bạn chưa có bạn bè nào.");
                noFriendsLabel.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.NORMAL, 16));
                noFriendsLabel.getStyleClass().add("info-label");
                noFriendsLabel.setTextFill(currentTheme.equals("light") ? Color.web("#4b5563") : Color.web("#a5b4fc"));
                friendsList.getChildren().add(noFriendsLabel);
            } else {
                for (Friend friend : friends) {
                    HBox friendCard = createFriendCard(friend);
                    friendsList.getChildren().add(friendCard);
                }
            }

            Separator separator = new Separator();
            separator.setOrientation(Orientation.HORIZONTAL);
            separator.setStyle("-fx-background-color: #a5b4fc; -fx-pref-height: 1.5px;");

            Label otherUsersLabel = new Label("Tìm kiếm và thêm bạn"); // Changed label text
            otherUsersLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20)); // Slightly larger font
            otherUsersLabel.getStyleClass().add("section-title");
            otherUsersLabel.setPadding(new Insets(10, 0, 5, 0)); // Add padding below title
            otherUsersLabel.setTextFill(currentTheme.equals("light") ? Color.web("#1f2937") : Color.web("#f3f4f6")); // Darken title in light theme

            VBox searchAndPotentialBox = new VBox(15);
            searchAndPotentialBox.setAlignment(Pos.TOP_LEFT);
            VBox.setVgrow(searchAndPotentialBox, Priority.ALWAYS);

            HBox searchBox = new HBox(10);
            searchBox.setAlignment(Pos.CENTER_LEFT);
            TextField searchField = new TextField();
            searchField.setPromptText("Nhập tên người dùng để tìm kiếm...");
            searchField.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
            searchField.setPrefWidth(350);
            searchField.setPrefHeight(30);
            searchField.setStyle(
                "-fx-background-radius: 8;" +
                "-fx-border-radius: 8;" +
                "-fx-border-color: #a5b4fc;" +
                "-fx-border-width: 1.2;" +
                "-fx-background-color: #fff;"
            );
            searchBox.getChildren().add(searchField);
            HBox.setHgrow(searchField, Priority.ALWAYS);

            VBox potentialFriendsList = new VBox(12);
            VBox.setVgrow(potentialFriendsList, Priority.ALWAYS);

            Runnable updatePotentialFriendsList = () -> {
                String searchText = searchField.getText().trim();
                List<Friend> potentialFriends = DatabaseManager.getPotentialFriends(
                    DatabaseManager.getCurrentUserId(), // TODO: Server call
                    searchText
                );

                potentialFriendsList.getChildren().clear();
                if (potentialFriends.isEmpty()) {
                    javafx.scene.control.Label noResultsLabel = new javafx.scene.control.Label("Không tìm thấy người dùng nào phù hợp.");
                    noResultsLabel.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.NORMAL, 16));
                    noResultsLabel.getStyleClass().add("info-label");
                    potentialFriendsList.getChildren().add(noResultsLabel);
                } else {
                    for (Friend user : potentialFriends) {
                        HBox userCard = createPotentialFriendCard(user);
                        potentialFriendsList.getChildren().add(userCard);
                    }
                }
            };

            updatePotentialFriendsList.run();

            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                updatePotentialFriendsList.run();
            });

            searchAndPotentialBox.getChildren().addAll(searchBox, potentialFriendsList);

            friendsContentBox.getChildren().addAll(
                requestsLabel,
                requestsList,
                separator1,
                friendsLabel,
                friendsList,
                separator,
                otherUsersLabel,
                searchAndPotentialBox
            );

            diaryListBox.getChildren().clear();
            diaryListBox.getChildren().add(friendsContentBox);
            fabBtn.setVisible(false);
        } else if (tab.equals("Cài đặt")) {
            headerLabel.setText("Cài đặt");
            VBox settingsBox = new VBox(28);
            settingsBox.setPadding(new Insets(40, 0, 0, 0));
            settingsBox.setAlignment(Pos.TOP_CENTER);

            VBox card = new VBox(24);
            card.setPadding(new Insets(32, 40, 32, 40));
            card.setStyle("-fx-background-color: #e5e7eb; -fx-background-radius: 18; -fx-effect: dropshadow(gaussian, #a5b4fc88, 10, 0.2, 0, 2);");
            card.setMaxWidth(400);
            card.setAlignment(Pos.TOP_CENTER);

            VBox notificationSettings = new VBox(12);
            Label notificationTitle = new Label("Thông báo");
            notificationTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            notificationTitle.setTextFill(currentTheme.equals("light") ? Color.web("#1f2937") : Color.web("#f3f4f6"));
            
            CheckBox notifyCheck = new CheckBox("Bật thông báo");
            notifyCheck.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            notifyCheck.setTextFill(currentTheme.equals("light") ? Color.web("#232136") : Color.web("#312e81"));
            notifyCheck.setSelected(notificationsEnabled);
            notifyCheck.setOnAction(e -> {
                notificationsEnabled = notifyCheck.isSelected();
                bellIcon.setVisible(notificationsEnabled);
                settings.setNotificationsEnabled(notificationsEnabled);
                DatabaseManager.updateUserSettings(settings);
                if (notificationsEnabled) {
                    startNotificationCheck();
                } else {
                    if (notificationTimer != null) {
                        notificationTimer.cancel();
                        notificationTimer.purge();
                    }
                }
            });
            
            Label notificationDesc = new Label("Nhận thông báo về lời mời kết bạn và kỷ niệm");
            notificationDesc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
            notificationDesc.setTextFill(currentTheme.equals("light") ? Color.web("#4b5563") : Color.web("#d1d5db"));
            
            notificationSettings.getChildren().addAll(notificationTitle, notifyCheck, notificationDesc);

            VBox themeSettings = new VBox(12);
            Label themeTitle = new Label("Giao diện");
            themeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            themeTitle.setTextFill(currentTheme.equals("light") ? Color.web("#1f2937") : Color.web("#f3f4f6"));
            
            Button changeThemeBtn = new Button("Đổi giao diện");
            changeThemeBtn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            changeThemeBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 10;");
            changeThemeBtn.setOnMouseEntered(e -> changeThemeBtn.setStyle("-fx-background-color: #a5b4fc; -fx-text-fill: #312e81; -fx-background-radius: 10;"));
            changeThemeBtn.setOnMouseExited(e -> changeThemeBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 10;"));
            changeThemeBtn.setOnAction(e -> {
                if (currentTheme.equals("light")) {
                    currentTheme = "dark";
                    mainBox.setStyle("-fx-background-color: linear-gradient(to right, #232136, #393552, #45475a);");
                } else {
                    currentTheme = "light";
                    mainBox.setStyle("-fx-background-color: linear-gradient(to right, #e0e7ff, #c7d2fe, #a5b4fc);");
                }
                updateTab(currentTab);
            });
            
            Label themeDesc = new Label("Chuyển đổi giữa giao diện sáng và tối");
            themeDesc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
            themeDesc.setTextFill(currentTheme.equals("light") ? Color.web("#4b5563") : Color.web("#d1d5db"));
            
            themeSettings.getChildren().addAll(themeTitle, changeThemeBtn, themeDesc);

            VBox languageSettings = new VBox(12);
            Label languageTitle = new Label("Ngôn ngữ");
            languageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            languageTitle.setTextFill(currentTheme.equals("light") ? Color.web("#1f2937") : Color.web("#f3f4f6"));
            
            Button changeLangBtn = new Button("Đổi ngôn ngữ");
            changeLangBtn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            changeLangBtn.setStyle(currentTheme.equals("light") ? "-fx-background-color: #d1d5db; -fx-text-fill: #312e81; -fx-background-radius: 10;" : "-fx-background-color: #d1d5db; -fx-text-fill: #312e81; -fx-background-radius: 10;"); // Ensure dark text in light theme
            changeLangBtn.setOnMouseEntered(e -> changeLangBtn.setStyle(currentTheme.equals("light") ? "-fx-background-color: #c7d2fe; -fx-text-fill: #232136; -fx-background-radius: 10;" : "-fx-background-color: #c7d2fe; -fx-text-fill: #232136; -fx-background-radius: 10;")); // Ensure dark text in light theme
            changeLangBtn.setOnMouseExited(e -> changeLangBtn.setStyle(currentTheme.equals("light") ? "-fx-background-color: #d1d5db; -fx-text-fill: #312e81; -fx-background-radius: 10;" : "-fx-background-color: #d1d5db; -fx-text-fill: #312e81; -fx-background-radius: 10;")); // Ensure dark text in light theme
            
            Label languageDesc = new Label("Thay đổi ngôn ngữ hiển thị");
            languageDesc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
            languageDesc.setTextFill(currentTheme.equals("light") ? Color.web("#4b5563") : Color.web("#d1d5db"));
            
            languageSettings.getChildren().addAll(languageTitle, changeLangBtn, languageDesc);

            card.getChildren().addAll(notificationSettings, new Separator(), themeSettings, new Separator(), languageSettings);
            settingsBox.getChildren().add(card);

            diaryListBox.getChildren().clear();
            diaryListBox.getChildren().add(settingsBox);
            fabBtn.setVisible(false);
        }
        updateHeaderGradient();
    }

    private void updateDiaryList(List<DiaryEntry> list) {
        diaryListBox.getChildren().clear();
        for (DiaryEntry entry : list) {
            diaryListBox.getChildren().add(createDiaryCard(entry));
        }
    }

    private void showAddDiaryDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Thêm nhật ký mới");
        Font handwritingFont = Font.loadFont("file:D:/dacs1/res/IndieFlower.ttf", 20);
        if (handwritingFont == null) {
            handwritingFont = Font.font("Arial", FontWeight.NORMAL, 20);
        }
        VBox box = new VBox(22);
        box.setPadding(new Insets(36, 36, 32, 36));
        box.setAlignment(Pos.CENTER);
        box.setSpacing(18);
        box.setStyle(
            "-fx-background-radius: 22;" +
            "-fx-background-insets: 0;" +
            "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #ede9fe, #c7d2fe);" +
            "-fx-effect: dropshadow(gaussian, #a5b4fc88, 18, 0.3, 0, 4);"
        );
        Label titleLbl = new Label("Thêm nhật ký mới");
        titleLbl.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 26));
        titleLbl.setTextFill(Color.web("#6d28d9"));
        titleLbl.setStyle("-fx-effect: dropshadow(gaussian, #a5b4fc, 8, 0.2, 0, 2);");
        TextField titleField = new TextField();
        titleField.setPromptText("Tiêu đề");
        titleField.setFont(handwritingFont);
        titleField.setPrefWidth(340);
        titleField.setStyle(
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-border-color: #a5b4fc;" +
            "-fx-border-width: 1.2;" +
            "-fx-background-color: #fff;"
        );
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Nội dung");
        contentArea.setFont(handwritingFont);
        contentArea.setPrefWidth(340);
        contentArea.setPrefHeight(180);
        contentArea.setStyle(
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-border-color: #a5b4fc;" +
            "-fx-border-width: 1.2;" +
            "-fx-background-color: #fff;"
        );
        ImageView imagePreview = new ImageView();
        imagePreview.setFitWidth(160);
        imagePreview.setFitHeight(120);
        imagePreview.setPreserveRatio(true);
        imagePreview.setVisible(false);
        final String[] imagePath = {null};
        Button attachImgBtn = new Button("🖼️ Đính kèm ảnh");
        attachImgBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        attachImgBtn.setStyle(
            "-fx-background-color: #ede9fe;" +
            "-fx-background-radius: 10;" +
            "-fx-text-fill: #6d28d9;"
        );
        attachImgBtn.setOnMouseEntered(e -> attachImgBtn.setStyle("-fx-background-color: #c7d2fe; -fx-background-radius: 10; -fx-text-fill: #312e81;"));
        attachImgBtn.setOnMouseExited(e -> attachImgBtn.setStyle("-fx-background-color: #ede9fe; -fx-background-radius: 10; -fx-text-fill: #6d28d9;"));
        attachImgBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Chọn ảnh");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            java.io.File file = fileChooser.showOpenDialog(dialog);
            if (file != null) {
                imagePath[0] = file.toURI().toString();
                Image img = new Image(imagePath[0]);
                imagePreview.setImage(img);
                imagePreview.setVisible(true);
            }
        });
        Button cancelBtn = new Button("Hủy");
        cancelBtn.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        cancelBtn.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #6d28d9; -fx-background-radius: 10;");
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle("-fx-background-color: #c7d2fe; -fx-text-fill: #232136; -fx-background-radius: 10;"));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #6d28d9; -fx-background-radius: 10;"));
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button("Lưu");
        saveBtn.setDefaultButton(true);
        saveBtn.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        saveBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 10;");
        saveBtn.setOnMouseEntered(e -> saveBtn.setStyle("-fx-background-color: #a5b4fc; -fx-text-fill: #232136; -fx-background-radius: 10;"));
        saveBtn.setOnMouseExited(e -> saveBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 10;"));

        Label tagFriendsLabel = new Label("Tag bạn bè:");
        tagFriendsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        tagFriendsLabel.setTextFill(Color.web("#312e81"));

        TextField tagSearchField = new TextField();
        tagSearchField.setPromptText("Tìm bạn để tag...");
        tagSearchField.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        tagSearchField.setStyle(
            "-fx-background-radius: 8;" +
            "-fx-border-radius: 8;" +
            "-fx-border-color: #a5b4fc;" +
            "-fx-border-width: 1.2;" +
            "-fx-background-color: #fff;"
        );
        tagSearchField.setPrefWidth(300);

        VBox taggedFriendsBox = new VBox(8);
        taggedFriendsBox.setPadding(new Insets(0, 0, 0, 0));

        ScrollPane tagScrollPane = new ScrollPane(taggedFriendsBox);
        tagScrollPane.setFitToWidth(true);
        tagScrollPane.setPrefHeight(120); // Set preferred height to enable scrolling
        tagScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: #a5b4fc; -fx-border-radius: 8;"); // Added border for scroll pane
        tagScrollPane.setPadding(new Insets(0, 0, 12, 0)); // Add padding to the scroll pane instead

        List<Friend> allFriendsList = DatabaseManager.getFriendsList(DatabaseManager.getCurrentUserId()); // TODO: Server call
        List<CheckBox> friendCheckboxes = new ArrayList<>(); // Keep track of all checkboxes

        Runnable updateTaggedFriendsList = () -> {
            taggedFriendsBox.getChildren().clear();
            String searchText = tagSearchField.getText().toLowerCase();
            List<Friend> filteredFriends = new ArrayList<>();

            if (allFriendsList.isEmpty()) {
                Label noFriendsToTagLabel = new Label("Chưa có bạn bè để tag.");
                noFriendsToTagLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
                noFriendsToTagLabel.setTextFill(currentTheme.equals("light") ? Color.web("#4b5563") : Color.web("#6b7280"));
                taggedFriendsBox.getChildren().add(noFriendsToTagLabel);
            } else {
                for (Friend friend : allFriendsList) {

                    if (friend.getUsername().toLowerCase().contains(searchText)) {
                         filteredFriends.add(friend);
                    }
                }

                if (filteredFriends.isEmpty() && !searchText.isEmpty()) {
                     Label noMatchLabel = new Label("Không tìm thấy bạn bè phù hợp.");
                     noMatchLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
                     noMatchLabel.setTextFill(currentTheme.equals("light") ? Color.web("#4b5563") : Color.web("#6b7280"));
                     taggedFriendsBox.getChildren().add(noMatchLabel);
                } else if (filteredFriends.isEmpty() && searchText.isEmpty()) {
                     Label noFriendsLabel = new Label("Chưa có bạn bè để tag.");
                     noFriendsLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
                     noFriendsLabel.setTextFill(currentTheme.equals("light") ? Color.web("#4b5563") : Color.web("#6b7280"));
                     taggedFriendsBox.getChildren().add(noFriendsLabel);
                }

                for (Friend friend : filteredFriends) {
                    CheckBox friendCheckBox = new CheckBox(friend.getUsername());
                    friendCheckBox.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
                    friendCheckBox.setTextFill(currentTheme.equals("light") ? Color.web("#232136") : Color.web("#c7d2fe"));
                    friendCheckBox.setUserData(friend.getFriendId());

                    for (CheckBox cb : friendCheckboxes) {
                         if ((Integer) cb.getUserData() == friend.getFriendId()) {
                              friendCheckBox.setSelected(cb.isSelected());
                              break;
                         }
                    }

                     friendCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                         for (CheckBox cb : friendCheckboxes) {
                              if ((Integer) cb.getUserData() == (Integer) friendCheckBox.getUserData()) {
                                   cb.setSelected(newVal);
                                   break;
                              }
                         }
                     });
                    taggedFriendsBox.getChildren().add(friendCheckBox);
                }
            }
        };

        for (Friend friend : allFriendsList) {
            CheckBox friendCheckBox = new CheckBox(friend.getUsername());
            friendCheckBox.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
            friendCheckBox.setTextFill(Color.web("#444"));
            friendCheckBox.setUserData(friend.getFriendId());
            friendCheckboxes.add(friendCheckBox);
        }
         updateTaggedFriendsList.run();

        tagSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateTaggedFriendsList.run();
        });

        HBox btnBox = new HBox(14, saveBtn, cancelBtn);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Label reminderLabel = new Label("Nhắc lại kỷ niệm:");
        reminderLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        reminderLabel.setTextFill(Color.web("#312e81"));
        
        HBox reminderBox = new HBox(12);
        reminderBox.setAlignment(Pos.CENTER_LEFT);
        
        Spinner<Integer> reminderSpinner = new Spinner<>(0, 100, 0);
        reminderSpinner.setEditable(true);
        reminderSpinner.setPrefWidth(80);
        reminderSpinner.setStyle(
            "-fx-background-radius: 8;" +
            "-fx-border-radius: 8;" +
            "-fx-border-color: #a5b4fc;" +
            "-fx-border-width: 1.2;" +
            "-fx-background-color: #fff;"
        );
        
        Label reminderDesc = new Label("năm một lần (0 = không nhắc)");
        reminderDesc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        reminderDesc.setTextFill(currentTheme.equals("light") ? Color.web("#4b5563") : Color.web("#6b7280")); // Darken text in light theme
        
        reminderBox.getChildren().addAll(reminderSpinner, reminderDesc);

        box.getChildren().addAll(
            titleLbl,
            titleField,
            contentArea,
            attachImgBtn,
            imagePreview,
            tagFriendsLabel,
            tagSearchField,
            tagScrollPane,
            reminderLabel,
            reminderBox,
            btnBox
        );

        saveBtn.setOnAction(e -> {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();
            String currentImagePath = imagePath[0];
            int reminderDays = reminderSpinner.getValue();

            if (title.isEmpty() || content.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Cảnh báo");
                alert.setHeaderText(null);
                alert.setContentText("Vui lòng nhập đầy đủ tiêu đề và nội dung!");
                alert.showAndWait();
                return;
            }

            try {
                String currentDate = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
                
                List<Integer> taggedUserIds = new ArrayList<>();

                for (CheckBox cb : friendCheckboxes) {
                    if (cb.isSelected()) {
                        try {
                            Object userData = cb.getUserData();
                            if (userData instanceof Integer) {
                                taggedUserIds.add((Integer) userData);
                            }
                        } catch (Exception ex) {
                            System.err.println("Error processing friend checkbox: " + ex.getMessage());
                        }
                    }
                }
                
                boolean success = DatabaseManager.addDiaryEntry(
                    DatabaseManager.getCurrentUserId(), // TODO: Server call
                    title,
                    content,
                    currentDate,
                    currentImagePath,
                    taggedUserIds,
                    reminderDays
                );

                if (success) {
                    updateTab(currentTab);
                    dialog.close();
                    
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Thành công");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Đã thêm nhật ký thành công!");
                    successAlert.showAndWait();
                } else {
                    throw new Exception("DatabaseManager.addDiaryEntry returned false");
                }
            } catch (Exception ex) {
                System.err.println("Error adding diary entry: " + ex.getMessage());
                ex.printStackTrace();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Lỗi");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Lỗi khi thêm nhật ký: " + ex.getMessage());
                errorAlert.showAndWait();
            }
        });

        dialog.setScene(new Scene(box));
        javafx.application.Platform.runLater(() -> dialog.centerOnScreen());
        dialog.showAndWait();
    }

    private void showEditDiaryDialog(DiaryEntry entry) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Chỉnh sửa nhật ký");

        Font handwritingFont = Font.loadFont("file:D:/dacs1/res/IndieFlower.ttf", 20);
        if (handwritingFont == null) {
            handwritingFont = Font.font("Arial", FontWeight.NORMAL, 20);
        }

        VBox box = new VBox(22);
        box.setPadding(new Insets(36, 36, 32, 36));
        box.setAlignment(Pos.CENTER);
        box.setSpacing(18);
        box.setStyle(
            "-fx-background-radius: 22;" +
            "-fx-background-insets: 0;" +
            "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #ede9fe, #c7d2fe);" +
            "-fx-effect: dropshadow(gaussian, #a5b4fc88, 18, 0.3, 0, 4);"
        );

        Label titleLbl = new Label("Chỉnh sửa nhật ký");
        titleLbl.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 26));
        titleLbl.setTextFill(Color.web("#6d28d9"));
        titleLbl.setStyle("-fx-effect: dropshadow(gaussian, #a5b4fc, 8, 0.2, 0, 2);");

        TextField titleField = new TextField(entry.getTitle());
        titleField.setPromptText("Tiêu đề");
        titleField.setFont(handwritingFont);
        titleField.setPrefWidth(340);
        titleField.setStyle(
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-border-color: #a5b4fc;" +
            "-fx-border-width: 1.2;" +
            "-fx-background-color: #fff;"
        );

        TextArea contentArea = new TextArea(entry.getContent());
        contentArea.setPromptText("Nội dung");
        contentArea.setFont(handwritingFont);
        contentArea.setPrefWidth(340);
        contentArea.setPrefHeight(180);
        contentArea.setStyle(
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-border-color: #a5b4fc;" +
            "-fx-border-width: 1.2;" +
            "-fx-background-color: #fff;"
        );

        ImageView imagePreview = new ImageView();
        imagePreview.setFitWidth(160);
        imagePreview.setFitHeight(120);
        imagePreview.setPreserveRatio(true);
        final String[] imagePath = {entry.getImagePath()};
        if (imagePath[0] != null && !imagePath[0].isEmpty()) {
             Image img = new Image(imagePath[0]);
             imagePreview.setImage(img);
             imagePreview.setVisible(true);
        } else {
            imagePreview.setVisible(false);
        }

        Button attachImgBtn = new Button("🖼️ Đính kèm ảnh");
        attachImgBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        attachImgBtn.setStyle(
            "-fx-background-color: #ede9fe;" +
            "-fx-background-radius: 10;" +
            "-fx-text-fill: #6d28d9;"
        );
        attachImgBtn.setOnMouseEntered(e -> attachImgBtn.setStyle("-fx-background-color: #c7d2fe; -fx-background-radius: 10; -fx-text-fill: #312e81;"));
        attachImgBtn.setOnMouseExited(e -> attachImgBtn.setStyle("-fx-background-color: #ede9fe; -fx-background-radius: 10; -fx-text-fill: #6d28d9;"));
        attachImgBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Chọn ảnh");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            java.io.File file = fileChooser.showOpenDialog(dialog);
            if (file != null) {
                imagePath[0] = file.toURI().toString();
                Image img = new Image(imagePath[0]);
                imagePreview.setImage(img);
                imagePreview.setVisible(true);
            }
        });

        Button saveBtn = new Button("Lưu");
        saveBtn.setDefaultButton(true);
        saveBtn.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        saveBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 10;");
        saveBtn.setOnMouseEntered(e -> saveBtn.setStyle("-fx-background-color: #a5b4fc; -fx-text-fill: #232136; -fx-background-radius: 10;"));
        saveBtn.setOnMouseExited(e -> saveBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 10;"));

        Button cancelBtn = new Button("Hủy");
        cancelBtn.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        cancelBtn.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #6d28d9; -fx-background-radius: 10;");
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle("-fx-background-color: #c7d2fe; -fx-text-fill: #232136; -fx-background-radius: 10;"));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #6d28d9; -fx-background-radius: 10;"));
        cancelBtn.setOnAction(e -> dialog.close());

        Label tagFriendsLabel = new Label("Tag bạn bè:");
        tagFriendsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        tagFriendsLabel.setTextFill(Color.web("#312e81"));

        TextField tagSearchField = new TextField();
        tagSearchField.setPromptText("Tìm bạn để tag...");
        tagSearchField.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        tagSearchField.setStyle(
            "-fx-background-radius: 8;" +
            "-fx-border-radius: 8;" +
            "-fx-border-color: #a5b4fc;" +
            "-fx-border-width: 1.2;" +
            "-fx-background-color: #fff;"
        );
        tagSearchField.setPrefWidth(300);

        VBox taggedFriendsBox = new VBox(8);
        taggedFriendsBox.setPadding(new Insets(0, 0, 0, 0));

        ScrollPane tagScrollPane = new ScrollPane(taggedFriendsBox);
        tagScrollPane.setFitToWidth(true);
        tagScrollPane.setPrefHeight(120);
        tagScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: #a5b4fc; -fx-border-radius: 8;");
        tagScrollPane.setPadding(new Insets(0, 0, 12, 0));

        List<Friend> allFriendsList = DatabaseManager.getFriendsList(DatabaseManager.getCurrentUserId()); // TODO: Server call
        List<Integer> currentlyTaggedUserIds = entry.getTaggedUserIds();
        List<CheckBox> friendCheckboxes = new ArrayList<>();

        Runnable updateTaggedFriendsList = () -> {
            taggedFriendsBox.getChildren().clear();
            String searchText = tagSearchField.getText().toLowerCase();
            List<Friend> filteredFriends = new ArrayList<>();

            if (allFriendsList.isEmpty()) {
                Label noFriendsToTagLabel = new Label("Chưa có bạn bè để tag.");
                noFriendsToTagLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
                noFriendsToTagLabel.setTextFill(currentTheme.equals("light") ? Color.web("#4b5563") : Color.web("#6b7280"));
                taggedFriendsBox.getChildren().add(noFriendsToTagLabel);
            } else {
                for (Friend friend : allFriendsList) {
                    // Filter by username
                    if (friend.getUsername().toLowerCase().contains(searchText)) {
                        filteredFriends.add(friend);
                    }
                }

                 if (filteredFriends.isEmpty() && !searchText.isEmpty()) {
                     Label noMatchLabel = new Label("Không tìm thấy bạn bè phù hợp.");
                     noMatchLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
                     noMatchLabel.setTextFill(currentTheme.equals("light") ? Color.web("#4b5563") : Color.web("#6b7280"));
                     taggedFriendsBox.getChildren().add(noMatchLabel);
                } else if (filteredFriends.isEmpty() && searchText.isEmpty()) {
                     Label noFriendsLabel = new Label("Chưa có bạn bè để tag.");
                     noFriendsLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
                     noFriendsLabel.setTextFill(currentTheme.equals("light") ? Color.web("#4b5563") : Color.web("#6b7280"));
                     taggedFriendsBox.getChildren().add(noFriendsLabel);
                }

                for (Friend friend : filteredFriends) {
                    CheckBox friendCheckBox = new CheckBox(friend.getUsername());
                    friendCheckBox.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
                    friendCheckBox.setTextFill(currentTheme.equals("light") ? Color.web("#232136") : Color.web("#c7d2fe"));
                    friendCheckBox.setUserData(friend.getFriendId());

                    if (currentlyTaggedUserIds != null && currentlyTaggedUserIds.contains(friend.getFriendId())) {
                         friendCheckBox.setSelected(true);
                    }

                    for (CheckBox cb : friendCheckboxes) {
                         if ((Integer) cb.getUserData() == friend.getFriendId()) {
                              friendCheckBox.setSelected(cb.isSelected());
                              break;
                         }
                    }

                     friendCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                         for (CheckBox cb : friendCheckboxes) {
                              if ((Integer) cb.getUserData() == (Integer) friendCheckBox.getUserData()) {
                                   cb.setSelected(newVal);
                                   break;
                              }
                         }
                     });
                    taggedFriendsBox.getChildren().add(friendCheckBox);
                }
            }
        };

        for (Friend friend : allFriendsList) {
            CheckBox friendCheckBox = new CheckBox(friend.getUsername());
            friendCheckBox.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
            friendCheckBox.setTextFill(Color.web("#444"));
            friendCheckBox.setUserData(friend.getFriendId());

            if (currentlyTaggedUserIds != null && currentlyTaggedUserIds.contains(friend.getFriendId())) {
                 friendCheckBox.setSelected(true);
            }
            friendCheckboxes.add(friendCheckBox);
        }
        updateTaggedFriendsList.run();

        tagSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateTaggedFriendsList.run();
        });

        HBox btnBox = new HBox(14, saveBtn, cancelBtn);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        box.getChildren().addAll(
            titleLbl,
            titleField,
            contentArea,
            attachImgBtn,
            imagePreview,
            tagFriendsLabel,
            tagSearchField,
            tagScrollPane,
            btnBox
        );

        Scene scene = new Scene(box);

        box.setOpacity(1);
        dialog.setScene(scene);
        javafx.application.Platform.runLater(() -> dialog.centerOnScreen());
        dialog.showAndWait();
    }

    private HBox createFriendCard(Friend friend) {
        HBox friendCard = new HBox(12);
        friendCard.setPadding(new Insets(14, 24, 14, 24));
        friendCard.setStyle("-fx-background-color: #e0e7ff; -fx-background-radius: 14; -fx-effect: dropshadow(gaussian, #a5b4fc44, 4, 0.1, 0, 1);");
        friendCard.setAlignment(Pos.CENTER_LEFT);

        Circle avatar = new Circle(18, Color.web("#6d28d9"));
        Label initial = new Label(friend.getUsername().substring(0, 1).toUpperCase());
        initial.setTextFill(Color.WHITE);
        initial.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        StackPane avatarPane = new StackPane(avatar, initial);

        VBox infoBox = new VBox(4);
        Label nameLabel = new Label(friend.getUsername());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        nameLabel.setTextFill(currentTheme.equals("light") ? Color.web("#000000") : Color.web("#c7d2fe"));

        Label emailLabel = new Label(friend.getEmail());
        emailLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        emailLabel.setTextFill(currentTheme.equals("light") ? Color.web("#333333") : Color.web("#a5b4fc"));

        infoBox.getChildren().addAll(nameLabel, emailLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button removeBtn = new Button("Xóa");
        removeBtn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        removeBtn.setStyle(
            "-fx-background-color: #fca5a5;" +
            "-fx-text-fill: #444;" +
            "-fx-background-radius: 6;" +
            "-fx-min-width: 80px;" +
            "-fx-padding: 8 16;"
        );
        removeBtn.setOnMouseEntered(e -> removeBtn.setStyle(
            "-fx-background-color: #f87171;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;" +
            "-fx-min-width: 80px;" +
            "-fx-padding: 8 16;"
        ));
        removeBtn.setOnMouseExited(e -> removeBtn.setStyle(
            "-fx-background-color: #fca5a5;" +
            "-fx-text-fill: #444;" +
            "-fx-background-radius: 6;" +
            "-fx-min-width: 80px;" +
            "-fx-padding: 8 16;"
        ));
        removeBtn.setOnAction(e -> {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Xác nhận xóa");
            confirmDialog.setHeaderText("Bạn có chắc chắn muốn xóa " + friend.getUsername() + " khỏi danh sách bạn bè?");
            confirmDialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    if (DatabaseManager.removeFriend(DatabaseManager.getCurrentUserId(), friend.getFriendId())) {
                        diaryListBox.getChildren().remove(friendCard);
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Đã xóa bạn thành công!");
                        successAlert.showAndWait();
                    } else {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Lỗi khi xóa bạn!");
                        errorAlert.showAndWait();
                    }
                }
            });
        });

        friendCard.getChildren().addAll(avatarPane, infoBox, spacer, removeBtn);
        return friendCard;
    }

    private void showAddFriendDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Thêm bạn mới");

        VBox box = new VBox(22);
        box.setPadding(new Insets(36, 36, 32, 36));
        box.setAlignment(Pos.CENTER);
        box.setSpacing(18);
        box.setStyle(
            "-fx-background-radius: 22;" +
            "-fx-background-insets: 0;" +
            "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #ede9fe, #c7d2fe);" +
            "-fx-effect: dropshadow(gaussian, #a5b4fc88, 18, 0.3, 0, 4);"
        );

        Label titleLbl = new Label("Thêm bạn mới");
        titleLbl.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 26));
        titleLbl.setTextFill(Color.web("#6d28d9"));
        titleLbl.setStyle("-fx-effect: dropshadow(gaussian, #a5b4fc, 8, 0.2, 0, 2);");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Tên đăng nhập của bạn");
        usernameField.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        usernameField.setPrefWidth(340);
        usernameField.setStyle(
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-border-color: #a5b4fc;" +
            "-fx-border-width: 1.2;" +
            "-fx-background-color: #fff;"
        );

        Button addBtn = new Button("Thêm");
        addBtn.setDefaultButton(true);
        addBtn.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        addBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 10;");
        addBtn.setOnMouseEntered(e -> addBtn.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-background-radius: 10;"));
        addBtn.setOnMouseExited(e -> addBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 10;"));
        addBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Vui lòng nhập tên đăng nhập!");
                alert.showAndWait();
            } else {
                if (DatabaseManager.addFriend(DatabaseManager.getCurrentUserId(), username)) {
                    updateTab("Bạn bè");
                    dialog.close();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Không tìm thấy người dùng hoặc đã là bạn!");
                    alert.showAndWait();
                }
            }
        });

        Button cancelBtn = new Button("Hủy");
        cancelBtn.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        cancelBtn.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #6d28d9; -fx-background-radius: 10;");
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle("-fx-background-color: #c7d2fe; -fx-text-fill: #232136; -fx-background-radius: 10;"));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #6d28d9; -fx-background-radius: 10;"));
        cancelBtn.setOnAction(e -> dialog.close());

        HBox btnBox = new HBox(14, addBtn, cancelBtn);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        box.getChildren().addAll(titleLbl, usernameField, btnBox);

        Scene scene = new Scene(box);
        box.setOpacity(1);
        dialog.setScene(scene);
        javafx.application.Platform.runLater(() -> dialog.centerOnScreen());
        dialog.showAndWait();
    }

    private HBox createPotentialFriendCard(Friend friend) {
        HBox friendCard = new HBox(12);
        friendCard.setPadding(new Insets(14, 24, 14, 24));
        friendCard.setPrefHeight(60); // Explicitly set preferred height
        friendCard.setStyle(currentTheme.equals("light") ? "-fx-background-color: #f3f4f6; -fx-background-radius: 14; -fx-effect: dropshadow(gaussian, #a5b4fc44, 4, 0.1, 0, 1);" // Darken background slightly
                                                          : "-fx-background-color: radial-gradient(center 50% 50%, radius 100%, #45475a, #232136); -fx-background-radius: 14; -fx-effect: dropshadow(gaussian, #6d28d988, 4, 0.1, 0, 1);"); // Adjusted dark theme effect
        friendCard.setAlignment(Pos.CENTER_LEFT);

        Circle avatar = new Circle(18, Color.web("#6d28d9"));
        Label initial = new Label(friend.getUsername().substring(0, 1).toUpperCase());
        initial.setTextFill(Color.WHITE);
        initial.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        StackPane avatarPane = new StackPane(avatar, initial);

        VBox infoBox = new VBox(4);
        Label nameLabel = new Label(friend.getUsername());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        nameLabel.setTextFill(currentTheme.equals("light") ? Color.web("#000000") : Color.web("#c7d2fe")); // Ensure black text in light theme

        Label emailLabel = new Label(friend.getEmail());
        emailLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        emailLabel.setTextFill(currentTheme.equals("light") ? Color.web("#333333") : Color.web("#a5b4fc")); // Use dark grey for email in light theme

        infoBox.getChildren().addAll(nameLabel, emailLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("Kết bạn");
        addBtn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        addBtn.setStyle(
            "-fx-background-color: #6d28d9;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;"
        );
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(
            "-fx-background-color: #7c3aed;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;"
        ));
        addBtn.setOnMouseExited(e -> addBtn.setStyle(
            "-fx-background-color: #6d28d9;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;"
        ));
        addBtn.setOnAction(e -> {
            if (DatabaseManager.addFriend(DatabaseManager.getCurrentUserId(), friend.getUsername())) {

                ((VBox) friendCard.getParent()).getChildren().remove(friendCard);

                updateTab("Bạn bè");
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Đã thêm bạn thành công!");
                successAlert.showAndWait();
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Lỗi khi thêm bạn!");
                errorAlert.showAndWait();
            }
        });

        friendCard.getChildren().addAll(avatarPane, infoBox, spacer, addBtn);
        return friendCard;
    }

    private HBox createFriendRequestCard(FriendRequest request) {
        HBox requestCard = new HBox(12);
        requestCard.setPadding(new Insets(14, 24, 14, 24));
        requestCard.setPrefHeight(60);
        requestCard.setStyle(currentTheme.equals("light") ? "-fx-background-color: #e0e7ff; -fx-background-radius: 14; -fx-effect: dropshadow(gaussian, #a5b4fc44, 4, 0.1, 0, 1);" // Darken background slightly
                                                           : "-fx-background-color: radial-gradient(center 50% 50%, radius 100%, #45475a, #232136); -fx-background-radius: 14; -fx-effect: dropshadow(gaussian, #6d28d988, 4, 0.1, 0, 1);"); // Adjusted dark theme effect
        requestCard.setAlignment(Pos.CENTER_LEFT);

        Circle avatar = new Circle(18, Color.web("#6d28d9"));
        Label initial = new Label(request.getSenderUsername().substring(0, 1).toUpperCase());
        initial.setTextFill(Color.WHITE);
        initial.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        StackPane avatarPane = new StackPane(avatar, initial);

        VBox infoBox = new VBox(4);
        Label nameLabel = new Label(request.getSenderUsername());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        nameLabel.setTextFill(currentTheme.equals("light") ? Color.web("#000000") : Color.web("#c7d2fe"));

        Label emailLabel = new Label(request.getSenderEmail());
        emailLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        emailLabel.setTextFill(currentTheme.equals("light") ? Color.web("#333333") : Color.web("#a5b4fc"));

        infoBox.getChildren().addAll(nameLabel, emailLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button acceptBtn = new Button("Chấp nhận");
        acceptBtn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        acceptBtn.setStyle(
            "-fx-background-color: #84cc16;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;"
        );
        acceptBtn.setOnMouseEntered(e -> acceptBtn.setStyle(
            "-fx-background-color: #a3e635;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;"
        ));
        acceptBtn.setOnMouseExited(e -> acceptBtn.setStyle(
            "-fx-background-color: #84cc16;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;"
        ));
        acceptBtn.setOnAction(e -> {
            if (DatabaseManager.acceptFriendRequest(request.getId())) { // TODO: Server call

                updateTab("Bạn bè");
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Đã chấp nhận lời mời kết bạn!");
                successAlert.showAndWait();
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Lỗi khi chấp nhận lời mời kết bạn!");
                errorAlert.showAndWait();
            }
        });

        Button rejectBtn = new Button("Từ chối");
        rejectBtn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        rejectBtn.setStyle(
            "-fx-background-color: #ef4444;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;"
        );
        rejectBtn.setOnMouseEntered(e -> rejectBtn.setStyle(
            "-fx-background-color: #f87171;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;"
        ));
        rejectBtn.setOnMouseExited(e -> rejectBtn.setStyle(
            "-fx-background-color: #ef4444;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;"
        ));
        rejectBtn.setOnAction(e -> {
             if (DatabaseManager.rejectFriendRequest(request.getId())) { // TODO: Server call

                updateTab("Bạn bè");
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Đã từ chối lời mời kết bạn!");
                successAlert.showAndWait();
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Lỗi khi từ chối lời mời kết bạn!");
                errorAlert.showAndWait();
            }
        });

        HBox buttonBox = new HBox(8, acceptBtn, rejectBtn);

        requestCard.getChildren().addAll(avatarPane, infoBox, spacer, buttonBox);
        return requestCard;
    }

    private void updateNotificationBox() {
        notificationBox.getChildren().clear();
        
        if (notifications.isEmpty()) {
            Label noNotifications = new Label("Không có thông báo mới");
            noNotifications.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
            noNotifications.setTextFill(currentTheme.equals("light") ? Color.web("#6b7280") : Color.web("#d1d5db"));
            notificationBox.getChildren().add(noNotifications);
            return;
        }
        
        for (Notification notification : notifications) {

            if (notification != null && notification.type != null) {
                HBox notificationItem = new HBox(8);
                notificationItem.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 6; -fx-padding: 8;");
                
                ImageView icon = new ImageView(new Image(
                    notification.type.equals("friend_request") ? 
                    "file:D:/dacs1/res/friend_request.png" : 
                    "file:D:/dacs1/res/memory.png"
                ));
                icon.setFitWidth(20);
                icon.setFitHeight(20);
                
                VBox content = new VBox(4);
                Label message = new Label(notification.message != null ? notification.message : "");
                message.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
                message.setTextFill(currentTheme.equals("light") ? Color.web("#1f2937") : Color.web("#f3f4f6"));
                
                Label time = new Label(notification.timestamp != null ? formatNotificationTime(notification.timestamp) : "");
                time.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
                time.setTextFill(currentTheme.equals("light") ? Color.web("#6b7280") : Color.web("#d1d5db"));
                
                content.getChildren().addAll(message, time);
                
                notificationItem.getChildren().addAll(icon, content);
                notificationBox.getChildren().add(notificationItem);
            } else {

                System.err.println("Skipping null or incomplete notification in updateNotificationBox. Notification: " + notification);
            }
        }
    }
    
    private String formatNotificationTime(LocalDateTime time) {
        java.time.Duration duration = java.time.Duration.between(time, LocalDateTime.now());
        if (duration.toMinutes() < 1) {
            return "Vừa xong";
        } else if (duration.toHours() < 1) {
            return duration.toMinutes() + " phút trước";
        } else if (duration.toDays() < 1) {
            return duration.toHours() + " giờ trước";
        } else {
            return duration.toDays() + " ngày trước";
        }
    }
    
    private void addNotification(String message, String type, int relatedId) {
        if (notificationsEnabled) {
            // TODO: Update DatabaseManager.addNotification to save relatedId

             if (notifications.stream().noneMatch(n -> 
                 n.type != null && 
                 n.type.equals(type) && 
                 n.relatedId == relatedId)) {
                
                notifications.add(0, new Notification(message, type, relatedId)); // Use new constructor
                if (notifications.size() > 10) {
                    notifications.remove(notifications.size() - 1);
                }
                updateNotificationBox();

            }
        }
    }

    private void loadExistingNotifications() {
        List<Notification> dbNotifications = DatabaseManager.getUnreadNotifications(DatabaseManager.getCurrentUserId());
        notifications.clear();
        for (Notification dbNotif : dbNotifications) {

            if (dbNotif != null && dbNotif.message != null && dbNotif.type != null && dbNotif.timestamp != null) {
                 notifications.add(0, new Notification(dbNotif.message, dbNotif.type, dbNotif.relatedId));
            } else {
                System.err.println("Skipping incomplete notification loaded from database: " + dbNotif);
            }
        }

        updateNotificationBox();
    }

    private void startNotificationCheck() {

        if (notificationTimer != null) {
            notificationTimer.cancel();
            notificationTimer.purge();
        }

        notificationTimer = new Timer(true); // Use a daemon thread
        notificationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (notificationsEnabled) {

                    List<FriendRequest> newRequests = DatabaseManager.getReceivedFriendRequests(DatabaseManager.getCurrentUserId()); // TODO: Server call
                    for (FriendRequest request : newRequests) {

                        if (!isNotificationAlreadyAdded("friend_request", request.getId())) {

                            javafx.application.Platform.runLater(() -> {
                                addNotification(
                                    request.getSenderUsername() + " đã gửi lời mời kết bạn",
                                    "friend_request",
                                    request.getId()
                                );
                            });
                        }
                    }

                    List<DiaryEntry> entries = DatabaseManager.getDiaryEntriesByUserId(DatabaseManager.getCurrentUserId()); // TODO: Server call
                    LocalDate today = LocalDate.now();
                    for (DiaryEntry entry : entries) {
                        if (entry.getReminderDays() > 0) {
                            try {
                                LocalDate entryDate = LocalDate.parse(entry.getDate());
                                if (entryDate.getMonth() == today.getMonth() && 
                                    entryDate.getDayOfMonth() == today.getDayOfMonth() &&
                                    entryDate.getYear() != today.getYear()) {
                                    int years = today.getYear() - entryDate.getYear();

                                    if (years > 0 && years % entry.getReminderDays() == 0) {


                                         if (!isNotificationAlreadyAdded("memory_reminder", entry.getId())) {

                                            javafx.application.Platform.runLater(() -> {
                                                addNotification(
                                                    "Kỷ niệm " + years + " năm: " + entry.getTitle(),
                                                    "memory_reminder",
                                                    entry.getId()
                                                );
                                            });
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("Error parsing entry date or checking memory: " + e.getMessage());
                                // Handle potential parsing errors
                            }
                        }
                    }
                }
            }
        }, 0, 30 * 1000); // Delay 0ms, repeat every 30 seconds (30 * 1000 ms)
    }

    private boolean isNotificationAlreadyAdded(String type, int relatedId) {
        for (Notification n : notifications) {

            if (n != null && n.type != null && n.type.equals(type) && n.relatedId == relatedId) {
                return true;
            }
        }
        return false;
    }

    private void createHomePage() {
        HBox homeContent = new HBox(24);
        homeContent.setPadding(new Insets(24, 40, 40, 40));
        homeContent.setAlignment(Pos.TOP_LEFT);

        VBox friendsSection = new VBox(16);
        friendsSection.setPrefWidth(400);
        friendsSection.setStyle(currentTheme.equals("light")
            ? "-fx-background-color: #e0e7ff; -fx-background-radius: 16; -fx-padding: 24; -fx-effect: dropshadow(gaussian, #a5b4fc44, 7, 0.12, 0, 2);"
            : "-fx-background-color: radial-gradient(center 50% 50%, radius 100%, #45475a, #232136); -fx-background-radius: 16; -fx-padding: 24; -fx-effect: dropshadow(gaussian, #6d28d988, 9, 0.18, 0, 2);");

        Label friendsTitle = new Label("Bạn bè gần đây");
        friendsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        friendsTitle.getStyleClass().add("section-title");
        friendsTitle.setTextFill(currentTheme.equals("light") ? Color.web("#000000") : Color.web("#c7d2fe"));

        VBox friendsList = new VBox(12);
        List<Friend> recentFriends = DatabaseManager.getFriendsList(DatabaseManager.getCurrentUserId());
        if (recentFriends.isEmpty()) {
            Label noFriends = new Label("Chưa có bạn bè nào");
            noFriends.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
            noFriends.setTextFill(currentTheme.equals("light") ? Color.web("#232136") : Color.web("#6d28d9"));
            friendsList.getChildren().add(noFriends);
        } else {
            for (Friend friend : recentFriends) {
                HBox friendItem = createFriendCard(friend);
                friendsList.getChildren().add(friendItem);
            }
        }

        ScrollPane friendsScroll = new ScrollPane(friendsList);
        friendsScroll.setFitToWidth(true);
        friendsScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        friendsScroll.setPrefHeight(500);

        friendsSection.getChildren().addAll(friendsTitle, friendsScroll);

        VBox profileSection = new VBox(24);
        profileSection.setPrefWidth(800); // Increased width from 600 to 800
         profileSection.setStyle(currentTheme.equals("light")
            ? "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e0e7ff, #c7d2fe); -fx-background-radius: 16; -fx-padding: 24; -fx-effect: dropshadow(gaussian, #a5b4fc44, 7, 0.12, 0, 2);" // Changed background to linear gradient
            : "-fx-background-color: radial-gradient(center 50% 50%, radius 100%, #45475a, #232136); -fx-background-radius: 16; -fx-padding: 24; -fx-effect: dropshadow(gaussian, #6d28d988, 9, 0.18, 0, 2);");


        Label profileTitle = new Label("Thông tin cá nhân");
        profileTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        profileTitle.getStyleClass().add("section-title");
        profileTitle.setTextFill(currentTheme.equals("light") ? Color.web("#000000") : Color.web("#c7d2fe"));

        UserProfile profile = DatabaseManager.getUserProfile(DatabaseManager.getCurrentUserId());

        VBox profileInfo = new VBox(16);
        profileInfo.setStyle(currentTheme.equals("light")
            ? "-fx-background-color: transparent; -fx-background-radius: 12; -fx-padding: 20;"
            : "-fx-background-color: #393552; -fx-background-radius: 12; -fx-padding: 20;");

        addProfileField(profileInfo, "Họ và tên", profile.getFullName());
        addProfileField(profileInfo, "Tên người dùng", profile.getUsername());
        addProfileField(profileInfo, "Email", profile.getEmail());
        addProfileField(profileInfo, "Số điện thoại", profile.getPhone());
        addProfileField(profileInfo, "Địa chỉ", profile.getAddress());

        Button editProfileBtn = new Button("Chỉnh sửa thông tin");
        editProfileBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
         editProfileBtn.setStyle(currentTheme.equals("light")
            ? "-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 8;"
            : "-fx-background-color: #a5b4fc; -fx-text-fill: #232136; -fx-background-radius: 8;");

         editProfileBtn.setOnMouseEntered(e -> editProfileBtn.setStyle(currentTheme.equals("light")
            ? "-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-background-radius: 8;"
            : "-fx-background-color: #c7d2fe; -fx-text-fill: #312e81; -fx-background-radius: 8;"));

         editProfileBtn.setOnMouseExited(e -> editProfileBtn.setStyle(currentTheme.equals("light")
            ? "-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 8;"
            : "-fx-background-color: #a5b4fc; -fx-text-fill: #232136; -fx-background-radius: 8;"));

        editProfileBtn.setOnAction(e -> showEditProfileDialog(profile));

        HBox profileButtonBox = new HBox();
        profileButtonBox.setAlignment(Pos.BOTTOM_RIGHT);
        Region profileSpacer = new Region();
        HBox.setHgrow(profileSpacer, Priority.ALWAYS);
        profileButtonBox.getChildren().addAll(profileSpacer, editProfileBtn);


        profileSection.getChildren().addAll(profileTitle, profileInfo, profileButtonBox);

        homeContent.getChildren().addAll(friendsSection, profileSection);
        diaryListBox.getChildren().clear();
        diaryListBox.getChildren().add(homeContent);
    }

    private void addProfileField(VBox container, String label, String value) {
        HBox field = new HBox(16);
        field.setAlignment(Pos.CENTER_LEFT);

        Label fieldLabel = new Label(label + ":");
        fieldLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        fieldLabel.getStyleClass().add("info-label");
        fieldLabel.setTextFill(currentTheme.equals("light") ? Color.web("#000000") : Color.web("#c7d2fe"));
        fieldLabel.setPrefWidth(180);
        fieldLabel.setWrapText(true);
        if (currentTheme.equals("light")) fieldLabel.setStyle("-fx-opacity: 1; -fx-text-fill: #1f2937;");

        Label fieldValue = new Label(value != null ? value : "Chưa cập nhật");
        fieldValue.setFont(Font.font("Arial", FontWeight.NORMAL, 17));
        fieldValue.getStyleClass().add("info-label");
        fieldValue.setTextFill(currentTheme.equals("light") ? Color.web("#000000") : Color.web("#e5e7eb"));
        fieldValue.setPrefWidth(300);
        fieldValue.setWrapText(true);
        if (currentTheme.equals("light")) fieldValue.setStyle("-fx-opacity: 1; -fx-text-fill: #1f2937;");

        field.getChildren().addAll(fieldLabel, fieldValue);
        container.getChildren().add(field);
    }

    private void showEditProfileDialog(UserProfile profile) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Chỉnh sửa thông tin cá nhân");

        VBox box = new VBox(22);
        box.setPadding(new Insets(36, 36, 32, 36));
        box.setAlignment(Pos.CENTER);
        box.setSpacing(18);
        box.setStyle(
            "-fx-background-radius: 22;" +
            "-fx-background-insets: 0;" +
            "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #ede9fe, #c7d2fe);" +
            "-fx-effect: dropshadow(gaussian, #a5b4fc88, 18, 0.3, 0, 4);"
        );

        Label titleLbl = new Label("Chỉnh sửa thông tin cá nhân");
        titleLbl.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 26));
        titleLbl.setTextFill(Color.web("#6d28d9"));
        titleLbl.setStyle("-fx-effect: dropshadow(gaussian, #a5b4fc, 8, 0.2, 0, 2);");

        TextField fullNameField = new TextField(profile.getFullName());
        fullNameField.setPromptText("Họ và tên");
        fullNameField.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        fullNameField.setPrefWidth(340);
        fullNameField.setStyle(
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-border-color: #a5b4fc;" +
            "-fx-border-width: 1.2;" +
            "-fx-background-color: #fff;" +
            "-fx-text-fill: " + (currentTheme.equals("light") ? "#232136;" : "#232136;")
        );

        // Add username and email fields
        TextField usernameField = new TextField(profile.getUsername());
        usernameField.setPromptText("Tên người dùng");
        usernameField.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        usernameField.setPrefWidth(340);
        usernameField.setStyle(
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-border-color: #a5b4fc;" +
            "-fx-border-width: 1.2;" +
            "-fx-background-color: #fff;" +
            "-fx-text-fill: " + (currentTheme.equals("light") ? "#232136;" : "#232136;")
        );

        TextField emailField = new TextField(profile.getEmail());
        emailField.setPromptText("Email");
        emailField.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        emailField.setPrefWidth(340);
        emailField.setStyle(
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-border-color: #a5b4fc;" +
            "-fx-border-width: 1.2;" +
            "-fx-background-color: #fff;" +
            "-fx-text-fill: " + (currentTheme.equals("light") ? "#232136;" : "#232136;")
        );

        TextField phoneField = new TextField(profile.getPhone());
        phoneField.setPromptText("Số điện thoại");
        phoneField.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        phoneField.setPrefWidth(340);
        phoneField.setStyle(
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-border-color: #a5b4fc;" +
            "-fx-border-width: 1.2;" +
            "-fx-background-color: #fff;" +
            "-fx-text-fill: " + (currentTheme.equals("light") ? "#232136;" : "#232136;")
        );

        TextField addressField = new TextField(profile.getAddress());
        addressField.setPromptText("Địa chỉ");
        addressField.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        addressField.setPrefWidth(340);
        addressField.setStyle(
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-border-color: #a5b4fc;" +
            "-fx-border-width: 1.2;" +
            "-fx-background-color: #fff;" +
            "-fx-text-fill: " + (currentTheme.equals("light") ? "#232136;" : "#232136;")
        );

        Button saveBtn = new Button("Lưu");
        saveBtn.setDefaultButton(true);
        saveBtn.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        saveBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 10;");
        saveBtn.setOnMouseEntered(e -> saveBtn.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-background-radius: 10;"));
        saveBtn.setOnMouseExited(e -> saveBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 10;"));

        Button cancelBtn = new Button("Hủy");
        cancelBtn.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        cancelBtn.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #6d28d9; -fx-background-radius: 10;");
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle("-fx-background-color: #c7d2fe; -fx-text-fill: #232136; -fx-background-radius: 10;"));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #6d28d9; -fx-background-radius: 10;"));
        cancelBtn.setOnAction(e -> dialog.close());

        HBox btnBox = new HBox(14, saveBtn, cancelBtn);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        saveBtn.setOnAction(e -> {
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();

            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String fullName = fullNameField.getText().trim();

            if (DatabaseManager.updateUserProfile(DatabaseManager.getCurrentUserId(), username, email, phone, address, fullName)) {
                updateTab("Trang chủ");
                dialog.close();
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Đã cập nhật thông tin thành công!");
                successAlert.showAndWait();
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Lỗi khi cập nhật thông tin!");
                errorAlert.showAndWait();
            }
        });

        box.getChildren().addAll(titleLbl, fullNameField, usernameField, emailField, phoneField, addressField, btnBox);

        Scene scene = new Scene(box);
        dialog.setScene(scene);
        javafx.application.Platform.runLater(() -> dialog.centerOnScreen());
        dialog.showAndWait();
    }

    private void showForgotPasswordDialog() {

        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Quên mật khẩu");
        dialog.setHeaderText("Nhập tên người dùng và số điện thoại của bạn:");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Tên người dùng");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Số điện thoại");

        grid.add(new Label("Tên người dùng:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Số điện thoại:"), 0, 1);
        grid.add(phoneField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                Map<String, String> result = new HashMap<>();
                result.put("username", usernameField.getText());
                result.put("phone", phoneField.getText());
                return result;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String username = result.get("username");
            String phone = result.get("phone");

            // TODO: Implement verification logic using DatabaseManager

            boolean verificationSuccessful = DatabaseManager.verifyUserForPasswordReset(username, phone);

            if (verificationSuccessful) {
                showPasswordResetDialog(username);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText("Tên người dùng hoặc số điện thoại không chính xác.");
                alert.showAndWait();
            }
        });
    }

    private void showPasswordResetDialog(String username) {
        // Second dialog: set and confirm new password
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Đặt lại mật khẩu");
        dialog.setHeaderText("Nhập mật khẩu mới:");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Mật khẩu mới");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Xác nhận mật khẩu mới");

        grid.add(new Label("Mật khẩu mới:"), 0, 0);
        grid.add(newPasswordField, 1, 0);
        grid.add(new Label("Xác nhận mật khẩu:"), 0, 1);
        grid.add(confirmPasswordField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                Map<String, String> result = new HashMap<>();
                result.put("newPassword", newPasswordField.getText());
                result.put("confirmPassword", confirmPasswordField.getText());
                return result;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String newPassword = result.get("newPassword");
            String confirmPassword = result.get("confirmPassword");

            if (newPassword.equals(confirmPassword)) {
                // TODO: Implement password update logic using DatabaseManager

                 boolean updateSuccessful = DatabaseManager.updatePassword(username, newPassword);

                 if (updateSuccessful) {
                     Alert alert = new Alert(Alert.AlertType.INFORMATION);
                     alert.setTitle("Thành công");
                     alert.setHeaderText(null);
                     alert.setContentText("Mật khẩu của bạn đã được đặt lại thành công!");
                     alert.showAndWait();
                 } else {
                      Alert alert = new Alert(Alert.AlertType.ERROR);
                      alert.setTitle("Lỗi");
                      alert.setHeaderText(null);
                      alert.setContentText("Có lỗi xảy ra khi đặt lại mật khẩu.");
                      alert.showAndWait();
                 }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText("Mật khẩu mới và xác nhận mật khẩu không khớp.");
                alert.showAndWait();
            }
        });
    }

    private void updateTheme() {
        String theme = currentTheme.equals("light") ? "" : "dark-theme";
        for (Node node : diaryListBox.getChildren()) {
            if (node instanceof VBox) {
                VBox card = (VBox) node;
                card.getStyleClass().remove("dark-theme");
                if (!theme.isEmpty()) {
                    card.getStyleClass().add(theme);
                }
                
                for (Node child : card.getChildren()) {
                    if (child instanceof Label) {
                        Label label = (Label) child;
                        label.getStyleClass().remove("dark-theme");
                        if (!theme.isEmpty()) {
                            label.getStyleClass().add(theme);
                        }
                    }
                }
            }
        }
    }
}