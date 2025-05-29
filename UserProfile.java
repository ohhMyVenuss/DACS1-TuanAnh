public class UserProfile {
    private int userId;
    private String username;
    private String email;
    private String phone;
    private String address;
    private String fullName;

    public UserProfile(int userId, String username, String email, String phone, String address, String fullName) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.fullName = fullName;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getFullName() {
        return fullName;
    }
} 