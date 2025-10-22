
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class SwipePanel extends JPanel {
    
    private static final Color purple = new Color(128, 0, 128);
    private static final Color lightPurple = new Color(230, 210, 255);
    private ArrayList<Candidate> allCandidates;
    private ArrayList<Candidate> candidates;
    private int currentIndex = 0;
    private JLabel nameLabel, imageLabel, bioLabel;
    private JTextArea bioArea;
    private JButton likeButton, passButton, backButton;
    private JLabel matchLabel;

    public SwipePanel(MainWindow mainWindow) {

    setLayout(new BorderLayout(10,10));

    
    allCandidates = new ArrayList<>();
    allCandidates.add(new Candidate("Alex", "Male", 21, "Music, Sports", "Outgoing and friendly.", "A"));
    allCandidates.add(new Candidate("Priya", "Female", 20, "Art, Reading", "Creative and thoughtful.", "P"));
    allCandidates.add(new Candidate("Sam", "Male", 22, "Tech, Gaming", "Loves coding and games.", "S"));
    allCandidates.add(new Candidate("Riya", "Female", 19, "Travel, Food", "Adventurous foodie.", "R"));
    allCandidates.add(new Candidate("Jordan", "Other", 23, "Movies, Writing", "Film buff and writer.", "J"));
    allCandidates.add(new Candidate("Neha", "Female", 21, "Photography, Baking", "Loves capturing moments and baking treats.", "N"));
    allCandidates.add(new Candidate("Aman", "Male", 22, "Football, Chess", "Strategic thinker and sports enthusiast.", "AM"));
    allCandidates.add(new Candidate("Sara", "Female", 20, "Fashion, Blogging", "Trendy and expressive.", "SARA"));
    allCandidates.add(new Candidate("Dev", "Male", 23, "Music, Coding", "Musician and coder.", "DEV"));
    allCandidates.add(new Candidate("Ishaan", "Male", 21, "Travel, Reading", "Explorer and bookworm.", "ISH"));

    filterCandidates(mainWindow);

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

        likeButton = new JButton("Like");
        likeButton.setBackground(purple);
        likeButton.setForeground(Color.WHITE);
        likeButton.setFont(new Font("Arial", Font.BOLD, 16));
        likeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        passButton = new JButton("Pass");
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
            matchLabel.setText("You liked " + candidates.get(currentIndex).name + "!");
            nextCandidate();
        });
        passButton.addActionListener(e -> {
            matchLabel.setText("You passed " + candidates.get(currentIndex).name + ".");
            nextCandidate();
        });
        backButton.addActionListener(e -> {
            mainWindow.showScreen("profile");
        });
        chatsButton.addActionListener(e -> {
            mainWindow.showScreen("chats");
        });
        refreshButton.addActionListener(e -> {
            
            allCandidates.add(new Candidate("Taylor", "Female", 21, "Dance, Yoga", "Energetic and positive.", "T"));
            allCandidates.add(new Candidate("Chris", "Male", 24, "Photography, Hiking", "Nature lover and photographer.", "C"));
            filterCandidates(mainWindow);
            updateCandidateCard();
            matchLabel.setText("Candidates refreshed!");
        });
    }

    private void filterCandidates(MainWindow mainWindow) {
        String prefGender = mainWindow.getPrefGender();
        int prefAge = mainWindow.getPrefAge();
        String prefInterests = mainWindow.getPrefInterests();
        candidates = new ArrayList<>();
        for (Candidate c : allCandidates) {
            boolean genderMatch = prefGender.equals("Any") || c.gender.equalsIgnoreCase(prefGender);
            boolean ageMatch = Math.abs(c.age - prefAge) <= 2;
            boolean interestsMatch = prefInterests.isEmpty() || c.interests.toLowerCase().contains(prefInterests);
            if (genderMatch && ageMatch && interestsMatch) {
                candidates.add(c);
            }
        }
        currentIndex = 0;
    }

    private void updateCandidateCard() {
        if (currentIndex < candidates.size()) {
            Candidate c = candidates.get(currentIndex);
            nameLabel.setText(c.name + " (" + c.gender + ", Age: " + c.age + ")");
            imageLabel.setText(c.image);
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
        String name, gender, interests, bio, image;
        int age;
        Candidate(String name, String gender, int age, String interests, String bio, String image) {
            this.name = name;
            this.gender = gender;
            this.age = age;
            this.interests = interests;
            this.bio = bio;
            this.image = image;
        }
    }
}
