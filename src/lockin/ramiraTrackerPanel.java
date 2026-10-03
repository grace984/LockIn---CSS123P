package lockin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class ramiraTrackerPanel extends JPanel {

    // colors taken from the UI design
    private static final Color PANEL_BG = new Color(0xF2E6DC);
    private static final Color BOX_BG = new Color(0xE0CEB3);
    private static final Color BAR_BG = new Color(0xC9B496);
    private static final Color TEXT = new Color(0x5C3A12);
    private static final Color TITLE = new Color(0x4A0F2A);

    // fonts: Newsreader 36pt for headers, Times New Roman for everything else
    private static final Font HEADER_FONT = loadHeaderFont();

    private static Font loadHeaderFont() {
        String name = "Newsreader_36pt-Regular.ttf";
        ArrayList<File> candidates = new ArrayList<>();
        candidates.add(new File("fonts/" + name));
        candidates.add(new File("../fonts/" + name));
        try {
            // .../LockIn/out  ->  .../LockIn/fonts
            File codeLocation = new File(
                    ramiraTrackerPanel.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            candidates.add(new File(codeLocation.getParentFile(), "fonts/" + name));
        } catch (Exception e) {
            // ignore, the other paths may still work
        }

        for (File f : candidates) {
            if (f.exists()) {
                try {
                    Font font = Font.createFont(Font.TRUETYPE_FONT, f);
                    GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
                    return font;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Newsreader font file not found. Looked in: " + candidates);
        return new Font("Newsreader 36pt", Font.PLAIN, 12); // works if installed on the computer
    }

    private static Font header(float size) {
        return HEADER_FONT.deriveFont(Font.PLAIN, size);
    }

    private static Font body(int style, int size) {
        return new Font("Times New Roman", style, size);
    }

    private ArrayList<String> restrictedSites = new ArrayList<>();
    private ArrayList<String> trackedSites = new ArrayList<>();
    private JPanel listPanel = new JPanel();
    private JPanel trackedPanel = new JPanel();
    private JTextField input;

    public ramiraTrackerPanel() {
        setBackground(PANEL_BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // title (Newsreader)
        JLabel title = new JLabel("Distraction sites:");
        title.setFont(header(28));
        title.setForeground(TITLE);
        title.setAlignmentX(CENTER_ALIGNMENT);
        add(title);
        add(Box.createVerticalStrut(8));

        // description (Times New Roman)
        JLabel desc = new JLabel("<html><div style='width:320px'>Add website links distracting you "
                + "from work. LockIn will warn you off the sites and keep you productive.</div></html>");
        desc.setFont(body(Font.PLAIN, 15));
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

        // "Tracked sites:" label, aligned to the left
        JLabel trackedLabel = new JLabel("Tracked sites:");
        trackedLabel.setFont(body(Font.PLAIN, 15));
        trackedLabel.setForeground(TITLE);
        JPanel trackedRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        trackedRow.setOpaque(false);
        trackedRow.add(trackedLabel);
        trackedRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        trackedRow.setAlignmentX(CENTER_ALIGNMENT);
        add(trackedRow);
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
        input.setFont(body(Font.BOLD, 16));
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

    // a rounded tan box with a scrollable list and a slim scrollbar
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
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JScrollBar vbar = scroll.getVerticalScrollBar();
        vbar.setUI(new SlimScrollBarUI());
        vbar.setPreferredSize(new Dimension(10, 0));
        vbar.setOpaque(false);
        vbar.setUnitIncrement(12);

        RoundedPanel box = new RoundedPanel(BOX_BG, 14);
        box.setLayout(new BorderLayout());
        box.setBorder(new EmptyBorder(8, 10, 8, 8));
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
        label.setFont(body(Font.PLAIN, 15));
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

    // slim rounded scrollbar like the design: darker track, lighter thumb, no arrow buttons
    private static class SlimScrollBarUI extends BasicScrollBarUI {
        private static final Color THUMB = new Color(0xEADFCB);

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return emptyButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return emptyButton();
        }

        private JButton emptyButton() {
            JButton b = new JButton();
            Dimension zero = new Dimension(0, 0);
            b.setPreferredSize(zero);
            b.setMinimumSize(zero);
            b.setMaximumSize(zero);
            return b;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Rectangle track = getTrackBounds();
            g2.setColor(BAR_BG);
            g2.fillRoundRect(track.x, track.y, track.width, track.height, track.width, track.width);

            // when there's nothing to scroll yet, still show a thumb like the design
            Rectangle thumb = getThumbBounds();
            int y = thumb.height > 0 ? thumb.y : track.y;
            int h = thumb.height > 0 ? thumb.height : track.height / 3;
            g2.setColor(THUMB);
            g2.fillRoundRect(track.x, y, track.width, h, track.width, track.width);
            g2.dispose();
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
        f.setSize(420, 600);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}  