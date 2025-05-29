import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;

import java.util.ArrayList;
import java.util.List;

public class AdminView extends StackPane {
    private VBox mainBox;
    private HBox topBar;
    private HBox menuBar;
    private Label headerLabel;
    private VBox contentBox;
    private ScrollPane scrollPane;
    private String currentTab = "Trang chủ";
    private List<Button> menuButtons = new ArrayList<>();

    public AdminView() {
        setPrefSize(1200, 800);
        mainBox = new VBox();
        mainBox.setSpacing(0);
        mainBox.setPrefSize(1200, 800);
        mainBox.setStyle("-fx-background-color: linear-gradient(to right, #e0e7ff, #c7d2fe, #a5b4fc);");

        getStylesheets().add(getClass().getResource("/res/diaryfx.css").toExternalForm());

        createTopBar();
        createMenuBar();
        createHeader();
        createContentArea();

        mainBox.getChildren().addAll(topBar, menuBar, headerLabel, scrollPane);
        getChildren().add(mainBox);

        updateTab("Trang chủ");
    }

    private void createTopBar() {
        topBar = new HBox();
        topBar.setPadding(new Insets(12, 32, 12, 32));
        topBar.setSpacing(16);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: transparent;");

        ImageView avatar = new ImageView(new Image("file:D:/dacs1/res/admin.png"));
        avatar.setFitWidth(36);
        avatar.setFitHeight(36);

        Label adminLabel = new Label("Admin");
        adminLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        adminLabel.getStyleClass().add("info-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("Đăng xuất");
        logoutBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        logoutBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 8;");
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-background-radius: 8;"));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 8;"));
        logoutBtn.setOnAction(e -> {
            Stage currentStage = (Stage) getScene().getWindow();
            LoginView loginView = new LoginView(currentStage);
            Scene loginScene = new Scene(loginView, 400, 500);
            currentStage.setScene(loginScene);
            currentStage.setTitle("Login");
            currentStage.show();
            javafx.application.Platform.runLater(() -> currentStage.centerOnScreen());
        });

        topBar.getChildren().addAll(avatar, adminLabel, spacer, logoutBtn);
    }

    private void createMenuBar() {
        menuBar = new HBox();
        menuBar.setSpacing(0);
        menuBar.setAlignment(Pos.CENTER_LEFT);
        menuBar.setStyle("-fx-background-color: #ede9fe; -fx-padding: 0 40 0 40;");
        menuBar.setPrefHeight(56);

        String[] menus = {
            "Trang chủ",
            "Quản lý nhật ký riêng tư",
            "Quản lý nhật ký được chia sẻ",
            "Cài đặt"
        };

        for (int i = 0; i < menus.length; i++) {
            Button btn = createMenuButton(menus[i]);
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

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 17));
        btn.getStyleClass().add("menu-btn");
        btn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-padding: 0 20 0 20; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 17px;"
        );
        btn.setPrefHeight(56);
        btn.setAlignment(Pos.CENTER);
        btn.setOnAction(e -> updateTab(text));
        return btn;
    }

    private void createHeader() {
        headerLabel = new Label();
        headerLabel.getStyleClass().add("header-label");
        headerLabel.setPadding(new Insets(28, 0, 18, 40));
    }

    private void createContentArea() {
        contentBox = new VBox(22);
        contentBox.setPadding(new Insets(0, 40, 40, 40));
        contentBox.setAlignment(Pos.TOP_CENTER);

        scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private void updateTab(String tab) {
        currentTab = tab;

        for (Button btn : menuButtons) {
            boolean isSelected = btn.getText().equals(tab);
            btn.getStyleClass().remove("selected-menu-btn");
            if (isSelected) {
                btn.getStyleClass().add("selected-menu-btn");
            }
        }

        contentBox.getChildren().clear();
        headerLabel.setText(tab);

        switch (tab) {
            case "Trang chủ":
                showAllUsers();
                break;
            case "Quản lý nhật ký riêng tư":
                showPrivateDiaries();
                break;
            case "Quản lý nhật ký được chia sẻ":
                showSharedDiaries();
                break;
            case "Cài đặt":
                showSettings();
                break;
        }
    }

    private void showAllUsers() {
        List<UserProfile> users = DatabaseManager.getAllUsers();
        
        if (users.isEmpty()) {
            Label noUsersLabel = new Label("Không có người dùng nào");
            noUsersLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
            noUsersLabel.getStyleClass().add("info-label");
            contentBox.getChildren().add(noUsersLabel);
            return;
        }

        for (UserProfile user : users) {
            HBox userCard = createUserCard(user);
            contentBox.getChildren().add(userCard);
        }
    }

    private HBox createUserCard(UserProfile user) {
        HBox card = new HBox(12);
        card.setPadding(new Insets(14, 24, 14, 24));
        card.getStyleClass().add("user-card");
        card.setAlignment(Pos.CENTER_LEFT);

        Circle avatar = new Circle(18, Color.web("#6d28d9"));
        Label initial = new Label(user.getUsername().substring(0, 1).toUpperCase());
        initial.getStyleClass().add("info-label");
        initial.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        StackPane avatarPane = new StackPane(avatar, initial);

        VBox infoBox = new VBox(4);
        Label nameLabel = new Label(user.getUsername());
        nameLabel.getStyleClass().add("info-label");
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Label emailLabel = new Label(user.getEmail());
        emailLabel.getStyleClass().add("info-label");
        emailLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));

        infoBox.getChildren().addAll(nameLabel, emailLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button("Xóa");
        deleteBtn.getStyleClass().add("button");
        deleteBtn.getStyleClass().add("delete-button");
        deleteBtn.setOnAction(e -> {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Xác nhận xóa");
            confirmDialog.setHeaderText("Bạn có chắc chắn muốn xóa người dùng " + user.getUsername() + "?");
            confirmDialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    if (DatabaseManager.deleteUser(user.getUserId())) {
                        contentBox.getChildren().remove(card);
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Đã xóa người dùng thành công!");
                        successAlert.showAndWait();
                    } else {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Lỗi khi xóa người dùng!");
                        errorAlert.showAndWait();
                    }
                }
            });
        });

        card.getChildren().addAll(avatarPane, infoBox, spacer, deleteBtn);
        return card;
    }

    private void showPrivateDiaries() {
        List<DiaryEntry> privateDiaries = DatabaseManager.getAllPrivateDiaries();
        
        if (privateDiaries.isEmpty()) {
            Label noDiariesLabel = new Label("Không có nhật ký riêng tư nào");
            noDiariesLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
            noDiariesLabel.getStyleClass().add("info-label");
            contentBox.getChildren().add(noDiariesLabel);
            return;
        }

        for (DiaryEntry entry : privateDiaries) {
            VBox diaryCard = createDiaryCard(entry);
            contentBox.getChildren().add(diaryCard);
        }
    }

    private void showSharedDiaries() {
        List<DiaryEntry> sharedDiaries = DatabaseManager.getAllSharedDiaries();
        
        if (sharedDiaries.isEmpty()) {
            Label noDiariesLabel = new Label("Không có nhật ký được chia sẻ nào");
            noDiariesLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
            noDiariesLabel.getStyleClass().add("info-label");
            contentBox.getChildren().add(noDiariesLabel);
            return;
        }

        for (DiaryEntry entry : sharedDiaries) {
            VBox diaryCard = createDiaryCard(entry);
            contentBox.getChildren().add(diaryCard);
        }
    }

    private VBox createDiaryCard(DiaryEntry entry) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(18, 24, 18, 24));
        card.setStyle("-fx-background-color: #e5e7eb; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, #a5b4fc44, 7, 0.12, 0, 2);");
        card.setMaxWidth(600);
        card.setMinWidth(350);
        card.setAlignment(Pos.TOP_LEFT);

        HBox top = new HBox(8);
        Label title = new Label(entry.getTitle());
        title.getStyleClass().add("section-title");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        Label author = new Label("Tác giả: " + DatabaseManager.getUsernameById(entry.getUserId()));
        author.getStyleClass().add("info-label");
        author.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label date = new Label(entry.getDate());
        date.getStyleClass().add("info-label");
        date.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        
        top.getChildren().addAll(title, author, spacer, date);

        VBox contentBox = new VBox(8);
        if (entry.getImagePath() != null && !entry.getImagePath().isEmpty()) {
            ImageView img = new ImageView(new Image(entry.getImagePath(), 150, 0, true, true));
            img.setSmooth(true);
            img.setPreserveRatio(true);
            img.setStyle("-fx-background-radius: 8;");
            contentBox.getChildren().add(img);
        }
        
        Label content = new Label(entry.getContent());
        content.getStyleClass().add("info-label");
        content.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        content.setWrapText(true);
        contentBox.getChildren().add(content);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button deleteBtn = new Button("Xóa");
        deleteBtn.getStyleClass().add("button");
        deleteBtn.getStyleClass().add("delete-button");
        deleteBtn.setOnAction(e -> {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Xác nhận xóa");
            confirmDialog.setHeaderText("Bạn có chắc chắn muốn xóa nhật ký này?");
            confirmDialog.setContentText("Nhật ký: " + entry.getTitle());

            confirmDialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    if (DatabaseManager.deleteDiaryEntry(entry.getId())) {
                        contentBox.getChildren().remove(card);
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Đã xóa nhật ký thành công!");
                        successAlert.showAndWait();
                    } else {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Lỗi khi xóa nhật ký!");
                        errorAlert.showAndWait();
                    }
                }
            });
        });

        buttonBox.getChildren().add(deleteBtn);
        card.getChildren().addAll(top, contentBox, buttonBox);

        return card;
    }

    private void showSettings() {
        VBox settingsBox = new VBox(28);
        settingsBox.setPadding(new Insets(40, 0, 0, 0));
        settingsBox.setAlignment(Pos.TOP_CENTER);

        VBox card = new VBox(24);
        card.setPadding(new Insets(32, 40, 32, 40));
        card.setStyle("-fx-background-color: #e5e7eb; -fx-background-radius: 18; -fx-effect: dropshadow(gaussian, #a5b4fc88, 10, 0.2, 0, 2);");
        card.setMaxWidth(400);
        card.setAlignment(Pos.TOP_CENTER);

        // Thông báo
        VBox notificationSettings = new VBox(12);
        Label notificationTitle = new Label("Thông báo");
        notificationTitle.getStyleClass().add("section-title");
        notificationTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        CheckBox notifyCheck = new CheckBox("Bật thông báo");
        notifyCheck.getStyleClass().add("info-label");
        notifyCheck.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        notifyCheck.setSelected(true);
        notifyCheck.setDisable(true);
        
        Label notificationDesc = new Label("Nhận thông báo về hoạt động hệ thống");
        notificationDesc.getStyleClass().add("info-label");
        notificationDesc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        
        notificationSettings.getChildren().addAll(notificationTitle, notifyCheck, notificationDesc);
        VBox themeSettings = new VBox(12);
        Label themeTitle = new Label("Giao diện");
        themeTitle.getStyleClass().add("section-title");
        themeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        Button changeThemeBtn = new Button("Đổi giao diện");
        changeThemeBtn.getStyleClass().add("button");
        changeThemeBtn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        changeThemeBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 10;");
        changeThemeBtn.setOnMouseEntered(e -> changeThemeBtn.setStyle("-fx-background-color: #a5b4fc; -fx-text-fill: #312e81; -fx-background-radius: 10;"));
        changeThemeBtn.setOnMouseExited(e -> changeThemeBtn.setStyle("-fx-background-color: #6d28d9; -fx-text-fill: white; -fx-background-radius: 10;"));
        changeThemeBtn.setOnAction(e -> {
            if (mainBox.getStyle().contains("#e0e7ff")) {
                mainBox.setStyle("-fx-background-color: linear-gradient(to right, #232136, #393552, #45475a);");
            } else {
                mainBox.setStyle("-fx-background-color: linear-gradient(to right, #e0e7ff, #c7d2fe, #a5b4fc);");
            }
        });
        
        Label themeDesc = new Label("Chuyển đổi giữa giao diện sáng và tối");
        themeDesc.getStyleClass().add("info-label");
        themeDesc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        
        themeSettings.getChildren().addAll(themeTitle, changeThemeBtn, themeDesc);

        VBox languageSettings = new VBox(12);
        Label languageTitle = new Label("Ngôn ngữ");
        languageTitle.getStyleClass().add("section-title");
        languageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        Button changeLangBtn = new Button("Đổi ngôn ngữ");
        changeLangBtn.getStyleClass().add("button");
        changeLangBtn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        changeLangBtn.setStyle("-fx-background-color: #d1d5db; -fx-text-fill: #312e81; -fx-background-radius: 10;");
        changeLangBtn.setOnMouseEntered(e -> changeLangBtn.setStyle("-fx-background-color: #c7d2fe; -fx-text-fill: #232136; -fx-background-radius: 10;"));
        changeLangBtn.setOnMouseExited(e -> changeLangBtn.setStyle("-fx-background-color: #d1d5db; -fx-text-fill: #312e81; -fx-background-radius: 10;"));
        
        Label languageDesc = new Label("Thay đổi ngôn ngữ hiển thị");
        languageDesc.getStyleClass().add("info-label");
        languageDesc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        
        languageSettings.getChildren().addAll(languageTitle, changeLangBtn, languageDesc);

        card.getChildren().addAll(notificationSettings, new Separator(), themeSettings, new Separator(), languageSettings);
        settingsBox.getChildren().add(card);
        contentBox.getChildren().add(settingsBox);
    }
} 