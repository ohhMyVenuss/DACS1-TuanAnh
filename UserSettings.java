public class UserSettings {
    private int userId;
    private boolean notificationsEnabled;
    private String theme;
    private String language;

    public UserSettings(int userId, boolean notificationsEnabled, String theme, String language) {
        this.userId = userId;
        this.notificationsEnabled = notificationsEnabled;
        this.theme = theme;
        this.language = language;
    }

    public int getUserId() {
        return userId;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
} 