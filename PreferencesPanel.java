import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;

public class PreferencesPanel extends JPanel {
    // Controls
    private JComboBox<String> genderCombo;
    private JSpinner minAgeSpinner, maxAgeSpinner;
    private JTextArea interestsArea;
    private JLabel warningLabel;
    private RangeBar rangeBar;
    private SuccessBanner successBanner;

    // Sparkles animation
    private static class Sparkle { int x; int y; float phase; }
    private Sparkle[] sparkles;
    private Timer sparkleTimer;

    public PreferencesPanel(MainWindow mainWindow) {
        setLayout(new BorderLayout());
        setOpaque(true);

        // Sparkles animation init
        initSparkles();
        sparkleTimer = new Timer(40, e -> { for (Sparkle s : sparkles) s.phase += 0.02f; repaint(); });
        sparkleTimer.start();

        // Top navigation bar
        TopNavBar nav = new TopNavBar(mainWindow);
        add(nav, BorderLayout.NORTH);

        // Main content scrollable
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(40, 0, 60, 0));

        // Centered main card
        MainCard card = new MainCard(mainWindow);
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(card);

        // Success banner overlay
        successBanner = new SuccessBanner();
        add(successBanner, BorderLayout.SOUTH);

        JScrollPane scroller = new JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.setBorder(null);
        scroller.getVerticalScrollBar().setUnitIncrement(16);
        scroller.getVerticalScrollBar().setUI(new PurpleScrollbarUI());
        add(scroller, BorderLayout.CENTER);
    }

    // Background gradient + sparkles
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g.create();
        int w=getWidth(), h=getHeight();
        g2.setPaint(new GradientPaint(0,0, Color.decode("#FAF0FF"), 0,h, Color.decode("#E8D5F2")));
        g2.fillRect(0,0,w,h);
        if (sparkles != null) {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            g2.setColor(new Color(0xD4,0xA5,0xF5, 77)); // #D4A5F5 30% opacity
            for (Sparkle s : sparkles) {
                int dy = (int)(Math.sin(s.phase*2*Math.PI)*3);
                g2.drawString("✨", s.x % Math.max(w-20,1), s.y + dy);
            }
        }
        g2.dispose();
    }

    private void initSparkles(){
        java.util.Random rnd = new java.util.Random(99);
        sparkles = new Sparkle[10];
        for (int i=0;i<sparkles.length;i++){
            Sparkle s = new Sparkle();
            s.x = rnd.nextInt(1000);
            s.y = 120 + rnd.nextInt(400);
            s.phase = rnd.nextFloat();
            sparkles[i]=s;
        }
    }

    // Top navigation bar implementation
    static class TopNavBar extends JPanel {
        TopNavBar(MainWindow mw){
            setPreferredSize(new Dimension(10,70)); setOpaque(false); setLayout(new BorderLayout());
            // Left back button
            GlossyButton back = new GlossyButton("\u2190 Back to Profile", new Color(0xFF6B9D), new Color(0xFF8FAB));
            back.setPreferredSize(new Dimension(150,45)); back.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
            back.addActionListener(e -> mw.showScreen("profile"));
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12)); left.setOpaque(false); left.add(back);
            add(left, BorderLayout.WEST);
            // Center title
            JLabel title = new JLabel("Preferences", SwingConstants.CENTER);
            title.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 24)); title.setForeground(Color.WHITE);
            JPanel center = new JPanel(new GridBagLayout()); center.setOpaque(false); center.add(title);
            add(center, BorderLayout.CENTER);
            // Right buttons
            GlossyButton swipe = new GlossyButton("Go to Swipe", new Color(0xFFB347), new Color(0xFF9E2C)); swipe.setPreferredSize(new Dimension(150,45));
            GlossyButton chats = new GlossyButton("Go to Chats", new Color(0x45D09E), new Color(0x3BB789)); chats.setPreferredSize(new Dimension(150,45));
            swipe.addActionListener(e -> mw.showScreen("swipe"));
            chats.addActionListener(e -> mw.showScreen("chats"));
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12)); right.setOpaque(false); right.add(swipe); right.add(chats); right.setBorder(BorderFactory.createEmptyBorder(0,0,0,15));
            add(right, BorderLayout.EAST);
        }
        @Override protected void paintComponent(Graphics g){ super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create(); int w=getWidth(), h=getHeight(); g2.setPaint(new GradientPaint(0,0, Color.decode("#9D4EDD"), 0,h, Color.decode("#7B2CBF"))); g2.fillRect(0,0,w,h); g2.dispose(); }
    }

    static class GlossyButton extends JButton {
        private boolean over=false; private float anim=0f; private final Color c1,c2;
        GlossyButton(String text, Color c1, Color c2){ super(text); this.c1=c1; this.c2=c2; setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false); setForeground(Color.WHITE); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            Timer t=new Timer(15, e->{ float target=over?1f:0f; if(Math.abs(anim-target)<0.05f){anim=target;((Timer)e.getSource()).stop();} else {anim+=(target-anim)*0.3f;} repaint();});
            addMouseListener(new MouseAdapter(){ @Override public void mouseEntered(MouseEvent e){over=true;if(!t.isRunning())t.start();} @Override public void mouseExited(MouseEvent e){over=false;if(!t.isRunning())t.start();}});
        }
        @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); int w=getWidth(), h=getHeight(); int arc=10; double scale=1.0+0.02*anim; g2.translate(w/2.0,h/2.0); g2.scale(scale,scale); g2.translate(-w/2.0,-h/2.0); g2.setPaint(new GradientPaint(0,0,c1,0,h,c2)); g2.fillRoundRect(0,0,w,h,arc,arc); if(anim>0){ g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f*anim)); g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(2.5f)); g2.drawRoundRect(1,1,w-2,h-2,arc,arc);} FontMetrics fm=g2.getFontMetrics(getFont()); int tx=(w-fm.stringWidth(getText()))/2; int ty=(h-fm.getHeight())/2 + fm.getAscent(); g2.setColor(Color.WHITE); g2.drawString(getText(), tx, ty); g2.dispose(); }
    }

    static class PurpleScrollbarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors(){ this.thumbColor=Color.decode("#9D4EDD"); this.trackColor=Color.decode("#F8F4FB"); }
        @Override public Dimension getPreferredSize(JComponent c){ return new Dimension(10, super.getPreferredSize(c).height); }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds){ Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(thumbColor); g2.fillRoundRect(thumbBounds.x+2,thumbBounds.y,thumbBounds.width-4,thumbBounds.height,10,10); g2.dispose(); }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds){ Graphics2D g2=(Graphics2D)g.create(); g2.setColor(trackColor); g2.fillRect(trackBounds.x,trackBounds.y,trackBounds.width,trackBounds.height); g2.dispose(); }
    }

    class MainCard extends JPanel {
        MainCard(MainWindow mw){ setOpaque(false); setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); setPreferredSize(new Dimension(650, 10)); setMaximumSize(new Dimension(800, Integer.MAX_VALUE)); setBorder(BorderFactory.createEmptyBorder(40,40,40,40));
            // Header
            JLabel title = new JLabel("Set Your Preferences", SwingConstants.CENTER); title.setFont(new Font("Segoe UI", Font.BOLD, 26)); title.setForeground(Color.decode("#7B2CBF")); title.setAlignmentX(Component.CENTER_ALIGNMENT); add(title);
            JLabel subtitle = new JLabel("Help us find your perfect match", SwingConstants.CENTER); subtitle.setFont(new Font("Segoe UI", Font.ITALIC, 14)); subtitle.setForeground(Color.decode("#9D4EDD")); subtitle.setAlignmentX(Component.CENTER_ALIGNMENT); add(Box.createVerticalStrut(4)); add(subtitle);
            add(Box.createVerticalStrut(6));
            JComponent underline = new JComponent(){ @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); int w=getWidth(); int x=(w-250)/2; int y=(getHeight()-3)/2; g2.setPaint(new GradientPaint(x,y, Color.decode("#FF69B4"), x+250, y, Color.decode("#9D4EDD"))); g2.fillRect(x,y,250,3); g2.setColor(Color.decode("#7B2CBF")); g2.setFont(new Font("Segoe UI", Font.PLAIN, 14)); g2.drawString("💜", x+250+6, y+10); g2.dispose(); } };
            underline.setPreferredSize(new Dimension(10, 16)); add(underline);
            add(Box.createVerticalStrut(25));

            // Section 1: Gender
            SectionPanel genderSec = new SectionPanel();
            JLabel glbl = sectionLabel("Preferred Gender:"); glbl.setAlignmentX(Component.CENTER_ALIGNMENT); genderSec.add(glbl);
            JLabel ghelp = helperText("Who would you like to meet?"); ghelp.setAlignmentX(Component.CENTER_ALIGNMENT); genderSec.add(ghelp);
            String[] genders = {"Any 🌟", "Male 👨", "Female 👩", "Other ⚧️"};
            genderCombo = new JComboBox<>(genders);
            genderCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            genderCombo.setBackground(Color.WHITE);
            genderCombo.setRenderer(new DefaultListCellRenderer(){ @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus){ Component c=super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus); if (c instanceof JLabel) { JLabel l=(JLabel)c; l.setOpaque(true); l.setBorder(BorderFactory.createEmptyBorder(8,15,8,15)); l.setBackground(isSelected? new Color(0xD4A5F5) : Color.WHITE); if (index>=0 && !isSelected) l.setBackground(index%2==0?Color.WHITE:Color.decode("#FAF0FF")); l.setForeground(Color.decode("#3C096C")); } return c; } });
            JPanel comboWrap = roundedWrap(genderCombo, 540, 50); comboWrap.setAlignmentX(Component.CENTER_ALIGNMENT);
            genderSec.add(comboWrap);
            add(genderSec);

            // Section 2: Age Range
            add(Box.createVerticalStrut(20));
            SectionPanel ageSec = new SectionPanel();
            JLabel albl = sectionLabel("Age Range:"); albl.setAlignmentX(Component.CENTER_ALIGNMENT); ageSec.add(albl);
            JLabel ahelp = helperText("Select the age range you're interested in"); ahelp.setAlignmentX(Component.CENTER_ALIGNMENT); ageSec.add(ahelp);
            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); row.setOpaque(false);
            row.add(miniLabel("Min Age:"));
            minAgeSpinner = spinner(18); setSpinnerModel(minAgeSpinner, 18, 60, 1); row.add(roundedWrap(minAgeSpinner, 120, 50));
            row.add(centerLabel("to"));
            row.add(miniLabel("Max Age:"));
            maxAgeSpinner = spinner(24); setSpinnerModel(maxAgeSpinner, 18, 60, 1); row.add(roundedWrap(maxAgeSpinner, 120, 50));
            ageSec.add(row);
            warningLabel = new JLabel(""); warningLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12)); warningLabel.setForeground(new Color(0xFFB347)); warningLabel.setAlignmentX(Component.CENTER_ALIGNMENT); ageSec.add(Box.createVerticalStrut(6)); ageSec.add(warningLabel);
            ageSec.add(Box.createVerticalStrut(10));
            rangeBar = new RangeBar(); rangeBar.setPreferredSize(new Dimension(540, 16)); rangeBar.setMaximumSize(new Dimension(540, 16)); rangeBar.setAlignmentX(Component.CENTER_ALIGNMENT); ageSec.add(rangeBar);
            // react to spinner changes
            ChangeListener cl = e -> { int min=(Integer)minAgeSpinner.getValue(); int max=(Integer)maxAgeSpinner.getValue(); if (max<min) { warningLabel.setText("⚠️ Max age must be greater than Min age"); } else warningLabel.setText(""); rangeBar.setRange(min, max); };
            minAgeSpinner.addChangeListener(cl); maxAgeSpinner.addChangeListener(cl);
            add(ageSec);

            // Section 3: Interests
            add(Box.createVerticalStrut(20));
            SectionPanel interestsSec = new SectionPanel();
            JLabel ilbl = sectionLabel("Shared Interests:"); ilbl.setAlignmentX(Component.CENTER_ALIGNMENT); interestsSec.add(ilbl);
            JLabel ihelp = helperText("Find people with similar interests"); ihelp.setAlignmentX(Component.CENTER_ALIGNMENT); interestsSec.add(ihelp);
            interestsArea = new JTextArea(); interestsArea.setLineWrap(true); interestsArea.setWrapStyleWord(true); interestsArea.setFont(new Font("Segoe UI", Font.PLAIN, 13)); interestsArea.setForeground(Color.decode("#3C096C"));
            PlaceholderSupport.install(interestsArea, "e.g., gaming, movies, fitness, coding...");
            JScrollPane isp = new JScrollPane(interestsArea); isp.setBorder(null); isp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); isp.getVerticalScrollBar().setUI(new PurpleScrollbarUI());
            JPanel areaWrap = roundedWrap(isp, 540, 100); areaWrap.setAlignmentX(Component.CENTER_ALIGNMENT); interestsSec.add(areaWrap);
            JLabel counter = new JLabel("0/200"); counter.setFont(new Font("Segoe UI", Font.PLAIN, 10)); counter.setForeground(Color.decode("#9D4EDD")); counter.setAlignmentX(Component.CENTER_ALIGNMENT);
            interestsArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){ private void upd(){ int n=Math.min(200, interestsArea.getText().length()); counter.setText(n+"/200"); } public void insertUpdate(javax.swing.event.DocumentEvent e){upd();} public void removeUpdate(javax.swing.event.DocumentEvent e){upd();} public void changedUpdate(javax.swing.event.DocumentEvent e){upd();}});
            interestsSec.add(Box.createVerticalStrut(4)); interestsSec.add(counter);
            // Chips
            JPanel chipRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0)); chipRow.setOpaque(false);
            String[] chips = {"Gaming 🎮","Movies 🎬","Fitness 💪","Music 🎵","Travel ✈️","Food 🍕","Coding 💻","Photography 📸"};
            for (String c : chips) { ChipButton b = new ChipButton(c); b.addActionListener(e -> addInterest(c)); chipRow.add(b);}            
            JPanel popularRow = new JPanel(); popularRow.setOpaque(false); popularRow.setLayout(new BoxLayout(popularRow, BoxLayout.Y_AXIS));
            JLabel popular = new JLabel("Popular:"); popular.setFont(new Font("Segoe UI", Font.PLAIN, 11)); popular.setForeground(Color.decode("#7B2CBF")); popular.setAlignmentX(Component.CENTER_ALIGNMENT);
            popularRow.add(popular); popularRow.add(Box.createVerticalStrut(6)); popularRow.add(chipRow);
            interestsSec.add(Box.createVerticalStrut(10)); interestsSec.add(popularRow);
            add(interestsSec);

            // Load saved prefs
            String savedGender = mw.getPrefGender();
            int savedMin = mw.getPrefMinAge();
            int savedMax = mw.getPrefMaxAge();
            String savedInterests = mw.getPrefInterests();
            if (savedGender != null) {
                String g = savedGender;
                if (g.equals("Any")) g = "Any 🌟"; else if (g.equals("Male")) g = "Male 👨"; else if (g.equals("Female")) g = "Female 👩"; else if (g.equals("Other")) g = "Other ⚧️";
                genderCombo.setSelectedItem(g);
            }
            if (savedMin>=18 && savedMin<=60) minAgeSpinner.setValue(savedMin);
            if (savedMax>=18 && savedMax<=60) maxAgeSpinner.setValue(savedMax);
            rangeBar.setRange((Integer)minAgeSpinner.getValue(), (Integer)maxAgeSpinner.getValue());
            interestsArea.setText(savedInterests!=null?savedInterests:"");

            // Buttons
            add(Box.createVerticalStrut(20));
            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0)); btnRow.setOpaque(false);
            GradientActionButton save = new GradientActionButton("Save Preferences ✓", Color.decode("#9D4EDD"), Color.decode("#7B2CBF")); save.setPreferredSize(new Dimension(260,55));
            GradientActionButton startSwipe = new GradientActionButton("Start Swiping →", Color.decode("#FFB347"), Color.decode("#FF9E2C")); startSwipe.setPreferredSize(new Dimension(260,55));
            btnRow.add(save); btnRow.add(startSwipe); add(btnRow);

            // Save action
            ActionListener saveAction = e -> {
                if ((Integer)maxAgeSpinner.getValue() < (Integer)minAgeSpinner.getValue()) {
                    warningLabel.setText("⚠️ Max age must be greater than Min age"); return;
                }
                String gsel = (String) genderCombo.getSelectedItem();
                String gender = gsel==null?"Any": gsel.split(" ")[0]; // strip icon
                int min = (Integer)minAgeSpinner.getValue();
                int max = (Integer)maxAgeSpinner.getValue();
                String interests = trimLen(interestsArea.getText().trim().toLowerCase(), 200);
                mw.setUserPreferences(gender, min, max, interests);
                successBanner.flash("Preferences saved!");
            };
            save.addActionListener(saveAction);
            startSwipe.addActionListener(e -> { saveAction.actionPerformed(null); mw.showScreen("swipe"); });
        }

        @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); int w=getWidth(), h=getHeight(); for (int i=8;i>=1;i--){ g2.setColor(new Color(0,0,0,35)); g2.fillRoundRect(4, 6+(8-i), w-8, h-12, 20,20);} g2.setColor(Color.WHITE); g2.fillRoundRect(0,0,w-1,h-1,20,20); g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0,0,w-1,h-1,20,20)); g2.setColor(Color.decode("#FAF0FF")); Path2D p=new Path2D.Float(); int baseY=h-18; p.moveTo(0,baseY); for(int x=0;x<=w;x+=18){ double y=baseY-5*Math.sin(x/28.0); p.lineTo(x,y);} p.lineTo(w,h); p.lineTo(0,h); p.closePath(); g2.fill(p); g2.setClip(null); g2.dispose(); super.paintComponent(g); }

        private JLabel sectionLabel(String t){ JLabel l=new JLabel(t); l.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15)); l.setForeground(Color.decode("#5A189A")); l.setAlignmentX(Component.LEFT_ALIGNMENT); return l; }
        private JLabel helperText(String t){ JLabel l=new JLabel(t); l.setFont(new Font("Segoe UI", Font.ITALIC, 11)); l.setForeground(Color.decode("#9D4EDD")); l.setAlignmentX(Component.LEFT_ALIGNMENT); return l; }
        private JLabel miniLabel(String t){ JLabel l=new JLabel(t); l.setFont(new Font("Segoe UI", Font.PLAIN, 13)); l.setForeground(Color.decode("#5A189A")); return l; }
        private JLabel centerLabel(String t){ JLabel l=new JLabel(t); l.setFont(new Font("Segoe UI", Font.PLAIN, 13)); l.setForeground(Color.decode("#7B2CBF")); return l; }
        private JPanel roundedWrap(JComponent inner, int w, int h){ JPanel p=new JPanel(new BorderLayout()){ boolean focused=false; { setOpaque(false);} @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(Color.WHITE); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10); g2.setStroke(new BasicStroke(2f)); g2.setColor(focused?Color.decode("#9D4EDD"):Color.decode("#D4A5F5")); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10); g2.dispose(); super.paintComponent(g);} }; p.setPreferredSize(new Dimension(w,h)); p.setMaximumSize(new Dimension(w,h)); p.setBorder(BorderFactory.createEmptyBorder(0,15,0,15)); p.add(inner, BorderLayout.CENTER); return p; }
        private JSpinner spinner(int val){ JSpinner s=new JSpinner(new SpinnerNumberModel(val,18,60,1)); s.setOpaque(false); ((JSpinner.DefaultEditor)s.getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER); ((JSpinner.DefaultEditor)s.getEditor()).getTextField().setFont(new Font("Segoe UI", Font.BOLD, 16)); ((JSpinner.DefaultEditor)s.getEditor()).getTextField().setForeground(Color.decode("#7B2CBF")); return s; }
        private void setSpinnerModel(JSpinner s, int min, int max, int step){ s.setModel(new SpinnerNumberModel((Integer)s.getValue(), Integer.valueOf(min), Integer.valueOf(max), Integer.valueOf(step))); }
        private void addInterest(String chip){ String txt=interestsArea.getText(); if (txt.isBlank()) interestsArea.setText(chip); else if (!txt.toLowerCase().contains(chip.toLowerCase())) interestsArea.setText(txt + (txt.trim().endsWith(",")?" ":", ") + chip); }
        private String trimLen(String s, int n){ return s.length()<=n? s : s.substring(0,n); }
    }

    static class SectionPanel extends JPanel { SectionPanel(){ setOpaque(false); setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); JPanel pad=new JPanel(); pad.setOpaque(false); add(pad); setBorder(BorderFactory.createEmptyBorder(0,0,0,0)); }
        @Override protected void paintComponent(Graphics g){ super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); int w=getWidth(); int h=getHeight(); g2.setColor(Color.decode("#F8F4FB")); g2.fillRoundRect((w-570)/2, 0, 570, h, 15,15); g2.dispose(); }
        @Override public Dimension getMaximumSize(){ return new Dimension(Integer.MAX_VALUE, super.getPreferredSize().height); }
        @Override public Insets getInsets(){ return new Insets(20, (getWidth()-570)/2 + 20, 20, (getWidth()-570)/2 + 20); }
    }

    static class ChipButton extends JButton { private boolean over=false; private float anim=0f; ChipButton(String t){ super(t); setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false); setFont(new Font("Segoe UI", Font.PLAIN, 12)); setForeground(Color.decode("#7B2CBF")); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); setBorder(BorderFactory.createEmptyBorder(6,14,6,14)); Timer timer=new Timer(15, e->{ float target=over?1f:0f; if(Math.abs(anim-target)<0.05f){anim=target;((Timer)e.getSource()).stop();} else {anim+=(target-anim)*0.3f;} repaint();}); addMouseListener(new MouseAdapter(){ @Override public void mouseEntered(MouseEvent e){over=true;if(!timer.isRunning())timer.start();} @Override public void mouseExited(MouseEvent e){over=false;if(!timer.isRunning())timer.start();} }); }
        @Override public Dimension getPreferredSize(){ Dimension d=super.getPreferredSize(); d.height=32; return d; }
        @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); int w=getWidth(), h=getHeight(); Color bg= (anim>0? Color.decode("#D4A5F5"): Color.decode("#E8D5F2")); g2.setColor(bg); g2.fillRoundRect(0,0,w,h,16,16); g2.setColor(Color.decode("#D4A5F5")); g2.drawRoundRect(0,0,w-1,h-1,16,16); FontMetrics fm=g2.getFontMetrics(getFont()); int tx=(w-fm.stringWidth(getText()))/2; int ty=(h-fm.getHeight())/2 + fm.getAscent(); g2.setColor(Color.decode("#7B2CBF")); g2.drawString(getText(), tx, ty); g2.dispose(); }
    }

    static class GradientActionButton extends JButton { private float hover=0f; private boolean over=false; private final Color c1,c2; GradientActionButton(String t, Color c1, Color c2){ super(t); this.c1=c1; this.c2=c2; setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false); setForeground(Color.WHITE); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); setFont(new Font("Segoe UI", Font.BOLD, 16)); Timer timer=new Timer(15, e->{ float target=over?1f:0f; if(Math.abs(hover-target)<0.05f){hover=target;((Timer)e.getSource()).stop();} else {hover+=(target-hover)*0.3f;} repaint();}); addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){over=true;if(!timer.isRunning())timer.start();}@Override public void mouseExited(MouseEvent e){over=false;if(!timer.isRunning())timer.start();}});} @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); int w=getWidth(), h=getHeight(); g2.setPaint(new GradientPaint(0,0,c1,0,h,c2)); g2.fillRoundRect(0,0,w,h,12,12); if(hover>0){ g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f)); g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(2f)); g2.drawRoundRect(1,1,w-2,h-2,12,12);} FontMetrics fm=g2.getFontMetrics(getFont()); int tx=(w-fm.stringWidth(getText()))/2; int ty=(h-fm.getHeight())/2 + fm.getAscent(); g2.setColor(Color.WHITE); g2.drawString(getText(), tx, ty); g2.dispose(); super.paintComponent(g);} }

    static class RangeBar extends JComponent { private float curMin=18, curMax=24; void setRange(int min, int max){ curMin=min; curMax=max; repaint(); }
        @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); int w=getWidth(), h=getHeight(); int r=6; // background gradient from 18..60
            g2.setColor(new Color(0xFF,0xE5,0xF1)); g2.fillRoundRect(0,0,w,h,r,r);
            // selected min/max relative positions
            float minPos = (curMin-18f)/(60f-18f); float maxPos=(curMax-18f)/(60f-18f); int x1=(int)(minPos*w); int x2=(int)(maxPos*w);
            g2.setPaint(new GradientPaint(x1,0, Color.decode("#FF69B4"), x2,0, Color.decode("#9D4EDD")));
            g2.fillRoundRect(x1,0, Math.max(4,x2-x1), h, r,r);
            g2.dispose(); }
    }

    static class SuccessBanner extends JPanel { private String text=""; private Timer hideTimer; SuccessBanner(){ setOpaque(false); setPreferredSize(new Dimension(10, 0)); }
        void flash(String t){ this.text=t; setPreferredSize(new Dimension(10, 36)); revalidate(); repaint(); if (hideTimer!=null && hideTimer.isRunning()) hideTimer.stop(); hideTimer=new Timer(3000, e->{ text=""; setPreferredSize(new Dimension(10,0)); revalidate(); repaint(); }); hideTimer.setRepeats(false); hideTimer.start(); }
        @Override protected void paintComponent(Graphics g){ super.paintComponent(g); if (text.isEmpty()) return; Graphics2D g2=(Graphics2D)g.create(); int w=getWidth(); g2.setColor(new Color(0x3B,0xB7,0x89)); g2.fillRect(0,0,w,36); g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI", Font.BOLD, 13)); FontMetrics fm=g2.getFontMetrics(); int tw=fm.stringWidth(text); g2.drawString(text, (w-tw)/2, (36-fm.getHeight())/2+fm.getAscent()); g2.dispose(); }
    }

    static class PlaceholderSupport { static void install(JTextArea area, String placeholder){ area.addFocusListener(new FocusAdapter(){ @Override public void focusGained(FocusEvent e){ area.repaint(); } @Override public void focusLost(FocusEvent e){ area.repaint(); }}); area.putClientProperty("placeholder", placeholder); area.setBorder(BorderFactory.createEmptyBorder(8,12,8,12)); }
    }
}
