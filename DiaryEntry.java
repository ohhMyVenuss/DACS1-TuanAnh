import java.util.List;
import java.util.ArrayList;

public class DiaryEntry {
    private int id;
    private String title;
    private String content;
    private String date;
    private String imagePath;
    private List<Integer> taggedUserIds;
    private int reminderDays;
    private int userId;

    public DiaryEntry(int id, String t, String c, String d, String img) {
        this.id = id;
        title = t; content = c; date = d; imagePath = img;
        this.taggedUserIds = new ArrayList<>(); // Initialize the list
        this.reminderDays = 0;
    }

    public DiaryEntry(int id, String t, String c, String d, String img, List<Integer> taggedUserIds) {
        this.id = id;
        title = t; content = c; date = d; imagePath = img;
        this.taggedUserIds = taggedUserIds != null ? new ArrayList<>(taggedUserIds) : new ArrayList<>();
        this.reminderDays = 0;
    }

    public DiaryEntry(int id, String t, String c, String d, String img, int userId) {
        this.id = id;
        title = t; content = c; date = d; imagePath = img;
        this.taggedUserIds = new ArrayList<>();
        this.reminderDays = 0;
        this.userId = userId;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getDate() { return date; }
    public String getImagePath() { return imagePath; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public List<Integer> getTaggedUserIds() {
        return taggedUserIds;
    }

    public void setTaggedUserIds(List<Integer> taggedUserIds) {
        this.taggedUserIds = taggedUserIds != null ? new ArrayList<>(taggedUserIds) : new ArrayList<>();
    }

    public int getReminderDays() {
        return reminderDays;
    }

    public void setReminderDays(int reminderDays) {
        this.reminderDays = reminderDays;
    }
} 