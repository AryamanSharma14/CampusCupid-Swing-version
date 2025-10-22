import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChatsPanel extends JPanel {
    public ChatsPanel(MainWindow mainWindow) {
        setLayout(new BorderLayout(10,10));
        Color purple = new Color(102, 0, 204);
        Color lightPurple = new Color(230, 220, 255);
        setBackground(lightPurple);

        JLabel titleLabel = new JLabel("Chats", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(purple);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20,0,10,0));

    DefaultListModel<String> chatListModel = new DefaultListModel<>();
    chatListModel.addElement("Alex Sharma");
    chatListModel.addElement("Priya Singh");
    chatListModel.addElement("Rahul Verma");
    chatListModel.addElement("Sneha Rao");
    chatListModel.addElement("Arjun Patel");
    chatListModel.addElement("Meera Joshi");
    chatListModel.addElement("Vikram Singh");
    JList<String> chatList = new JList<>(chatListModel);
    chatList.setFont(new Font("Arial", Font.PLAIN, 16));
    chatList.setBackground(Color.WHITE);
    chatList.setSelectionBackground(new Color(180, 140, 255));
    chatList.setSelectionForeground(purple);
    chatList.setBorder(BorderFactory.createLineBorder(purple, 2));

        JPanel conversationPanel = new JPanel();
        conversationPanel.setLayout(new BoxLayout(conversationPanel, BoxLayout.Y_AXIS));
        conversationPanel.setBackground(lightPurple);
        conversationPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JTextPane conversationArea = new JTextPane();
        conversationArea.setEditable(false);
        conversationArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        conversationArea.setBackground(Color.WHITE);
        conversationArea.setMargin(new Insets(10,10,10,10));
        conversationArea.setText("");

        
        javax.swing.text.StyleContext sc = new javax.swing.text.StyleContext();
        javax.swing.text.Style userStyle = sc.addStyle("user", null);
        javax.swing.text.Style otherStyle = sc.addStyle("other", null);
        javax.swing.text.Style boldStyle = sc.addStyle("bold", null);
        javax.swing.text.Style timeStyle = sc.addStyle("time", null);
        javax.swing.text.StyleConstants.setFontFamily(userStyle, "Segoe UI");
        javax.swing.text.StyleConstants.setFontSize(userStyle, 16);
        javax.swing.text.StyleConstants.setForeground(userStyle, new Color(102,0,204));
        javax.swing.text.StyleConstants.setBold(userStyle, true);
        javax.swing.text.StyleConstants.setFontFamily(otherStyle, "Segoe UI");
        javax.swing.text.StyleConstants.setFontSize(otherStyle, 16);
        javax.swing.text.StyleConstants.setForeground(otherStyle, new Color(0,0,0));
        javax.swing.text.StyleConstants.setBold(otherStyle, true);
        javax.swing.text.StyleConstants.setFontFamily(timeStyle, "Segoe UI");
        javax.swing.text.StyleConstants.setFontSize(timeStyle, 12);
        javax.swing.text.StyleConstants.setForeground(timeStyle, new Color(150,150,150));

        
        Runnable setAlexChat = () -> {
            conversationArea.setText("");
            javax.swing.text.StyledDocument doc = conversationArea.getStyledDocument();
            try {
                doc.insertString(doc.getLength(), "Alex Sharma: ", otherStyle);
                doc.insertString(doc.getLength(), "Hey!\n", null);
                doc.insertString(doc.getLength(), "You: ", userStyle);
                doc.insertString(doc.getLength(), "Hi Alex!\n", null);
                doc.insertString(doc.getLength(), "Alex Sharma: ", otherStyle);
                doc.insertString(doc.getLength(), "How's campus life?\n", null);
                doc.insertString(doc.getLength(), "You: ", userStyle);
                doc.insertString(doc.getLength(), "Pretty good, how about you?", null);
            } catch (Exception ex) {}
        };
        setAlexChat.run();

        JTextField messageField = new JTextField(20);
        messageField.setFont(new Font("Arial", Font.PLAIN, 15));
        JButton sendButton = new JButton("Send");
        sendButton.setBackground(purple);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFont(new Font("Arial", Font.BOLD, 15));

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        inputPanel.setBackground(lightPurple);
        inputPanel.add(messageField);
        inputPanel.add(Box.createHorizontalStrut(10));
        inputPanel.add(sendButton);

        conversationPanel.add(conversationArea);
        conversationPanel.add(Box.createVerticalStrut(10));
        conversationPanel.add(inputPanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatList, conversationPanel);
        splitPane.setDividerLocation(150);
        splitPane.setResizeWeight(0.3);
        splitPane.setBackground(lightPurple);

        add(titleLabel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        chatList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = chatList.getSelectedValue();
                javax.swing.text.StyledDocument doc = conversationArea.getStyledDocument();
                conversationArea.setText("");
                try {
                    if (selected.equals("Alex Sharma")) {
                        setAlexChat.run();
                    } else if (selected.equals("Priya Singh")) {
                        doc.insertString(doc.getLength(), "Priya Singh: ", otherStyle);
                        doc.insertString(doc.getLength(), "Hello!\n", null);
                        doc.insertString(doc.getLength(), "You: ", userStyle);
                        doc.insertString(doc.getLength(), "Hi Priya!\n", null);
                        doc.insertString(doc.getLength(), "Priya Singh: ", otherStyle);
                        doc.insertString(doc.getLength(), "Are you going to the music fest?\n", null);
                        doc.insertString(doc.getLength(), "You: ", userStyle);
                        doc.insertString(doc.getLength(), "Yes, see you there!", null);
                    } else if (selected.equals("Rahul Verma")) {
                        doc.insertString(doc.getLength(), "Rahul Verma: ", otherStyle);
                        doc.insertString(doc.getLength(), "Hi!\n", null);
                        doc.insertString(doc.getLength(), "You: ", userStyle);
                        doc.insertString(doc.getLength(), "Hey Rahul!\n", null);
                        doc.insertString(doc.getLength(), "Rahul Verma: ", otherStyle);
                        doc.insertString(doc.getLength(), "Up for a cricket match?\n", null);
                        doc.insertString(doc.getLength(), "You: ", userStyle);
                        doc.insertString(doc.getLength(), "Always!", null);
                    } else if (selected.equals("Sneha Rao")) {
                        doc.insertString(doc.getLength(), "Sneha Rao: ", otherStyle);
                        doc.insertString(doc.getLength(), "Hi!\n", null);
                        doc.insertString(doc.getLength(), "You: ", userStyle);
                        doc.insertString(doc.getLength(), "Hello Sneha!\n", null);
                        doc.insertString(doc.getLength(), "Sneha Rao: ", otherStyle);
                        doc.insertString(doc.getLength(), "Are you joining the coding club?\n", null);
                        doc.insertString(doc.getLength(), "You: ", userStyle);
                        doc.insertString(doc.getLength(), "Yes, excited!", null);
                    } else if (selected.equals("Arjun Patel")) {
                        doc.insertString(doc.getLength(), "Arjun Patel: ", otherStyle);
                        doc.insertString(doc.getLength(), "Good morning!\n", null);
                        doc.insertString(doc.getLength(), "You: ", userStyle);
                        doc.insertString(doc.getLength(), "Morning Arjun!\n", null);
                        doc.insertString(doc.getLength(), "Arjun Patel: ", otherStyle);
                        doc.insertString(doc.getLength(), "Ready for the hackathon?\n", null);
                        doc.insertString(doc.getLength(), "You: ", userStyle);
                        doc.insertString(doc.getLength(), "Absolutely!", null);
                    } else if (selected.equals("Meera Joshi")) {
                        doc.insertString(doc.getLength(), "Meera Joshi: ", otherStyle);
                        doc.insertString(doc.getLength(), "Hey!\n", null);
                        doc.insertString(doc.getLength(), "You: ", userStyle);
                        doc.insertString(doc.getLength(), "Hi Meera!\n", null);
                        doc.insertString(doc.getLength(), "Meera Joshi: ", otherStyle);
                        doc.insertString(doc.getLength(), "Want to study together?\n", null);
                        doc.insertString(doc.getLength(), "You: ", userStyle);
                        doc.insertString(doc.getLength(), "Sure, let's plan!", null);
                    } else if (selected.equals("Vikram Singh")) {
                        doc.insertString(doc.getLength(), "Vikram Singh: ", otherStyle);
                        doc.insertString(doc.getLength(), "Hi!\n", null);
                        doc.insertString(doc.getLength(), "You: ", userStyle);
                        doc.insertString(doc.getLength(), "Hey Vikram!\n", null);
                        doc.insertString(doc.getLength(), "Vikram Singh: ", otherStyle);
                        doc.insertString(doc.getLength(), "Movie night this weekend?\n", null);
                        doc.insertString(doc.getLength(), "You: ", userStyle);
                        doc.insertString(doc.getLength(), "I'm in!", null);
                    }
                } catch (Exception ex) {}
            }
        });

        sendButton.addActionListener(e -> {
            String msg = messageField.getText().trim();
            if (!msg.isEmpty()) {
                try {
                    javax.swing.text.StyledDocument doc = conversationArea.getStyledDocument();
                    doc.insertString(doc.getLength(), "\nYou: ", userStyle);
                    doc.insertString(doc.getLength(), msg, null);
                } catch (Exception ex) {}
                messageField.setText("");
            }
        });

        JButton backToSwipeButton = new JButton("Back to Swipe");
        backToSwipeButton.setBackground(purple);
        backToSwipeButton.setForeground(Color.WHITE);
        backToSwipeButton.setFont(new Font("Arial", Font.BOLD, 15));
        backToSwipeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backToSwipeButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(backToSwipeButton, BorderLayout.SOUTH);

        backToSwipeButton.addActionListener(e -> {
            mainWindow.showScreen("swipe");
        });
    }
}
