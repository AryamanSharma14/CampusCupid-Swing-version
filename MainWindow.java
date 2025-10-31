import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class MainWindow extends JFrame {
    // Store user preferences
    private String prefGender = "Any";
    private int prefAge = 24;
    private String prefInterests = "";
    private Integer loggedInUserId = null;

    public void setUserPreferences(String gender, int age, String interests) {
        this.prefGender = gender;
        this.prefAge = age;
        this.prefInterests = interests;
        if (loggedInUserId != null) {
            Database.upsertPreferences(loggedInUserId, gender, age, interests);
        }
    }

    public String getPrefGender() { return prefGender; }
    public int getPrefAge() { return prefAge; }
    public String getPrefInterests() { return prefInterests; }
    private CardLayout cardLayout;
    private JPanel mainPanel;
    // Store logged-in user
    private String loggedInEmail = null;
    // Track if profile is completed
    private boolean profileCompleted = false;

    public MainWindow() {
        setTitle("CampusCupid - Java Swing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);

        // Improve Look & Feel (Nimbus) if available
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        // Initialize database schema
        Database.init();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Add screens
    mainPanel.add(new LoginPanel(this), "login");
    mainPanel.add(new RegistrationPanel(this), "register");
    mainPanel.add(new ProfilePanel(this), "profile");
    mainPanel.add(new PreferencesPanel(this), "preferences");
    mainPanel.add(new SwipePanel(this), "swipe");
    mainPanel.add(new ChatsPanel(this), "chats");

        add(mainPanel);
        showScreen("login");
    }

    public void showScreen(String name) {
        // Restrict access to profile/preferences/swipe/chats if not logged in
        if ((name.equals("profile") || name.equals("preferences") || name.equals("swipe") || name.equals("chats")) && loggedInEmail == null) {
            JOptionPane.showMessageDialog(this, "Please log in first.");
            cardLayout.show(mainPanel, "login");
            return;
        }
        // Restrict preferences/swipe/chats if profile not completed
        if ((name.equals("preferences") || name.equals("swipe") || name.equals("chats")) && !profileCompleted) {
            JOptionPane.showMessageDialog(this, "Please complete your profile first.");
            cardLayout.show(mainPanel, "profile");
            return;
        }
        // After profile, go to preferences before swipe
        // Only redirect to preferences if coming directly from profile
        if (name.equals("swipe") && profileCompleted && cardLayout != null) {
            // Check if current screen is profile, then redirect to preferences
            // Otherwise, allow swipe
            java.awt.Component current = null;
            for (java.awt.Component comp : mainPanel.getComponents()) {
                if (comp.isVisible()) {
                    current = comp;
                    break;
                }
            }
            if (current != null && current.getName() != null && current.getName().equals("profile")) {
                cardLayout.show(mainPanel, "preferences");
                return;
            }
        }
        cardLayout.show(mainPanel, name);
    }

    // Registration logic
    public boolean registerUser(String email, String password) {
        // Use database-backed registration
        return Database.registerUser(email, password);
    }

    // Login logic
    public boolean loginUser(String email, String password) {
        Integer uid = Database.loginUser(email, password);
        if (uid != null) {
            loggedInEmail = email;
            loggedInUserId = uid;
            // Load saved preferences for this user, if any
            Map<String, Object> p = Database.getPreferences(uid);
            if (p.get("gender") != null) this.prefGender = (String) p.get("gender");
            if (p.get("age") != null) this.prefAge = (Integer) p.get("age");
            if (p.get("interests") != null) this.prefInterests = (String) p.get("interests");
            return true;
        }
        return false;
    }

    public String getLoggedInEmail() {
        return loggedInEmail;
    }

    public Integer getLoggedInUserId() {
        return loggedInUserId;
    }

    public void logout() {
        loggedInEmail = null;
        loggedInUserId = null;
        profileCompleted = false;
        showScreen("login");
    }

    public void setProfileCompleted(boolean completed) {
        profileCompleted = completed;
    }

    public boolean isProfileCompleted() {
        return profileCompleted;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainWindow().setVisible(true);
        });
    }
}
