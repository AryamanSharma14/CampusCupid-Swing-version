
import javax.swing.*;
import java.awt.*;

public class RegistrationPanel extends JPanel {
    public RegistrationPanel(MainWindow mainWindow) {
    Color purple = new Color(102, 0, 204);
    Color lightPurple = new Color(230, 220, 255);
    setBackground(lightPurple);

    JPanel regCard = new JPanel();
    regCard.setLayout(new BoxLayout(regCard, BoxLayout.Y_AXIS));
    regCard.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
    regCard.setBackground(lightPurple);

    JLabel avatar = new JLabel();
    avatar.setPreferredSize(new Dimension(100,100));
    avatar.setMaximumSize(new Dimension(100,100));
    avatar.setOpaque(true);
    avatar.setBackground(new Color(180, 140, 255));
    avatar.setHorizontalAlignment(SwingConstants.CENTER);
    avatar.setText("👤");
    avatar.setFont(new Font("Arial", Font.BOLD, 48));
    avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel titleLabel = new JLabel("CampusCupid Registration", SwingConstants.CENTER);
    titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
    titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    titleLabel.setForeground(purple);

    JLabel nameLabel = new JLabel("Full Name:");
    nameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
    nameLabel.setForeground(purple);
    nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    JTextField nameField = new JTextField(20);
    nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    nameField.setHorizontalAlignment(JTextField.CENTER);

    JLabel emailLabel = new JLabel("SRM Email:");
    emailLabel.setFont(new Font("Arial", Font.PLAIN, 16));
    emailLabel.setForeground(purple);
    emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    JTextField emailField = new JTextField(20);
    emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    emailField.setHorizontalAlignment(JTextField.CENTER);

    JLabel passwordLabel = new JLabel("Password:");
    passwordLabel.setFont(new Font("Arial", Font.PLAIN, 16));
    passwordLabel.setForeground(purple);
    passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    JPasswordField passwordField = new JPasswordField(20);
    passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    passwordField.setHorizontalAlignment(JTextField.CENTER);

    JButton registerButton = new JButton("Register");
    registerButton.setBackground(purple);
    registerButton.setForeground(Color.WHITE);
    registerButton.setFont(new Font("Arial", Font.BOLD, 16));
    registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    JButton backButton = new JButton("Back to Login");
    backButton.setBackground(Color.WHITE);
    backButton.setForeground(purple);
    backButton.setFont(new Font("Arial", Font.BOLD, 16));
    backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    JLabel messageLabel = new JLabel("");
    messageLabel.setForeground(new Color(0, 153, 51));
    messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    messageLabel.setFont(new Font("Arial", Font.BOLD, 14));

    regCard.add(avatar);
    regCard.add(Box.createVerticalStrut(15));
    regCard.add(titleLabel);
    regCard.add(Box.createVerticalStrut(20));
    regCard.add(nameLabel);
    regCard.add(nameField);
    regCard.add(Box.createVerticalStrut(10));
    regCard.add(emailLabel);
    regCard.add(emailField);
    regCard.add(Box.createVerticalStrut(10));
    regCard.add(passwordLabel);
    regCard.add(passwordField);
    regCard.add(Box.createVerticalStrut(20));
    regCard.add(registerButton);
    regCard.add(Box.createVerticalStrut(10));
    regCard.add(backButton);
    regCard.add(Box.createVerticalStrut(10));
    regCard.add(messageLabel);

    add(regCard, BorderLayout.CENTER);

        registerButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String fullName = nameField.getText().trim();
            if (!email.endsWith("@srmist.edu.in")) {
                messageLabel.setText("Email must end with @srmist.edu.in");
                messageLabel.setForeground(Color.RED);
            } else if (password.isEmpty()) {
                messageLabel.setText("Password cannot be empty");
                messageLabel.setForeground(Color.RED);
            } else if (fullName.isEmpty()) {
                messageLabel.setText("Full Name cannot be empty");
                messageLabel.setForeground(Color.RED);
            } else if (!mainWindow.registerUser(email, password, fullName)) {
                messageLabel.setText("Email already registered");
                messageLabel.setForeground(Color.RED);
            } else {
                messageLabel.setText("Registration successful!");
                messageLabel.setForeground(new Color(0, 153, 51));
                mainWindow.showScreen("login");
            }
        });
        backButton.addActionListener(e -> {
            mainWindow.showScreen("login");
        });
    }
}
