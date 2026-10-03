package lockin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;

public class ramiraTrackerPanel extends JPanel {

    // colors taken from the UI design
    private static final Color PANEL_BG = new Color(0xF2E6DC);
    private static final Color BOX_BG = new Color(0xE0CEB3);
    private static final Color BAR_BG = new Color(0xC9B496);
    private static final Color TEXT = new Color(0x5C3A12);
    private static final Color TITLE = new Color(0x4A0F2A);

    private ArrayList<String> restrictedSites = new ArrayList<>();
    private ArrayList<String> trackedSites = new ArrayList<>();
    private JPanel listPanel = new JPanel();
    private JPanel trackedPanel = new JPanel();
    private JTextField input;

    public ramiraTrackerPanel() {
        setBackground(PANEL_BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // title
        JLabel title = new JLabel("Distraction sites:");
        title.setFont(new Font("Serif", Font.PLAIN, 24));
        title.setForeground(TITLE);
        title.setAlignmentX(CENTER_ALIGNMENT);
        add(title);
        add(Box.createVerticalStrut(8));

        // description
        JLabel desc = new JLabel("<html><div style='width:260px'>Add website links distracting you "
                + "from work. LockIn will warn you off the sites and keep you productive.</div></html>");
        desc.setFont(new Font("Serif", Font.PLAIN, 15));
        desc.setForeground(TITLE);
        desc.setAlignmentX(CENTER_ALIGNMENT);
        add(desc);
        add(Box.createVerticalStrut(12));

        // add site bar: [+] Add site
        add(buildAddBar());
        add(Box.createVerticalStrut(6));

        // box with the restricted sites (bullet + trash icon)
        add(buildListBox(listPanel));
        add(Box.createVerticalStrut(16));

        // tracked sites
        JLabel trackedLabel = new JLabel("Tracked sites:");
        trackedLabel.setFont(new Font("Serif", Font.PLAIN, 15));
        trackedLabel.setForeground(TITLE);
        trackedLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(trackedLabel);
        add(Box.createVerticalStrut(6));
        add(buildListBox(trackedPanel));
        add(Box.createVerticalGlue());
    }

    // ---------- building the screen ----------

    private JPanel buildAddBar() {
        RoundedPanel bar = new RoundedPanel(BAR_BG, 10);
        bar.setLayout(new BorderLayout(6, 0));
        bar.setBorder(new EmptyBorder(6, 8, 6, 8));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        bar.setPreferredSize(new Dimension(300, 40));
        bar.setAlignmentX(CENTER_ALIGNMENT);

        JButton plus = iconButton(new PlusIcon());
                plus.addActionListener(e -> {
            if (input.getText().trim().isEmpty()) {
                input.requestFocusInWindow(); // nothing typed yet: just focus the box
            } else {
                addSite(input.getText());
            }
        });

        input = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) { // "Add site" hint text
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(TEXT);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString("Add site", getInsets().left, y);
                    g2.dispose();
                }
            }
        };
        input.setOpaque(false);
        input.setBorder(new EmptyBorder(0, 4, 0, 4));
        input.setFont(new Font("Serif", Font.BOLD, 16));
        input.setForeground(TEXT);
        input.setCaretColor(TEXT);
        input.addActionListener(e -> addSite(input.getText())); // Enter key

        bar.add(plus, BorderLayout.WEST);
        bar.add(input, BorderLayout.CENTER);
                bar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                input.requestFocusInWindow(); // clicking the bar focuses the text box
            }
        });
        return bar;
    }

    // a rounded tan box with a scrollable list inside
    private JPanel buildListBox(JPanel list) {
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(list, BorderLayout.NORTH); // keeps rows at the top

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        RoundedPanel box = new RoundedPanel(BOX_BG, 14);
        box.setLayout(new BorderLayout());
        box.setBorder(new EmptyBorder(8, 10, 8, 6));
        box.setPreferredSize(new Dimension(300, 150));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        box.setAlignmentX(CENTER_ALIGNMENT);
        box.add(scroll, BorderLayout.CENTER);
        return box;
    }

    private JButton iconButton(Icon icon) {
        JButton b = new JButton(icon);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JPanel buildRow(String site, boolean withDelete) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel label = new JLabel("\u2022  " + site); // bullet + site
        label.setFont(new Font("Serif", Font.PLAIN, 15));
        label.setForeground(Color.BLACK);
        row.add(label, BorderLayout.CENTER);

        if (withDelete) {
            JButton trash = iconButton(new TrashIcon());
            trash.addActionListener(e -> {
                restrictedSites.remove(site);
                refreshLists();
            });
            row.add(trash, BorderLayout.EAST);
        }
        return row;
    }

    // ---------- the logic ----------

    // makes "https://www.YouTube.com/watch?v=1" become "youtube.com"
    private String clean(String site) {
        site = site.trim().toLowerCase();
        site = site.replace("https://", "").replace("http://", "").replace("www.", "");
        int slash = site.indexOf('/');
        if (slash != -1) {
            site = site.substring(0, slash);
        }
        return site;
    }

    private void addSite(String site) {
        site = clean(site);
        if (site.isEmpty() || restrictedSites.contains(site)) {
            return; // ignore empty or duplicate
        }
        restrictedSites.add(site);
        input.setText("");
        refreshLists();
    }

    private void refreshLists() {
        listPanel.removeAll();
        for (String site : restrictedSites) {
            listPanel.add(buildRow(site, true));
        }
        trackedPanel.removeAll();
        for (String site : trackedSites) {
            trackedPanel.add(buildRow(site, false));
        }
        listPanel.revalidate();
        listPanel.repaint();
        trackedPanel.revalidate();
        trackedPanel.repaint();
    }

    // Jhenica's server and the warning will call this
    // youtube.com is restricted -> m.youtube.com is too
    public boolean isRestricted(String site) {
        String host = clean(site);
        for (String s : restrictedSites) {
            if (host.equals(s) || host.endsWith("." + s)) {
                return true;
            }
        }
        return false;
    }

    // Call this when a restricted site is caught during Work, so it shows under "Tracked sites"
    public void logTrackedSite(String site) {
        site = clean(site);
        if (!trackedSites.contains(site)) {
            trackedSites.add(site);
            refreshLists();
        }
    }

    // ---------- small drawing helpers ----------

    private static class RoundedPanel extends JPanel {
        private Color color;
        private int radius;

        RoundedPanel(Color color, int radius) {
            this.color = color;
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius * 2, radius * 2);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class PlusIcon implements Icon {
        public int getIconWidth() { return 22; }
        public int getIconHeight() { return 22; }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(TEXT);
            g2.fillOval(x, y, 22, 22);
            g2.setColor(BAR_BG);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(x + 6, y + 11, x + 16, y + 11);
            g2.drawLine(x + 11, y + 6, x + 11, y + 16);
            g2.dispose();
        }
    }

    private static class TrashIcon implements Icon {
        public int getIconWidth() { return 16; }
        public int getIconHeight() { return 16; }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(TEXT);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(x + 1, y + 4, x + 15, y + 4);      // lid
            g2.drawRect(x + 6, y + 1, 4, 3);               // handle
            g2.drawRoundRect(x + 3, y + 5, 10, 10, 2, 2);  // body
            g2.drawLine(x + 6, y + 7, x + 6, y + 13);      // lines
            g2.drawLine(x + 10, y + 7, x + 10, y + 13);
            g2.dispose();
        }
    }

    // TEST ONLY: delete this method before the final push
    public static void main(String[] args) {
        JFrame f = new JFrame("Tracker test");
        f.add(new ramiraTrackerPanel());
        f.setSize(360, 560);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}