import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.util.*;

public class SwipePanel extends JPanel {

    private final MainWindow mainWindow;
    // Data
    private java.util.List<Candidate> candidates = new ArrayList<>();
    private int currentIndex = 0;

    // UI root pieces
    private JLayeredPane centerLayer; // holds card + overlays
    private CardWrapper cardWrapper;  // transformed card during drags
    private ProfileCard card;         // actual card content
    private StyledButton btnPass, btnLike, btnRefresh, btnBack, btnChats;
    private LoadingOverlay loadingOverlay;
    private EmptyOverlay emptyOverlay;
    private MatchOverlay matchOverlay;

    // Drag/animation state
    private int dragStartX=-1, dragStartY=-1;
    private double dragDx=0, dragDy=0, dragAngle=0; // angle in radians
    private javax.swing.Timer animTimer; // reuse for snapback/exit/pulse

    public SwipePanel(MainWindow mainWindow){
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout());
        setOpaque(false);

        // Top nav
        JPanel topNav = new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0,0, Color.decode("#9D4EDD"), 0,getHeight(), Color.decode("#7B2CBF")));
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
            }
        };
        topNav.setPreferredSize(new Dimension(10,70));

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        rightBtns.setOpaque(false);
        btnBack = StyledButton.gradient("Back to Profile", new Color(0xFF69B4), new Color(0xFF8FAB));
        btnBack.setPreferredSize(new Dimension(160,45));
        btnBack.addActionListener(e -> mainWindow.showScreen("profile"));
        btnChats = StyledButton.gradient("Go to Chats", new Color(0x45D09E), new Color(0x3BB789));
        btnChats.setPreferredSize(new Dimension(140,45));
        btnChats.addActionListener(e -> mainWindow.showScreen("chats"));
        rightBtns.setBorder(new EmptyBorder(0,0,0,15));
        rightBtns.add(btnBack); rightBtns.add(btnChats);
        topNav.add(rightBtns, BorderLayout.EAST);
        add(topNav, BorderLayout.NORTH);

        // Center layered area (card + overlays)
        centerLayer = new JLayeredPane(){
            @Override protected void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                int w=getWidth(), h=getHeight();
                GradientPaint gp = new GradientPaint(0,0, Color.decode("#FAF0FF"), 0, (int)(h*0.6), Color.decode("#E8D5F2"));
                g2.setPaint(gp); g2.fillRect(0,0,w,h);
                g2.setPaint(new GradientPaint(0,(int)(h*0.6), Color.decode("#E8D5F2"), 0,h, Color.decode("#D4A5F5")));
                g2.fillRect(0,(int)(h*0.6),w,h);
                // floating decorative hearts
                long t=System.currentTimeMillis();
                int[] xs={40, w-60, 90, w-120, 60, w-80, w/2};
                Color[] cs={Color.decode("#FF69B4"), Color.decode("#9D4EDD"), Color.decode("#FFB347"), Color.decode("#FF69B4"), Color.decode("#9D4EDD"), Color.decode("#FFB347"), Color.decode("#FF69B4")};
                for (int i=0;i<xs.length;i++){
                    int phase = (i*3333);
                    int baseY = 120 + (i%3)*50;
                    int y = baseY + (int)(Math.sin((t+phase)/1000.0)*10);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
                    g2.setColor(cs[i]);
                    g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18 + (i%3)*6));
                    g2.drawString("❤", xs[i], y);
                }
                g2.dispose();
            }
            @Override public void doLayout(){
                super.doLayout();
                int w = getWidth(); int h=getHeight();
                // Make the card larger when there is space, but ensure it always fits including actions bar
                int cardW = Math.min(540, Math.max(460, (int)(w*0.5)));
                int cardH = Math.min(720, Math.max(560, h - 100));
                int y = Math.max(20, (h - cardH) / 2);
                int x = (w - cardW) / 2;
                if (cardWrapper!=null) cardWrapper.setBounds(x,y,cardW,cardH);
                if (loadingOverlay!=null) loadingOverlay.setBounds(0,0,w,h);
                if (emptyOverlay!=null) emptyOverlay.setBounds(x,y,cardW,cardH);
                if (matchOverlay!=null) matchOverlay.setBounds(0,0,w,h);
            }
        };
        add(centerLayer, BorderLayout.CENTER);

        // Build card
        card = new ProfileCard();
        cardWrapper = new CardWrapper(card);
        centerLayer.add(cardWrapper, JLayeredPane.DEFAULT_LAYER);

        // Overlays
        loadingOverlay = new LoadingOverlay();
        centerLayer.add(loadingOverlay, JLayeredPane.PALETTE_LAYER);

        emptyOverlay = new EmptyOverlay();
        centerLayer.add(emptyOverlay, JLayeredPane.MODAL_LAYER);
        emptyOverlay.setVisible(false);

        matchOverlay = new MatchOverlay();
        centerLayer.add(matchOverlay, JLayeredPane.DRAG_LAYER);
        matchOverlay.setVisible(false);

        // Mouse interactions for swipe
        MouseAdapter drag = new MouseAdapter(){
            @Override public void mousePressed(MouseEvent e){ dragStartX=e.getXOnScreen(); dragStartY=e.getYOnScreen(); }
            @Override public void mouseDragged(MouseEvent e){
                if (dragStartX<0) return;
                dragDx = e.getXOnScreen()-dragStartX; dragDy = e.getYOnScreen()-dragStartY;
                dragAngle = Math.max(-Math.toRadians(15), Math.min(Math.toRadians(15), (dragDx/200.0)));
                cardWrapper.setTransform(dragDx, dragDy, dragAngle);
                cardWrapper.setOverlayHint(dragDx);
            }
            @Override public void mouseReleased(MouseEvent e){
                if (Math.abs(dragDx) > 100){
                    boolean like = dragDx>0;
                    animateCardExit(like);
                } else {
                    animateSnapBack();
                }
                dragStartX=dragStartY=-1;
            }
        };
        cardWrapper.addMouseListener(drag);
        cardWrapper.addMouseMotionListener(drag);

        // Action buttons (bottom of card content)
        btnPass = card.actions.pass;
        btnLike = card.actions.like;
        btnRefresh = card.actions.refresh;

        btnPass.addActionListener(e -> animateCardExit(false));
        btnLike.addActionListener(e -> animateCardExit(true));
        btnRefresh.addActionListener(e -> reload());

        // Initial load
        reload();
    }

    // ------------ Data loading ------------
    public void reload(){
        showLoading(true);
        new SwingWorker<java.util.List<Candidate>, Void>(){
            @Override protected java.util.List<Candidate> doInBackground(){
                java.util.List<Candidate> list = new ArrayList<>();
                Integer uid = mainWindow.getLoggedInUserId(); if (uid==null) return list;
                String g = mainWindow.getPrefGender();
                int minA = mainWindow.getPrefMinAge();
                int maxA = mainWindow.getPrefMaxAge();
                String ints = mainWindow.getPrefInterests();
                java.util.List<Map<String,Object>> rows = Database.listCandidates(uid, g, minA, maxA, ints);
                for (Map<String,Object> r : rows){
                    Integer age = (Integer) r.get("age");
                    list.add(new Candidate(
                            (Integer)r.get("id"),
                            (String)r.get("name"),
                            (String)r.get("gender"),
                            age==null?0:age,
                            (String)r.get("interests"),
                            (String)r.get("bio"),
                            (String)r.get("photoUrl")
                    ));
                }
                return list;
            }
            @Override protected void done(){
                try {
                    candidates = get();
                } catch (Exception ex) { candidates = new ArrayList<>(); }
                currentIndex = 0;
                showLoading(false);
                updateCard();
            }
        }.execute();
    }

    private void updateCard(){
        Candidate c = currentIndex < candidates.size()? candidates.get(currentIndex) : null;
        if (c == null){
            emptyOverlay.setVisible(true);
            card.setCandidate(null);
            btnPass.setEnabled(false); btnLike.setEnabled(false);
            return;
        }
        emptyOverlay.setVisible(false);
        card.setCandidate(c);
        btnPass.setEnabled(true); btnLike.setEnabled(true);
        cardWrapper.resetTransform();
    }

    // ------------ Animations ------------
    private void animateSnapBack(){
        if (animTimer!=null && animTimer.isRunning()) animTimer.stop();
        animTimer = new javax.swing.Timer(15, null);
        final double startDx = dragDx, startDy = dragDy, startAng = dragAngle;
        final long t0 = System.currentTimeMillis();
        animTimer.addActionListener(e->{
            double p = Math.min(1.0, (System.currentTimeMillis()-t0)/200.0); // 200ms
            double ease = 1 - Math.pow(1-p, 3);
            double ndx = startDx*(1-ease);
            double ndy = startDy*(1-ease);
            double nang = startAng*(1-ease);
            cardWrapper.setTransform(ndx, ndy, nang);
            if (p>=1){ ((javax.swing.Timer)e.getSource()).stop(); cardWrapper.setOverlayHint(0); }
        });
        animTimer.start();
    }

    private void animateCardExit(boolean like){
        if (animTimer!=null && animTimer.isRunning()) animTimer.stop();
        animTimer = new javax.swing.Timer(15, null);
        final double dir = like? 1 : -1;
        final long t0 = System.currentTimeMillis();
        animTimer.addActionListener(e->{
            double p = Math.min(1.0, (System.currentTimeMillis()-t0)/400.0); // 400ms
            double ease = 1 - Math.pow(1-p, 3);
            double ndx = dragDx + dir* (200 + getWidth())*ease;
            double ndy = dragDy + 40*ease;
            double nang = (Math.toRadians(15))*dir*ease;
            cardWrapper.setTransform(ndx, ndy, nang);
            cardWrapper.setOverlayHint(dir*200); // keep icon showing
            cardWrapper.setOpacity((float)(1.0 - 0.6*ease));
            if (p>=1){ ((javax.swing.Timer)e.getSource()).stop(); onSwipeComplete(like); }
        });
        animTimer.start();
    }

    private void onSwipeComplete(boolean like){
        Candidate c = currentIndex < candidates.size()? candidates.get(currentIndex) : null;
        if (c != null){
            boolean matched = Database.recordSwipe(mainWindow.getLoggedInUserId(), c.userId, like);
            if (matched && like){
                showMatchOverlay(c);
            }
        }
        currentIndex++;
        dragDx=dragDy=0; dragAngle=0; cardWrapper.resetOpacity(); cardWrapper.setOverlayHint(0);
        updateCard();
        // bring next card from top
        animateCardIntro();
    }

    private void animateCardIntro(){
        if (card==null) return;
        if (animTimer!=null && animTimer.isRunning()) animTimer.stop();
        animTimer = new javax.swing.Timer(15, null);
        final long t0 = System.currentTimeMillis();
        animTimer.addActionListener(e->{
            double p = Math.min(1.0, (System.currentTimeMillis()-t0)/400.0);
            double ease = 1 - Math.pow(1-p, 3);
            double ty = -80*(1-ease);
            double scale = 0.95 + 0.05*ease;
            cardWrapper.setIntroTransform(ty, scale);
            if (p>=1){ ((javax.swing.Timer)e.getSource()).stop(); cardWrapper.clearIntroTransform(); }
        });
        animTimer.start();
    }

    private void showLoading(boolean show){ loadingOverlay.setVisible(show); }

    private void showMatchOverlay(Candidate c){
        matchOverlay.showFor(c);
    }

    // ------------ Components ------------
    private static class StyledButton extends JButton {
        private float scale = 1f; private boolean glow=false; private Color c1, c2; private boolean gradient;
        private StyledButton(String text){ super(text); setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(false); setForeground(Color.WHITE); setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14)); addMouseListener(new MouseAdapter(){ @Override public void mouseEntered(MouseEvent e){ animateTo(1.02f, true);} @Override public void mouseExited(MouseEvent e){ animateTo(1f, false);} }); }
        static StyledButton gradient(String text, Color c1, Color c2){ StyledButton b = new StyledButton(text); b.gradient=true; b.c1=c1; b.c2=c2; return b; }
        static StyledButton outlinePink(String text){ StyledButton b = new StyledButton(text); b.gradient=false; b.c1 = Color.WHITE; b.c2 = new Color(0xFF,0x6B,0x9D); b.setForeground(new Color(0xFF,0x6B,0x9D)); return b; }
    private void animateTo(float target, boolean glow){ javax.swing.Timer t = new javax.swing.Timer(15,null); t.addActionListener(e->{ float d = target - scale; if (Math.abs(d) < 0.01f){ scale=target; this.glow=glow; ((javax.swing.Timer)e.getSource()).stop(); repaint(); } else { scale += d*0.3f; this.glow=glow; repaint(); } }); t.start(); }
        @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); int w=getWidth(), h=getHeight(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); AffineTransform at=g2.getTransform(); g2.translate(w/2.0, h/2.0); g2.scale(scale, scale); g2.translate(-w/2.0, -h/2.0);
            Shape rr = new RoundRectangle2D.Float(0,0,w-1,h-1, 12,12);
            if (gradient){ g2.setPaint(new GradientPaint(0,0,c1,0,h,c2)); g2.fill(rr); }
            else { g2.setColor(Color.WHITE); g2.fill(rr); g2.setStroke(new BasicStroke(3f)); g2.setColor(c2); g2.draw(rr); }
            if (glow){ g2.setColor(new Color(255,255,255,110)); g2.setStroke(new BasicStroke(2f)); g2.draw(rr); }
            g2.setTransform(at); super.paintComponent(g2); g2.dispose(); }
        @Override public void updateUI(){ super.updateUI(); setOpaque(false); setForeground(Color.WHITE); setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14)); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
    }

    private class ProfileCard extends JPanel {
        private PhotoPanel photo; private InfoPanel info; private ActionsBar actions;
        ProfileCard(){ setOpaque(false); setLayout(new BorderLayout());
            photo = new PhotoPanel(); add(photo, BorderLayout.NORTH);
            info = new InfoPanel(); add(info, BorderLayout.CENTER);
            actions = new ActionsBar(); add(actions, BorderLayout.SOUTH);
        }
        void setCandidate(Candidate c){ photo.setCandidate(c); info.setCandidate(c); revalidate(); repaint(); }
    }

    private static class CardWrapper extends JPanel {
        private double dx=0, dy=0, angle=0; private float opacity=1f; private double introTy=0; private double introScale=1.0; private double overlayHint=0; CardWrapper(ProfileCard child){ setOpaque(false); setLayout(new BorderLayout()); add(child, BorderLayout.CENTER);}        
        void setTransform(double dx,double dy,double ang){ this.dx=dx; this.dy=dy; this.angle=ang; repaint(); }
        void resetTransform(){ this.dx=0; this.dy=0; this.angle=0; repaint(); }
        void setOpacity(float a){ this.opacity=a; repaint(); }
        void resetOpacity(){ this.opacity=1f; repaint(); }
        void setIntroTransform(double ty, double scale){ this.introTy=ty; this.introScale=scale; repaint(); }
        void clearIntroTransform(){ this.introTy=0; this.introScale=1.0; repaint(); }
        void setOverlayHint(double v){ this.overlayHint=v; repaint(); }
        @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); int w=getWidth(), h=getHeight();
            AffineTransform at=g2.getTransform(); g2.translate(w/2.0, h/2.0 + introTy); g2.scale(introScale, introScale); g2.rotate(angle); g2.translate(-w/2.0 + dx, -h/2.0 + dy);
            // Card background with border + shadow
            Shape rr = new RoundRectangle2D.Float(0,0,w-1,h-1,25,25);
            // Shadow
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f*opacity));
            g2.setColor(new Color(0,0,0,100)); g2.fillRoundRect(10,8,w-1,h-1,25,25);
            // White card
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f*opacity));
            g2.setColor(Color.WHITE); g2.fill(rr);
            // Border gradient
            g2.setStroke(new BasicStroke(4f));
            g2.setPaint(new GradientPaint(0,0, Color.decode("#FF69B4"), 0,h, Color.decode("#9D4EDD")));
            g2.draw(rr);
            // Paint children
            super.paintChildren(g2);
            // Swipe hint overlay
            if (overlayHint!=0){
                float a = (float)Math.min(0.35, Math.abs(overlayHint)/240.0);
                if (overlayHint>0){ g2.setColor(new Color(0,255,0, (int)(a*255))); g2.setFont(new Font("Segoe UI", Font.BOLD, 42)); g2.drawString("❤", w-80, 60); }
                else { g2.setColor(new Color(255,0,0, (int)(a*255))); g2.setFont(new Font("Segoe UI", Font.BOLD, 42)); g2.drawString("✕", 40, 60); }
            }
            g2.setTransform(at); g2.dispose(); }
    }

    private static class PhotoPanel extends JComponent {
        private Candidate c; void setCandidate(Candidate c){ this.c=c; repaint(); }
        @Override public Dimension getPreferredSize(){ return new Dimension(480, 380); }
        @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); int w=getWidth(), h=getHeight();
            Shape clip = new RoundRectangle2D.Float(0,0,w,h,25,25);
            g2.setClip(clip);
            // Photo or fallback gradient with icon
            Image img=null;
            if (c!=null && c.image!=null && !c.image.isBlank()){
                try {
                    java.net.URL url = new java.net.URI(c.image).toURL();
                    img = new ImageIcon(url).getImage();
                } catch(Exception ignore){}
            }
            if (img!=null){
                // scale to cover
                double scale = Math.max(w/(double)img.getWidth(null), h/(double)img.getHeight(null));
                int iw=(int)(img.getWidth(null)*scale), ih=(int)(img.getHeight(null)*scale);
                int x=(w-iw)/2, y=(h-ih)/2;
                g2.drawImage(img, x,y,iw,ih,null);
            } else {
                g2.setPaint(new GradientPaint(0,0, Color.decode("#E8D5F2"), 0,h, Color.decode("#D4A5F5")));
                g2.fillRect(0,0,w,h);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 120)); g2.setColor(Color.WHITE); FontMetrics fm=g2.getFontMetrics(); String face="👤"; int tw=fm.stringWidth(face); g2.drawString(face, (w-tw)/2, h/2+40);
            }
            // Vignette radial
            java.awt.geom.Point2D center = new java.awt.geom.Point2D.Float(w/2f, h/2f);
            float radius = Math.max(w,h);
            float[] dist = {0.6f, 1f}; Color[] cols = {new Color(0,0,0,0), new Color(0,0,0,100)};
            g2.setPaint(new java.awt.RadialGradientPaint(center, radius, dist, cols));
            g2.fillRect(0,0,w,h);

            // Bottom gradient overlay for text readability
            g2.setPaint(new GradientPaint(0,h-100,new Color(0,0,0,0), 0,h,new Color(0,0,0,96)));
            g2.fillRect(0,h-100,w,100);

            // Name, Age text
            if (c!=null){
                String nameAge = toTitleCase(c.name) + (c.age>0? ", "+c.age : "");
                g2.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 28));
                // shadow
                g2.setColor(new Color(0,0,0,140));
                g2.drawString(nameAge, 22+2, h-30+2);
                g2.setColor(Color.WHITE);
                g2.drawString(nameAge, 22, h-30);
            }
            g2.dispose(); }
    }

    private static class InfoPanel extends JPanel{ private JTextArea bio; private TagFlow tags, hobbies;
        InfoPanel(){ setOpaque(false); setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); setBorder(new EmptyBorder(8,20,8,20));
            // Quick info line
            add(Box.createVerticalStrut(8));
            JLabel quick = new JLabel("📅  "); quick.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14)); quick.setForeground(Color.decode("#7B2CBF"));
            add(quick);
            // Bio
            add(Box.createVerticalStrut(6));
            JLabel about = new JLabel("About Me  ❤️"); about.setFont(new Font("Segoe UI Bold", Font.BOLD, 16)); about.setForeground(Color.decode("#7B2CBF")); add(about);
            bio = new JTextArea(); bio.setEditable(false); bio.setOpaque(false); bio.setLineWrap(true); bio.setWrapStyleWord(true); bio.setFont(new Font("Segoe UI", Font.PLAIN, 13)); bio.setForeground(Color.decode("#3C096C")); bio.setBorder(new EmptyBorder(0,15,0,0));
            JPanel bioWrap = new JPanel(new BorderLayout()){ @Override protected void paintComponent(Graphics g){ super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create(); g2.setColor(Color.decode("#F8F4FB")); g2.fillRect(0,0,getWidth(),getHeight()); g2.setColor(Color.decode("#FF69B4")); g2.fillRect(0,0,3,getHeight()); g2.dispose(); } }; bioWrap.setOpaque(false); bioWrap.add(bio, BorderLayout.CENTER); bioWrap.setBorder(new EmptyBorder(6,0,6,0));
            add(bioWrap);
            // Interests
            JLabel interestLbl = new JLabel("Interests  ✨"); interestLbl.setFont(new Font("Segoe UI Bold", Font.BOLD, 14)); interestLbl.setForeground(Color.decode("#7B2CBF")); add(interestLbl);
            tags = new TagFlow(); add(tags);
            // Hobbies (optional)
            hobbies = new TagFlow(new Color(0xFF,0xE5,0xF1)); add(hobbies);
        }
        void setCandidate(Candidate c){ String ageTxt = (c==null||c.age<=0)? "" : String.valueOf(c.age); ((JLabel)getComponent(1)).setText("📅  "+ageTxt);
            if (c==null){ bio.setText(""); tags.setTags(java.util.Collections.emptyList(), false); hobbies.setTags(java.util.Collections.emptyList(), true); repaint(); return; }
            String b = (c.bio==null||c.bio.isBlank())? "No bio yet..." : sentenceCase(c.bio);
            bio.setText(b);
            if (c.bio==null||c.bio.isBlank()) bio.setFont(bio.getFont().deriveFont(Font.ITALIC)); else bio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            tags.setTags(parseList(c.interests), false);
            hobbies.setTags(parseList(c.hobbies), true);
            revalidate(); repaint();
        }
        @Override protected void paintComponent(Graphics g){ super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create(); int w=getWidth(), h=getHeight(); g2.setPaint(new GradientPaint(0,0, Color.decode("#F8F4FB"), 0,h, Color.WHITE)); g2.fillRect(0,0,w,h); g2.setColor(new Color(0xE8,0xD5,0xF2, 20)); for(int y=14;y<h;y+=26){ for(int x=18;x<w;x+=28){ g2.drawString("❤", x,y); } } g2.dispose(); }
        private java.util.List<String> parseList(String s){ if (s==null||s.isBlank()) return java.util.Collections.emptyList(); String[] parts=s.split(","); java.util.List<String> out=new ArrayList<>(); for(String p:parts){ String t=p.trim(); if(!t.isEmpty()) out.add(toTitleCase(t)); } return out; }
    }

    private static class ActionsBar extends JPanel { final StyledButton pass, like, refresh; ActionsBar(){ setOpaque(true); setBackground(Color.WHITE); setPreferredSize(new Dimension(10,90)); setLayout(new BorderLayout());
            JPanel topBorder = new JPanel(){ @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); g2.setColor(Color.decode("#E8D5F2")); g2.fillRect(0,0,getWidth(),2); g2.dispose(); } }; topBorder.setOpaque(false); topBorder.setPreferredSize(new Dimension(10,2)); add(topBorder, BorderLayout.NORTH);
        JPanel row = new JPanel(new GridBagLayout()); row.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints(); gbc.gridy=0; gbc.insets=new Insets(12,0,12,0); gbc.fill=GridBagConstraints.NONE; gbc.weightx=1; gbc.anchor=GridBagConstraints.WEST;
        pass = StyledButton.outlinePink("Pass ✕"); pass.setPreferredSize(new Dimension(180,55));
        JPanel leftWrap = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); leftWrap.setOpaque(false); leftWrap.setBorder(new EmptyBorder(0,30,0,0)); leftWrap.add(pass);
        row.add(leftWrap, gbc);
        gbc.gridx=1; gbc.anchor=GridBagConstraints.EAST; gbc.weightx=1;
        like = StyledButton.gradient("Like ❤", new Color(0xFF69B4), new Color(0xFF8FAB)); like.setPreferredSize(new Dimension(180,55));
        JPanel rightWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0)); rightWrap.setOpaque(false); rightWrap.setBorder(new EmptyBorder(0,0,0,30)); rightWrap.add(like);
            row.add(rightWrap, gbc);
            add(row, BorderLayout.CENTER);
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER,0,8)); bottom.setOpaque(false); refresh = StyledButton.gradient("🔄  Refresh Candidates", new Color(0x3BB789), new Color(0x45D09E)); refresh.setPreferredSize(new Dimension(240,50)); bottom.add(refresh); add(bottom, BorderLayout.SOUTH);
        } }

    private static class TagFlow extends JPanel { private final Color bg; TagFlow(){ this(new Color(0xE8,0xD5,0xF2)); } TagFlow(Color bg){ super(new FlowLayout(FlowLayout.LEFT,8,8)); this.bg=bg; setOpaque(false);} void setTags(java.util.List<String> items, boolean subtle){ removeAll(); for(String it:items){ add(new Tag(it, bg)); } revalidate(); repaint(); } }
    private static class Tag extends JComponent{ private final String text; private final Color bg; Tag(String t, Color bg){ this.text=t; this.bg=bg; setPreferredSize(new Dimension(Math.max(80, textWidth()+26), 32)); }
        private int textWidth(){ Graphics g=getGraphics(); if(g==null) return text.length()*7; Font f=new Font("Segoe UI Semibold", Font.PLAIN, 12); FontMetrics fm=getFontMetrics(f); return fm.stringWidth(emojiFor(text)+" "+text); }
        @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); int w=getWidth(), h=getHeight(); Shape rr=new RoundRectangle2D.Float(0,0,w-1,h-1, 16,16); g2.setColor(bg); g2.fill(rr); g2.setStroke(new BasicStroke(2f)); g2.setColor(Color.decode("#D4A5F5")); g2.draw(rr); g2.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12)); g2.setColor(Color.decode("#7B2CBF")); g2.drawString(emojiFor(text)+" "+text, 12, h/2+5); g2.dispose(); }
        private String emojiFor(String t){ String s=t.toLowerCase(); if(s.contains("game")) return "🎮"; if(s.contains("movie")||s.contains("film")) return "🎬"; if(s.contains("music")) return "🎵"; if(s.contains("book")||s.contains("read")) return "📚"; if(s.contains("travel")) return "✈"; if(s.contains("art")) return "🎨"; if(s.contains("yoga")) return "🧘"; if(s.contains("cook")||s.contains("food")) return "🍜"; if(s.contains("gym")||s.contains("fit")) return "🏋"; if(s.contains("cricket")||s.contains("football")||s.contains("sport")) return "🏆"; return "✨"; }
    }

    private class LoadingOverlay extends JComponent { private int angle=0; private javax.swing.Timer t; LoadingOverlay(){ setOpaque(false); t=new javax.swing.Timer(30, e->{ angle=(angle+6)%360; repaint(); }); t.start(); setVisible(false);} @Override protected void paintComponent(Graphics g){ if(!isVisible()) return; Graphics2D g2=(Graphics2D)g.create(); int w=getWidth(), h=getHeight(); g2.setColor(new Color(255,255,255,220)); g2.fillRect(0,0,w,h); g2.setFont(new Font("Segoe UI", Font.PLAIN, 14)); g2.setColor(Color.decode("#9D4EDD")); String msg="Finding your next match..."; int tw=g2.getFontMetrics().stringWidth(msg); g2.drawString(msg, (w-tw)/2, h/2+60); // rotating heart
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
            AffineTransform at=g2.getTransform(); g2.translate(w/2.0, h/2.0); g2.rotate(Math.toRadians(angle)); g2.translate(-24,-24); g2.drawString("❤️", 0, 36); g2.setTransform(at); g2.dispose(); } }

    private class EmptyOverlay extends JComponent { EmptyOverlay(){ setOpaque(false);} @Override protected void paintComponent(Graphics g){ if(!isVisible()) return; Graphics2D g2=(Graphics2D)g.create(); int w=getWidth(), h=getHeight(); // card-shaped container to match layout area
            Shape rr = new RoundRectangle2D.Float(0,0,w-1,h-1,25,25); g2.setColor(new Color(255,255,255,240)); g2.fill(rr); g2.setStroke(new BasicStroke(4f)); g2.setColor(Color.decode("#D4A5F5")); g2.draw(rr);
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 96)); g2.setColor(Color.decode("#D4A5F5")); g2.drawString("💔", w/2-48, 160);
            g2.setFont(new Font("Segoe UI Bold", Font.BOLD, 22)); g2.setColor(Color.decode("#7B2CBF")); String title="No more matches"; int tw=g2.getFontMetrics().stringWidth(title); g2.drawString(title, (w-tw)/2, 210);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 14)); g2.setColor(Color.decode("#9D4EDD")); String sub="Check back later or adjust your preferences"; tw=g2.getFontMetrics().stringWidth(sub); g2.drawString(sub, (w-tw)/2, 240);
            // button
            StyledButton pref = StyledButton.gradient("Update Preferences", Color.decode("#9D4EDD"), Color.decode("#7B2CBF")); pref.setBounds(w/2-100, 270, 200, 50); pref.addActionListener(e -> mainWindow.showScreen("preferences"));
            setLayout(null); if (pref.getParent()!=this) add(pref); g2.dispose(); } }

    private class MatchOverlay extends JComponent { private javax.swing.Timer t; private long start; MatchOverlay(){ setOpaque(false);} void showFor(Candidate c){ this.start=System.currentTimeMillis(); setVisible(true); if(t!=null && t.isRunning()) t.stop(); t=new javax.swing.Timer(16, e->{ if(System.currentTimeMillis()-start>5000) { setVisible(false); t.stop(); } repaint(); }); t.start(); }
        @Override protected void paintComponent(Graphics g){ if(!isVisible()) return; Graphics2D g2=(Graphics2D)g.create(); int w=getWidth(), h=getHeight(); // radial gradient backdrop
            java.awt.RadialGradientPaint rg = new java.awt.RadialGradientPaint(new java.awt.geom.Point2D.Float(w/2f,h/2f), Math.max(w,h)/1.2f, new float[]{0f,1f}, new Color[]{new Color(0xFF,0x69,0xB4,230), new Color(0x9D,0x4E,0xDD,230)}); g2.setPaint(rg); g2.fillRect(0,0,w,h);
            // Title
            g2.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 48)); g2.setColor(Color.WHITE); String t1="It's a Match!"; int tw=g2.getFontMetrics().stringWidth(t1); g2.drawString(t1, (w-tw)/2, h/2-80);
            // hearts explosion
            long ms = System.currentTimeMillis()-start; for(int i=0;i<12;i++){ double ang = i*(Math.PI*2/12.0); double r = Math.min(200, ms*0.2); int x = (int)(w/2 + Math.cos(ang)*r); int y=(int)(h/2 + Math.sin(ang)*r); g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28)); g2.drawString("❤️", x, y); }
            // buttons
            StyledButton msg = StyledButton.gradient("Send Message", Color.WHITE, Color.WHITE); msg.setForeground(Color.decode("#FF69B4")); msg.setBounds(w/2-110, h/2+40, 220, 60); msg.addActionListener(e->{ setVisible(false); mainWindow.showScreen("chats"); });
            StyledButton keep = StyledButton.outlinePink("Keep Swiping"); keep.setForeground(Color.WHITE); keep.setBounds(w/2-100, h/2+110, 200, 50); keep.addActionListener(e->{ setVisible(false); });
            setLayout(null); if (msg.getParent()!=this) add(msg); if (keep.getParent()!=this) add(keep);
            g2.dispose(); }
    }

    // ------------- Data model -------------
    static class Candidate { final int userId; final String name, gender, interests, bio, image, hobbies; final int age; Candidate(int userId,String name,String gender,int age,String interests,String bio,String image){ this.userId=userId; this.name=name; this.gender=gender; this.age=age; this.interests=interests; this.bio=bio; this.image=image; this.hobbies=""; } }

    // --- Helpers for casing and formatting ---
    private static String toTitleCase(String s) { if (s == null || s.isEmpty()) return ""; String[] parts = s.split("\\s+"); StringBuilder sb = new StringBuilder(); for (int i=0;i<parts.length;i++) { String p = parts[i]; if (p.isEmpty()) continue; if (i>0) sb.append(' '); sb.append(Character.toUpperCase(p.charAt(0))); if (p.length()>1) sb.append(p.substring(1).toLowerCase()); } return sb.toString(); }
    private static String sentenceCase(String s) { if (s == null || s.trim().isEmpty()) return ""; s = s.trim(); return Character.toUpperCase(s.charAt(0)) + (s.length()>1 ? s.substring(1) : ""); }
}
