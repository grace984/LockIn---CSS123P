package lockin;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class lauriceSettingsPanel extends JPanel {

    // FIXED UI COLORS (same as the Tracker and Themes panels)

    private static final Color PANEL_BACKGROUND = new Color(0xF2E6DC);
    private static final Color BOX_BG = new Color(0xE0CEB3);
    private static final Color TITLE = new Color(0x4A0F2A);
    private static final Color ORNAMENT_LINE = new Color(0xB8, 0x9A, 0x6B);
    private static final Color ROW_HOVER = new Color(0xEBDFC9);
    private static final Color TEXT = new Color(0x5C3A12);      // brown, for the subtitle
    private static final Color BAR_BG = new Color(0xC9B496);    // card outline

    // width of the centered content column (same as the other panels)
    private static final int COLUMN_W = 440;

    // font of the big title (change it here if you want a different one)
    private static final Font TITLE_FONT = new Font("Times New Roman", Font.BOLD, 32);

    // height of one radio option row
    private static final int ROW_H = 36;

    private static final String PRIVACY_TEXT =
            "LockIn only monitors websites added to your distraction tracker during "
            + "active Work sessions. No account or personal information is required. "
            + "Your settings and preferences are stored locally on your device.";

    // the options
    private OptionButton[] soundButtons;
    private OptionButton[] warningButtons;

    public lauriceSettingsPanel() {
        createComponents();
        createLayout();
    }

    // =========================================================
    // COMPONENTS
    // =========================================================

    private void createComponents() {

        // Timer Sound: Sound 2 is selected by default
        soundButtons = new OptionButton[]{
                new OptionButton("Sound 1"),
                new OptionButton("Sound 2")
        };
        soundButtons[1].setSelected(true);

        ButtonGroup soundGroup = new ButtonGroup();
        for (OptionButton b : soundButtons) {
            soundGroup.add(b);
        }

        // Tracker Warning Message: the choices come from the Tracker panel,
        // and the picked one is saved straight back to it
        String[] messages = ramiraTrackerPanel.getWarningMessages();
        warningButtons = new OptionButton[messages.length];

        int picked = Math.max(0,
                Math.min(messages.length - 1, ramiraTrackerPanel.getWarningVersion()));

        ButtonGroup warningGroup = new ButtonGroup();
        for (int i = 0; i < messages.length; i++) {
            final int index = i;
            warningButtons[i] = new OptionButton(messages[i]);
            warningButtons[i].setSelected(i == picked);
            warningButtons[i].addActionListener(e -> ramiraTrackerPanel.setWarningVersion(index));
            warningGroup.add(warningButtons[i]);
        }
    }

    // =========================================================
    // LAYOUT
    // =========================================================

    private void createLayout() {

        setBackground(PANEL_BACKGROUND);
        setOpaque(true);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // same padding as the other panels
        setBorder(new EmptyBorder(24, 48, 24, 48));

        // pushes everything to the vertical middle
        add(Box.createVerticalGlue());

        // header (same as the other panels)
        JLabel title = new JLabel("Settings", SwingConstants.CENTER);
        title.setFont(spaced(TITLE_FONT, 0.04f));
        title.setForeground(TITLE);
        add(fixed(title, 42));
        add(Box.createVerticalStrut(4));

        add(fixed(new OrnamentDivider(), 16));
        add(Box.createVerticalStrut(6));

        JLabel subtitle = new JLabel("Customize your LockIn settings", SwingConstants.CENTER);
        subtitle.setFont(new Font("Times New Roman", Font.PLAIN, 15));
        subtitle.setForeground(TEXT);
        subtitle.setVerticalAlignment(SwingConstants.TOP);
        add(fixed(subtitle, 52));
        add(Box.createVerticalStrut(18));

        // Timer Sound
        add(fixed(sectionLabel("TIMER SOUND"), 24));
        add(Box.createVerticalStrut(6));
        add(buildOptionCard(soundButtons));
        add(Box.createVerticalStrut(16));

        // Tracker Warning Message
        add(fixed(sectionLabel("TRACKER WARNING MESSAGE"), 24));
        add(Box.createVerticalStrut(6));
        add(buildOptionCard(warningButtons));
        add(Box.createVerticalStrut(16));

        // Privacy Statement
        add(fixed(sectionLabel("PRIVACY STATEMENT"), 24));
        add(Box.createVerticalStrut(6));
        add(buildPrivacyCard());

        add(Box.createVerticalGlue());
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(spaced(new Font("Times New Roman", Font.BOLD, 13), 0.18f));
        l.setForeground(TITLE);
        l.setBorder(new EmptyBorder(0, 4, 0, 4));
        return l;
    }

    // a rounded box that holds a group of radio options
    private JPanel buildOptionCard(OptionButton[] buttons) {
        RoundedPanel card = new RoundedPanel(BOX_BG, 16);
        card.setBaseOutline(BAR_BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(8, 8, 12, 8)); // extra bottom space is for the shadow

        for (OptionButton b : buttons) {
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setPreferredSize(new Dimension(100, ROW_H));
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_H));
            card.add(b);
        }

        return fixed(card, buttons.length * ROW_H + 20);
    }

    private JPanel buildPrivacyCard() {
        RoundedPanel card = new RoundedPanel(BOX_BG, 16);
        card.setBaseOutline(BAR_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(14, 18, 18, 18)); // extra bottom space is for the shadow
        card.add(new WrappedText(
                PRIVACY_TEXT,
                new Font("Times New Roman", Font.ITALIC, 15),
                TITLE), BorderLayout.CENTER);
        return fixed(card, 132); // tall enough for 5 lines of text
    }

    // =========================================================
    // SMALL HELPERS
    // =========================================================

    // same font with extra letter spacing
    private static Font spaced(Font f, float tracking) {
        Map<TextAttribute, Object> attrs = new HashMap<>();
        attrs.put(TextAttribute.TRACKING, tracking);
        return f.deriveFont(attrs);
    }

    // gives a component a fixed height and centers it in the column
    private <T extends JComponent> T fixed(T c, int height) {
        c.setPreferredSize(new Dimension(COLUMN_W, height));
        c.setMaximumSize(new Dimension(COLUMN_W, height));
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
        return c;
    }

    // =========================================================
    // WHAT THE REST OF THE APP READS
    // =========================================================

    // 1 or 2
    public int getSelectedSound() {
        for (int i = 0; i < soundButtons.length; i++) {
            if (soundButtons[i].isSelected()) {
                return i + 1;
            }
        }
        return 2;
    }

    // ---------------------------------------------------------
    // Old methods, kept so other classes that call them still compile.
    // The new design has no on/off switches or duration boxes,
    // so these just return the old default values.
    // ---------------------------------------------------------

    public boolean isSoundOn() {
        return true;
    }

    public boolean isWarningOn() {
        return true;
    }

    public int getWorkMinutes() {
        return 25;
    }

    public int getBreakMinutes() {
        return 5;
    }

    // =========================================================
    // RADIO OPTION (round button + label, row lights up on hover)
    // =========================================================

    private static class OptionButton extends JRadioButton {

        OptionButton(String text) {
            super(text);
            setFont(new Font("Times New Roman", Font.PLAIN, 17));
            setForeground(TITLE);
            setIcon(new RadioIcon());
            setIconTextGap(10);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setBorder(new EmptyBorder(0, 16, 0, 0));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (getModel().isRollover()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ROW_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    // empty circle when not selected, filled circle when selected
    private static class RadioIcon implements Icon {
        public int getIconWidth() { return 18; }
        public int getIconHeight() { return 18; }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            AbstractButton b = (AbstractButton) c;
            boolean selected = b.isSelected();
            boolean hot = b.getModel().isRollover();

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(TITLE);

            if (selected) {
                g2.fillOval(x + 1, y + 1, 16, 16);
            } else {
                g2.setStroke(new BasicStroke(hot ? 2.2f : 1.6f));
                g2.drawOval(x + 2, y + 2, 14, 14);
            }
            g2.dispose();
        }
    }

    // =========================================================
    // BOX, TEXT AND ORNAMENT (same look as the other panels)
    // =========================================================

    // rounded box with a thin outline and a soft shadow (the bottom 4px are reserved for it)
    private static class RoundedPanel extends JPanel {
        private final Color color;
        private final int radius;
        private Color baseOutline;

        RoundedPanel(Color color, int radius) {
            this.color = color;
            this.radius = radius;
            setOpaque(false);
        }

        void setBaseOutline(Color c) {
            this.baseOutline = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight() - 4;

            for (int i = 0; i < 4; i++) {
                g2.setColor(new Color(0x4A, 0x0F, 0x2A, 10));
                g2.fillRoundRect(2, i + 1, w - 4, h, radius * 2, radius * 2);
            }

            g2.setColor(color);
            g2.fillRoundRect(0, 0, w, h, radius * 2, radius * 2);

            if (baseOutline != null) {
                g2.setColor(baseOutline);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, radius * 2, radius * 2);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // left-aligned text that wraps to the width it actually has (no HTML, so it never gets cut off)
    private static class WrappedText extends JComponent {
        private final String text;

        WrappedText(String text, Font font, Color color) {
            this.text = text;
            setFont(font);
            setForeground(color);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();

            int y = fm.getAscent();
            StringBuilder line = new StringBuilder();
            for (String word : text.split(" ")) {
                String test = line.length() == 0 ? word : line + " " + word;
                if (line.length() > 0 && fm.stringWidth(test) > getWidth()) {
                    g2.drawString(line.toString(), 0, y);
                    y += fm.getHeight();
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(test);
                }
            }
            if (line.length() > 0) {
                g2.drawString(line.toString(), 0, y);
            }
            g2.dispose();
        }
    }

    // thin line with a diamond in the middle, fading out at both ends
    private static class OrnamentDivider extends JComponent {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            Color clear = new Color(0xB8, 0x9A, 0x6B, 0);

            g2.setPaint(new GradientPaint(cx - 150, 0, clear, cx - 14, 0, ORNAMENT_LINE));
            g2.drawLine(cx - 150, cy, cx - 14, cy);
            g2.setPaint(new GradientPaint(cx + 14, 0, ORNAMENT_LINE, cx + 150, 0, clear));
            g2.drawLine(cx + 14, cy, cx + 150, cy);

            g2.setColor(TITLE);
            g2.fillPolygon(new int[]{cx, cx + 5, cx, cx - 5}, new int[]{cy - 5, cy, cy + 5, cy}, 4);
            g2.dispose();
        }
    }
}