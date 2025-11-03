import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatsPanel extends JPanel {
    private final MainWindow mainWindow;
    // Contacts model
    private final DefaultListModel<Contact> contactModel = new DefaultListModel<>();
    private JList<Contact> contactsList;
    private int hoverIndex = -1;

    // Message area
    private JPanel messagesPanel;
    private JScrollPane messagesScroll;
    private ChatHeader chatHeader;
    private TypingIndicator typingIndicator;
    private JTextArea inputArea;
    private JButton sendButton;
    private JButton emojiButton;

    // Polling (run off the EDT)
    private ScheduledExecutorService pollExec;
    private Integer selectedOtherId = null;
    private long lastTs = 0L; // last shown ts for current chat
    private final Map<Integer, Long> lastSeenPerUser = new HashMap<>(); // per-contact last seen ts
    private final SimpleDateFormat timeFmt = new SimpleDateFormat("h:mm a");
    private static final long GROUP_WINDOW_MS = 5L * 60L * 1000L; // 5 minutes

    // Async guards
    private final AtomicBoolean contactsLoading = new AtomicBoolean(false);
    private final AtomicBoolean convoLoading = new AtomicBoolean(false);

    public ChatsPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Build left contacts sidebar
        JPanel contactsSidebar = buildContactsSidebar();

        // Build right chat area
        JPanel chatArea = buildChatArea();

        // Split pane
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, contactsSidebar, chatArea);
        split.setDividerSize(6);
        split.setResizeWeight(0); // left fixed feel
        split.setDividerLocation(250);
        split.setUI(new PurpleSplitPaneUI());
        contactsSidebar.setMinimumSize(new Dimension(180, 200));
        chatArea.setMinimumSize(new Dimension(400, 200));
        add(split, BorderLayout.CENTER);

    // Load contacts
    loadContactsAsync();

        // Show first contact if available
        if (!contactModel.isEmpty()) {
            contactsList.setSelectedIndex(0);
        }

    // Start polling
    startPolling();

        // Stop polling when hidden
        this.addHierarchyListener(ev -> {
            if ((ev.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (this.isShowing()) {
                    startPolling();
                } else {
                    stopPolling();
                }
            }
        });
    }

    // Helper: convert a name to Title Case for display
    private static String toTitleCase(Object o) {
        if (o == null) return "";
        String s = o.toString();
        if (s.isEmpty()) return s;
        String[] parts = s.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            if (p.isEmpty()) continue;
            if (i > 0) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) sb.append(p.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    // ============================ UI BUILDERS ============================= //
    private JPanel buildContactsSidebar() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                int w=getWidth(), h=getHeight();
                // Main gradient
                GradientPaint gp = new GradientPaint(0,0, Color.decode("#9D4EDD"), 0,h, Color.decode("#7B2CBF"));
                g2.setPaint(gp); g2.fillRect(0,0,w,h);
                g2.dispose();
            }
        };
        root.setPreferredSize(new Dimension(250, 400));
        root.setOpaque(false);

        // Top header 60px darker gradient and Back button
        JPanel header = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                int w=getWidth(), h=getHeight();
                g2.setPaint(new GradientPaint(0,0, Color.decode("#7B2CBF"), 0,h, Color.decode("#5A189A")));
                g2.fillRect(0,0,w,h);
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(250, 60));

        JButton back = new JButton("\u2190 Back to Swipe");
        back.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        back.setForeground(Color.WHITE);
        back.setBackground(Color.decode("#FF6B9D"));
        back.setFocusPainted(false);
        back.setBorderPainted(false);
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.setBounds(10,7,230,45);
        back.addMouseListener(new HoverBrighten(back, Color.decode("#FF6B9D"), 0.1f));
        back.addActionListener(e -> mainWindow.showScreen("swipe"));
        header.add(back);
        root.add(header, BorderLayout.NORTH);

        // Contacts list container background
        JPanel listContainer = new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics g){ super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create(); g2.setColor(Color.decode("#8B4EBF")); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose(); }
        };

        contactsList = new JList<>(contactModel);
        contactsList.setOpaque(false);
        contactsList.setCellRenderer(new ContactCellRenderer());
        contactsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactsList.addMouseMotionListener(new MouseMotionAdapter(){ @Override public void mouseMoved(MouseEvent e){ int idx=contactsList.locationToIndex(e.getPoint()); if (idx!=hoverIndex){ hoverIndex=idx; contactsList.repaint(); } }});
        contactsList.addMouseListener(new MouseAdapter(){ @Override public void mouseExited(MouseEvent e){ hoverIndex=-1; contactsList.repaint(); } });
    contactsList.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()){ Contact c = contactsList.getSelectedValue(); if (c!=null){ onContactSelected(c); } }});

        JScrollPane sp = new JScrollPane(contactsList);
        sp.setOpaque(false); sp.getViewport().setOpaque(false);
        sp.setBorder(new EmptyBorder(0,0,0,0));
        sp.getVerticalScrollBar().setUI(new PurpleScrollbarUI());
        sp.getVerticalScrollBar().setUnitIncrement(16);
        listContainer.add(sp, BorderLayout.CENTER);
        root.add(listContainer, BorderLayout.CENTER);

        return root;
    }

    private JPanel buildChatArea(){
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // Header
        chatHeader = new ChatHeader();
        root.add(chatHeader, BorderLayout.NORTH);

        // Messages area
        messagesPanel = new MessagesPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBorder(new EmptyBorder(15,15,15,15));
        messagesScroll = new JScrollPane(messagesPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        messagesScroll.setBorder(null);
        messagesScroll.getVerticalScrollBar().setUI(new PurpleScrollbarUI());
        messagesScroll.getVerticalScrollBar().setUnitIncrement(16);
        root.add(messagesScroll, BorderLayout.CENTER);

        // Typing indicator at bottom inside messages area
        typingIndicator = new TypingIndicator();

        // Input area
        JPanel input = buildInputArea();
        root.add(input, BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildInputArea(){
        JPanel p = new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics g){ super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create(); int w=getWidth(); g2.setColor(Color.WHITE); g2.fillRect(0,0,w,getHeight()); // top border and inner shadow
                g2.setColor(Color.decode("#E8D5F2")); g2.fillRect(0,0,w,2);
                g2.setPaint(new GradientPaint(0,2,new Color(0,0,0,25),0,8,new Color(0,0,0,0))); g2.fillRect(0,2,w,8); g2.dispose(); }
        };
        p.setPreferredSize(new Dimension(10,90));

        inputArea = new PlaceholderTextArea("Type a message...");
        inputArea.setLineWrap(true); inputArea.setWrapStyleWord(true); inputArea.setFont(new Font("Segoe UI", Font.PLAIN, 13)); inputArea.setForeground(Color.decode("#3C096C"));
        JScrollPane taScroll = new JScrollPane(inputArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        taScroll.setBorder(BorderFactory.createEmptyBorder());
        taScroll.getVerticalScrollBar().setUI(new PurpleScrollbarUI());
        JPanel taWrap = new JPanel(new BorderLayout()){ @Override protected void paintComponent(Graphics g){ super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create(); int w=getWidth(), h=getHeight(); g2.setColor(Color.decode("#F8F4FB")); g2.fillRoundRect(0,15,w-180,h-30,15,15); g2.setStroke(new BasicStroke(2f)); g2.setColor(Color.decode("#D4A5F5")); g2.drawRoundRect(0,15,w-180,h-30,15,15); g2.dispose(); } };
        taWrap.setOpaque(false); taWrap.setBorder(new EmptyBorder(0,15,0,10)); taWrap.add(taScroll, BorderLayout.CENTER);

        // Emoji button
        emojiButton = new JButton("😊"); emojiButton.setFocusPainted(false); emojiButton.setBorderPainted(false); emojiButton.setContentAreaFilled(false); emojiButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        emojiButton.addMouseListener(new HoverRounded(emojiButton, new Color(0xE8,0xD5,0xF2)));
        emojiButton.setPreferredSize(new Dimension(35,35));
        emojiButton.addActionListener(e -> showEmojiPicker());

        // Send button
        sendButton = new JButton("➤ Send");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false); sendButton.setBorderPainted(false);
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendButton.setPreferredSize(new Dimension(140,50));
        sendButton.addMouseListener(new ScaleBrightness(sendButton, new Color(0xFF,0x69,0xB4), new Color(0xFF,0x8F,0xAB)));
        sendButton.addActionListener(e -> onSend());
        updateSendButtonState();

        inputArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){ private void upd(){ updateSendButtonState(); } public void insertUpdate(javax.swing.event.DocumentEvent e){upd();} public void removeUpdate(javax.swing.event.DocumentEvent e){upd();} public void changedUpdate(javax.swing.event.DocumentEvent e){upd();}});

        JPanel right = new JPanel(); right.setOpaque(false); right.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 20));
        right.add(emojiButton); right.add(sendButton);

        p.add(taWrap, BorderLayout.CENTER);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private void updateSendButtonState(){
        boolean hasText = inputArea.getText()!=null && !inputArea.getText().trim().isEmpty();
        sendButton.setEnabled(hasText);
        if (hasText){ sendButton.setBackground(null); sendButton.setOpaque(false); } // background painted in listener
        else { sendButton.setOpaque(true); sendButton.setBackground(Color.decode("#D4A5F5")); sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)); }
    }

    // ============================ DATA & EVENTS ============================= //
    private void loadContactsAsync(){
        if (contactsLoading.getAndSet(true)) return;
        Integer uid = mainWindow.getLoggedInUserId();
        if (uid == null) { contactsLoading.set(false); return; }
        new SwingWorker<List<Contact>, Void>(){
            @Override protected List<Contact> doInBackground(){
                List<Contact> out = new ArrayList<>();
                List<Map<String,Object>> rows = Database.getMatches(uid);
                for (Map<String,Object> r : rows){
                    int id = (Integer) r.get("id");
                    String name = toTitleCase(r.get("name"));
                    Map<String,Object> last = Database.getLastMessageBetween(uid, id);
                    String body = ""; long ts=0L; boolean fromOther=false;
                    if (last != null){
                        body = Objects.toString(last.get("body"), "");
                        ts = last.get("ts") == null ? 0L : ((Number)last.get("ts")).longValue();
                        Object f = last.get("from");
                        fromOther = f != null && ((Number)f).intValue() == id;
                    }
                    boolean unread = ts > lastSeenPerUser.getOrDefault(id, 0L) && fromOther;
                    out.add(new Contact(id, name, body, ts, unread));
                }
                // Sort by ts desc for nicer ordering
                out.sort(Comparator.comparingLong((Contact c) -> c.ts).reversed());
                return out;
            }
            @Override protected void done(){
                try {
                    List<Contact> list = get();
                    int keepId = contactsList.getSelectedValue()!=null? contactsList.getSelectedValue().id : -1;
                    DefaultListModel<Contact> newModel = new DefaultListModel<>();
                    for (Contact c : list) newModel.addElement(c);
                    contactsList.setModel(newModel);
                    // replace reference so renderers use correct model instance going forward
                    // (this.contactModel is only used at init for construction)
                    // But keep the field for compatibility
                    contactModel.clear();
                    for (int i=0;i<newModel.getSize();i++) contactModel.addElement(newModel.getElementAt(i));
                    // restore selection
                    if (keepId != -1){
                        for (int i=0;i<newModel.getSize();i++){ if (newModel.getElementAt(i).id == keepId){ contactsList.setSelectedIndex(i); break; } }
                    }
                } catch (Exception ignore) { }
                finally { contactsLoading.set(false); }
            }
        }.execute();
    }

    private void onContactSelected(Contact c){
        selectedOtherId = c.id;
        lastTs = Math.max(lastSeenPerUser.getOrDefault(c.id, 0L), 0L);
        chatHeader.setContact(c.name, true); // show Active now static
        buildConversationAsync(c.id);
        // Mark read
        lastSeenPerUser.put(c.id, System.currentTimeMillis());
        // Refresh model to clear unread dot
        loadContactsAsync();
        // Keep selection on the same contact
        for (int i=0;i<contactModel.size();i++){ if (contactModel.get(i).id==c.id){ contactsList.setSelectedIndex(i); break; }}
        // Autoscroll
        SwingUtilities.invokeLater(() -> scrollToBottom());
    }

    private void onSend(){
        String msg = inputArea.getText().trim();
        if (msg.isEmpty() || selectedOtherId == null) return;
        Integer uid = mainWindow.getLoggedInUserId();
        if (uid != null) {
            Database.sendMessage(uid, selectedOtherId, msg);
        }
        inputArea.setText("");
        updateSendButtonState();
    // Rebuild conversation and refresh contacts
    buildConversationAsync(selectedOtherId);
    loadContactsAsync();
        // Ensure selection remains
        for (int i=0;i<contactModel.size();i++){ if (contactModel.get(i).id.equals(selectedOtherId)){ contactsList.setSelectedIndex(i); break; }}
        SwingUtilities.invokeLater(this::scrollToBottom);
    }

    private void startPolling(){
        if (pollExec != null && !pollExec.isShutdown()) return;
        pollExec = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "chat-poller");
            t.setDaemon(true);
            return t;
        });
        pollExec.scheduleAtFixedRate(this::pollOnceBg, 0, 1500, TimeUnit.MILLISECONDS);
    }
    private void stopPolling(){ if (pollExec!=null) { pollExec.shutdownNow(); pollExec=null; } }
    private void pollOnceBg(){
        Integer uid = mainWindow.getLoggedInUserId(); if (uid==null) return;
        try {
            // Update current conversation if there's a newer message
            if (selectedOtherId != null){
                Map<String,Object> last = Database.getLastMessageBetween(uid, selectedOtherId);
                long newest = (last==null||last.get("ts")==null) ? lastTs : ((Number)last.get("ts")).longValue();
                if (newest > lastTs && !convoLoading.get()){
                    SwingUtilities.invokeLater(() -> buildConversationAsync(selectedOtherId));
                }
            }
            // Update contacts list in background (it will coalesce via guard)
            loadContactsAsync();
        } catch (Exception ignore) { }
    }

    private void buildConversationAsync(Integer otherId){
        if (otherId == null) return;
        if (convoLoading.getAndSet(true)) return;
        Integer uid = mainWindow.getLoggedInUserId(); if (uid==null) { convoLoading.set(false); return; }
        new SwingWorker<List<Map<String,Object>>, Void>(){
            @Override protected List<Map<String,Object>> doInBackground(){
                return Database.getMessagesBetween(uid, otherId);
            }
            @Override protected void done(){
                try { renderConversation(otherId, get()); }
                catch (Exception ignore) { }
                finally { convoLoading.set(false); }
            }
        }.execute();
    }

    private void renderConversation(Integer otherId, List<Map<String,Object>> msgs){
        messagesPanel.removeAll();
        Integer uid = mainWindow.getLoggedInUserId(); if (uid==null) return;
        // Grouping
        List<Group> groups = new ArrayList<>();
        Group cur=null;
        for (Map<String,Object> m : msgs){
            long ts = m.get("ts") == null ? 0L : ((Number)m.get("ts")).longValue();
            boolean fromOther = ((Integer)m.get("from")).intValue() == otherId;
            String body = Objects.toString(m.get("body"), "");
            if (cur==null || cur.fromOther!=fromOther || (ts - cur.lastTs) > GROUP_WINDOW_MS){
                cur = new Group(fromOther); groups.add(cur);
            }
            cur.add(new Msg(body, ts));
        }
        // Render
        String otherName = contactsList.getSelectedValue()!=null? contactsList.getSelectedValue().name : "";
        for (int gi=0; gi<groups.size(); gi++){
            Group g = groups.get(gi);
            boolean first = true;
            for (int i=0;i<g.messages.size();i++){
                Msg m = g.messages.get(i);
                boolean isLast = i==g.messages.size()-1;
                if (g.fromOther && first){
                    // Name label
                    JLabel name = new JLabel(otherName);
                    name.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 11));
                    name.setForeground(Color.decode("#7B2CBF"));
                    JPanel wrap = alignLeft(name);
                    wrap.setBorder(new EmptyBorder(0,0,2,0));
                    messagesPanel.add(wrap);
                    first=false;
                }
                Bubble bubble = new Bubble(m.text, g.fromOther);
                JPanel bubbleWrap = g.fromOther? alignLeft(bubble) : alignRight(bubble);
                messagesPanel.add(bubbleWrap);
                if (isLast){
                    String ts = timeFmt.format(new java.util.Date(m.ts));
                    JLabel meta = new JLabel(g.fromOther? ts : ts + "  ✓");
                    meta.setFont(new Font("Segoe UI", Font.ITALIC, 10));
                    meta.setForeground(Color.decode("#9D4EDD"));
                    JPanel metaWrap = g.fromOther? alignLeft(meta) : alignRight(meta);
                    metaWrap.setBorder(new EmptyBorder(2,0,10,0));
                    messagesPanel.add(metaWrap);
                } else {
                    messagesPanel.add(Box.createVerticalStrut(8));
                }
            }
            if (gi < groups.size()-1) messagesPanel.add(Box.createVerticalStrut(15));
        }
        messagesPanel.revalidate(); messagesPanel.repaint();
        // Add typing indicator if visible
        if (typingIndicator.isVisible()){
            JPanel tiWrap = alignLeft(typingIndicator);
            tiWrap.setBorder(new EmptyBorder(6,0,6,0));
            messagesPanel.add(tiWrap);
        }
        // Update lastTs
        if (!msgs.isEmpty()){
            Map<String,Object> last = msgs.get(msgs.size()-1);
            lastTs = last.get("ts") == null ? lastTs : Math.max(lastTs, ((Number)last.get("ts")).longValue());
        }
    }

    private JPanel alignLeft(JComponent c){ JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); p.setOpaque(false); p.add(c); return p; }
    private JPanel alignRight(JComponent c){ JPanel p=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0)); p.setOpaque(false); p.add(c); return p; }
    private void scrollToBottom(){ JScrollBar sb = messagesScroll.getVerticalScrollBar(); sb.setValue(sb.getMaximum()); }

    private void showEmojiPicker(){
        JPopupMenu menu = new JPopupMenu();
        String[] emojis = {"😀","😂","😍","🤩","😭","😅","😉","😘","😎","👍","🙏","🔥","✨","💖","💯"};
        JPanel grid = new JPanel(new GridLayout(3,5,6,6)); grid.setBorder(new EmptyBorder(6,6,6,6));
        for (String e : emojis){ JButton b=new JButton(e); b.setFocusPainted(false); b.setBorderPainted(false); b.setContentAreaFilled(false); b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18)); b.addActionListener(evt->{ inputArea.replaceSelection(e); menu.setVisible(false); updateSendButtonState(); }); grid.add(b);}        
        menu.add(grid); menu.show(emojiButton, 0, emojiButton.getHeight());
    }

    public void showTypingIndicator(boolean show){ typingIndicator.setVisible(show); buildConversationAsync(selectedOtherId); }

    // ============================ MODELS & RENDERERS ============================= //
    private static class Contact {
        final Integer id; final String name; final String last; final long ts; final boolean unread;
        Contact(Integer id, String name, String last, long ts, boolean unread){ this.id=id; this.name=name; this.last=last; this.ts=ts; this.unread=unread; }
        @Override public String toString(){ return name; }
    }

    private class ContactCellRenderer extends JPanel implements ListCellRenderer<Contact> {
        private final JLabel name = new JLabel();
        private final JLabel preview = new JLabel();
        private final JLabel time = new JLabel();
        private Contact value;
        private int thisIndex = -1;
        private boolean thisSelected = false;
        ContactCellRenderer(){ setOpaque(false); setLayout(null); }
        @Override public Component getListCellRendererComponent(JList<? extends Contact> list, Contact value, int index, boolean isSelected, boolean cellHasFocus){
            this.value = value; removeAll(); setPreferredSize(new Dimension(10,70));
            this.thisIndex = index; this.thisSelected = isSelected;
            // Name
            name.setText(value==null?"":value.name); name.setFont(new Font("Segoe UI", Font.BOLD, 14)); name.setForeground(Color.WHITE);
            // Preview 25 chars, italic
            String pv = value==null?"":value.last; pv = pv==null?"":pv; if (pv.length()>25) pv = pv.substring(0,25)+"…";
            preview.setText(pv); preview.setFont(new Font("Segoe UI", Font.ITALIC, 11)); preview.setForeground(Color.decode("#E8D5F2"));
            // Time
            String tt = value==null||value.ts==0?"": timeFmt.format(new java.util.Date(value.ts));
            time.setText(tt); time.setFont(new Font("Segoe UI", Font.PLAIN, 9)); time.setForeground(Color.decode("#D4A5F5"));
            // Layout bounds
            name.setBounds(70, 12, getWidth()-140, 18);
            preview.setBounds(70, 35, getWidth()-140, 14);
            time.setBounds(getWidth()-70, 10, 60, 12);
            add(name); add(preview); add(time);
            return this;
        }
        @Override public void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g.create(); int w=getWidth(), h=getHeight();
            // background alternating
            int row = this.thisIndex < 0 ? 0 : this.thisIndex;
            Color base = (row % 2 == 0 ? Color.decode("#9D4EDD") : Color.decode("#8B4EBF"));
            if (thisSelected){ g2.setPaint(new GradientPaint(0,0, Color.decode("#FF69B4"), 0,h, Color.decode("#FF8FAB"))); }
            else { g2.setColor(base); }
            g2.fillRect(0,0,w,h);
            // Hover effect
            int idx = row;
            if (!thisSelected && idx == hoverIndex){ g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.10f)); g2.setColor(Color.WHITE); g2.fillRect(0,0,w,h); }
            // Bottom border 30% opacity
            g2.setComposite(AlphaComposite.SrcOver); g2.setColor(new Color(0x7B,0x2C,0xBF, 77)); g2.fillRect(0,h-1,w,1);
            // Unread indicator
            if (value!=null && value.unread){ g2.setColor(Color.decode("#FF6B9D")); g2.fillOval(10, h/2-4, 8, 8); }
            // Avatar circle and initials
            int ax=22, ay= h/2-22; g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(3f)); g2.drawOval(ax-1, ay-1, 46,46); g2.setColor(Color.decode("#E8D5F2")); g2.fillOval(ax,ay,45,45);
            if (value!=null){ String initials = initialsOf(value.name); g2.setFont(new Font("Segoe UI", Font.BOLD, 16)); FontMetrics fm=g2.getFontMetrics(); int tw=fm.stringWidth(initials); int tx=ax+22 - tw/2; int ty=ay+22 + fm.getAscent()/2 - 2; g2.setColor(Color.decode("#7B2CBF")); g2.drawString(initials, tx, ty);}            
            g2.dispose();
            super.paintComponent(g);
            // Reposition labels now we know actual width
            name.setBounds(70, 12, w-140, 18); preview.setBounds(70, 35, w-160, 14); time.setBounds(w-65, 10, 55, 12);
        }
        private String initialsOf(String name){ if (name==null || name.isBlank()) return "?"; String[] parts=name.trim().split("\\s+"); String a=parts[0].substring(0,1).toUpperCase(); String b=parts.length>1? parts[1].substring(0,1).toUpperCase():""; return a+b; }
    }

    // ============================ COMPONENTS ============================= //
    private static class PurpleSplitPaneUI extends BasicSplitPaneUI {
        @Override public BasicSplitPaneDivider createDefaultDivider(){ return new BasicSplitPaneDivider(this){ @Override public void paint(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); g2.setColor(Color.decode("#D4A5F5")); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose(); } }; }
    }

    private static class PurpleScrollbarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors(){ this.thumbColor=Color.decode("#7B2CBF"); this.trackColor=Color.decode("#9D4EDD"); }
        @Override public Dimension getPreferredSize(JComponent c){ return new Dimension(8, super.getPreferredSize(c).height); }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle tb){ Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(thumbColor); g2.fillRoundRect(tb.x, tb.y, tb.width, tb.height, 8,8); g2.dispose(); }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds){ Graphics2D g2=(Graphics2D)g.create(); g2.setColor(trackColor); g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height); g2.dispose(); }
    }

    private class ChatHeader extends JPanel {
        private final JLabel name = new JLabel("");
        private final JLabel status = new JLabel("Active now");
        ChatHeader(){ setPreferredSize(new Dimension(10,70)); setOpaque(false); setLayout(new BorderLayout());
            JPanel left = new JPanel(){ @Override protected void paintComponent(Graphics g){ super.paintComponent(g);} }; left.setOpaque(false); left.setLayout(new GridBagLayout());
            GridBagConstraints gbc=new GridBagConstraints(); gbc.gridx=0; gbc.gridy=0; gbc.anchor=GridBagConstraints.WEST; gbc.insets=new Insets(0,20,0,0);
            name.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 20)); name.setForeground(Color.decode("#7B2CBF"));
            status.setFont(new Font("Segoe UI", Font.PLAIN, 12)); status.setForeground(Color.decode("#9D4EDD"));
            JPanel textCol = new JPanel(); textCol.setOpaque(false); textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS)); textCol.add(name); JPanel row=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); row.setOpaque(false); row.add(new OnlineDot()); row.add(status); textCol.add(row);
            left.add(textCol, gbc);
            add(left, BorderLayout.WEST);

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 20)); right.setOpaque(false);
            right.add(circleButton()); right.add(circleButton());
            add(right, BorderLayout.EAST);
        }
        void setContact(String n, boolean online){ name.setText(n); status.setText(online?"Active now":"Offline"); }
        @Override protected void paintComponent(Graphics g){ super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create(); int w=getWidth(), h=getHeight(); g2.setPaint(new GradientPaint(0,0, Color.decode("#E8D5F2"), 0,h, Color.decode("#D4A5F5"))); g2.fillRect(0,0,w,h); // corner stars
            g2.setColor(new Color(0x7B,0x2C,0xBF, 127)); g2.setFont(new Font("Segoe UI", Font.PLAIN, 12)); g2.drawString("✦", 8, 16); g2.drawString("✦", w-20, 16); g2.dispose(); }
        private JComponent circleButton(){ return new JComponent(){ { setPreferredSize(new Dimension(25,25)); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
            @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); g2.setColor(Color.decode("#9D4EDD")); g2.fillOval(0,0,25,25); g2.dispose(); } };
        }
    }

    private static class OnlineDot extends JComponent{ OnlineDot(){ setPreferredSize(new Dimension(10,10)); }
        @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); g2.setColor(Color.decode("#3BB789")); g2.fillOval(0,0,10,10); g2.dispose(); }
    }

    private static class MessagesPanel extends JPanel{ MessagesPanel(){ setOpaque(false);} @Override protected void paintComponent(Graphics g){ super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create(); int w=getWidth(), h=getHeight(); g2.setColor(Color.decode("#F5F0FA")); g2.fillRect(0,0,w,h); // subtle hearts pattern
            g2.setColor(new Color(0xE8,0xD5,0xF2, 12)); for(int y=10;y<h;y+=28){ for(int x=20;x<w;x+=32){ g2.drawString("❤", x, y); } } g2.dispose(); } }

    private static class Bubble extends JComponent { private final String text; private final boolean fromOther; Bubble(String t, boolean fromOther){ this.text=t; this.fromOther=fromOther; setPreferredSize(new Dimension(10, 10)); }
        @Override public Dimension getPreferredSize(){ Font f=new Font("Segoe UI", Font.PLAIN, 13); FontMetrics fm=getFontMetrics(f); int maxWidth=(int)(getParent().getParent().getWidth()*0.65); if (maxWidth<=0) maxWidth=320; String[] lines=wrap(text, fm, maxWidth-24); int h=lines.length*(fm.getHeight()) + 14; int w=0; for(String ln:lines){ w=Math.max(w, fm.stringWidth(ln)); } w += 24; w=Math.min(w, maxWidth); return new Dimension(w, h);}        
    @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); int w=getWidth(), h=getHeight(); int r=18; Shape round; if (fromOther){ round = new java.awt.geom.RoundRectangle2D.Float(0,0,w-1,h-1, r, r); } else { round = new java.awt.geom.RoundRectangle2D.Float(0,0,w-1,h-1, r, r); }
            if (fromOther){ g2.setColor(Color.WHITE); g2.fill(round); g2.setStroke(new BasicStroke(2f)); g2.setColor(Color.decode("#E8D5F2")); g2.draw(round); }
            else { g2.setPaint(new GradientPaint(0,0, Color.decode("#9D4EDD"), 0,h, Color.decode("#7B2CBF"))); g2.fill(round); }
            // drop shadow
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fromOther?0.08f:0.12f)); g2.setColor(Color.BLACK); g2.fillRoundRect(2,2,w-1,h-1, r, r);
            // Text
            g2.setComposite(AlphaComposite.SrcOver); g2.setFont(new Font("Segoe UI", Font.PLAIN, 13)); g2.setColor(fromOther? Color.decode("#3C096C"): Color.WHITE); FontMetrics fm=g2.getFontMetrics(); int x=12, y=10+fm.getAscent(); int maxWidth=(int)(getWidth()-24); for(String ln:wrap(text,fm,maxWidth)){ g2.drawString(ln, x, y); y += fm.getHeight(); }
            g2.dispose(); }
        private static String[] wrap(String text, FontMetrics fm, int maxW){ java.util.List<String> out=new ArrayList<>(); String[] words=text.split("\\s+"); String cur=""; for(String w:words){ String test=cur.isEmpty()? w : cur+" "+w; if (fm.stringWidth(test) > maxW){ if(!cur.isEmpty()) out.add(cur); cur = w; } else cur = test; } if(!cur.isEmpty()) out.add(cur); return out.toArray(new String[0]); }
    }

    private static class TypingIndicator extends JComponent { private int step=0; private final javax.swing.Timer t; TypingIndicator(){ setPreferredSize(new Dimension(40,28)); setVisible(false); t=new javax.swing.Timer(500, e->{ step=(step+1)%3; repaint(); }); t.start(); }
        @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); g2.setColor(Color.decode("#E8D5F2")); g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14); g2.setColor(Color.decode("#9D4EDD")); int cx=10; for(int i=0;i<3;i++){ float a = (i<=step?1f:0.4f); g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a)); g2.fillOval(cx, getHeight()/2-3, 6,6); cx+=10; } g2.dispose(); }
    }

    private static class HoverBrighten extends MouseAdapter { private final JComponent c; private final Color base; private final float pct; HoverBrighten(JComponent c, Color base, float pct){ this.c=c; this.base=base; this.pct=pct; }
        @Override public void mouseEntered(MouseEvent e){ c.setBackground(brighten(base, pct)); }
        @Override public void mouseExited(MouseEvent e){ c.setBackground(base); }
    }

    private static class HoverRounded extends MouseAdapter { private final JComponent c; private final Color hover; HoverRounded(JComponent c, Color hover){ this.c=c; this.hover=hover; }
        @Override public void mouseEntered(MouseEvent e){ c.setOpaque(true); c.setBackground(hover); c.repaint(); }
        @Override public void mouseExited(MouseEvent e){ c.setOpaque(false); c.repaint(); }
    }

    private static class ScaleBrightness extends MouseAdapter { private float factor=1f; private boolean over=false; ScaleBrightness(JComponent c, Color c1, Color c2){ c.setOpaque(true); c.setBackground(c1); javax.swing.Timer t=new javax.swing.Timer(15, e->{ float target = over?1.1f:1f; if (Math.abs(factor-target) < 0.02f){ factor=target; ((javax.swing.Timer)e.getSource()).stop(); } else { factor += (target-factor)*0.3f; } c.repaint(); }); c.addMouseListener(new MouseAdapter(){ @Override public void mouseEntered(MouseEvent e){ over=true; if(!t.isRunning()) t.start(); } @Override public void mouseExited(MouseEvent e){ over=false; if(!t.isRunning()) t.start(); } @Override public void mousePressed(MouseEvent e){ c.setBackground(darken(c1, 0.05f)); } @Override public void mouseReleased(MouseEvent e){ c.setBackground(c1); } }); }
    }

    private static class PlaceholderTextArea extends JTextArea { private final String ph; PlaceholderTextArea(String ph){ super(); this.ph=ph; }
        @Override protected void paintComponent(Graphics g){ super.paintComponent(g); if (getText().isEmpty() && !isFocusOwner()){ Graphics2D g2=(Graphics2D)g.create(); g2.setColor(Color.decode("#A0A0A0")); g2.setFont(getFont()); g2.drawString(ph, 8, getInsets().top + g2.getFontMetrics().getAscent()+2); g2.dispose(); } }
    }

    private static Color brighten(Color c, float pct){ int r=Math.min(255, (int)(c.getRed()*(1+pct))); int g=Math.min(255, (int)(c.getGreen()*(1+pct))); int b=Math.min(255, (int)(c.getBlue()*(1+pct))); return new Color(r,g,b); }
    private static Color darken(Color c, float pct){ int r=Math.max(0, (int)(c.getRed()*(1-pct))); int g=Math.max(0, (int)(c.getGreen()*(1-pct))); int b=Math.max(0, (int)(c.getBlue()*(1-pct))); return new Color(r,g,b); }

    private static class Group { final boolean fromOther; final java.util.List<Msg> messages=new ArrayList<>(); long lastTs=0; Group(boolean fromOther){ this.fromOther=fromOther;} void add(Msg m){ messages.add(m); lastTs=m.ts; } }
    private static class Msg { final String text; final long ts; Msg(String t,long ts){this.text=t; this.ts=ts;} }
}
