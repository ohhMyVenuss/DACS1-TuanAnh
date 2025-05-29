import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class NewDiaryEntryDialog extends JDialog {
    private JTextField titleField;
    private JTextArea contentArea;
    private JButton saveButton;
    private JButton cancelButton;
    private boolean saved = false;
    private String title;
    private String content;

    public NewDiaryEntryDialog(Frame parent) {
        super(parent, "Viết nhật ký mới", true);
        setSize(600, 500);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        initializeComponents();
    }

    private void initializeComponents() {

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(255, 255, 255));

        JPanel titlePanel = new JPanel(new BorderLayout(5, 0));
        titlePanel.setBackground(new Color(255, 255, 255));
        
        JLabel titleLabel = new JLabel("Tiêu đề:");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        titleField = new JTextField();
        titleField.setFont(new Font("Arial", Font.PLAIN, 14));
        titleField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(titleField, BorderLayout.CENTER);

        JPanel contentPanel = new JPanel(new BorderLayout(5, 0));
        contentPanel.setBackground(new Color(255, 255, 255));
        
        JLabel contentLabel = new JLabel("Nội dung:");
        contentLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        contentArea = new JTextArea();
        contentArea.setFont(new Font("Arial", Font.PLAIN, 14));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        JScrollPane scrollPane = new JScrollPane(contentArea);
        scrollPane.setPreferredSize(new Dimension(0, 300));
        
        contentPanel.add(contentLabel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.setBackground(new Color(255, 255, 255));
        
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        JLabel dateLabel = new JLabel("Ngày: " + currentDate);
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(100, 100, 100));
        
        datePanel.add(dateLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(255, 255, 255));
        
        saveButton = new JButton("Lưu");
        saveButton.setFont(new Font("Arial", Font.BOLD, 12));
        saveButton.setBackground(new Color(70, 130, 180));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBorderPainted(false);
        saveButton.setFocusPainted(false);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        cancelButton = new JButton("Hủy");
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 12));
        cancelButton.setBackground(new Color(240, 240, 240));
        cancelButton.setBorderPainted(false);
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        saveButton.addActionListener(e -> saveEntry());
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(saveButton);

        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(datePanel, BorderLayout.SOUTH);
        
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void saveEntry() {
        title = titleField.getText().trim();
        content = contentArea.getText().trim();
        
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập tiêu đề!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập nội dung!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
} 