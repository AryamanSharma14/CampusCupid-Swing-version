import javax.swing.*;
import java.awt.*;

public class ProfilePanel extends JPanel {
    // Keep references to controls for refresh
    private final MainWindow mainWindow;
    private JLabel avatar;
    private JTextField nameField;
    private JComboBox<String> genderCombo;
    private JSpinner ageSpinner;
    private JTextField photoField;
    private JTextArea bioArea;
    private JTextField interestsField;
    private JTextField hobbiesField;
    private JTextField occupationField;
    private JLabel messageLabel;

    public ProfilePanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout(10,10));

        Color purple = new Color(102, 0, 204);
        Color lightPurple = new Color(230, 220, 255);
        setBackground(lightPurple);

        JPanel profileCard = new JPanel();
        profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));
        profileCard.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        profileCard.setBackground(lightPurple);

    avatar = new JLabel();
        avatar.setPreferredSize(new Dimension(100,100));
        avatar.setMaximumSize(new Dimension(100,100));
        avatar.setOpaque(true);
        avatar.setBackground(new Color(180, 140, 255));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setText("👤");
        avatar.setFont(new Font("Arial", Font.BOLD, 48));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Your Profile", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(purple);

    JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        nameLabel.setForeground(purple);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    nameField = new JTextField(20);
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        nameField.setHorizontalAlignment(JTextField.CENTER);

    JLabel genderLabel = new JLabel("Your Gender:");
    genderLabel.setFont(new Font("Arial", Font.PLAIN, 16));
    genderLabel.setForeground(purple);
    genderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    String[] genders = {"Male", "Female", "Other"};
    genderCombo = new JComboBox<>(genders);
    genderCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    genderCombo.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel ageLabel = new JLabel("Your Age:");
    ageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
    ageLabel.setForeground(purple);
    ageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    ageSpinner = new JSpinner(new SpinnerNumberModel(21, 18, 60, 1));
    ageSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

    JLabel photoLabel = new JLabel("Photo URL:");
    photoLabel.setFont(new Font("Arial", Font.PLAIN, 16));
    photoLabel.setForeground(purple);
    photoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    photoField = new JTextField(20);
    photoField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    photoField.setHorizontalAlignment(JTextField.CENTER);

    

    JLabel bioLabel = new JLabel("Bio:");
    bioLabel.setFont(new Font("Arial", Font.PLAIN, 16));
    bioLabel.setForeground(purple);
    bioLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    bioArea = new JTextArea(3, 20);
    bioArea.setLineWrap(true);
    bioArea.setWrapStyleWord(true);
    bioArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
    bioArea.setFont(new Font("Arial", Font.PLAIN, 15));
    bioArea.setBackground(Color.WHITE);
    bioArea.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel interestsLabel = new JLabel("Interests:");
    interestsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
    interestsLabel.setForeground(purple);
    interestsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    interestsField = new JTextField(20);
    interestsField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    interestsField.setHorizontalAlignment(JTextField.CENTER);

    JLabel hobbiesLabel = new JLabel("Hobbies:");
    hobbiesLabel.setFont(new Font("Arial", Font.PLAIN, 16));
    hobbiesLabel.setForeground(purple);
    hobbiesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    hobbiesField = new JTextField(20);
    hobbiesField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    hobbiesField.setHorizontalAlignment(JTextField.CENTER);

    JLabel occupationLabel = new JLabel("Occupation:");
    occupationLabel.setFont(new Font("Arial", Font.PLAIN, 16));
    occupationLabel.setForeground(purple);
    occupationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    occupationField = new JTextField(20);
    occupationField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    occupationField.setHorizontalAlignment(JTextField.CENTER);

    // Initial prefill if user already logged in
    refresh();

    

    JButton saveButton = new JButton("Save Profile");
    saveButton.setBackground(purple);
    saveButton.setForeground(Color.WHITE);
    saveButton.setFont(new Font("Arial", Font.BOLD, 16));
    saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    JButton goToPreferencesButton = new JButton("Go to Preferences");
    goToPreferencesButton.setBackground(Color.WHITE);
    goToPreferencesButton.setForeground(purple);
    goToPreferencesButton.setFont(new Font("Arial", Font.BOLD, 16));
    goToPreferencesButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    messageLabel = new JLabel("");
    messageLabel.setForeground(new Color(0, 153, 51));
    messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    messageLabel.setFont(new Font("Arial", Font.BOLD, 14));

        profileCard.add(avatar);
        profileCard.add(Box.createVerticalStrut(15));
        profileCard.add(titleLabel);
        profileCard.add(Box.createVerticalStrut(15));
        profileCard.add(nameLabel);
        profileCard.add(nameField);
        profileCard.add(Box.createVerticalStrut(10));
    profileCard.add(genderLabel);
    profileCard.add(genderCombo);
    profileCard.add(Box.createVerticalStrut(10));
    profileCard.add(ageLabel);
    profileCard.add(ageSpinner);
    profileCard.add(Box.createVerticalStrut(10));
    profileCard.add(photoLabel);
    profileCard.add(photoField);
    profileCard.add(Box.createVerticalStrut(10));
    profileCard.add(bioLabel);
    profileCard.add(bioArea);
    profileCard.add(Box.createVerticalStrut(10));
    profileCard.add(interestsLabel);
    profileCard.add(interestsField);
    profileCard.add(Box.createVerticalStrut(10));
    profileCard.add(hobbiesLabel);
    profileCard.add(hobbiesField);
    profileCard.add(Box.createVerticalStrut(10));
    profileCard.add(occupationLabel);
    profileCard.add(occupationField);
    profileCard.add(Box.createVerticalStrut(10));
    profileCard.add(Box.createVerticalStrut(20));
    profileCard.add(saveButton);
    profileCard.add(Box.createVerticalStrut(10));
    profileCard.add(goToPreferencesButton);
    profileCard.add(Box.createVerticalStrut(10));
    profileCard.add(messageLabel);

        add(profileCard, BorderLayout.CENTER);

        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String bio = bioArea.getText().trim();
            if (name.isEmpty() || bio.isEmpty()) {
                messageLabel.setText("Name and Bio cannot be empty");
                messageLabel.setForeground(Color.RED);
            } else {
                // Persist to database
                Integer uid = mainWindow.getLoggedInUserId();
                if (uid != null) {
                    Integer age = (Integer) ageSpinner.getValue();
                    Database.upsertProfile(uid, name, (String)genderCombo.getSelectedItem(), age, bio, interestsField.getText().trim(), hobbiesField.getText().trim(), occupationField.getText().trim(), photoField.getText().trim());
                    // Try to preview avatar
                    String url = photoField.getText().trim();
                    if (!url.isEmpty()) {
                        try {
                            java.net.URL u = new java.net.URI(url).toURL();
                            ImageIcon icon = new ImageIcon(u);
                            Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                            avatar.setText("");
                            avatar.setIcon(new ImageIcon(img));
                        } catch(Exception ex2) {
                            avatar.setIcon(null);
                            avatar.setText("👤");
                        }
                    }
                }
                messageLabel.setText("Profile saved! Go to Preferences");
                messageLabel.setForeground(new Color(0, 153, 51));
                mainWindow.setProfileCompleted(true);
            }
        });
        goToPreferencesButton.addActionListener(e -> {
            if (mainWindow.isProfileCompleted()) {
                mainWindow.showScreen("preferences");
            } else {
                messageLabel.setText("Please save your profile first.");
                messageLabel.setForeground(Color.RED);
            }
        });
    }

    // Reload the form fields from the database for the logged-in user
    public void refresh() {
        if (mainWindow == null) return;
        Integer uid = mainWindow.getLoggedInUserId();
        if (uid == null) return;
        java.util.Map<String,Object> prof = Database.getProfile(uid);
        if (prof != null && !prof.isEmpty()) {
            if (prof.get("name") != null) nameField.setText((String)prof.get("name"));
            if (prof.get("gender") != null) genderCombo.setSelectedItem((String)prof.get("gender"));
            if (prof.get("age") != null) ageSpinner.setValue((Integer)prof.get("age"));
            if (prof.get("bio") != null) bioArea.setText((String)prof.get("bio"));
            if (prof.get("interests") != null) interestsField.setText((String)prof.get("interests"));
            if (prof.get("hobbies") != null) hobbiesField.setText((String)prof.get("hobbies"));
            if (prof.get("occupation") != null) occupationField.setText((String)prof.get("occupation"));
            String purl = (String) prof.get("photoUrl");
            photoField.setText(purl == null ? "" : purl);
            if (purl != null && !purl.isBlank()) {
                try {
                    java.net.URL u = new java.net.URI(purl).toURL();
                    ImageIcon icon = new ImageIcon(u);
                    Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    avatar.setText("");
                    avatar.setIcon(new ImageIcon(img));
                } catch(Exception ignore) {
                    avatar.setIcon(null);
                    avatar.setText("👤");
                }
            } else {
                avatar.setIcon(null);
                avatar.setText("👤");
            }
        }
        if (messageLabel != null) {
            messageLabel.setText("");
        }
    }
}
