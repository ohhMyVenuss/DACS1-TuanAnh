CREATE TABLE IF NOT EXISTS diary_entry_tagged_users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    diary_entry_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (diary_entry_id) REFERENCES diary_entries(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_tagged_user (diary_entry_id, user_id)
); 