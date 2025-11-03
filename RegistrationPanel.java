
import javax.swing.*;
import java.awt.*;

public class RegistrationPanel extends JPanel {
    public RegistrationPanel(MainWindow mainWindow) {
        setLayout(new BorderLayout());
        setOpaque(true);

        // Background gradient with subtle diagonal stripes
        JPanel background = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth(), h = getHeight();
                GradientPaint gp1 = new GradientPaint(0, 0, Color.decode("#E8D5F2"), 0, h/2, Color.decode("#D4A5F5"));
                g2.setPaint(gp1); g2.fillRect(0,0,w,h/2);
                g2.setPaint(new GradientPaint(0, h/2, Color.decode("#D4A5F5"), 0, h, Color.decode("#C9A5E0")));
                g2.fillRect(0,h/2,w,h);
                // stripes overlay (3% opacity white)
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.03f));
                g2.setColor(Color.WHITE);
                for (int x=-h; x<w+h; x+=20) {
                    g2.rotate(Math.toRadians(45), x, 0);
                    g2.fillRect(x, 0, 6, h*2);
                    g2.rotate(-Math.toRadians(45), x, 0);
                }
                g2.dispose();
            }
        };
        background.setLayout(new BorderLayout());
        add(background, BorderLayout.CENTER);

        // Top Navigation Bar
        JPanel top = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setPaint(new GradientPaint(0,0, Color.decode("#9D4EDD"), 0, getHeight(), Color.decode("#7B2CBF")));
                g2.fillRect(0,0,getWidth(),getHeight());
                // corner stars
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                g2.setColor(Color.decode("#D4A5F5")); g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g2.drawString("⭐", 8, 16); g2.drawString("⭐", getWidth()-24, 16);
                g2.dispose();
            }
        };
        top.setPreferredSize(new Dimension(10,70));
        JButton backBtn = new JButton("\u2190 Back to Login");
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.setForeground(Color.WHITE);
        backBtn.setBorderPainted(false); backBtn.setFocusPainted(false); backBtn.setContentAreaFilled(false);
        backBtn.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        JComponent backWrap = new JComponent(){
            private float a=0f; private boolean over=false; {
                addMouseListener(new java.awt.event.MouseAdapter(){
                    @Override public void mouseEntered(java.awt.event.MouseEvent e){ over=true; start(); }
                    @Override public void mouseExited(java.awt.event.MouseEvent e){ over=false; start(); }
                });
            }
            void start(){ new javax.swing.Timer(15, ev->{ float t=over?1f:0f; if(Math.abs(a-t)<0.05f){ a=t; ((javax.swing.Timer)ev.getSource()).stop(); repaint(); } else { a += (t-a)*0.3f; repaint(); } }).start(); }
            @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); int w=getWidth(), h=getHeight(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); double s=1.0+0.02*a; g2.translate(w/2.0,h/2.0); g2.scale(s,s); g2.translate(-w/2.0,-h/2.0); g2.setPaint(new GradientPaint(0,0, Color.decode("#FF6B9D"), 0,h, Color.decode("#FF8FAB"))); g2.fillRoundRect(0,0,w,h,10,10); g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14)); FontMetrics fm=g2.getFontMetrics(); String t="\u2190 Back to Login"; g2.drawString(t, 16, (h-fm.getHeight())/2 + fm.getAscent()); g2.dispose(); }
            @Override public Dimension getPreferredSize(){ return new Dimension(140,45); }
        };
        backWrap.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backWrap.addMouseListener(new java.awt.event.MouseAdapter(){ @Override public void mouseClicked(java.awt.event.MouseEvent e){ backBtn.doClick(); }});
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 12)); left.setOpaque(false); left.add(backWrap); top.add(left, BorderLayout.WEST);

        JLabel title = new JLabel("Create your account ✓");
        title.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 24)); title.setForeground(Color.WHITE);
        JPanel centerTitle = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 18)); centerTitle.setOpaque(false); centerTitle.add(title);
        top.add(centerTitle, BorderLayout.CENTER);
        background.add(top, BorderLayout.NORTH);

        // Scrollable centered form card
        JPanel card = new JPanel(){ @Override protected void paintComponent(Graphics g){ super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create(); // shadow
            int w=getWidth(), h=getHeight(); for(int i=8;i>=1;i--){ g2.setColor(new Color(0,0,0,10)); g2.fillRoundRect(4, 4+(8-i), w-8, h-8, 20,20);} g2.setColor(Color.WHITE); g2.fillRoundRect(0,0,w-1,h-1,20,20); g2.dispose(); } };
        card.setOpaque(false); card.setLayout(new GridBagLayout()); card.setBorder(BorderFactory.createEmptyBorder(40,40,40,40));
        card.setPreferredSize(new Dimension(700, 10));

        // Header section inside card
        GridBagConstraints gc = new GridBagConstraints(); gc.gridx=0; gc.gridy=0; gc.anchor=GridBagConstraints.WEST; gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1;
        JLabel header = new JLabel("CampusCupid Registration"); header.setFont(new Font("Segoe UI", Font.BOLD, 26)); header.setForeground(Color.decode("#7B2CBF")); card.add(header, gc);
        gc.gridy++; JLabel subtitle = new JLabel("Join thousands of students finding connections on campus"); subtitle.setFont(new Font("Segoe UI", Font.ITALIC, 13)); subtitle.setForeground(Color.decode("#9D4EDD")); card.add(subtitle, gc);
    gc.gridy++; JComponent underline = new JComponent(){ @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); int x=0; int y=getHeight()/2; g2.setPaint(new GradientPaint(x,y, Color.decode("#FF69B4"), x+280, y, Color.decode("#9D4EDD"))); g2.fillRect(0, y-2, 280, 3); g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14)); g2.drawString("💜", 288, y+4); g2.dispose(); } @Override public Dimension getPreferredSize(){ return new Dimension(300, 12);} }; card.add(underline, gc);

        // Form fields (basic subset per spec; can extend similarly)
        gc.gridy++; gc.insets=new Insets(20,0,5,0); JLabel nameLbl = new JLabel("Full Name *"); nameLbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13)); nameLbl.setForeground(Color.decode("#5A189A")); card.add(nameLbl, gc);
        gc.gridy++; JTextField name = new JTextField(); name.setPreferredSize(new Dimension(620,50)); name.setBorder(BorderFactory.createEmptyBorder(0,15,0,15)); name.setFont(new Font("Segoe UI", Font.PLAIN, 13)); name.setForeground(Color.decode("#3C096C")); card.add(roundField(name), gc);

        gc.gridy++; gc.insets=new Insets(15,0,2,0); JLabel emLbl = new JLabel("SRM Email Address *"); emLbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13)); emLbl.setForeground(Color.decode("#5A189A")); card.add(emLbl, gc);
        gc.gridy++; JPanel emailWrap = new JPanel(new BorderLayout()); emailWrap.setOpaque(false); JLabel mailIcon = new JLabel("  📧 "); mailIcon.setForeground(Color.decode("#5A189A")); JTextField email = new JTextField(); email.setPreferredSize(new Dimension(620,50)); email.setBorder(BorderFactory.createEmptyBorder(0,35,0,40)); email.setFont(new Font("Segoe UI", Font.PLAIN, 13)); email.setForeground(Color.decode("#3C096C")); emailWrap.add(mailIcon, BorderLayout.WEST); emailWrap.add(email, BorderLayout.CENTER); JLabel emailOk = new JLabel(""); emailOk.setForeground(new Color(0x3B,0xB7,0x89)); emailWrap.add(emailOk, BorderLayout.EAST); card.add(roundField(emailWrap), gc);

        gc.gridy++; gc.insets=new Insets(15,0,2,0); JLabel pwLbl = new JLabel("Create Password *"); pwLbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13)); pwLbl.setForeground(Color.decode("#5A189A")); card.add(pwLbl, gc);
        gc.gridy++; JPanel pwRow = new JPanel(new BorderLayout()); pwRow.setOpaque(false); JLabel lock = new JLabel("  🔒 "); JPasswordField pw = new JPasswordField(); pw.setBorder(BorderFactory.createEmptyBorder(0,35,0,50)); pw.setFont(new Font("Segoe UI", Font.PLAIN, 13)); JLabel show = new JLabel(" 👁️ "); show.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); show.setToolTipText("Show/Hide"); show.addMouseListener(new java.awt.event.MouseAdapter(){ boolean vis=false; @Override public void mouseClicked(java.awt.event.MouseEvent e){ vis=!vis; pw.setEchoChar(vis?(char)0:'•'); } }); JPanel rightIcons=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,10)); rightIcons.setOpaque(false); rightIcons.add(show); pwRow.add(lock, BorderLayout.WEST); pwRow.add(pw, BorderLayout.CENTER); pwRow.add(rightIcons, BorderLayout.EAST); card.add(roundField(pwRow), gc);

        gc.gridy++; gc.insets=new Insets(15,0,2,0); JLabel cpwLbl = new JLabel("Confirm Password *"); cpwLbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13)); cpwLbl.setForeground(Color.decode("#5A189A")); card.add(cpwLbl, gc);
        gc.gridy++; JPasswordField cpw = new JPasswordField(); cpw.setBorder(BorderFactory.createEmptyBorder(0,15,0,40)); card.add(roundField(cpw), gc);

        // Actions
        gc.gridy++; gc.insets=new Insets(30,0,0,0); JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0)); actions.setOpaque(false);
        JButton register = new JButton("Register & Continue ✓"); stylePrimary(register); actions.add(register);
        JButton backLogin = new JButton("\u2190 Back to Login"); styleSecondary(backLogin); actions.add(backLogin);
        card.add(actions, gc);

        // Center and make scrollable
        JPanel wrap = new JPanel(new GridBagLayout()); wrap.setOpaque(false); GridBagConstraints cc=new GridBagConstraints(); cc.gridx=0; cc.gridy=0; cc.weightx=1; cc.anchor=GridBagConstraints.NORTH; wrap.add(card, cc);
        JScrollPane sc = new JScrollPane(wrap, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); sc.setBorder(null); sc.getVerticalScrollBar().setUI(new ProfilePanel.PurpleScrollbarUI()); sc.setOpaque(false); sc.getViewport().setOpaque(false);
        background.add(sc, BorderLayout.CENTER);

        // Interactions
        backBtn.addActionListener(e -> mainWindow.showScreen("login"));
        register.addActionListener(e -> {
            String fullName = name.getText().trim();
            String em = email.getText().trim();
            String pwv = new String(pw.getPassword());
            if (fullName.length() < 3){ JOptionPane.showMessageDialog(this, "Name must be at least 3 characters"); return; }
            if (!em.matches("^[A-Za-z0-9._%+-]+@srmist\\.edu\\.in$")){ JOptionPane.showMessageDialog(this, "Use a valid @srmist.edu.in email"); return; }
            if (!mainWindow.registerUser(em, pwv, fullName)) { JOptionPane.showMessageDialog(this, "Email already registered"); return; }
            JOptionPane.showMessageDialog(this, "Account Created Successfully!");
            mainWindow.showScreen("login");
        });
    }

    // Helpers for rounded input containers styled per spec
    private JComponent roundField(JComponent inner){
        return new JPanel(new BorderLayout()){
            boolean focused=false;{ setOpaque(false); setPreferredSize(new Dimension(620,50)); setMaximumSize(new Dimension(620,50)); setBorder(BorderFactory.createEmptyBorder()); add(inner, BorderLayout.CENTER); java.awt.event.FocusListener fl=new java.awt.event.FocusAdapter(){ public void focusGained(java.awt.event.FocusEvent e){ focused=true; repaint(); } public void focusLost(java.awt.event.FocusEvent e){ focused=false; repaint(); } }; for (Component c:getComponents()){ c.addFocusListener(fl);} }
            @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); int w=getWidth(), h=getHeight(); g2.setColor(Color.decode("#F8F4FB")); g2.fillRoundRect(0,0,w,h,10,10); g2.setStroke(new BasicStroke(2f)); g2.setColor(focused?Color.decode("#9D4EDD"):Color.decode("#D4A5F5")); g2.drawRoundRect(0,0,w-1,h-1,10,10); if(focused){ g2.setColor(new Color(157,78,221,40)); g2.drawRoundRect(1,1,w-3,h-3,10,10);} g2.dispose(); }
        };
    }

    private void stylePrimary(JButton b){ b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.setFocusPainted(false); b.setBorderPainted(false); b.setContentAreaFilled(false); b.setForeground(Color.WHITE); b.setFont(new Font("Segoe UI", Font.BOLD, 16)); b.setPreferredSize(new Dimension(260,55)); }
    private void styleSecondary(JButton b){ b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.setFocusPainted(false); b.setBorderPainted(false); b.setContentAreaFilled(false); b.setForeground(Color.decode("#9D4EDD")); b.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15)); b.setPreferredSize(new Dimension(260,55)); }
}
