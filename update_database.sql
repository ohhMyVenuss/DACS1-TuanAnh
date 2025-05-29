-- Thêm cột reminder_days vào bảng diary_entries
ALTER TABLE diary_entries ADD COLUMN reminder_days INT DEFAULT 0;

-- Tạo bảng notifications để lưu thông báo
CREATE TABLE notifications (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    message VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Tạo bảng user_settings để lưu cài đặt người dùng
CREATE TABLE user_settings (
    user_id INT PRIMARY KEY,
    notifications_enabled BOOLEAN DEFAULT TRUE,
    theme VARCHAR(20) DEFAULT 'light',
    language VARCHAR(10) DEFAULT 'vi',
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Thêm trigger để tự động tạo bản ghi user_settings khi có người dùng mới
DELIMITER //
CREATE TRIGGER after_user_insert
AFTER INSERT ON users
FOR EACH ROW
BEGIN
    INSERT INTO user_settings (user_id) VALUES (NEW.id);
END//
DELIMITER ; 