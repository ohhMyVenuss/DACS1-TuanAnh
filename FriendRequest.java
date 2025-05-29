public class FriendRequest {
    private int id;
    private int senderId;
    private int receiverId;
    private String status;
    private String createdAt;
    private String senderUsername;
    private String senderEmail;

    public FriendRequest(int id, int senderId, int receiverId, String status, String createdAt, String senderUsername, String senderEmail) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status;
        this.createdAt = createdAt;
        this.senderUsername = senderUsername;
        this.senderEmail = senderEmail;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    // Setters (if needed, but typically requests are immutable after creation)
    // public void setId(int id) { this.id = id; }
    // public void setSenderId(int senderId) { this.senderId = senderId; }
    // public void setReceiverId(int receiverId) { this.receiverId = receiverId; }
    // public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }
    // public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "FriendRequest{" +
               "id=" + id +
               ", senderId=" + senderId +
               ", receiverId=" + receiverId +
               ", senderUsername='" + senderUsername + '\'' +
               ", status='" + status + '\'' +
               '}';
    }
} 