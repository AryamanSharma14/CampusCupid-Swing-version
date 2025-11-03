
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginPanel extends JPanel {
    public LoginPanel(MainWindow mainWindow) {
        setLayout(new BorderLayout());

        // Gradient background (top #E8D5F2 to bottom #C9A5E0)
        setOpaque(true);

        // HEADER
        HeaderPanel header = new HeaderPanel();
        add(header, BorderLayout.NORTH);

        // FOOTER
        FooterPanel footer = new FooterPanel();
        add(footer, BorderLayout.SOUTH);

        // CENTER content
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        // Logo/Glossy circle
        CircleLogo logo = new CircleLogo();
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(Box.createVerticalStrut(30));
        center.add(logo);
        center.add(Box.createVerticalStrut(30));

        // LOGIN FORM CARD
        FormCard form = new FormCard(mainWindow);
        form.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(form);

        // Bottom spacing before footer
        center.add(Box.createVerticalStrut(40));

        add(center, BorderLayout.CENTER);
    }

    // Paint gradient background
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();
        Color top = Color.decode("#E8D5F2");
        Color bottom = Color.decode("#C9A5E0");
        g2.setPaint(new GradientPaint(0, 0, top, 0, h, bottom));
        g2.fillRect(0, 0, w, h);
        g2.dispose();
    }

    // Header with gradient and drop-shadowed title
    static class HeaderPanel extends JPanel {
        HeaderPanel() { setPreferredSize(new Dimension(10, 100)); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(); int h = getHeight();
            Color c1 = Color.decode("#9D4EDD");
            Color c2 = Color.decode("#7B2CBF");
            g2.setPaint(new GradientPaint(0, 0, c1, 0, h, c2));
            g2.fillRect(0, 0, w, h);
            // Title with drop shadow
            String title = "CampusCupid";
            Font f = new Font("Arial Rounded MT Bold", Font.BOLD, 36);
            g2.setFont(f);
            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(title);
            int x = (w - textW) / 2;
            int y = 20 + (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g2.setColor(Color.black);
            g2.drawString(title, x + 2, y + 2);
            g2.setComposite(AlphaComposite.SrcOver);
            g2.setColor(Color.white);
            g2.drawString(title, x, y);
            g2.dispose();
        }
    }

    // Glossy circular logo with heart overlay
    static class CircleLogo extends JComponent {
        CircleLogo() { setPreferredSize(new Dimension(150, 150)); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight());
            int x = (getWidth() - size)/2; int y = (getHeight() - size)/2;
            // Shadow
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
            g2.setColor(Color.black);
            g2.fillOval(x+4, y+6, size-8, size-8);
            g2.setComposite(AlphaComposite.SrcOver);
            // Gradient circle
            Color top = Color.decode("#D4A5F5");
            Color bottom = Color.decode("#9D4EDD");
            GradientPaint gp = new GradientPaint(0, y, top, 0, y+size, bottom);
            g2.setPaint(gp);
            g2.fillOval(x, y, size, size);
            // Gloss highlight
            g2.setPaint(new GradientPaint(x, y, new Color(255,255,255,160), x, y+size/2, new Color(255,255,255,0)));
            g2.fillOval(x+10, y+10, size-20, size-30);
            // White border
            g2.setStroke(new BasicStroke(4f));
            g2.setColor(Color.white);
            g2.drawOval(x+2, y+2, size-4, size-4);
            // Heart overlay bottom-right
            String heart = "❤";
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 24));
            g2.setColor(Color.decode("#FF69B4"));
            FontMetrics fm = g2.getFontMetrics();
            int hw = fm.stringWidth(heart);
            int hx = x + size - hw - 12;
            int hy = y + size - 12;
            g2.drawString(heart, hx, hy);
            g2.dispose();
        }
    }

    // Rounded text field with placeholder, icon, focus glow
    static class RoundedTextField extends JTextField {
        private final String placeholder;
        private final String iconText;
        private boolean focused = false;
        RoundedTextField(String placeholder, String iconText) {
            super();
            this.placeholder = placeholder;
            this.iconText = iconText;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(0, 35, 0, 15));
            setForeground(Color.decode("#3C096C"));
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setPreferredSize(new Dimension(340,45));
            setMaximumSize(new Dimension(340,45));
            setBackground(new Color(0,0,0,0));
            addFocusListener(new FocusAdapter(){
                @Override public void focusGained(FocusEvent e){ focused = true; repaint(); }
                @Override public void focusLost(FocusEvent e){ focused = false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(); int h = getHeight();
            // Soft glow if focused
            if (focused) {
                for (int i=4;i>=1;i--) {
                    g2.setColor(new Color(157,78,221, 20*i)); // #9D4EDD with alpha
                    g2.fillRoundRect(2-i, 2-i, w-4+2*i, h-4+2*i, 20, 20);
                }
            }
            // Background
            g2.setColor(Color.decode("#F8F4FB"));
            g2.fillRoundRect(0,0,w,h,20,20);
            // Border
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(focused ? Color.decode("#9D4EDD") : Color.decode("#D4A5F5"));
            g2.drawRoundRect(0,0,w-1,h-1,20,20);
            // Icon
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            g2.setColor(new Color(90,24,154)); // #5A189A
            FontMetrics fm = g2.getFontMetrics();
            int iy = (h - fm.getHeight())/2 + fm.getAscent();
            g2.drawString(iconText, 8, iy);
            g2.dispose();
            super.paintComponent(g);
            // Placeholder
            if (!focused && getText().isEmpty()) {
                Graphics2D g3 = (Graphics2D) g.create();
                g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g3.setColor(new Color(160,160,160));
                g3.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                FontMetrics pfm = g3.getFontMetrics();
                int py = (h - pfm.getHeight())/2 + pfm.getAscent();
                g3.drawString(placeholder, 35, py);
                g3.dispose();
            }
        }
    }

    static class RoundedPasswordField extends JPasswordField {
        private final String placeholder;
        private final String iconText;
        private boolean focused = false;
        RoundedPasswordField(String placeholder, String iconText) {
            this.placeholder = placeholder; this.iconText = iconText;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(0, 35, 0, 15));
            setForeground(Color.decode("#3C096C"));
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setPreferredSize(new Dimension(340,45));
            setMaximumSize(new Dimension(340,45));
            addFocusListener(new FocusAdapter(){
                @Override public void focusGained(FocusEvent e){ focused = true; repaint(); }
                @Override public void focusLost(FocusEvent e){ focused = false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(); int h = getHeight();
            if (focused) {
                for (int i=4;i>=1;i--) {
                    g2.setColor(new Color(157,78,221, 20*i));
                    g2.fillRoundRect(2-i, 2-i, w-4+2*i, h-4+2*i, 20, 20);
                }
            }
            g2.setColor(Color.decode("#F8F4FB"));
            g2.fillRoundRect(0,0,w,h,20,20);
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(focused ? Color.decode("#9D4EDD") : Color.decode("#D4A5F5"));
            g2.drawRoundRect(0,0,w-1,h-1,20,20);
            // Icon
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            g2.setColor(new Color(90,24,154));
            FontMetrics fm = g2.getFontMetrics();
            int iy = (h - fm.getHeight())/2 + fm.getAscent();
            g2.drawString(iconText, 8, iy);
            g2.dispose();
            super.paintComponent(g);
            if (!focused && getPassword().length == 0) {
                Graphics2D g3 = (Graphics2D) g.create();
                g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g3.setColor(new Color(160,160,160));
                g3.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                FontMetrics pfm = g3.getFontMetrics();
                int py = (h - pfm.getHeight())/2 + pfm.getAscent();
                g3.drawString(placeholder, 35, py);
                g3.dispose();
            }
        }
    }

    // Form card with shadow and rounded corners
    static class ShadowRoundedPanel extends JPanel {
        private final int arc;
        ShadowRoundedPanel(int arc) {
            this.arc = arc; setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(); int h = getHeight();
            // Shadow (simple soft shadow)
            for (int i=8; i>=1; i--) {
                g2.setColor(new Color(0,0,0, 8));
                g2.fillRoundRect(4, 4 + (8 - i)/2, w-8, h-8, arc, arc);
            }
            // Card background
            g2.setColor(Color.white);
            g2.fillRoundRect(0, 0, w-1, h-1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class GradientButton extends JButton {
        private float hover = 0f; // 0..1
        private float pulse = 0f; // 0..1
        private boolean over = false;
        GradientButton(String text) {
            super(text);
            setOpaque(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(new Font("Segoe UI", Font.BOLD, 16));
            setForeground(Color.white);
            setPreferredSize(new Dimension(340,50));
            setMaximumSize(new Dimension(340,50));
            // Hover animation
            Timer hoverTimer = new Timer(15, e -> {
                float target = over ? 1f : 0f;
                if (Math.abs(hover - target) < 0.05f) { hover = target; ((Timer)e.getSource()).stop(); repaint(); return; }
                hover += (target - hover) * 0.3f; repaint();
            });
            addMouseListener(new MouseAdapter(){
                @Override public void mouseEntered(MouseEvent e){ over = true; if (!hoverTimer.isRunning()) hoverTimer.start(); }
                @Override public void mouseExited(MouseEvent e){ over = false; if (!hoverTimer.isRunning()) hoverTimer.start(); }
            });
            // Pulse animation
            Timer pulseTimer = new Timer(40, e -> { // ~25 FPS
                pulse += 0.02f; if (pulse > 1f) pulse = 0f; repaint();
            });
            pulseTimer.start();
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(); int h = getHeight(); int arc = 12;
            // Base gradient
            Color c1 = Color.decode("#9D4EDD");
            Color c2 = Color.decode("#7B2CBF");
            // Apply pulse (brightness 0.8..1.0)
            float pulseBright = 0.8f + 0.2f * (0.5f - (float)Math.cos(pulse*Math.PI*2)/2f);
            c1 = adjustBrightness(c1, pulseBright + 0.1f*hover);
            c2 = adjustBrightness(c2, pulseBright + 0.1f*hover);
            g2.setPaint(new GradientPaint(0, 0, c1, 0, h, c2));
            g2.fillRoundRect(0, 0, w, h, arc, arc);
            // Pressed inner shadow
            if (getModel().isArmed()) {
                g2.setColor(new Color(0,0,0,30));
                g2.fillRoundRect(3, 3, w-6, h-6, arc, arc);
            }
            // Text
            FontMetrics fm = g2.getFontMetrics(getFont());
            int tw = fm.stringWidth(getText());
            int tx = (w - tw)/2;
            int ty = (h - fm.getHeight())/2 + fm.getAscent();
            g2.setColor(getForeground());
            g2.drawString(getText(), tx, ty);
            g2.dispose();
        }
        private static Color adjustBrightness(Color c, float factor) {
            int r = Math.min(255, Math.max(0, (int)(c.getRed() * factor)));
            int g = Math.min(255, Math.max(0, (int)(c.getGreen() * factor)));
            int b = Math.min(255, Math.max(0, (int)(c.getBlue() * factor)));
            return new Color(r,g,b,c.getAlpha());
        }
    }

    static class OutlinedButton extends JButton {
        private float hover = 0f;
        private boolean over = false;
        OutlinedButton(String text) {
            super(text);
            setOpaque(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
            setForeground(Color.decode("#9D4EDD"));
            setPreferredSize(new Dimension(340,50));
            setMaximumSize(new Dimension(340,50));
            Timer hoverTimer = new Timer(15, e -> {
                float target = over ? 1f : 0f;
                if (Math.abs(hover - target) < 0.05f) { hover = target; ((Timer)e.getSource()).stop(); repaint(); return; }
                hover += (target - hover) * 0.3f; repaint();
            });
            addMouseListener(new MouseAdapter(){
                @Override public void mouseEntered(MouseEvent e){ over = true; if (!hoverTimer.isRunning()) hoverTimer.start(); }
                @Override public void mouseExited(MouseEvent e){ over = false; if (!hoverTimer.isRunning()) hoverTimer.start(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(); int h = getHeight(); int arc = 12;
            Color border = Color.decode("#9D4EDD");
            Color bg = new Color(255,255,255, 255 - (int)(hover*40)); // hover -> slightly tinted
            if (hover > 0.5f) bg = Color.decode("#F8F4FB");
            g2.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue()));
            g2.fillRoundRect(0,0,w,h,arc,arc);
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(border);
            g2.drawRoundRect(0,0,w-1,h-1,arc,arc);
            // Text
            g2.setColor(Color.decode("#9D4EDD"));
            FontMetrics fm = g2.getFontMetrics(getFont());
            int tw = fm.stringWidth(getText());
            int tx = (w - tw)/2; int ty = (h - fm.getHeight())/2 + fm.getAscent();
            g2.drawString(getText(), tx, ty);
            g2.dispose();
        }
    }

    // Footer with gradient and decorative circles + tagline
    static class FooterPanel extends JPanel {
        private final Point[] circles;
        FooterPanel() {
            setOpaque(false); setPreferredSize(new Dimension(10, 80));
            circles = new Point[5];
            java.util.Random rnd = new java.util.Random(42);
            for (int i=0;i<circles.length;i++) circles[i]=new Point(rnd.nextInt(600)+20, rnd.nextInt(60)+10);
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(); int h = getHeight();
            g2.setPaint(new GradientPaint(0,0, Color.decode("#C9A5E0"), 0, h, Color.decode("#E8D5F2")));
            g2.fillRect(0,0,w,h);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g2.setColor(Color.decode("#9D4EDD"));
            for (Point p : circles) {
                int cx = p.x % Math.max(w-10,1); int cy = p.y;
                g2.fillOval(cx, cy, 10, 10);
            }
            g2.setComposite(AlphaComposite.SrcOver);
            String t = "Find your perfect match on campus ✨";
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(Color.decode("#7B2CBF"));
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(t);
            g2.drawString(t, (w-tw)/2, (h - fm.getHeight())/2 + fm.getAscent());
            g2.dispose();
        }
    }

    // The actual form content
    static class FormCard extends ShadowRoundedPanel {
        FormCard(MainWindow mainWindow) {
            super(25);
            setPreferredSize(new Dimension(400, 350));
            setMaximumSize(new Dimension(400, 350));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

            // Title
            JLabel formTitle = new JLabel("CampusCupid Login", SwingConstants.CENTER);
            formTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
            formTitle.setForeground(Color.decode("#7B2CBF"));
            formTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(Box.createVerticalStrut(10));
            add(formTitle);
            // Underline bar
            add(Box.createVerticalStrut(6));
            JComponent underline = new JComponent(){
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2=(Graphics2D)g.create();
                    int w=getWidth(); int h=getHeight();
                    int uw=60; int uh=2; int x=(w-uw)/2; int y=(h-uh)/2;
                    g2.setPaint(new GradientPaint(x, y, Color.decode("#FF69B4"), x+uw, y, Color.decode("#9D4EDD")));
                    g2.fillRect(x,y,uw,uh);
                    g2.dispose();
                }
            };
            underline.setPreferredSize(new Dimension(10, 12));
            add(underline);

            add(Box.createVerticalStrut(12));

            // Email label
            JLabel emailLabel = new JLabel("SRM Email:");
            emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            emailLabel.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
            emailLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
            emailLabel.setForeground(Color.decode("#5A189A"));
            add(emailLabel);
            add(Box.createVerticalStrut(6));

            // Email field
            RoundedTextField emailField = new RoundedTextField("yourname@srmist.edu.in", "📧");
            emailField.setHorizontalAlignment(JTextField.CENTER); // center letters inside field
            emailField.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(emailField);

            add(Box.createVerticalStrut(20));

            // Password label
            JLabel pwLabel = new JLabel("Password:");
            pwLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            pwLabel.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
            pwLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
            pwLabel.setForeground(Color.decode("#5A189A"));
            add(pwLabel);
            add(Box.createVerticalStrut(6));

            // Password field
            RoundedPasswordField pwField = new RoundedPasswordField("••••••••", "🔒");
            pwField.setHorizontalAlignment(JTextField.CENTER); // center letters inside field
            pwField.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(pwField);

            add(Box.createVerticalStrut(20));

            // Buttons
            GradientButton loginBtn = new GradientButton("Login");
            loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(loginBtn);
            add(Box.createVerticalStrut(15));
            OutlinedButton signUpBtn = new OutlinedButton("Sign Up");
            signUpBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(signUpBtn);

            // Interactivity
            pwField.addActionListener(e -> loginBtn.doClick());
            loginBtn.addActionListener(e -> {
                String email = emailField.getText().trim();
                String password = new String(pwField.getPassword()).trim();
                if (!email.endsWith("@srmist.edu.in")) {
                    JOptionPane.showMessageDialog(this, "Email must end with @srmist.edu.in", "Login", JOptionPane.ERROR_MESSAGE);
                } else if (password.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Password cannot be empty", "Login", JOptionPane.ERROR_MESSAGE);
                } else if (!mainWindow.loginUser(email, password)) {
                    JOptionPane.showMessageDialog(this, "Invalid email or password", "Login", JOptionPane.ERROR_MESSAGE);
                } else {
                    mainWindow.showScreen("profile");
                }
            });
            signUpBtn.addActionListener(e -> mainWindow.showScreen("register"));
        }
    }
}
