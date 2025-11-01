import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ChatsPanel extends JPanel {
    private final MainWindow mainWindow;
    private final DefaultListModel<String> chatListModel = new DefaultListModel<>();
    private final DefaultListModel<Integer> chatIdModel = new DefaultListModel<>();
    private JList<String> chatList;
    private JTextPane conversationArea;
    private javax.swing.text.StyledDocument doc;
    private javax.swing.Timer pollTimer;
    private Integer selectedOtherId = null;
    private long lastTs = 0L;
    private final Map<Integer, Long> lastSeenPerUser = new HashMap<>();

    public ChatsPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout(10,10));
        Color purple = new Color(102, 0, 204);
        Color lightPurple = new Color(230, 220, 255);
        setBackground(lightPurple);

        JLabel titleLabel = new JLabel("Chats", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(purple);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20,0,10,0));

    chatList = new JList<>(chatListModel);
    chatList.setFont(new Font("Arial", Font.PLAIN, 16));
    chatList.setBackground(Color.WHITE);
    chatList.setSelectionBackground(new Color(180, 140, 255));
    chatList.setSelectionForeground(purple);
    chatList.setBorder(BorderFactory.createLineBorder(purple, 2));

        JPanel conversationPanel = new JPanel();
        conversationPanel.setLayout(new BoxLayout(conversationPanel, BoxLayout.Y_AXIS));
        conversationPanel.setBackground(lightPurple);
        conversationPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

    conversationArea = new JTextPane();
        conversationArea.setEditable(false);
        conversationArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        conversationArea.setBackground(Color.WHITE);
        conversationArea.setMargin(new Insets(10,10,10,10));
        conversationArea.setText("");

        
    javax.swing.text.StyleContext sc = new javax.swing.text.StyleContext();
    javax.swing.text.Style userStyle = sc.addStyle("user", null);
    javax.swing.text.Style otherStyle = sc.addStyle("other", null);
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

    doc = conversationArea.getStyledDocument();

        
        Runnable loadMatches = () -> {
            chatListModel.clear();
            chatIdModel.clear();
            Integer uid = mainWindow.getLoggedInUserId();
            if (uid == null) return;
            List<Map<String,Object>> rows = Database.getMatches(uid);
            for (Map<String,Object> r : rows) {
                chatIdModel.addElement((Integer) r.get("id"));
                chatListModel.addElement((String) r.get("name"));
            }
        };
        loadMatches.run();

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

    JScrollPane convoScroll = new JScrollPane(conversationArea);
    convoScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
    conversationPanel.add(convoScroll);
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
                int idx = chatList.getSelectedIndex();
                if (idx < 0) return;
                Integer otherId = chatIdModel.get(idx);
                selectedOtherId = otherId;
                conversationArea.setText("");
                lastTs = 0L;
                Long seen = lastSeenPerUser.getOrDefault(selectedOtherId, 0L);
                lastTs = Math.max(lastTs, seen);
                try {
                    Integer uid = mainWindow.getLoggedInUserId();
                    if (uid == null) return;
                    List<Map<String,Object>> msgs = Database.getMessagesBetween(uid, otherId);
                    for (Map<String,Object> m : msgs) {
                        boolean fromOther = ((Integer)m.get("from")).intValue() == otherId;
                        String who = fromOther ? (chatListModel.get(idx)+": ") : "You: ";
                        doc.insertString(doc.getLength(), who, fromOther ? otherStyle : userStyle);
                        doc.insertString(doc.getLength(), (String)m.get("body") + "\n", null);
                        if (m.get("ts") != null) {
                            long ts = ((Number)m.get("ts")).longValue();
                            if (ts > lastTs) lastTs = ts;
                        }
                    }
                    conversationArea.setCaretPosition(doc.getLength());
                } catch (Exception ex) {}
                startPolling(otherStyle, userStyle);
            }
        });

        sendButton.addActionListener(e -> {
            String msg = messageField.getText().trim();
            if (!msg.isEmpty()) {
                try {
                    int idx = chatList.getSelectedIndex();
                    if (idx >= 0) {
                        Integer otherId = chatIdModel.get(idx);
                        Integer uid = mainWindow.getLoggedInUserId();
                        if (uid != null) Database.sendMessage(uid, otherId, msg);
                        // After sending, force a fresh reload from DB to avoid duplicates
                        reloadConversation(otherStyle, userStyle);
                    }
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

        // Refresh matches and start/stop polling when panel is shown/hidden
        this.addHierarchyListener(ev -> {
            if ((ev.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (this.isShowing()) {
                    loadMatches.run();
                    startPolling(otherStyle, userStyle);
                } else {
                    stopPolling();
                }
            }
        });
    }

    private void reloadConversation(javax.swing.text.Style otherStyle, javax.swing.text.Style userStyle) {
        int idx = chatList.getSelectedIndex();
        if (idx < 0) return;
        Integer otherId = chatIdModel.get(idx);
        selectedOtherId = otherId;
        conversationArea.setText("");
        lastTs = 0L;
        try {
            Integer uid = mainWindow.getLoggedInUserId();
            if (uid == null) return;
            List<Map<String,Object>> msgs = Database.getMessagesBetween(uid, otherId);
            for (Map<String,Object> m : msgs) {
                boolean fromOther = ((Integer)m.get("from")).intValue() == otherId;
                String who = fromOther ? (chatList.getSelectedValue()+": ") : "You: ";
                doc.insertString(doc.getLength(), who, fromOther ? otherStyle : userStyle);
                doc.insertString(doc.getLength(), (String)m.get("body") + "\n", null);
                if (m.get("ts") != null) {
                    long ts = ((Number)m.get("ts")).longValue();
                    if (ts > lastTs) lastTs = ts;
                }
            }
            conversationArea.setCaretPosition(doc.getLength());
            lastSeenPerUser.put(selectedOtherId, lastTs);
        } catch (Exception ex) {}
    }

    private void startPolling(javax.swing.text.Style otherStyle, javax.swing.text.Style userStyle) {
        if (pollTimer != null && pollTimer.isRunning()) return;
        pollTimer = new javax.swing.Timer(1500, evt -> pollOnce(otherStyle, userStyle));
        pollTimer.setRepeats(true);
        pollTimer.start();
    }

    private void stopPolling() {
        if (pollTimer != null) {
            pollTimer.stop();
        }
    }

    private void pollOnce(javax.swing.text.Style otherStyle, javax.swing.text.Style userStyle) {
        if (selectedOtherId == null) return;
        try {
            Integer uid = mainWindow.getLoggedInUserId();
            if (uid == null) return;
            List<Map<String,Object>> msgs = Database.getMessagesBetween(uid, selectedOtherId);
            boolean appended = false;
            for (Map<String,Object> m : msgs) {
                long ts = m.get("ts") == null ? 0L : ((Number)m.get("ts")).longValue();
                if (ts <= lastTs) continue;
                boolean fromOther = ((Integer)m.get("from")).intValue() == selectedOtherId;
                String who = fromOther ? (chatList.getSelectedValue()+": ") : "You: ";
                try {
                    doc.insertString(doc.getLength(), who, fromOther ? otherStyle : userStyle);
                    doc.insertString(doc.getLength(), (String)m.get("body") + "\n", null);
                    appended = true;
                } catch (Exception ignore) {}
                if (ts > lastTs) lastTs = ts;
            }
            if (appended) {
                conversationArea.setCaretPosition(doc.getLength());
                lastSeenPerUser.put(selectedOtherId, lastTs);
            }
        } catch (Exception ex) {}
    }
}
