import javax.swing.*;
import java.awt.*;

public class UIHelpers {
    // === New design system (non-breaking; old constants kept below) ===
    public static final class DS {
        // Colors
        public static final Color PRIMARY_PURPLE = Color.decode("#9D4EDD");
        public static final Color DARK_PURPLE = Color.decode("#7B2CBF");
        public static final Color DARK_PURPLE_2 = Color.decode("#5A189A");
        public static final Color TEXT_DARK = Color.decode("#3C096C");
        public static final Color LIGHT_PURPLE = Color.decode("#D4A5F5");
        public static final Color LAVENDER = Color.decode("#E8D5F2");
        public static final Color LAVENDER_2 = Color.decode("#F8F4FB");
        public static final Color LAVENDER_3 = Color.decode("#FAF0FF");
        public static final Color ACCENT_PINK = Color.decode("#FF69B4");
        public static final Color ACCENT_PINK_2 = Color.decode("#FF8FAB");
        public static final Color ACCENT_PINK_3 = Color.decode("#FF6B9D");
        public static final Color ACCENT_GREEN = Color.decode("#3BB789");
        public static final Color ACCENT_GREEN_2 = Color.decode("#45D09E");
        public static final Color ACCENT_ORANGE = Color.decode("#FFB347");
        public static final Color ACCENT_ORANGE_2 = Color.decode("#FF9E2C");
        public static final Color TEXT_MEDIUM = Color.decode("#7B2CBF");
        public static final Color TEXT_LIGHT = Color.decode("#9D4EDD");
        public static final Color SUBTLE_GRAY = Color.decode("#A0A0A0");
        public static final Color WHITE = Color.WHITE;

        // Typography helpers
        public static Font displayHeader(float pt){ return new Font("Arial Rounded MT Bold", Font.BOLD, Math.round(pt)); }
        public static Font pageTitle(float pt){ return new Font("Arial Rounded MT Bold", Font.BOLD, Math.round(pt)); }
        public static Font sectionHeader(float pt, boolean semi){ return new Font(semi?"Segoe UI Semibold":"Segoe UI Bold", Font.BOLD, Math.round(pt)); }
        public static Font body(float pt){ return new Font("Segoe UI", Font.PLAIN, Math.round(pt)); }
        public static Font small(float pt){ return new Font("Segoe UI", Font.PLAIN, Math.round(pt)); }
        public static Font button(float pt, boolean semi){ return new Font(semi?"Segoe UI Semibold":"Segoe UI Bold", Font.PLAIN, Math.round(pt)); }

        // Spacing scale (px)
        public static final int XS=5, S=10, M=15, L=20, XL=30, XXL=40;

        // Radii
        public static final int R_SMALL=10, R_MED=14, R_LARGE=20, R_XL=25;

        // Shadows
        public static final Color SHADOW_LIGHT = new Color(0,0,0,0x15);
        public static final Color SHADOW_MED = new Color(0,0,0,0x25);
        public static final Color SHADOW_HEAVY = new Color(0,0,0,0x40);

        // Anim timings (ms)
        public static final int T_QUICK=150, T_NORMAL=300, T_SLOW=500;

        // Utilities
        public static void enableAA(Graphics2D g2){
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        public static GradientPaint vertical(Color c1, Color c2, int h){ return new GradientPaint(0,0,c1, 0,h,c2); }
        public static JPanel spacer(int h){ JPanel p=new JPanel(); p.setOpaque(false); p.setPreferredSize(new Dimension(10,h)); return p; }

        public static JButton gradientButton(String text, Color c1, Color c2, int radius){
            JButton b = new JButton(text){ @Override protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g.create(); DS.enableAA(g2); int w=getWidth(), h=getHeight(); g2.setPaint(DS.vertical(c1,c2,h)); g2.fillRoundRect(0,0,w-1,h-1,radius,radius); g2.dispose(); super.paintComponent(g); } };
            b.setFocusPainted(false); b.setBorderPainted(false); b.setContentAreaFilled(false); b.setForeground(Color.WHITE); b.setFont(button(14,true)); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
        }
    }

    // Shared theme colors
    public static final Color PURPLE = new Color(102, 0, 204);
    public static final Color LIGHT_PURPLE = new Color(230, 220, 255);
    public static final Color CARD_BORDER = new Color(220, 220, 220);

    // Shared fonts
    public static Font titleFont() { return new Font("Arial", Font.BOLD, 18); }
    public static Font bigTitleFont() { return new Font("Arial", Font.BOLD, 20); }
    public static Font labelFont() { return new Font("Arial", Font.PLAIN, 17); }
    public static Font buttonFont() { return new Font("Arial", Font.BOLD, 16); }
    public static Font smallButtonFont() { return new Font("Arial", Font.BOLD, 12); }

    // Create a top bar with optional left/right components
    public static JPanel createTopBar(String title, JComponent left, JComponent right) {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(LIGHT_PURPLE);
        if (left != null) topBar.add(left, BorderLayout.WEST);
        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setFont(titleFont());
        lbl.setForeground(PURPLE);
        topBar.add(lbl, BorderLayout.CENTER);
        if (right != null) topBar.add(right, BorderLayout.EAST);
        return topBar;
    }

    // Wrap a card inside a vertical centering wrapper
    public static JPanel wrapCenteredCard(JPanel card) {
        JPanel centerWrapper = new JPanel();
        centerWrapper.setBackground(LIGHT_PURPLE);
        centerWrapper.setLayout(new BoxLayout(centerWrapper, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerWrapper.add(Box.createVerticalGlue());
        centerWrapper.add(card);
        centerWrapper.add(Box.createVerticalGlue());
        return centerWrapper;
    }

    // Create a white rounded card with padding
    public static JPanel createCard(int padTop, int padLeft, int padBottom, int padRight) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(CARD_BORDER, 1, true),
                BorderFactory.createEmptyBorder(padTop, padLeft, padBottom, padRight)
        ));
        return card;
    }

    public static JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(labelFont());
        l.setForeground(PURPLE);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(PURPLE);
        b.setForeground(Color.WHITE);
        b.setFont(buttonFont());
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        return b;
    }

    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(Color.WHITE);
        b.setForeground(PURPLE);
        b.setFont(buttonFont());
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        return b;
    }

    // Small right-side nav container
    public static JPanel rightNav(JButton... buttons) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        p.setOpaque(false);
        for (JButton b : buttons) {
            b.setBackground(Color.WHITE);
            b.setForeground(PURPLE);
            b.setFont(smallButtonFont());
            p.add(b);
        }
        return p;
    }
}
