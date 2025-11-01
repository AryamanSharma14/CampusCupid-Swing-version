import javax.swing.*;
import java.awt.*;

public class PreferencesPanel extends JPanel {
    public PreferencesPanel(MainWindow mainWindow) {
        setLayout(new BorderLayout(10,10));
        Color purple = new Color(102, 0, 204);
        Color lightPurple = new Color(230, 220, 255);
        setBackground(lightPurple);

        JPanel prefCard = new JPanel();
        prefCard.setLayout(new BoxLayout(prefCard, BoxLayout.Y_AXIS));
        prefCard.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        prefCard.setBackground(lightPurple);

        JLabel titleLabel = new JLabel("Set Your Preferences", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(purple);

        JLabel genderLabel = new JLabel("Preferred Gender:");
        genderLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        genderLabel.setForeground(purple);
        genderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    String[] genders = {"Any", "Male", "Female", "Other"};
        JComboBox<String> genderCombo = new JComboBox<>(genders);
        genderCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        genderCombo.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel ageLabel = new JLabel("Age Range:");
        ageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        ageLabel.setForeground(purple);
        ageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    // Load saved preferences
    String savedGender = mainWindow.getPrefGender();
    int savedMin = mainWindow.getPrefMinAge();
    int savedMax = mainWindow.getPrefMaxAge();
    String savedInterests = mainWindow.getPrefInterests();

    // Min / Max age spinners 18..60
    int initMin = (savedMin >= 18 && savedMin <= 60) ? savedMin : 18;
    int initMax = (savedMax >= 18 && savedMax <= 60 && savedMax >= initMin) ? savedMax : 24;
    JLabel minAgeLabel = new JLabel("Min Age:");
    minAgeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
    minAgeLabel.setForeground(purple);
    minAgeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    JSpinner minAgeSpinner = new JSpinner(new SpinnerNumberModel(initMin, 18, 60, 1));
    minAgeSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    JLabel maxAgeLabel = new JLabel("Max Age:");
    maxAgeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
    maxAgeLabel.setForeground(purple);
    maxAgeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    JSpinner maxAgeSpinner = new JSpinner(new SpinnerNumberModel(initMax, 18, 60, 1));
    maxAgeSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel interestsLabel = new JLabel("Interests:");
        interestsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        interestsLabel.setForeground(purple);
        interestsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField interestsField = new JTextField(20);
        interestsField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        interestsField.setAlignmentX(Component.CENTER_ALIGNMENT);
        interestsField.setHorizontalAlignment(JTextField.CENTER);

        // Set initial control values from saved prefs
        if (savedGender != null) {
            genderCombo.setSelectedItem(savedGender);
        }
        interestsField.setText(savedInterests != null ? savedInterests : "");

    JButton saveButton = new JButton("Save Preferences");
    saveButton.setBackground(purple);
    saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Arial", Font.BOLD, 16));
        saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton goToSwipeButton = new JButton("Go to Swipe");
    goToSwipeButton.setBackground(Color.WHITE);
    goToSwipeButton.setForeground(purple);
        goToSwipeButton.setFont(new Font("Arial", Font.BOLD, 16));
        goToSwipeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel messageLabel = new JLabel("");
        messageLabel.setForeground(new Color(0, 153, 51));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 14));

        prefCard.add(titleLabel);
        prefCard.add(Box.createVerticalStrut(20));
        prefCard.add(genderLabel);
        prefCard.add(genderCombo);
        prefCard.add(Box.createVerticalStrut(10));
        prefCard.add(ageLabel);
    prefCard.add(minAgeLabel);
    prefCard.add(minAgeSpinner);
    prefCard.add(Box.createVerticalStrut(8));
    prefCard.add(maxAgeLabel);
    prefCard.add(maxAgeSpinner);
        prefCard.add(Box.createVerticalStrut(10));
        prefCard.add(interestsLabel);
        prefCard.add(interestsField);
        prefCard.add(Box.createVerticalStrut(20));
        prefCard.add(saveButton);
        prefCard.add(Box.createVerticalStrut(10));
        prefCard.add(goToSwipeButton);
        prefCard.add(Box.createVerticalStrut(10));
        prefCard.add(messageLabel);

        add(prefCard, BorderLayout.CENTER);

        saveButton.addActionListener(e -> {
            String genderPref = (String) genderCombo.getSelectedItem();
            int minAge = (Integer) minAgeSpinner.getValue();
            int maxAge = (Integer) maxAgeSpinner.getValue();
            if (maxAge < minAge) { int tmp = minAge; minAge = maxAge; maxAge = tmp; }
            String interestsPref = interestsField.getText().trim().toLowerCase();
            mainWindow.setUserPreferences(genderPref, minAge, maxAge, interestsPref);
            messageLabel.setText("Preferences saved! Redirecting to Swipe...");
            mainWindow.showScreen("swipe");
        });
        goToSwipeButton.addActionListener(e -> {
            String genderPref = (String) genderCombo.getSelectedItem();
            int minAge = (Integer) minAgeSpinner.getValue();
            int maxAge = (Integer) maxAgeSpinner.getValue();
            if (maxAge < minAge) { int tmp = minAge; minAge = maxAge; maxAge = tmp; }
            String interestsPref = interestsField.getText().trim().toLowerCase();
            mainWindow.setUserPreferences(genderPref, minAge, maxAge, interestsPref);
            mainWindow.showScreen("swipe");
        });
    }
}
