import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class MainWindow extends JFrame {
    // Store user preferences
    private String prefGender = "Any";
    private int prefAge = 24;
    private String prefInterests = "";

    public void setUserPreferences(String gender, int age, String interests) {
        this.prefGender = gender;
        this.prefAge = age;
        this.prefInterests = interests;
    }

    public String getPrefGender() { return prefGender; }
    public int getPrefAge() { return prefAge; }
    public String getPrefInterests() { return prefInterests; }
    private CardLayout cardLayout;
    private JPanel mainPanel;
    // Store registered users: email -> password
    private HashMap<String, String> users = new HashMap<>();
    // Store logged-in user
    private String loggedInEmail = null;
    // Track if profile is completed
    private boolean profileCompleted = false;

    public MainWindow() {
        setTitle("CampusCupid - Java Swing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);

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
        if (users.containsKey(email)) return false;
        users.put(email, password);
        return true;
    }

    // Login logic
    public boolean loginUser(String email, String password) {
        if (users.containsKey(email) && users.get(email).equals(password)) {
            loggedInEmail = email;
            return true;
        }
        return false;
    }

    public String getLoggedInEmail() {
        return loggedInEmail;
    }

    public void logout() {
        loggedInEmail = null;
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
