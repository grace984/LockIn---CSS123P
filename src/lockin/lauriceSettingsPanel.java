package lockin;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class lauriceSettingsPanel extends JPanel {

    private static final Color PANEL_BG = new Color(0xF2E6DC);
    private static final Color BOX_BG = new Color(0xE0CEB3);
    private static final Color BAR_BG = new Color(0xC9B496);
    private static final Color TEXT = new Color(0x5C3A12);
    private static final Color TITLE = new Color(0x4A0F2A);
    private static final Color ROW_HOVER = new Color(0xEBDFC9);
    private static final Color ROW_TEXT = new Color(0x2E1B0B);

    private static final int COLUMN_W = 440;

    private static Font body(int style, int size) {
        return new Font("Times New Roman", style, size);
    }

    private static Font spaced(Font f, float tracking) {
        Map<TextAttribute, Object> attrs = new HashMap<>();
        attrs.put(TextAttribute.TRACKING, tracking);
        return f.deriveFont(attrs);
    }

    private final RadioGroup soundGroup = new RadioGroup();
    private final RadioGroup warningGroup = new RadioGroup();

    public lauriceSettingsPanel() {
        setBackground(PANEL_BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(24, 48, 24, 48));

        add(Box.createVerticalGlue());

        JLabel title = new JLabel("Settings", SwingConstants.CENTER);
        title.setFont(spaced(body(Font.BOLD, 32), 0.04f));
        title.setForeground(TITLE);
        add(col(title, 42));
        add(Box.createVerticalStrut(4));

        add(col(new OrnamentDivider(), 16));
        add(Box.createVerticalStrut(6));

        add(col(new CenteredText("Customize your LockIn settings.", body(Font.PLAIN, 15), TITLE), 24));
        add(Box.createVerticalStrut(18));

        // timer sound
        add(col(sectionHeader(sectionLabel("TIMER SOUND")), 24));
        add(Box.createVerticalStrut(6));
        add(col(buildCard(soundGroup, 1, "Sound 1", "Sound 2"), 92));
        add(Box.createVerticalStrut(20));

        // tracker warning message
        add(col(sectionHeader(sectionLabel("TRACKER WARNING MESSAGE")), 24));
        add(Box.createVerticalStrut(6));
        add(col(buildCard(warningGroup, 0, "Version 1", "Version 2", "Version 3"), 128));
        add(Box.createVerticalStrut(20));

        // privacy
        add(col(sectionHeader(sectionLabel("PRIVACY STATEMENT")), 24));
        add(Box.createVerticalStrut(6));
        add(col(buildPrivacyCard(), 100));

        add(Box.createVerticalGlue());
    }

    // ---------- building the screen ----------

    private <T extends JComponent> T col(T c, int height) {
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

    // tan card with radio rows
    private JPanel buildCard(RadioGroup group, int defaultIndex, String... options) {
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);
        for (String option : options) {
            RadioRow row = new RadioRow(option, group);
            group.rows.add(row);
            list.add(row);
        }
        group.select(defaultIndex);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(list, BorderLayout.NORTH);

        RoundedPanel box = new RoundedPanel(BOX_BG, 16, true);
        box.setLayout(new BorderLayout());
        box.setBorder(new EmptyBorder(8, 8, 12, 8));
        box.add(wrapper, BorderLayout.CENTER);
        return box;
    }

    private JPanel buildPrivacyCard() {
        JTextArea text = new JTextArea("LockIn only monitors websites added to your distraction tracker "
                + "during active Work sessions. No account or personal information is required. "
                + "Your settings and preferences are stored locally on your device.");
        text.setFont(body(Font.ITALIC, 15));
        text.setForeground(TEXT);
        text.setOpaque(false);
        text.setEditable(false);
        text.setFocusable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);

        RoundedPanel box = new RoundedPanel(BOX_BG, 16, true);
        box.setLayout(new BorderLayout());
        box.setBorder(new EmptyBorder(14, 18, 18, 18));
        box.add(text, BorderLayout.CENTER);
        return box;
    }

    // ---------- values the rest of the app reads ----------

    // 1 or 2
    public int getSelectedSound() { return soundGroup.selected + 1; }

    // 1, 2 or 3
    public int getWarningVersion() { return warningGroup.selected + 1; }

    // ---------- small drawing helpers ----------

    private static class RadioGroup {
        final List<RadioRow> rows = new ArrayList<>();
        int selected = -1;

        void select(int i) {
            selected = i;
            for (int k = 0; k < rows.size(); k++) {
                rows.get(k).repaint();
            }
        }
    }

    // one option: radio dot + label, lights up on hover
    private static class RadioRow extends JPanel {
        private final String text;
        private final RadioGroup group;
        private boolean hover;

        RadioRow(String text, RadioGroup group) {
            this.text = text;
            this.group = group;
            setOpaque(false);
            setPreferredSize(new Dimension(100, 36));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { hover = true; repaint(); }

                @Override
                public void mouseExited(MouseEvent e) { hover = false; repaint(); }

                @Override
                public void mousePressed(MouseEvent e) { group.select(group.rows.indexOf(RadioRow.this)); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (hover) {
                g2.setColor(ROW_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }

            int d = 16;
            int x = 14;
            int y = (getHeight() - d) / 2;
            boolean on = group.rows.indexOf(this) == group.selected;
            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(on ? TITLE : TEXT);
            g2.drawOval(x, y, d, d);
            if (on) {
                g2.setColor(TITLE);
                g2.fillOval(x + 4, y + 4, d - 8, d - 8);
            }

            g2.setColor(ROW_TEXT);
            g2.setFont(body(Font.PLAIN, 16));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, x + d + 12, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

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
                    g2.setColor(new Color(0x4A, 0x0F, 0x2A, 10));
                    g2.fillRoundRect(2, i + 1, w - 4, h, radius * 2, radius * 2);
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
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            g2.drawString(text, x, fm.getAscent());
            g2.dispose();
        }
    }

    private static class OrnamentDivider extends JComponent {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cx = getWidth() / 2;
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