
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SwipePanel extends JPanel {
    
    private static final Color purple = new Color(128, 0, 128);
    private static final Color lightPurple = new Color(230, 210, 255);
    private final MainWindow mainWindow;
    private ArrayList<Candidate> candidates;
    private int currentIndex = 0;
    private JLabel nameLabel, imageLabel, bioLabel;
    private JTextArea bioArea;
    private JButton likeButton, passButton, backButton;
    private JLabel matchLabel;

    public SwipePanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;

    setLayout(new BorderLayout(10,10));

    loadCandidates(mainWindow);

        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        cardPanel.setBackground(lightPurple);
        cardPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

    imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(100,100));
        imageLabel.setMaximumSize(new Dimension(100,100));
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(180, 140, 255));
        imageLabel.setFont(new Font("Arial", Font.BOLD, 48));
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        nameLabel = new JLabel("", SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 22));
        nameLabel.setForeground(purple);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        bioLabel = new JLabel("Bio:");
        bioLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        bioLabel.setForeground(purple);
        bioLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bioArea = new JTextArea(3, 20);
        bioArea.setEditable(false);
        bioArea.setLineWrap(true);
        bioArea.setWrapStyleWord(true);
        bioArea.setFont(new Font("Arial", Font.PLAIN, 15));
        bioArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        bioArea.setBackground(Color.WHITE);
        bioArea.setAlignmentX(Component.CENTER_ALIGNMENT);

    likeButton = new JButton("Like ❤");
        likeButton.setBackground(purple);
        likeButton.setForeground(Color.WHITE);
        likeButton.setFont(new Font("Arial", Font.BOLD, 16));
        likeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    passButton = new JButton("Pass ✕");
        passButton.setBackground(Color.WHITE);
        passButton.setForeground(purple);
        passButton.setFont(new Font("Arial", Font.BOLD, 16));
        passButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton = new JButton("Back to Profile");
        backButton.setBackground(Color.WHITE);
        backButton.setForeground(purple);
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    JButton chatsButton = new JButton("Go to Chats");
    chatsButton.setBackground(purple);
    chatsButton.setForeground(Color.WHITE);
    chatsButton.setFont(new Font("Arial", Font.BOLD, 16));
    chatsButton.setAlignmentX(Component.CENTER_ALIGNMENT);

    JButton refreshButton = new JButton("Refresh Candidates");
    refreshButton.setBackground(new Color(0, 153, 51));
    refreshButton.setForeground(Color.WHITE);
    refreshButton.setFont(new Font("Arial", Font.BOLD, 16));
    refreshButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(lightPurple);
    buttonPanel.add(likeButton);
    buttonPanel.add(passButton);
    buttonPanel.add(backButton);
    buttonPanel.add(chatsButton);
    buttonPanel.add(refreshButton);

        matchLabel = new JLabel("", SwingConstants.CENTER);
        matchLabel.setFont(new Font("Arial", Font.BOLD, 16));
        matchLabel.setForeground(new Color(0, 153, 51));
        matchLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        cardPanel.add(imageLabel);
        cardPanel.add(Box.createVerticalStrut(15));
        cardPanel.add(nameLabel);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(bioLabel);
        cardPanel.add(bioArea);
        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(buttonPanel);

        add(cardPanel, BorderLayout.CENTER);
        add(matchLabel, BorderLayout.NORTH);

        updateCandidateCard();

        likeButton.addActionListener(e -> {
            if (currentIndex < candidates.size()) {
                Candidate c = candidates.get(currentIndex);
                boolean matched = Database.recordSwipe(mainWindow.getLoggedInUserId(), c.userId, true);
                matchLabel.setText(matched ? ("It's a match with " + c.name + "!") : ("You liked " + c.name + "!"));
                nextCandidate();
            }
        });
        passButton.addActionListener(e -> {
            if (currentIndex < candidates.size()) {
                Candidate c = candidates.get(currentIndex);
                Database.recordSwipe(mainWindow.getLoggedInUserId(), c.userId, false);
                matchLabel.setText("You passed " + c.name + ".");
                nextCandidate();
            }
        });
        backButton.addActionListener(e -> {
            mainWindow.showScreen("profile");
        });
        chatsButton.addActionListener(e -> {
            mainWindow.showScreen("chats");
        });
        refreshButton.addActionListener(e -> {
            reload();
            matchLabel.setText("Candidates refreshed!");
        });
    }

    private void loadCandidates(MainWindow mainWindow) {
        candidates = new ArrayList<>();
        Integer uid = mainWindow.getLoggedInUserId();
        if (uid == null) return;
        String prefGender = mainWindow.getPrefGender();
    int prefMinAge = mainWindow.getPrefMinAge();
    int prefMaxAge = mainWindow.getPrefMaxAge();
        String prefInterests = mainWindow.getPrefInterests();
    List<Map<String,Object>> rows = Database.listCandidates(uid, prefGender, prefMinAge, prefMaxAge, prefInterests);
        for (Map<String,Object> r : rows) {
            Integer age = (Integer) r.get("age");
            candidates.add(new Candidate(
                (Integer) r.get("id"),
                (String) r.get("name"),
                (String) r.get("gender"),
                age == null ? 0 : age,
                (String) r.get("interests"),
                (String) r.get("bio"),
                (String) r.get("photoUrl")
            ));
        }
        currentIndex = 0;
    }

    // Reload candidates based on current preferences
    public void reload() {
        loadCandidates(mainWindow);
        updateCandidateCard();
    }

    private void updateCandidateCard() {
        if (currentIndex < candidates.size()) {
            Candidate c = candidates.get(currentIndex);
            nameLabel.setText(c.name + " (" + c.gender + ", Age: " + c.age + ")");
            if (c.image != null && !c.image.isBlank()) {
                try {
                    java.net.URL u = new java.net.URI(c.image).toURL();
                    ImageIcon icon = new ImageIcon(u);
                    Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(img));
                    imageLabel.setText("");
                } catch(Exception ex) {
                    imageLabel.setIcon(null);
                    imageLabel.setText("🧑");
                }
            } else {
                imageLabel.setIcon(null);
                imageLabel.setText("🧑");
            }
            bioArea.setText(c.bio + "\nInterests: " + c.interests);
            matchLabel.setText("");
            likeButton.setEnabled(true);
            passButton.setEnabled(true);
        } else {
            nameLabel.setText("");
            imageLabel.setText("");
            bioArea.setText("");
            matchLabel.setText("No more candidates! Check back later.");
            likeButton.setEnabled(false);
            passButton.setEnabled(false);
        }
    }

    private void nextCandidate() {
        currentIndex++;
        updateCandidateCard();
    }

    // Detailed candidate data class
    static class Candidate {
        int userId;
        String name, gender, interests, bio, image;
        int age;
        Candidate(int userId, String name, String gender, int age, String interests, String bio, String image) {
            this.userId = userId;
            this.name = name;
            this.gender = gender;
            this.age = age;
            this.interests = interests;
            this.bio = bio;
            this.image = image;
        }
    }
}
