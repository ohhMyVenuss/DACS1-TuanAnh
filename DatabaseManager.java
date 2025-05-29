import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String URL = "jdbc:mysql://localhost:3306/diary_app";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    private static int currentUserId = -1;

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static int validateUser(String username, String password) {
        String query = "SELECT id FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentUserId = rs.getInt("id");
                return currentUserId;
            } else {
                currentUserId = -1;
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            currentUserId = -1;
            return -1;
        }
    }

    public static boolean isAdmin(String username, String password) {
        String adminUsername = "admin";
        String adminPassword = "123";

        return username.equals(adminUsername) && password.equals(adminPassword);
    }

    public static boolean addUser(String username, String password, String email, String address, String phone, String fullName) {
        String query = "INSERT INTO users (username, password, email, address, phone, full_name) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.setString(4, address);
            stmt.setString(5, phone);
            stmt.setString(6, fullName);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static List<DiaryEntry> getDiaryEntriesByUserId(int userId) {
        List<DiaryEntry> entries = new ArrayList<>();
        String query = "SELECT id, title, content, created_at, image_path FROM diary_entries WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String content = rs.getString("content");
                String date = rs.getString("created_at");
                String imagePath = rs.getString("image_path");
                // Fetch tagged user IDs for this entry
                List<Integer> taggedUserIds = getTaggedUserIdsForEntry(id);
                entries.add(new DiaryEntry(id, title, content, date, imagePath, taggedUserIds));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

    public static boolean deleteDiaryEntry(int diaryId) {
        String query = "DELETE FROM diary_entries WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, diaryId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateDiaryEntry(int diaryId, String title, String content, String imagePath, List<Integer> taggedUserIds) {
        String updateEntryQuery = "UPDATE diary_entries SET title = ?, content = ?, image_path = ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement updateEntryStmt = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            updateEntryStmt = conn.prepareStatement(updateEntryQuery);
            updateEntryStmt.setString(1, title);
            updateEntryStmt.setString(2, content);
            updateEntryStmt.setString(3, imagePath);
            updateEntryStmt.setInt(4, diaryId);
            int rowsAffected = updateEntryStmt.executeUpdate();

            if (rowsAffected == 0) {
                conn.rollback();
                return false;
            }

            if (!deleteTaggedUsersForEntry(diaryId)) {
                conn.rollback();
                return false;
            }
            if (!addTaggedUsersForEntry(diaryId, taggedUserIds)) {
                 conn.rollback();
                 return false;
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            closeConnection(conn);
            try { if (updateEntryStmt != null) updateEntryStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public static boolean addDiaryEntry(int userId, String title, String content, String date, String imagePath, List<Integer> taggedUserIds, int reminderDays) {
        String insertEntryQuery = "INSERT INTO diary_entries (user_id, title, content, created_at, image_path, reminder_days) VALUES (?, ?, ?, ?, ?, ?)";
        String insertTaggedUserQuery = "INSERT INTO diary_entry_tagged_users (diary_entry_id, user_id) VALUES (?, ?)";
        
        Connection conn = null;
        PreparedStatement insertEntryStmt = null;
        PreparedStatement insertTaggedUserStmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            insertEntryStmt = conn.prepareStatement(insertEntryQuery, PreparedStatement.RETURN_GENERATED_KEYS);
            insertEntryStmt.setInt(1, userId);
            insertEntryStmt.setString(2, title);
            insertEntryStmt.setString(3, content);
            insertEntryStmt.setString(4, date);
            insertEntryStmt.setString(5, imagePath);
            insertEntryStmt.setInt(6, reminderDays);
            int rowsAffected = insertEntryStmt.executeUpdate();
            
            if (rowsAffected == 0) {
                conn.rollback();
                return false;
            }

            rs = insertEntryStmt.getGeneratedKeys();
            int diaryEntryId = -1;
            if (rs.next()) {
                diaryEntryId = rs.getInt(1);
            } else {
                conn.rollback();
                return false;
            }

            if (taggedUserIds != null && !taggedUserIds.isEmpty()) {
                insertTaggedUserStmt = conn.prepareStatement(insertTaggedUserQuery);
                for (Integer taggedUserId : taggedUserIds) {
                    insertTaggedUserStmt.setInt(1, diaryEntryId);
                    insertTaggedUserStmt.setInt(2, taggedUserId);
                    insertTaggedUserStmt.addBatch();
                }
                insertTaggedUserStmt.executeBatch();
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            closeConnection(conn);
            try { if (insertEntryStmt != null) insertEntryStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (insertTaggedUserStmt != null) insertTaggedUserStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private static List<Integer> getTaggedUserIdsForEntry(int diaryEntryId) {
        List<Integer> taggedUserIds = new ArrayList<>();
        String query = "SELECT user_id FROM diary_entry_tagged_users WHERE diary_entry_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, diaryEntryId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                taggedUserIds.add(rs.getInt("user_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return taggedUserIds;
    }

    public static List<DiaryEntry> getSharedDiaryEntries(int userId) {
        List<DiaryEntry> entries = new ArrayList<>();
        String query = "SELECT DISTINCT de.id, de.title, de.content, de.created_at, de.image_path " +
                      "FROM diary_entries de " +
                      "LEFT JOIN diary_entry_tagged_users detu ON de.id = detu.diary_entry_id " +
                      "WHERE detu.user_id = ? " +
                      "   OR (de.user_id = ? AND EXISTS (SELECT 1 FROM diary_entry_tagged_users sub_detu WHERE sub_detu.diary_entry_id = de.id)) " +
                      "ORDER BY de.created_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String content = rs.getString("content");
                String date = rs.getString("created_at");
                String imagePath = rs.getString("image_path");
                List<Integer> taggedUserIds = getTaggedUserIdsForEntry(id);
                entries.add(new DiaryEntry(id, title, content, date, imagePath, taggedUserIds));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

    public static String getUsernameById(int userId) {
        String query = "SELECT username FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            } else {
                return "";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static List<Friend> getFriendsList(int userId) {
        List<Friend> friends = new ArrayList<>();
        String query = "SELECT f.id, f.friend_id, u.username, u.email, u.address, u.phone " +
                      "FROM friends f " +
                      "JOIN users u ON f.friend_id = u.id " +
                      "WHERE f.user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                int friendId = rs.getInt("friend_id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                String address = rs.getString("address");
                String phone = rs.getString("phone");
                friends.add(new Friend(id, friendId, username, email, address, phone));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friends;
    }

    public static boolean sendFriendRequest(int senderId, int receiverId) {

        if (isFriendshipExists(senderId, receiverId)) {
            return false;
        }

        String checkQuery = "SELECT COUNT(*) FROM friend_requests WHERE sender_id = ? AND receiver_id = ? AND status = 'pending'";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkQuery)) {
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return false; // Request already exists
            }

            String insertQuery = "INSERT INTO friend_requests (sender_id, receiver_id, status, created_at) VALUES (?, ?, 'pending', NOW())";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setInt(1, senderId);
                insertStmt.setInt(2, receiverId);
                int rowsAffected = insertStmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<FriendRequest> getReceivedFriendRequests(int userId) {
        List<FriendRequest> requests = new ArrayList<>();
        String query = "SELECT fr.id, fr.sender_id, fr.receiver_id, fr.status, fr.created_at, u.username, u.email " +
                      "FROM friend_requests fr " +
                      "JOIN users u ON fr.sender_id = u.id " +
                      "WHERE fr.receiver_id = ? AND fr.status = 'pending' " +
                      "ORDER BY fr.created_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                int senderId = rs.getInt("sender_id");
                int receiverId = rs.getInt("receiver_id");
                String status = rs.getString("status");
                String createdAt = rs.getString("created_at");
                String senderUsername = rs.getString("username");
                String senderEmail = rs.getString("email");
                requests.add(new FriendRequest(id, senderId, receiverId, status, createdAt, senderUsername, senderEmail));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    public static boolean acceptFriendRequest(int requestId) {
        String getRequestQuery = "SELECT sender_id, receiver_id FROM friend_requests WHERE id = ? AND status = 'pending'";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(getRequestQuery)) {
            stmt.setInt(1, requestId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int senderId = rs.getInt("sender_id");
                int receiverId = rs.getInt("receiver_id");

                conn.setAutoCommit(false);
                try {
                    String addFriendQuery = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?), (?, ?)";
                    try (PreparedStatement addStmt = conn.prepareStatement(addFriendQuery)) {
                        addStmt.setInt(1, senderId);
                        addStmt.setInt(2, receiverId);
                        addStmt.setInt(3, receiverId);
                        addStmt.setInt(4, senderId);
                        addStmt.executeUpdate();
                    }

                    String updateQuery = "UPDATE friend_requests SET status = 'accepted' WHERE id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, requestId);
                        updateStmt.executeUpdate();
                    }
                    
                    conn.commit();
                    return true;
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean rejectFriendRequest(int requestId) {
        String query = "UPDATE friend_requests SET status = 'rejected' WHERE id = ? AND status = 'pending'";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, requestId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addFriend(int userId, String friendUsername) {

        String getFriendIdQuery = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(getFriendIdQuery)) {
            stmt.setString(1, friendUsername);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int friendId = rs.getInt("id");

                return sendFriendRequest(userId, friendId);
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean removeFriend(int userId, int friendId) {
        String query = "DELETE FROM friends WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, friendId);
            stmt.setInt(3, friendId);
            stmt.setInt(4, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean isFriendshipExists(int userId, int friendId) {
        String query = "SELECT COUNT(*) FROM friends WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, friendId);
            stmt.setInt(3, friendId);
            stmt.setInt(4, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<Friend> getPotentialFriends(int userId, String searchQuery) {
        List<Friend> potentialFriends = new ArrayList<>();
        String query = "SELECT u.id, u.username, u.email, u.address, u.phone " +
                      "FROM users u " +
                      "WHERE u.id != ? " +
                      "AND u.username != 'admin' " +
                      "AND u.id NOT IN ( " +
                      "    SELECT friend_id FROM friends WHERE user_id = ? " +
                      "    UNION " +
                      "    SELECT user_id FROM friends WHERE friend_id = ? " +
                      ") " +
                      "AND u.username LIKE ? " +
                      "ORDER BY u.username";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            stmt.setString(4, "%" + searchQuery + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                String address = rs.getString("address");
                String phone = rs.getString("phone");
                potentialFriends.add(new Friend(0, id, username, email, address, phone));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return potentialFriends;
    }

    public static boolean deleteTaggedUsersForEntry(int diaryEntryId) {
        String query = "DELETE FROM diary_entry_tagged_users WHERE diary_entry_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, diaryEntryId);
            int rowsAffected = stmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean addTaggedUsersForEntry(int diaryId, List<Integer> taggedUserIds) {
        if (taggedUserIds == null || taggedUserIds.isEmpty()) {
            System.out.println("No tagged users to add");
            return true;
        }

        String insertTagQuery = "INSERT INTO diary_tagged_users (diary_id, user_id) VALUES (?, ?)";
        System.out.println("Adding tagged users for diary " + diaryId + ": " + taggedUserIds);

        try (Connection conn = getConnection();
             PreparedStatement insertTagStmt = conn.prepareStatement(insertTagQuery)) {

            for (Integer userId : taggedUserIds) {
                insertTagStmt.setInt(1, diaryId);
                insertTagStmt.setInt(2, userId);
                int result = insertTagStmt.executeUpdate();
                System.out.println("Added tag for user " + userId + ": " + (result > 0 ? "success" : "failed"));
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding tagged users: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static UserSettings getUserSettings(int userId) {
        String query = "SELECT notifications_enabled, theme, language FROM user_settings WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                boolean notificationsEnabled = rs.getBoolean("notifications_enabled");
                String theme = rs.getString("theme");
                String language = rs.getString("language");
                return new UserSettings(userId, notificationsEnabled, theme, language);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new UserSettings(userId, true, "light", "vi");
    }

    public static boolean updateUserSettings(UserSettings settings) {
        String query = "UPDATE user_settings SET notifications_enabled = ?, theme = ?, language = ? WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBoolean(1, settings.isNotificationsEnabled());
            stmt.setString(2, settings.getTheme());
            stmt.setString(3, settings.getLanguage());
            stmt.setInt(4, settings.getUserId());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addNotification(int userId, String message, String type) {
        String query = "INSERT INTO notifications (user_id, message, type) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, message);
            stmt.setString(3, type);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<DiaryMainView.Notification> getUnreadNotifications(int userId) {
        List<DiaryMainView.Notification> notifications = new ArrayList<>();
        String query = "SELECT id, message, type, created_at FROM notifications WHERE user_id = ? AND is_read = FALSE ORDER BY created_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String message = rs.getString("message");
                String type = rs.getString("type");
                String createdAt = rs.getString("created_at");
                notifications.add(new Notification(id, message, type, createdAt));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    public static boolean markNotificationsAsRead(int userId) {
        String query = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<DiaryEntry> getDiaryEntriesWithReminders(int userId) {
        List<DiaryEntry> entries = new ArrayList<>();
        String query = "SELECT id, title, content, created_at, image_path, reminder_days FROM diary_entries WHERE user_id = ? AND reminder_days > 0";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String content = rs.getString("content");
                String date = rs.getString("created_at");
                String imagePath = rs.getString("image_path");
                int reminderDays = rs.getInt("reminder_days");
                List<Integer> taggedUserIds = getTaggedUserIdsForEntry(id);
                DiaryEntry entry = new DiaryEntry(id, title, content, date, imagePath, taggedUserIds);
                entry.setReminderDays(reminderDays);
                entries.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

    public static boolean updateDiaryEntryReminderDays(int diaryId, int reminderDays) {
        String query = "UPDATE diary_entries SET reminder_days = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, reminderDays);
            stmt.setInt(2, diaryId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static UserProfile getUserProfile(int userId) {
        String query = "SELECT id, username, email, address, phone, full_name FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                String address = rs.getString("address");
                String phone = rs.getString("phone");
                String fullName = rs.getString("full_name");
                return new UserProfile(id, username, email, address, phone, fullName);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean updateUserProfile(int userId, String username, String email, String phone, String address, String fullName) {
        String query = "UPDATE users SET username = ?, email = ?, phone = ?, address = ?, full_name = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setString(4, address);
            stmt.setString(5, fullName);
            stmt.setInt(6, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean verifyUserForPasswordReset(String username, String phone) {
        // TODO: Implement actual database logic to verify if the username and phone number match a user
        String query = "SELECT COUNT(*) FROM users WHERE username = ? AND phone = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, phone);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("DatabaseManager: User verified for password reset - Username: " + username);
                return true;
            } else {
                System.out.println("DatabaseManager: User verification failed - Username: " + username);
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updatePassword(String username, String newPassword) {
        // TODO: Implement actual database logic to update the password for the user with the given username.

        String query = "UPDATE users SET password = ? WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newPassword);
            stmt.setString(2, username);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("DatabaseManager: Password updated successfully for user: " + username);
                return true;
            } else {
                System.out.println("DatabaseManager: Password update failed for user: " + username);
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean updatePassword(int currentUserId, String newPassword) {
        return false;
    }

    public static List<UserProfile> getAllUsers() {
        List<UserProfile> users = new ArrayList<>();
        String query = "SELECT id, username, email, phone, address, full_name FROM users";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                UserProfile user = new UserProfile(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("address"),
                    rs.getString("full_name")
                );
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return users;
    }

    public static List<DiaryEntry> getAllPrivateDiaries() {
        List<DiaryEntry> entries = new ArrayList<>();
        String query = "SELECT * FROM diary_entries de WHERE NOT EXISTS (SELECT 1 FROM diary_entry_tagged_users detu WHERE detu.diary_entry_id = de.id) ORDER BY created_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                DiaryEntry entry = new DiaryEntry(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getString("created_at"),
                    rs.getString("image_path")
                );
                entry.setUserId(rs.getInt("user_id"));
                entries.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

    public static List<DiaryEntry> getAllSharedDiaries() {
        List<DiaryEntry> entries = new ArrayList<>();
        String query = "SELECT * FROM diary_entries de WHERE EXISTS (SELECT 1 FROM diary_entry_tagged_users detu WHERE detu.diary_entry_id = de.id) ORDER BY created_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                DiaryEntry entry = new DiaryEntry(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getString("created_at"),
                    rs.getString("image_path")
                );
                entry.setUserId(rs.getInt("user_id"));
                entries.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

    public static boolean deleteUser(int userId) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Start transaction

            String deleteSettingsSql = "DELETE FROM user_settings WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteSettingsSql)) {
                pstmt.setInt(1, userId);
                pstmt.executeUpdate();
            }

            String deleteNotificationsSql = "DELETE FROM notifications WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteNotificationsSql)) {
                pstmt.setInt(1, userId);
                pstmt.executeUpdate();
            }

            String deleteTaggedUsersSql = "DELETE FROM diary_entry_tagged_users WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteTaggedUsersSql)) {
                pstmt.setInt(1, userId);
                pstmt.executeUpdate();
            }

            String deleteFriendsSql = "DELETE FROM friends WHERE user_id = ? OR friend_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteFriendsSql)) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, userId);
                pstmt.executeUpdate();
            }

            String deleteFriendRequestsSql = "DELETE FROM friend_requests WHERE sender_id = ? OR receiver_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteFriendRequestsSql)) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, userId);
                pstmt.executeUpdate();
            }

            String deleteDiaryEntriesSql = "DELETE FROM diary_entries WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteDiaryEntriesSql)) {
                pstmt.setInt(1, userId);
                pstmt.executeUpdate();
            }

            String deleteUserSql = "DELETE FROM users WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteUserSql)) {
                pstmt.setInt(1, userId);
                int result = pstmt.executeUpdate();
                if (result > 0) {
                    conn.commit();
                    return true;
                }
            }

            conn.rollback();
            return false;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getPasswordByUsername(String username) {
        String query = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}