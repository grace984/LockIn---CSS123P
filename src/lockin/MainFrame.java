package lockin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFrame extends JFrame {

    // colors from the UI design
    private static final Color TAB_BG = new Color(0xFFF5E4);
    private static final Color TAB_SELECTED = new Color(0xF7E9DF);
    private static final Color PANEL_BG = new Color(0xF7E9DF);
    private static final Color MAROON = new Color(0x59132C);
    private static final Color CREAM = new Color(0xFFF5E4);

    private CardLayout cardLayout = new CardLayout();
    private JPanel contentPanel = new JPanel(cardLayout);
    private JPanel drawer = new JPanel(new BorderLayout());
    private Map<String, JButton> tabButtons = new LinkedHashMap<>();
    private GradientPanel timerArea;

    public MainFrame() {
        setTitle("LockIn");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ---------- RIGHT SIDE: gradient area with the hamburger button ----------
        Color[] themeColors = julianneThemeManager.getSelectedColors();
        timerArea = new GradientPanel(
                themeColors[0],
                themeColors[themeColors.length - 1]
        );

        timerArea.setPreferredSize(new Dimension(731, 810));
        timerArea.setLayout(new BorderLayout());
        julianneThemeManager.addListener(colors -> timerArea.setColors(
                colors[0], colors[colors.length - 1]));

        JButton hamburger = new JButton(new HamburgerIcon());
        hamburger.setBorderPainted(false);
        hamburger.setContentAreaFilled(false);
        hamburger.setFocusPainted(false);
        hamburger.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        hamburger.addActionListener(
                e -> toggleDrawer()
        );

        JPanel topBar = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        10,
                        10
                )
        );

        topBar.setOpaque(false);
        topBar.add(hamburger);

        timerArea.add(
                topBar,
                BorderLayout.NORTH
        );

        lauriceTimerPanel timerPanel = new lauriceTimerPanel();
        JPanel timerCenter = new JPanel(new GridBagLayout());
        timerCenter.setOpaque(false);
        timerCenter.setBorder(BorderFactory.createEmptyBorder(
                0, 0, topBar.getPreferredSize().height, 0));
        timerCenter.add(timerPanel);
        timerArea.add(timerCenter, BorderLayout.CENTER);

        add(
                timerArea,
                BorderLayout.CENTER
        );

        // ---------- LEFT SIDE: drawer = tabs column + content panel ----------

        JPanel tabsGrid = new JPanel(new GridLayout(4, 1));
        tabsGrid.setOpaque(false);
        tabsGrid.setPreferredSize(new Dimension(276, 330));

        addTab(
                tabsGrid,
                "TIME",
                "time"
        );

        addTab(
                tabsGrid,
                "TRACKER",
                "tracker"
        );

        addTab(
                tabsGrid,
                "THEMES",
                "themes"
        );

        addTab(
                tabsGrid,
                "SETTINGS",
                "settings"
        );

        JPanel tabsHolder = new JPanel();
        tabsHolder.setBackground(TAB_BG);
        tabsHolder.setOpaque(true);

        contentPanel.add(createTimePanel(timerPanel), "time");

        contentPanel.add(
                new ramiraTrackerPanel(),
                "tracker"
        );

        contentPanel.add(new julianneThemePanel(), "themes");

        contentPanel.add(createSettingsPanel(), "settings");

        contentPanel.setPreferredSize(new Dimension(471, 810));

        JPanel columns = new JPanel(null) {
            @Override
            public void doLayout() {
                tabsHolder.setBounds(0, 0, 238, getHeight());
                contentPanel.setBounds(238, 0, 471, getHeight());
                tabsGrid.setBounds(0, 0, 276, 330);
            }
        };
        columns.setOpaque(false);
        columns.setPreferredSize(new Dimension(709, 810));
        columns.add(tabsHolder);
        columns.add(contentPanel);
        columns.add(tabsGrid);
        columns.setComponentZOrder(tabsGrid, 0);
        columns.setComponentZOrder(contentPanel, 1);
        columns.setComponentZOrder(tabsHolder, 2);

        drawer.setPreferredSize(new Dimension(709, 810));
        drawer.add(columns, BorderLayout.CENTER);
        drawer.setVisible(true);

        add(
                drawer,
                BorderLayout.WEST
        );

        getContentPane().setPreferredSize(new Dimension(1440, 810));
        pack();
        setLocationRelativeTo(null);

        selectTab("time");
    }

    // ---------- helpers ----------

    private void toggleDrawer() {

        drawer.setVisible(
                !drawer.isVisible()
        );

        getContentPane().revalidate();
        getContentPane().repaint();
    }

    private void addTab(
            JPanel grid,
            String label,
            String key
    ) {

        Font base = new Font(
                "Times New Roman",
                Font.PLAIN,
                30
        );

        Map<TextAttribute, Object> attrs =
                new HashMap<TextAttribute, Object>(
                        base.getAttributes()
                );

        attrs.put(
                TextAttribute.TRACKING,
                0.25
        );

        TabButton b = new TabButton(label);

        b.setFont(
                base.deriveFont(attrs)
        );

        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setPreferredSize(new Dimension(276, 82));

        b.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        b.addActionListener(
                e -> selectTab(key)
        );

        tabButtons.put(
                key,
                b
        );

        grid.add(b);
    }

    private void selectTab(String key) {

        cardLayout.show(
                contentPanel,
                key
        );

        for (
                Map.Entry<String, JButton> entry :
                tabButtons.entrySet()
        ) {

            ((TabButton) entry.getValue()).setTabSelected(
                    entry.getKey().equals(key));
        }
    }

    private JPanel createTimePanel(lauriceTimerPanel timerPanel) {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL_BG);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(30, 38, 20, 38));
        panel.add(Box.createVerticalGlue());
        panel.add(createDurationEditor("Work", timerPanel.getWorkDuration(),
                timerPanel::setWorkDuration));
        panel.add(Box.createVerticalStrut(28));
        panel.add(createDurationEditor("Break", timerPanel.getBreakDuration(),
                timerPanel::setBreakDuration));
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel createDurationEditor(String title, int seconds,
                                        java.util.function.IntConsumer update) {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Times New Roman", Font.PLAIN, 32));
        heading.setForeground(MAROON);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel value = new JLabel(formatDuration(seconds));
        value.setFont(new Font("Times New Roman", Font.PLAIN, 52));
        value.setForeground(MAROON);

        int[] duration = {seconds};
        JButton minus = textButton("−", 46);
        JButton plus = textButton("+", 46);
        minus.addActionListener(e -> {
            duration[0] = Math.max(60, duration[0] - 60);
            value.setText(formatDuration(duration[0]));
        });
        plus.addActionListener(e -> {
            duration[0] += 60;
            value.setText(formatDuration(duration[0]));
        });

        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        row.setOpaque(false);
        row.add(minus);
        row.add(value);
        row.add(plus);

        JButton done = new RoundedActionButton("Done", 142, 50, false);
        done.setFont(new Font("Times New Roman", Font.PLAIN, 28));
        done.addActionListener(e -> update.accept(duration[0]));

        section.add(heading);
        section.add(Box.createVerticalStrut(18));
        section.add(row);
        section.add(Box.createVerticalStrut(22));
        done.setAlignmentX(Component.CENTER_ALIGNMENT);
        section.add(done);
        return section;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(PANEL_BG);

        JLabel title = new JLabel("SETTINGS");
        title.setFont(new Font("Times New Roman", Font.PLAIN, 30));
        title.setForeground(MAROON);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBounds(0, 80, 471, 40);

        JLabel subtitle = new JLabel("Customize your LockIn settings");
        subtitle.setFont(new Font("Times New Roman", Font.ITALIC, 18));
        subtitle.setForeground(MAROON);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        subtitle.setBounds(0, 124, 471, 30);

        JPanel soundSettings = settingsGroup("Timer Sound",
                new String[]{"Sound 1", "Sound 2"}, 1);
        soundSettings.setBounds(58, 190, 410, 128);

        JPanel warningSettings = settingsGroup("Tracker Warning Message",
                new String[]{"Version 1", "Version 2", "Version 3"}, 0);
        warningSettings.setBounds(58, 354, 410, 164);

        JLabel privacyTitle = new JLabel("Privacy Statement");
        privacyTitle.setFont(new Font("Times New Roman", Font.PLAIN, 20));
        privacyTitle.setForeground(MAROON);
        privacyTitle.setBounds(58, 542, 350, 30);

        JLabel privacy = new JLabel("<html><div style='width:370px'>LockIn only monitors websites added to your distraction tracker during active Work sessions. No account or personal information is required. Your settings and preferences are stored locally on your device.</div></html>");
        privacy.setFont(new Font("Times New Roman", Font.ITALIC, 14));
        privacy.setForeground(MAROON);
        JPanel privacyCard = new SoftPanel(new Color(0xE9D6BF), 20);
        privacyCard.setLayout(new BorderLayout());
        privacyCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        privacyCard.add(privacy, BorderLayout.CENTER);
        privacyCard.setBounds(42, 580, 410, 156);

        panel.add(title);
        panel.add(subtitle);
        panel.add(soundSettings);
        panel.add(warningSettings);
        panel.add(privacyTitle);
        panel.add(privacyCard);
        return panel;
    }

    private JPanel settingsGroup(String label, String[] values, int selectedIndex) {
        JPanel section = new JPanel(new BorderLayout(0, 8));
        section.setOpaque(false);

        JLabel heading = new JLabel(label);
        heading.setFont(new Font("Times New Roman", Font.PLAIN, 20));
        heading.setForeground(MAROON);
        section.add(heading, BorderLayout.NORTH);

        JPanel options = new SoftPanel(new Color(0xE9D6BF), 20);
        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
        options.setBorder(new EmptyBorder(6, 36, 6, 8));
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < values.length; i++) {
            JRadioButton option = new JRadioButton(values[i], i == selectedIndex);
            option.setFont(new Font("Times New Roman", Font.PLAIN, 18));
            option.setForeground(MAROON);
            option.setOpaque(false);
            option.setIcon(new RadioCircleIcon(false));
            option.setSelectedIcon(new RadioCircleIcon(true));
            option.setFocusPainted(false);
            option.setPreferredSize(new Dimension(360, 34));
            option.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            group.add(option);
            options.add(option);
            if (i + 1 < values.length) {
                options.add(Box.createVerticalStrut(6));
            }
        }
        section.add(options, BorderLayout.CENTER);
        return section;
    }

    private static String formatDuration(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    private JButton textButton(String text, int width) {
        JButton button = new JButton(text);
        button.setFont(new Font("Times New Roman", Font.PLAIN, 48));
        button.setForeground(MAROON);
        button.setPreferredSize(new Dimension(width, 66));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static class RadioCircleIcon implements Icon {
        private final boolean selected;

        RadioCircleIcon(boolean selected) {
            this.selected = selected;
        }

        @Override
        public int getIconWidth() {
            return 15;
        }

        @Override
        public int getIconHeight() {
            return 15;
        }

        @Override
        public void paintIcon(Component component, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(MAROON);
            if (selected) {
                g2.fillOval(x + 1, y + 1, 13, 13);
            } else {
                g2.setStroke(new BasicStroke(1f));
                g2.drawOval(x + 1, y + 1, 13, 13);
            }
            g2.dispose();
        }
    }

    // Julianne's ThemeManager will call this to change the background gradient
    public void setThemeColors(
            Color top,
            Color bottom
    ) {

        timerArea.setColors(
                top,
                bottom
        );
    }

    // ---------- small drawing classes ----------

    private static class TabButton extends JButton {
        private boolean selected;

        TabButton(String label) {
            super(label);
            setHorizontalAlignment(SwingConstants.CENTER);
            setForeground(MAROON);
        }

        void setTabSelected(boolean selected) {
            this.selected = selected;
            setForeground(selected ? MAROON : MAROON);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int width = selected ? getWidth() : 238;
            int height = getHeight() - 1;
            int radius = Math.min(18, height / 3);
            Path2D shape = new Path2D.Float();
            shape.moveTo(0, 0);
            shape.lineTo(width - radius, 0);
            shape.quadTo(width, 0, width, radius);
            shape.lineTo(width, height - radius);
            shape.quadTo(width, height, width - radius, height);
            shape.lineTo(0, height);
            shape.closePath();

            g2.setColor(selected ? TAB_SELECTED : TAB_BG);
            g2.fill(shape);
            g2.setColor(new Color(0x4A3A34));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(shape);

            FontMetrics fm = g2.getFontMetrics(getFont());
            int textWidth = fm.stringWidth(getText());
            int x = Math.max(0, (width - textWidth) / 2);
            int y = (height - fm.getHeight()) / 2 + fm.getAscent();
            g2.setFont(getFont());
            g2.setColor(getForeground());
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    private static class SoftPanel extends JPanel {
        private final Color fill;
        private final int arc;

        SoftPanel(Color fill, int arc) {
            this.fill = fill;
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedActionButton extends JButton {
        private final boolean filled;

        RoundedActionButton(String label, int width, int height, boolean filled) {
            super(label);
            this.filled = filled;
            setPreferredSize(new Dimension(width, height));
            setForeground(new Color(0x5C3A12));
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int inset = 1;
            int arc = getHeight();
            if (filled) {
                g2.setColor(CREAM);
                g2.fillRoundRect(inset, inset, getWidth() - 2 * inset,
                        getHeight() - 2 * inset, arc, arc);
            }
            g2.setColor(filled ? CREAM : MAROON);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(inset, inset, getWidth() - 2 * inset,
                    getHeight() - 2 * inset, arc, arc);
            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    private static class GradientPanel
            extends JPanel {

        private Color top;
        private Color bottom;

        GradientPanel(
                Color top,
                Color bottom
        ) {

            this.top = top;
            this.bottom = bottom;
        }

        void setColors(
                Color top,
                Color bottom
        ) {

            this.top = top;
            this.bottom = bottom;

            repaint();
        }

        @Override
        protected void paintComponent(
                Graphics g
        ) {

            super.paintComponent(g);

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setPaint(
                    new GradientPaint(
                            0,
                            0,
                            top,
                            getWidth(),
                            getHeight(),
                            bottom
                    )
            );

            g2.fillRect(
                    0,
                    0,
                    getWidth(),
                    getHeight()
            );

            g2.dispose();
        }
    }

    private static class HamburgerIcon
            implements Icon {

        public int getIconWidth() {
            return 30;
        }

        public int getIconHeight() {
            return 24;
        }

        public void paintIcon(
                Component c,
                Graphics g,
                int x,
                int y
        ) {

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setColor(CREAM);

            g2.setStroke(
                    new BasicStroke(3f)
            );

            g2.drawLine(
                    x + 2,
                    y + 4,
                    x + 28,
                    y + 4
            );

            g2.drawLine(
                    x + 2,
                    y + 12,
                    x + 28,
                    y + 12
            );

            g2.drawLine(
                    x + 2,
                    y + 20,
                    x + 28,
                    y + 20
            );

            g2.dispose();
        }
    }
}
