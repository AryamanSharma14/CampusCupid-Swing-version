import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class MainWindow extends JFrame {
    // Store user preferences
    private String prefGender = "Any";
    private int prefMinAge = 18;
    private int prefMaxAge = 60;
    private String prefInterests = "";
    private Integer loggedInUserId = null;

    public void setUserPreferences(String gender, int minAge, int maxAge, String interests) {
        this.prefGender = gender;
        this.prefMinAge = minAge;
        this.prefMaxAge = maxAge;
        this.prefInterests = interests;
        if (loggedInUserId != null) {
            Database.upsertPreferences(loggedInUserId, gender, minAge, maxAge, interests);
        }
    }

    public String getPrefGender() { return prefGender; }
    public int getPrefMinAge() { return prefMinAge; }
    public int getPrefMaxAge() { return prefMaxAge; }
    public String getPrefInterests() { return prefInterests; }
    private CardLayout cardLayout;
    private JPanel mainPanel;
    // Keep references to panels so we can refresh data when shown
    private ProfilePanel profilePanel;
    private PreferencesPanel preferencesPanel;
    private SwipePanel swipePanel;
    private ChatsPanel chatsPanel;
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

        // Ask for optional server URL for multi-laptop demo
        String url = JOptionPane.showInputDialog(this, "Server URL (leave blank for local DB):", "http://localhost:8080");
        if (url != null && !url.trim().isEmpty()) {
            Database.setRemoteBaseUrl(url.trim());
        }
    // Initialize database schema (used by local mode and also safe on server)
    Database.init();
    // Ensure there are candidates even in local mode; safe no-op if already seeded
    Database.seedDemoUsers();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Add screens
        mainPanel.add(new LoginPanel(this), "login");
        mainPanel.add(new RegistrationPanel(this), "register");
        profilePanel = new ProfilePanel(this);
        profilePanel.setName("profile");
        mainPanel.add(profilePanel, "profile");
        preferencesPanel = new PreferencesPanel(this);
        preferencesPanel.setName("preferences");
        mainPanel.add(preferencesPanel, "preferences");
        swipePanel = new SwipePanel(this);
        swipePanel.setName("swipe");
        mainPanel.add(swipePanel, "swipe");
        chatsPanel = new ChatsPanel(this);
        chatsPanel.setName("chats");
        mainPanel.add(chatsPanel, "chats");

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
        // If navigating to profile after login, refresh its fields from DB
        if (name.equals("profile") && profilePanel != null) {
            profilePanel.refresh();
        }
        // If navigating to swipe, ensure it reloads candidates from current preferences
        if (name.equals("swipe") && swipePanel != null) {
            swipePanel.reload();
        }
        cardLayout.show(mainPanel, name);
    }

    // Registration logic
    public boolean registerUser(String email, String password, String name) {
        // Use database-backed registration
        boolean ok = Database.registerUser(email, password);
        if (ok) {
            Integer uid = Database.getUserIdByEmail(email);
            if (uid != null) {
                // Seed profile with name so candidates show display name even before profile save
                Database.upsertProfile(uid, name, null, null, "", "", "", "", "");
            }
        }
        return ok;
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
            if (p.get("minAge") != null) this.prefMinAge = (Integer) p.get("minAge");
            if (p.get("maxAge") != null) this.prefMaxAge = (Integer) p.get("maxAge");
            if (p.get("interests") != null) this.prefInterests = (String) p.get("interests");
            if (Database.hasProfile(uid)) this.profileCompleted = true;
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
