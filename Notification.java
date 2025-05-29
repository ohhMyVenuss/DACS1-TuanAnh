public class Notification extends DiaryMainView.Notification {
    private int id;
    private String message;
    private String type;
    private String createdAt;

    public Notification(int id, String message, String type, String createdAt) {
        super();
        this.id = id;
        this.message = message;
        this.type = type;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getCreatedAt() {
        return createdAt;
    }
} 