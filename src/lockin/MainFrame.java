package lockin;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFrame extends JFrame {

    // colors from the UI design
    private static final Color TAB_BG = new Color(0xF5E1CC);
    private static final Color TAB_SELECTED = new Color(0xF7EFE4);
    private static final Color PANEL_BG = new Color(0xF2E6DC);
    private static final Color MAROON = new Color(0x5A1030);
    private static final Color CREAM = new Color(0xFAEBD7);

    private CardLayout cardLayout = new CardLayout();
    private JPanel contentPanel = new JPanel(cardLayout);
    private JPanel drawer = new JPanel(new BorderLayout());
    private Map<String, JButton> tabButtons = new LinkedHashMap<>();
    private GradientPanel timerArea;

    public MainFrame() {
        setTitle("LockIn");
        setSize(1000, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ---------- RIGHT SIDE: gradient area with the hamburger button ----------
        timerArea = new GradientPanel(new Color(0xA52A62), new Color(0xF8B4E0));
        timerArea.setLayout(new BorderLayout());

        JButton hamburger = new JButton(new HamburgerIcon());
        hamburger.setBorderPainted(false);
        hamburger.setContentAreaFilled(false);
        hamburger.setFocusPainted(false);
        hamburger.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        hamburger.addActionListener(e -> toggleDrawer());

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topBar.setOpaque(false);
        topBar.add(hamburger);
        timerArea.add(topBar, BorderLayout.NORTH);

        // TODO (Laurice's panel): replace this label with  timerArea.add(new lauriceTimerPanel(), BorderLayout.CENTER);
        // her panel must call setOpaque(false) so the gradient shows through
        lauriceTimerPanel timerPanel = new lauriceTimerPanel();
        timerPanel.setOpaque(false);
        timerArea.add(timerPanel, BorderLayout.CENTER);

        add(timerArea, BorderLayout.CENTER);

        // ---------- LEFT SIDE: drawer = tabs column + content panel ----------
        JPanel tabsGrid = new JPanel(new GridLayout(4, 1));
        tabsGrid.setBackground(TAB_BG);
        addTab(tabsGrid, "TIME", "time");
        addTab(tabsGrid, "TRACKER", "tracker");
        addTab(tabsGrid, "THEMES", "themes");
        addTab(tabsGrid, "SETTINGS", "settings");

        JPanel tabsHolder = new JPanel(new BorderLayout());
        tabsHolder.setBackground(TAB_BG);
        tabsHolder.add(tabsGrid, BorderLayout.NORTH);

        // screens: swap each placeholder for the real panel when it's ready
        contentPanel.add(placeholder("Time settings (Laurice)"), "time");
        contentPanel.add(new ramiraTrackerPanel(), "tracker");
        contentPanel.add(placeholder("Themes (Julianne)"), "themes");
        contentPanel.add(placeholder("Settings (Laurice)"), "settings");
        contentPanel.setPreferredSize(new Dimension(380, 100));

        drawer.add(tabsHolder, BorderLayout.WEST);
        drawer.add(contentPanel, BorderLayout.CENTER);
        drawer.setVisible(false); // starts closed: only the hamburger shows
        add(drawer, BorderLayout.WEST);

        selectTab("time");
    }

    // ---------- helpers ----------

    private void toggleDrawer() {
        drawer.setVisible(!drawer.isVisible());
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    private void addTab(JPanel grid, String label, String key) {
        Font base = new Font("Serif", Font.PLAIN, 17);
        Map<TextAttribute, Object> attrs = new HashMap<TextAttribute, Object>(base.getAttributes());
        attrs.put(TextAttribute.TRACKING, 0.25); // letter spacing like the design

        JButton b = new JButton(label);
        b.setFont(base.deriveFont(attrs));
        b.setForeground(MAROON);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x8A6A5A)));
        b.setPreferredSize(new Dimension(150, 60));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> selectTab(key));

        tabButtons.put(key, b);
        grid.add(b);
    }

    private void selectTab(String key) {
        cardLayout.show(contentPanel, key);
        for (Map.Entry<String, JButton> entry : tabButtons.entrySet()) {
            entry.getValue().setBackground(entry.getKey().equals(key) ? TAB_SELECTED : TAB_BG);
        }
    }

    private JPanel placeholder(String text) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(PANEL_BG);
        JLabel l = new JLabel(text);
        l.setFont(new Font("Serif", Font.PLAIN, 18));
        l.setForeground(MAROON);
        p.add(l);
        return p;
    }

    // Julianne's ThemeManager will call this to change the background gradient
    public void setThemeColors(Color top, Color bottom) {
        timerArea.setColors(top, bottom);
    }

    // ---------- small drawing classes ----------

    private static class GradientPanel extends JPanel {
        private Color top;
        private Color bottom;

        GradientPanel(Color top, Color bottom) {
            this.top = top;
            this.bottom = bottom;
        }

        void setColors(Color top, Color bottom) {
            this.top = top;
            this.bottom = bottom;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0, top, getWidth(), getHeight(), bottom));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    private static class HamburgerIcon implements Icon {
        public int getIconWidth() { return 30; }
        public int getIconHeight() { return 24; }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(CREAM);
            g2.setStroke(new BasicStroke(3f));
            g2.drawLine(x + 2, y + 4, x + 28, y + 4);
            g2.drawLine(x + 2, y + 12, x + 28, y + 12);
            g2.drawLine(x + 2, y + 20, x + 28, y + 20);
            g2.dispose();
        }
    }
}