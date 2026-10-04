package lockin;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class lauriceSettingsPanel extends JPanel {

    // Colors matching ramiraTrackerPanel exactly
    private static final Color PANEL_BG = new Color(0xF2E6DC);
    private static final Color BOX_BG = new Color(0xE0CEB3);
    private static final Color BAR_BG = new Color(0xC9B496);
    private static final Color TITLE = new Color(0x4A0F2A);
    private static final Color FIELD_BG = new Color(0xFFF9F0);
    private static final Color ROW_TEXT = new Color(0x2E1B0B);

    // Width of the centered content column
    private static final int COLUMN_W = 440;

    private JCheckBox soundCheckBox;
    private JCheckBox warningCheckBox;
    private JSpinner workSpinner;
    private JSpinner breakSpinner;

    // Times New Roman font helper
    private static Font body(int style, int size) {
        return new Font("Times New Roman", style, size);
    }

    // Font helper with letter spacing tracking
    private static Font spaced(Font f, float tracking) {
        Map attrs = new HashMap<>();
        attrs.put(TextAttribute.TRACKING, tracking);
        return f.deriveFont(attrs);
    }

    public lauriceSettingsPanel() {
        setBackground(PANEL_BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(24, 48, 24, 48));

        add(Box.createVerticalGlue()); // Centers content vertically

        // Title, centered
        JLabel title = new JLabel("SETTINGS", SwingConstants.CENTER);
        title.setFont(spaced(body(Font.BOLD, 32), 0.04f));
        title.setForeground(TITLE);
        add(col(title, 42));
        add(Box.createVerticalStrut(4));

        // Ornament divider under title
        add(col(new OrnamentDivider(), 16));
        add(Box.createVerticalStrut(6));

        // Description, centered
        CenteredText desc = new CenteredText("Customize your LockIn timer and warning preferences.", body(Font.PLAIN, 15), TITLE);
        add(col(desc, 30));
        add(Box.createVerticalStrut(18));

        // Preferences Section Header
        JLabel prefSectionLabel = sectionLabel("PREFERENCES");
        add(col(sectionHeader(prefSectionLabel), 24));
        add(Box.createVerticalStrut(6));

        // Settings Form Card Box (matches Tracker card dimensions)
        add(col(buildSettingsBox(), 210));

        add(Box.createVerticalGlue());
    }

    // Centered column sizing helper
    private JComponent col(JComponent c, int height) {
        c.setPreferredSize(new Dimension(COLUMN_W, height));
        c.setMaximumSize(new Dimension(COLUMN_W, height));
        c.setAlignmentX(CENTER_ALIGNMENT);
        return c;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(spaced(body(Font.BOLD, 13), 0.18f));
        l.setForeground(TITLE);
        return l;
    }

    private JPanel sectionHeader(JLabel label) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 4, 0, 4));
        p.add(label, BorderLayout.WEST);
        return p;
    }

    private JPanel buildSettingsBox() {
        JPanel innerPanel = new JPanel(new GridBagLayout());
        innerPanel.setOpaque(false);
        innerPanel.setBorder(new EmptyBorder(14, 18, 14, 18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 8, 10, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Initialize components
        soundCheckBox = new JCheckBox("Sound ON", true);
        styleCheckBox(soundCheckBox);

        warningCheckBox = new JCheckBox("Warning ON", true);
        styleCheckBox(warningCheckBox);

        workSpinner = new JSpinner(new SpinnerNumberModel(25, 1, 180, 1));
        styleSpinner(workSpinner);

        breakSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 60, 1));
        styleSpinner(breakSpinner);

        // Row 0: Sound
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel soundLbl = new JLabel("Sound:");
        soundLbl.setFont(body(Font.PLAIN, 16));
        soundLbl.setForeground(ROW_TEXT);
        innerPanel.add(soundLbl, gbc);

        gbc.gridx = 1;
        innerPanel.add(soundCheckBox, gbc);

        // Row 1: Warning
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel warningLbl = new JLabel("Warning:");
        warningLbl.setFont(body(Font.PLAIN, 16));
        warningLbl.setForeground(ROW_TEXT);
        innerPanel.add(warningLbl, gbc);

        gbc.gridx = 1;
        innerPanel.add(warningCheckBox, gbc);

        // Row 2: Work Duration
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel workLbl = new JLabel("Work Duration (min):");
        workLbl.setFont(body(Font.PLAIN, 16));
        workLbl.setForeground(ROW_TEXT);
        innerPanel.add(workLbl, gbc);

        gbc.gridx = 1;
        innerPanel.add(workSpinner, gbc);

        // Row 3: Break Duration
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel breakLbl = new JLabel("Break Duration (min):");
        breakLbl.setFont(body(Font.PLAIN, 16));
        breakLbl.setForeground(ROW_TEXT);
        innerPanel.add(breakLbl, gbc);

        gbc.gridx = 1;
        innerPanel.add(breakSpinner, gbc);

        // Wrap inside RoundedPanel matching exact Tracker card style
        RoundedPanel box = new RoundedPanel(BOX_BG, 16, true);
        box.setLayout(new BorderLayout());
        box.add(innerPanel, BorderLayout.CENTER);
        return box;
    }

    private void styleCheckBox(JCheckBox cb) {
        cb.setFont(body(Font.PLAIN, 15));
        cb.setForeground(ROW_TEXT);
        cb.setOpaque(false);
        cb.setFocusPainted(false);
        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(body(Font.PLAIN, 14));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setForeground(ROW_TEXT);
            tf.setBackground(FIELD_BG);
            tf.setFont(body(Font.PLAIN, 14));
            tf.setBorder(BorderFactory.createLineBorder(BAR_BG, 1));
        }
        spinner.setPreferredSize(new Dimension(80, 30));
    }

    public boolean isSoundOn() {
        return soundCheckBox.isSelected();
    }

    public boolean isWarningOn() {
        return warningCheckBox.isSelected();
    }

    public int getWorkMinutes() {
        return (Integer) workSpinner.getValue();
    }

    public int getBreakMinutes() {
        return (Integer) breakSpinner.getValue();
    }

    // ---------- Helper Classes (Exact Match to Tracker Panel) ----------

    private static class RoundedPanel extends JPanel {
        private final Color color;
        private final int radius;
        private final boolean shadow;

        RoundedPanel(Color color, int radius, boolean shadow) {
            this.color = color;
            this.radius = radius;
            this.shadow = shadow;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight() - (shadow ? 4 : 0);

            if (shadow) {
                for (int i = 0; i < 4; i++) {
                    g2.setColor(new Color(0x4A, 0x0F, 0x2A, 8));
                    g2.fillRoundRect(2, i + 2, w - 4, h - 2, radius * 2, radius * 2);
                }
            }

            g2.setColor(color);
            g2.fillRoundRect(0, 0, w, h, radius * 2, radius * 2);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class CenteredText extends JComponent {
        private final String text;

        CenteredText(String text, Font font, Color color) {
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
                    drawCentered(g2, fm, line.toString(), y);
                    y += fm.getHeight();
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(test);
                }
            }
            if (line.length() > 0) {
                drawCentered(g2, fm, line.toString(), y);
            }
            g2.dispose();
        }

        private void drawCentered(Graphics2D g2, FontMetrics fm, String s, int y) {
            g2.drawString(s, (getWidth() - fm.stringWidth(s)) / 2, y);
        }
    }

    private static class OrnamentDivider extends JComponent {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int cx = w / 2;
            int cy = getHeight() / 2;
            Color solid = new Color(0xB8, 0x9A, 0x6B);
            Color clear = new Color(0xB8, 0x9A, 0x6B, 0);

            g2.setPaint(new GradientPaint(cx - 150, 0, clear, cx - 14, 0, solid));
            g2.drawLine(cx - 150, cy, cx - 14, cy);
            g2.setPaint(new GradientPaint(cx + 14, 0, solid, cx + 150, 0, clear));
            g2.drawLine(cx + 14, cy, cx + 150, cy);

            g2.setColor(TITLE);
            g2.fillPolygon(new int[]{cx, cx + 5, cx, cx - 5}, new int[]{cy - 5, cy, cy + 5, cy}, 4);
            g2.dispose();
        }
    }
}