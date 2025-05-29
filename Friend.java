public class Friend {
    private int id;
    private int friendId;
    private String username;
    private String email;
    private String address;
    private String phone;

    public Friend(int id, int friendId, String username, String email, String address, String phone) {
        this.id = id;
        this.friendId = friendId;
        this.username = username;
        this.email = email;
        this.address = address;
        this.phone = phone;
    }

    // Getters
    public int getId() { return id; }
    public int getFriendId() { return friendId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setFriendId(int friendId) { this.friendId = friendId; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setAddress(String address) { this.address = address; }
    public void setPhone(String phone) { this.phone = phone; }
} 