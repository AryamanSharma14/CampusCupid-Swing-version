
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginPanel extends JPanel {
    public LoginPanel(MainWindow mainWindow) {
        setLayout(new BorderLayout(10,10));

        Color purple = new Color(102, 0, 204);
        Color lightPurple = new Color(230, 220, 255);

        JPanel loginCard = new JPanel();
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));
        loginCard.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        loginCard.setBackground(lightPurple);

        // Avatar/photo at the top
        JLabel avatar = new JLabel();
        avatar.setPreferredSize(new Dimension(100,100));
        avatar.setMaximumSize(new Dimension(100,100));
        avatar.setOpaque(true);
        avatar.setBackground(new Color(180, 140, 255));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setText("👤");
        avatar.setFont(new Font("Arial", Font.BOLD, 48));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("CampusCupid Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(purple);

        JLabel emailLabel = new JLabel("SRM Email:");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        emailLabel.setForeground(purple);
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField emailField = new JTextField(20);
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        emailField.setBackground(Color.WHITE);
        emailField.setFont(new Font("Arial", Font.PLAIN, 15));
        emailField.setHorizontalAlignment(JTextField.CENTER);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordLabel.setForeground(purple);
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        passwordField.setBackground(Color.WHITE);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 15));
        passwordField.setHorizontalAlignment(JTextField.CENTER);

        JButton loginButton = new JButton("Login");
        loginButton.setBackground(purple);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton signupButton = new JButton("Sign Up");
        signupButton.setBackground(Color.WHITE);
        signupButton.setForeground(purple);
        signupButton.setFont(new Font("Arial", Font.BOLD, 16));
        signupButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel messageLabel = new JLabel("");
        messageLabel.setForeground(new Color(0, 153, 51));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 14));

        loginCard.add(avatar);
        loginCard.add(Box.createVerticalStrut(15));
        loginCard.add(titleLabel);
        loginCard.add(Box.createVerticalStrut(25));
        loginCard.add(emailLabel);
        loginCard.add(emailField);
        loginCard.add(Box.createVerticalStrut(15));
        loginCard.add(passwordLabel);
        loginCard.add(passwordField);
        loginCard.add(Box.createVerticalStrut(25));
        loginCard.add(loginButton);
        loginCard.add(Box.createVerticalStrut(10));
        loginCard.add(signupButton);
        loginCard.add(Box.createVerticalStrut(15));
        loginCard.add(messageLabel);

        add(loginCard, BorderLayout.CENTER);
        setBackground(lightPurple);

        loginButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            if (!email.endsWith("@srmist.edu.in")) {
                messageLabel.setText("Email must end with @srmist.edu.in");
                messageLabel.setForeground(Color.RED);
            } else if (password.isEmpty()) {
                messageLabel.setText("Password cannot be empty");
                messageLabel.setForeground(Color.RED);
            } else if (!mainWindow.loginUser(email, password)) {
                messageLabel.setText("Invalid email or password");
                messageLabel.setForeground(Color.RED);
            } else {
                messageLabel.setText("");
                mainWindow.showScreen("profile");
            }
        });
        signupButton.addActionListener(e -> {
            mainWindow.showScreen("register");
        });
    }
}
