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

        JLabel ageLabel = new JLabel("Preferred Age:");
        ageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        ageLabel.setForeground(purple);
        ageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    // Load saved preferences
    String savedGender = mainWindow.getPrefGender();
    int savedAge = mainWindow.getPrefAge();
    String savedInterests = mainWindow.getPrefInterests();

    // Age slider 18..60, default 24
    int initialAge = (savedAge >= 18 && savedAge <= 60) ? savedAge : 24;
    JSlider ageSlider = new JSlider(18, 60, initialAge);
        ageSlider.setMajorTickSpacing(6);
        ageSlider.setMinorTickSpacing(1);
        ageSlider.setPaintTicks(true);
        ageSlider.setPaintLabels(true);
        ageSlider.setBackground(lightPurple);
        ageSlider.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel ageValue = new JLabel("Selected: " + initialAge + " years");
        ageValue.setFont(new Font("Arial", Font.PLAIN, 14));
        ageValue.setForeground(purple);
        ageValue.setAlignmentX(Component.CENTER_ALIGNMENT);

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
    prefCard.add(ageSlider);
    prefCard.add(ageValue);
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

        // Live update label as slider moves
        ageSlider.addChangeListener(e -> {
            ageValue.setText("Selected: " + ageSlider.getValue() + " years");
        });

        saveButton.addActionListener(e -> {
            String genderPref = (String) genderCombo.getSelectedItem();
            int agePref = ageSlider.getValue();
            String interestsPref = interestsField.getText().trim().toLowerCase();
            mainWindow.setUserPreferences(genderPref, agePref, interestsPref);
            messageLabel.setText("Preferences saved! Redirecting to Swipe...");
            mainWindow.showScreen("swipe");
        });
        goToSwipeButton.addActionListener(e -> {
            String genderPref = (String) genderCombo.getSelectedItem();
            int agePref = ageSlider.getValue();
            String interestsPref = interestsField.getText().trim().toLowerCase();
            mainWindow.setUserPreferences(genderPref, agePref, interestsPref);
            mainWindow.showScreen("swipe");
        });
    }
}
