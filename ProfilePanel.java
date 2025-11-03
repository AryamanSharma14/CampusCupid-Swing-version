import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;

public class ProfilePanel extends JPanel {
    // Keep references to controls for refresh
    private final MainWindow mainWindow;
    // removed old avatar label; using PhotoFrame now
    private JTextField nameField;
    private JComboBox<String> genderCombo;
    private JSpinner ageSpinner;
    private JTextField photoField;
    private JTextArea bioArea;
    private JTextField interestsField;
    private JTextField hobbiesField;
    private JTextField occupationField;
    private JLabel messageLabel;

    // Decorative background hearts animation
    private static class Heart { int x; int baseY; float phase; }
    private Heart[] hearts;
    private Timer heartsTimer;
    private PhotoFrame photoFrame;
    private boolean saveAttempted = false;
    private JLabel nameStatusIcon;
    private JLabel bioStatusIcon;

    public ProfilePanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout());

        // Hearts background animation data
        initHearts();
        heartsTimer = new Timer(40, e -> {
            for (Heart h : hearts) { h.phase += 0.04f; }
            repaint();
        });
        heartsTimer.start();

        // Top Navigation Bar
        TopNavBar top = new TopNavBar(mainWindow);
        add(top, BorderLayout.NORTH);

    // Center content scrollable (wrap with GridBag to center horizontally like other tabs)
    JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));

        // Profile picture frame
        photoFrame = new PhotoFrame();
        photoFrame.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(photoFrame);
        content.add(Box.createVerticalStrut(30));

    // Form card
        FormCard form = new FormCard(mainWindow);
        form.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(form);

    // Wrap content in a centering panel so the card stays perfectly centered
    JPanel centerWrap = new JPanel(new GridBagLayout());
    centerWrap.setOpaque(false);
    GridBagConstraints cc = new GridBagConstraints();
    cc.gridx = 0; cc.gridy = 0; cc.weightx = 1; cc.anchor = GridBagConstraints.NORTH; cc.fill = GridBagConstraints.NONE;
    centerWrap.add(content, cc);

    JScrollPane scroller = new JScrollPane(centerWrap, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scroller.setBorder(null);
    scroller.setOpaque(false);
    scroller.getViewport().setOpaque(false);
    scroller.getVerticalScrollBar().setUnitIncrement(16);
    scroller.getVerticalScrollBar().setUI(new PurpleScrollbarUI());
        add(scroller, BorderLayout.CENTER);

        // Global Ctrl+S shortcut to save profile (wired inside FormCard too)
        int mask;
        try { mask = (int)Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(); }
        catch (Throwable t) { mask = InputEvent.CTRL_DOWN_MASK; }
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, mask), "saveProfile");
        am.put("saveProfile", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) {
            form.saveBtn.doClick();
        }});
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
            if (photoFrame != null) photoFrame.loadPhoto(purl);
        }
        if (messageLabel != null) {
            messageLabel.setText("");
        }
    }

    // Paint gradient background + floating hearts
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(); int h = getHeight();
        g2.setPaint(new GradientPaint(0, 0, Color.decode("#FAF0FF"), 0, h, Color.decode("#E8D5F2")));
        g2.fillRect(0,0,w,h);
        // Hearts
        if (hearts != null) {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            for (Heart heart : hearts) {
                int y = (int)(heart.baseY + Math.sin(heart.phase*2*Math.PI) * 5);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
                g2.setColor(Color.decode("#D4A5F5"));
                g2.drawString("♥", heart.x % Math.max(w-20,1), y);
            }
            g2.setComposite(AlphaComposite.SrcOver);
        }
        g2.dispose();
    }

    private void initHearts() {
        java.util.Random rnd = new java.util.Random(123);
        int count = 9;
        hearts = new Heart[count];
        for (int i=0;i<count;i++) {
            Heart h = new Heart();
            h.x = rnd.nextInt(900) + 20;
            h.baseY = rnd.nextInt(400) + 120;
            h.phase = rnd.nextFloat();
            hearts[i] = h;
        }
    }

    // Custom purple scrollbar
    static class PurpleScrollbarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            this.thumbColor = Color.decode("#9D4EDD");
            this.trackColor = Color.decode("#F8F4FB");
        }
        @Override protected Dimension getMinimumThumbSize() { return new Dimension(10, 30); }
        @Override protected Dimension getMaximumThumbSize() { return new Dimension(10, 100); }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x+2, thumbBounds.y, thumbBounds.width-4, thumbBounds.height, 10,10);
            g2.dispose();
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(trackColor);
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g2.dispose();
        }
    @Override public Dimension getPreferredSize(JComponent c) { return new Dimension(10, super.getPreferredSize(c).height); }
    }

    // Top navigation bar
    static class TopNavBar extends JPanel {
    // buttons are local; no fields needed
        TopNavBar(MainWindow mw) {
            setPreferredSize(new Dimension(10, 70));
            setOpaque(false);
            setLayout(new BorderLayout());
            // Title left
            JLabel title = new JLabel("Your Profile");
            title.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 22));
            title.setForeground(Color.WHITE);
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 18));
            left.setOpaque(false);
            left.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
            left.add(title);
            add(left, BorderLayout.WEST);
            // Buttons right
            GlossyButton prefs = new GlossyButton("Go to Preferences", new Color(0xFF6B9D), new Color(0xFF8FAB));
            prefs.setPreferredSize(new Dimension(180,45));
            GlossyButton chats = new GlossyButton("Go to Chats", new Color(0x45D09E), new Color(0x3BB789));
            chats.setPreferredSize(new Dimension(150,45));
            prefs.addActionListener(e -> mw.showScreen("preferences"));
            chats.addActionListener(e -> mw.showScreen("chats"));
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
            right.setOpaque(false);
            right.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
            right.add(prefs); right.add(chats);
            add(right, BorderLayout.EAST);
            
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth(); int h = getHeight();
            g2.setPaint(new GradientPaint(0,0, Color.decode("#9D4EDD"), 0,h, Color.decode("#7B2CBF")));
            g2.fillRect(0,0,w,h);
            g2.dispose();
        }
    }

    // Glossy gradient button with hover scale + glow
    static class GlossyButton extends JButton {
        private boolean over=false;
        private float anim=0f;
        private final Color c1, c2;
        GlossyButton(String text, Color c1, Color c2) {
            super(text); this.c1=c1; this.c2=c2;
            setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            Timer t = new Timer(15, e -> { float target = over?1f:0f; if (Math.abs(anim-target)<0.05f){anim=target;((Timer)e.getSource()).stop();} else {anim += (target-anim)*0.3f;} repaint();});
            addMouseListener(new MouseAdapter(){
                @Override public void mouseEntered(MouseEvent e){ over=true; if(!t.isRunning()) t.start(); }
                @Override public void mouseExited(MouseEvent e){ over=false; if(!t.isRunning()) t.start(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight(); int arc=10;
            double scale = 1.0 + 0.02*anim;
            g2.translate(w/2.0, h/2.0); g2.scale(scale, scale); g2.translate(-w/2.0, -h/2.0);
            g2.setPaint(new GradientPaint(0,0,c1,0,h,c2));
            g2.fillRoundRect(0,0,w,h,arc,arc);
            if (anim>0) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f*anim));
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(1,1,w-2,h-2,arc,arc);
            }
            g2.setComposite(AlphaComposite.SrcOver);
            FontMetrics fm = g2.getFontMetrics(getFont());
            int tx=(w-fm.stringWidth(getText()))/2; int ty=(h-fm.getHeight())/2 + fm.getAscent();
            g2.setColor(Color.WHITE); g2.drawString(getText(), tx, ty);
            g2.dispose();
        }
    }

    // Photo Frame with gradient border and overlay
    class PhotoFrame extends JComponent {
        private Image img; private boolean over=false;
        PhotoFrame(){ setPreferredSize(new Dimension(200,200)); setOpaque(false);
            addMouseListener(new MouseAdapter(){
                @Override public void mouseEntered(MouseEvent e){ over=true; repaint(); }
                @Override public void mouseExited(MouseEvent e){ over=false; repaint(); }
                @Override public void mouseClicked(MouseEvent e){
                    String url = JOptionPane.showInputDialog(ProfilePanel.this, "Photo URL:", photoField!=null?photoField.getText():"");
                    if (url!=null) { if (photoField!=null) photoField.setText(url.trim()); loadPhoto(url.trim()); }
                }
            });
        }
        void loadPhoto(String url){
            if (url==null || url.isBlank()) { img=null; repaint(); return; }
            try { java.net.URL u=new java.net.URI(url).toURL(); ImageIcon ic=new ImageIcon(u); Image raw=ic.getImage();
                img=raw.getScaledInstance(180,180, Image.SCALE_SMOOTH);
            } catch(Exception ex){ img=null; }
            repaint();
        }
        @Override protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight();
            // Outer rounded border
            g2.setStroke(new BasicStroke(6f));
            GradientPaint gp = new GradientPaint(0,0, Color.decode("#FF69B4"), 0,h, Color.decode("#9D4EDD"));
            g2.setPaint(gp);
            g2.drawRoundRect(3,3,w-6,h-6,30,30);
            // Inner area 180x180
            int ix=(w-180)/2, iy=(h-180)/2;
            g2.setColor(Color.decode("#E8D5F2"));
            g2.fillRoundRect(ix,iy,180,180,25,25);
            if (img!=null) {
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(ix,iy,180,180,25,25));
                g2.drawImage(img, ix, iy, null);
                g2.setClip(null);
            } else {
                // Camera icon
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 40));
                g2.setColor(Color.decode("#9D4EDD"));
                FontMetrics fm=g2.getFontMetrics();
                String cam="📷"; int tw=fm.stringWidth(cam); int tx=ix+(180-tw)/2; int ty=iy+(180-fm.getHeight())/2 + fm.getAscent();
                g2.drawString(cam, tx, ty);
            }
            // Corner stars
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 16)); g2.setColor(Color.decode("#FFD700"));
            g2.drawString("⭐", ix-8, iy+14);
            g2.drawString("⭐", ix+180-8, iy+14);
            g2.drawString("⭐", ix-8, iy+180-4);
            g2.drawString("⭐", ix+180-8, iy+180-4);
            // Hover overlay
            if (over){
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                g2.setColor(new Color(0,0,0,128));
                g2.fillRoundRect(ix,iy,180,180,25,25);
                g2.setComposite(AlphaComposite.SrcOver);
                g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                String t="Change Photo"; FontMetrics fm=g2.getFontMetrics();
                g2.drawString(t, ix+(180-fm.stringWidth(t))/2, iy+180/2 + fm.getAscent()/2);
            }
            g2.dispose();
        }
    }

    // Rounded input components helpers
    static class RoundedTextField extends JTextField {
        private boolean focused=false;
        RoundedTextField(){ setOpaque(false); setPreferredSize(new Dimension(560,50)); setMaximumSize(new Dimension(800,50)); setBorder(BorderFactory.createEmptyBorder(0,12,0,12)); setBackground(new Color(0,0,0,0));
            addFocusListener(new FocusAdapter(){ @Override public void focusGained(FocusEvent e){focused=true;repaint();} @Override public void focusLost(FocusEvent e){focused=false;repaint();}});
            setFont(new Font("Segoe UI", Font.PLAIN, 13)); setForeground(Color.decode("#3C096C")); }
        @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight(); g2.setColor(Color.decode("#F8F4FB")); g2.fillRoundRect(0,0,w,h,16,16);
            g2.setStroke(new BasicStroke(2f)); g2.setColor(focused?Color.decode("#9D4EDD"):Color.decode("#D4A5F5")); g2.drawRoundRect(0,0,w-1,h-1,16,16);
            if (focused) { g2.setColor(new Color(157,78,221,40)); g2.drawRoundRect(1,1,w-3,h-3,16,16); }
            g2.dispose(); super.paintComponent(g); }
    }

    static class RoundedWrapper extends JPanel { boolean focused=false; RoundedWrapper(JComponent inner){ setOpaque(false); setLayout(new BorderLayout()); add(inner, BorderLayout.CENTER); setPreferredSize(new Dimension(560,50)); setMaximumSize(new Dimension(800,50)); setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
            // Click anywhere on the wrapper to focus the first focusable child
            addMouseListener(new MouseAdapter(){ @Override public void mousePressed(MouseEvent e){ Component target = findFocusableDescendant(RoundedWrapper.this); if (target!=null) target.requestFocusInWindow(); }});
        }
        private Component findFocusableDescendant(Container c){ for (Component ch : c.getComponents()){ if (ch.isFocusable()) return ch; if (ch instanceof Container){ Component d=findFocusableDescendant((Container)ch); if (d!=null) return d; } } return null; }
        @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); int w=getWidth(), h=getHeight(); g2.setColor(Color.decode("#F8F4FB")); g2.fillRoundRect(0,0,w,h,16,16); g2.setStroke(new BasicStroke(2f)); g2.setColor(focused?Color.decode("#9D4EDD"):Color.decode("#D4A5F5")); g2.drawRoundRect(0,0,w-1,h-1,16,16); if (focused) { g2.setColor(new Color(157,78,221,40)); g2.drawRoundRect(1,1,w-3,h-3,16,16);} g2.dispose(); super.paintComponent(g);} }

    class FormCard extends JPanel {
        public JButton saveBtn;
        FormCard(MainWindow mw){
            setOpaque(false);
            setLayout(new GridBagLayout());
            // Let layout compute natural height; avoid forcing tiny preferred height that collapses content
            setMaximumSize(new Dimension(800, Integer.MAX_VALUE));
            setBorder(BorderFactory.createEmptyBorder(30,30,30,30));

            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx=0; gc.gridy=0; gc.anchor=GridBagConstraints.WEST; gc.insets=new Insets(0,0,0,0);
            // Section header
            JLabel header = new JLabel("Profile details"); header.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 24)); header.setForeground(Color.decode("#7B2CBF"));
            add(header, gc);
            gc.gridy++; gc.insets=new Insets(6,0,15,0);
            JComponent underline = new JComponent(){ @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); int h=getHeight(); int x=0; int y=(h-3)/2; g2.setPaint(new GradientPaint(x,y, Color.decode("#FF69B4"), x+200, y, Color.decode("#9D4EDD"))); g2.fillRect(0,y,200,3); g2.dispose(); } };
            underline.setPreferredSize(new Dimension(200, 8)); add(underline, gc);

            // Name
            gc.gridy++; gc.insets=new Insets(0,0,0,0); gc.fill=GridBagConstraints.NONE; gc.weightx=0;
            add(label("Full Name:"), gc);
            gc.gridy++; nameField = new RoundedTextField(); gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1; add(nameField, gc);
            nameStatusIcon = new JLabel(""); nameStatusIcon.setFont(new Font("Segoe UI", Font.PLAIN, 14)); nameStatusIcon.setForeground(new Color(0x3BB789));
            gc.gridx=1; gc.insets=new Insets(0,8,0,0); gc.fill=GridBagConstraints.NONE; gc.weightx=0; add(nameStatusIcon, gc); gc.gridx=0; gc.insets=new Insets(0,0,0,0);

            // Gender
            gc.gridy++; add(label("Your Gender:"), gc);
            gc.gridy++;
            String[] genders = {"Male","Female","Other"}; genderCombo = new JComboBox<>(genders);
            genderCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13)); genderCombo.setBackground(Color.decode("#F8F4FB"));
            genderCombo.setRenderer(new DefaultListCellRenderer(){ @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus){ Component c=super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus); if(c instanceof JLabel){ JLabel l=(JLabel)c; l.setOpaque(true); if(index%2==0) l.setBackground(Color.WHITE); else l.setBackground(Color.decode("#FAF0FF")); if(isSelected) l.setBackground(new Color(157,78,221)); l.setForeground(isSelected?Color.WHITE:Color.decode("#3C096C")); }
                return c; }});
            JPanel genderWrap = new RoundedWrapper(genderCombo); gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1; add(genderWrap, gc); gc.fill=GridBagConstraints.NONE; gc.weightx=0;

            // Age
            gc.gridy++; add(label("Your Age:"), gc);
            gc.gridy++;
            ageSpinner = new JSpinner(new SpinnerNumberModel(21,18,60,1));
            ((JSpinner.DefaultEditor)ageSpinner.getEditor()).getTextField().setBorder(BorderFactory.createEmptyBorder(0,12,0,12));
            ((JSpinner.DefaultEditor)ageSpinner.getEditor()).getTextField().setBackground(new Color(0,0,0,0));
            ((JSpinner.DefaultEditor)ageSpinner.getEditor()).getTextField().setOpaque(false);
            JPanel ageWrap = new RoundedWrapper(ageSpinner); gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1; add(ageWrap, gc); gc.fill=GridBagConstraints.NONE; gc.weightx=0;

            // Photo URL
            gc.gridy++; add(label("Photo URL:"), gc);
            gc.gridy++; photoField = new RoundedTextField(); gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1; add(photoField, gc); gc.fill=GridBagConstraints.NONE; gc.weightx=0;

            // Bio
            gc.gridy++; add(label("Bio:"), gc);
            gc.gridy++;
            bioArea = new JTextArea(7, 50); bioArea.setLineWrap(true); bioArea.setWrapStyleWord(true); bioArea.setFont(new Font("Segoe UI", Font.PLAIN, 13)); bioArea.setForeground(Color.decode("#3C096C"));
            bioArea.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
            JScrollPane bioScroll = new JScrollPane(bioArea); bioScroll.setBorder(null); bioScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            bioScroll.setOpaque(false); bioScroll.getViewport().setOpaque(false);
            bioScroll.getVerticalScrollBar().setUI(new PurpleScrollbarUI());
            JPanel bioWrap = new RoundedWrapper(bioScroll); bioWrap.setPreferredSize(new Dimension(560, 220)); bioWrap.setMaximumSize(new Dimension(800, 260)); gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1; add(bioWrap, gc); gc.fill=GridBagConstraints.NONE; gc.weightx=0;
            bioStatusIcon = new JLabel(""); bioStatusIcon.setFont(new Font("Segoe UI", Font.PLAIN, 14)); bioStatusIcon.setForeground(new Color(0x3BB789));
            gc.gridx=1; gc.insets=new Insets(0,8,0,0); add(bioStatusIcon, gc); gc.gridx=0; gc.insets=new Insets(0,0,0,0);

            // Interests
            gc.gridy++; add(label("Interests:"), gc);
            gc.gridy++; interestsField = new RoundedTextField(); gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1; add(interestsField, gc); gc.fill=GridBagConstraints.NONE; gc.weightx=0;

            // Hobbies
            gc.gridy++; add(label("Hobbies:"), gc);
            gc.gridy++; hobbiesField = new RoundedTextField(); gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1; add(hobbiesField, gc); gc.fill=GridBagConstraints.NONE; gc.weightx=0;

            // Occupation
            gc.gridy++; add(label("Occupation:"), gc);
            gc.gridy++; occupationField = new RoundedTextField(); gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1; add(occupationField, gc); gc.fill=GridBagConstraints.NONE; gc.weightx=0;

            // Buttons row
            gc.gridy++; gc.insets=new Insets(20,0,0,0);
            JPanel btnRow = new JPanel(){ { setOpaque(false); setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); } };
            GradientActionButton save = new GradientActionButton("Save Profile ✓", Color.decode("#9D4EDD"), Color.decode("#7B2CBF")); save.setAlignmentX(Component.CENTER_ALIGNMENT); save.setPreferredSize(new Dimension(260,55)); save.setMaximumSize(new Dimension(300,55));
            OutlinedActionButton goPrefs = new OutlinedActionButton("Go to Preferences →"); goPrefs.setAlignmentX(Component.CENTER_ALIGNMENT); goPrefs.setPreferredSize(new Dimension(260,55)); goPrefs.setMaximumSize(new Dimension(300,55));
            btnRow.add(save); btnRow.add(Box.createVerticalStrut(10)); btnRow.add(goPrefs);
            gc.gridwidth=2; add(btnRow, gc); gc.gridwidth=1;

            // Actions
            save.addActionListener(e -> {
                saveAttempted = true;
                String name = nameField.getText().trim();
                String bio = bioArea.getText().trim();
                nameStatusIcon.setText(name.isEmpty()?"⚠️":"✓");
                nameStatusIcon.setForeground(name.isEmpty()?new Color(0xFFB347):new Color(0x3BB789));
                bioStatusIcon.setText(bio.isEmpty()?"⚠️":"✓");
                bioStatusIcon.setForeground(bio.isEmpty()?new Color(0xFFB347):new Color(0x3BB789));
                if (name.isEmpty() || bio.isEmpty()) { JOptionPane.showMessageDialog(FormCard.this, "Name and Bio cannot be empty", "Profile", JOptionPane.ERROR_MESSAGE); return; }
                Integer uid = mw.getLoggedInUserId();
                if (uid != null) {
                    Integer age = (Integer) ageSpinner.getValue();
                    Database.upsertProfile(uid, name, (String)genderCombo.getSelectedItem(), age, bio, interestsField.getText().trim(), hobbiesField.getText().trim(), occupationField.getText().trim(), photoField.getText().trim());
                    if (photoFrame != null) photoFrame.loadPhoto(photoField.getText().trim());
                }
                mw.setProfileCompleted(true);
                JOptionPane.showMessageDialog(FormCard.this, "Profile saved!", "Profile", JOptionPane.INFORMATION_MESSAGE);
            });
            goPrefs.addActionListener(e -> mw.showScreen("preferences"));

            // expose for Ctrl+S
            saveBtn = save;
        }

        @Override protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); int w=getWidth(), h=getHeight();
            for(int i=6;i>=1;i--){ g2.setColor(new Color(0,0,0,30)); g2.fillRoundRect(3, 6+(6-i), w-6, h-12, 20,20);} 
            g2.setColor(Color.WHITE); g2.fillRoundRect(0,0,w-1,h-1,20,20);
            g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0,0,w-1,h-1,20,20));
            g2.setColor(Color.decode("#E8D5F2"));
            Path2D path = new Path2D.Float(); int baseY = h-20; path.moveTo(0, baseY);
            for (int x=0;x<=w;x+=20){ double y = baseY - 6*Math.sin(x/30.0); path.lineTo(x, y); }
            path.lineTo(w,h); path.lineTo(0,h); path.closePath(); g2.fill(path);
            g2.setClip(null); g2.dispose(); super.paintComponent(g);
        }

        private JLabel label(String text){ JLabel l=new JLabel(text); l.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13)); l.setForeground(Color.decode("#5A189A")); return l; }
    }

    static class GradientActionButton extends JButton {
        private final Color c1,c2; private float anim=0f; private boolean over=false;
        GradientActionButton(String text, Color c1, Color c2){ super(text); this.c1=c1; this.c2=c2; setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false); setForeground(Color.WHITE); setFont(new Font("Segoe UI", Font.BOLD, 16)); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            Timer t = new Timer(15, e->{ float target=over?1f:0f; if(Math.abs(anim-target)<0.05f){anim=target;((Timer)e.getSource()).stop();} else {anim+=(target-anim)*0.3f;} repaint();});
            addMouseListener(new MouseAdapter(){ @Override public void mouseEntered(MouseEvent e){over=true;if(!t.isRunning())t.start();} @Override public void mouseExited(MouseEvent e){over=false;if(!t.isRunning())t.start();} }); }
        @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); int w=getWidth(), h=getHeight(); int arc=12; double scale=1.0+0.01*anim; g2.translate(w/2.0,h/2.0); g2.scale(scale,scale); g2.translate(-w/2.0,-h/2.0); g2.setPaint(new GradientPaint(0,0,c1,0,h,c2)); g2.fillRoundRect(0,0,w,h,arc,arc); if (getModel().isArmed()){ g2.setColor(new Color(0,0,0,30)); g2.fillRoundRect(3,3,w-6,h-6,arc,arc);} if(anim>0){ g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.1f+0.2f*anim)); g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(2f)); g2.drawRoundRect(1,1,w-2,h-2,arc,arc);} FontMetrics fm=g2.getFontMetrics(getFont()); int tx=(w-fm.stringWidth(getText()))/2; int ty=(h-fm.getHeight())/2 + fm.getAscent(); g2.setColor(Color.WHITE); g2.drawString(getText(), tx, ty); g2.dispose(); }
    }

    static class OutlinedActionButton extends JButton {
        private float anim=0f; private boolean over=false;
        OutlinedActionButton(String text){ super(text); setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false); setForeground(Color.decode("#9D4EDD")); setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15)); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            Timer t=new Timer(15, e->{ float target=over?1f:0f; if(Math.abs(anim-target)<0.05f){anim=target;((Timer)e.getSource()).stop();} else {anim+=(target-anim)*0.3f;} repaint();});
            addMouseListener(new MouseAdapter(){ @Override public void mouseEntered(MouseEvent e){over=true;if(!t.isRunning())t.start();} @Override public void mouseExited(MouseEvent e){over=false;if(!t.isRunning())t.start();} }); }
        @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); int w=getWidth(), h=getHeight(); int arc=12; Color border=anim>0?Color.decode("#7B2CBF"):Color.decode("#9D4EDD"); Color bg=anim>0?Color.decode("#F8F4FB"):Color.WHITE; g2.setColor(bg); g2.fillRoundRect(0,0,w,h,arc,arc); g2.setStroke(new BasicStroke(2f)); g2.setColor(border); g2.drawRoundRect(0,0,w-1,h-1,arc,arc); FontMetrics fm=g2.getFontMetrics(getFont()); int tx=(w-fm.stringWidth(getText()))/2; int ty=(h-fm.getHeight())/2 + fm.getAscent(); g2.setColor(Color.decode("#9D4EDD")); g2.drawString(getText(), tx, ty); g2.dispose(); }
    }
}
