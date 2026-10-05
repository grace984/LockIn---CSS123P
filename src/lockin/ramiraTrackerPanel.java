package lockin;


import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;


public class ramiraTrackerPanel extends JPanel {


    // colors taken from the UI design
    private static final Color PANEL_BG = new Color(0xF2E6DC);  // same as the other tabs
    private static final Color BOX_BG = new Color(0xE0CEB3);    // tan cards
    private static final Color BAR_BG = new Color(0xC9B496);    // card outlines / scrollbar track
    private static final Color TEXT = new Color(0x5C3A12);      // warm brown text
    private static final Color TITLE = new Color(0x4A0F2A);     // maroon title


    // accents taken from theme 1 (maroon -> raspberry -> rose)
    private static final Color ACCENT = new Color(0x4A0F2A);
    private static final Color ROSE = new Color(0xB89A6B);


    // extra colors for the polished look
    private static final Color FIELD_BG = new Color(0xFFFDFA);
    private static final Color CREAM = new Color(0xFFF6EE);
    private static final Color ROW_HOVER = new Color(0xEBDFC9);
    private static final Color CORAL = new Color(0x9C6B3C);
    private static final Color DANGER = new Color(0xC0392B);
    private static final Color MUTED = new Color(0x4A, 0x0F, 0x2A, 130);
    private static final Color ROW_TEXT = new Color(0x2B1A14);


    // width of the centered content column
    private static final int COLUMN_W = 440;


    // ---------- fonts ----------
    // Newsreader 36pt for the title, Times New Roman for everything else

    private static final Font HEADER_FONT = loadHeaderFont();

    private static Font loadHeaderFont() {
        String name = "Newsreader_36pt-Regular.ttf";

        ArrayList<File> candidates = new ArrayList<>();

        candidates.add(new File("fonts/" + name));
        candidates.add(new File("../fonts/" + name));

        try {
            // .../LockIn/out -> .../LockIn/fonts
            File codeLocation = new File(
                    ramiraTrackerPanel.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI());

            candidates.add(
                    new File(codeLocation.getParentFile(), "fonts/" + name)
            );

        } catch (Exception e) {
            // ignore, the other paths may still work
        }

        for (File f : candidates) {

            if (f.exists()) {

                try {
                    Font font = Font.createFont(
                            Font.TRUETYPE_FONT,
                            f
                    );

                    GraphicsEnvironment
                            .getLocalGraphicsEnvironment()
                            .registerFont(font);

                    return font;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println(
                "Newsreader font file not found. Looked in: "
                        + candidates
        );

        return new Font(
                "Newsreader 36pt",
                Font.PLAIN,
                12
        );
    }

    public static Font header(float size) {
        return HEADER_FONT.deriveFont(Font.PLAIN, size);
    }

    public static Font body(int style, int size) {
        return new Font("Times New Roman", style, size);
    }


    // same font with extra letter spacing (matches the spaced-out sidebar tabs)
    private static Font spaced(Font f, float tracking) {
        Map<TextAttribute, Object> attrs = new HashMap<>();
        attrs.put(TextAttribute.TRACKING, tracking);
        return f.deriveFont(attrs);
    }


    // ---------- warning message (picked in Settings) ----------

    private static final String[] WARNING_MESSAGES = {
        "This website is on your distraction list.",   // Version 1
        "Go back to work!",                            // Version 2
        "Eyes on the task!"                            // Version 3
    };

    private static final Preferences PREFS =
            Preferences.userNodeForPackage(ramiraTrackerPanel.class);

    // 0 = Version 1, 1 = Version 2, 2 = Version 3 (loaded from the saved choice)
    private static volatile int warningVersion =
            clampVersion(PREFS.getInt("warningVersion", 0));

    private static int clampVersion(int v) {
        return Math.max(0, Math.min(v, WARNING_MESSAGES.length - 1));
    }

    public static int getWarningVersion() {
        return warningVersion;
    }

    public static void setWarningVersion(int version) {
        warningVersion = clampVersion(version);
        PREFS.putInt("warningVersion", warningVersion); // saved, survives closing the app
    }

    public static String getWarningMessage() {
        return WARNING_MESSAGES[warningVersion];
    }

    // all messages, so Settings can show the real text next to each radio button
    public static String[] getWarningMessages() {
        return WARNING_MESSAGES.clone();
    }


    // restrictedSites is read by Jhenica's server (another thread) while the UI edits it,
    // so it uses a thread-safe list. It is static because the server reads it as
    // ramiraTrackerPanel.restrictedSites. trackedSites is only touched on the UI thread.
    public static final CopyOnWriteArrayList<String> restrictedSites = new CopyOnWriteArrayList<>();
    private final ArrayList<String> trackedSites = new ArrayList<>();
    private final JPanel listPanel = new JPanel();
    private final JPanel trackedPanel = new JPanel();
    private JTextField input;
    private RoundedPanel addBar;
    private JLabel restrictedLabel;
    private JLabel trackedLabel;


    public ramiraTrackerPanel() {
        setBackground(PANEL_BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(24, 48, 24, 48));


        add(Box.createVerticalGlue()); // pushes everything to the vertical middle


        // title, centered (Newsreader)
        JLabel title = new JLabel("Distraction Sites", SwingConstants.CENTER);
        title.setFont(spaced(body(Font.BOLD, 32), 0.04f));
        title.setForeground(TITLE);
        add(col(title, 42));
        add(Box.createVerticalStrut(4));


        // little ornament under the title
        add(col(new OrnamentDivider(), 16));
        add(Box.createVerticalStrut(6));


        // description, centered
        CenteredText desc = new CenteredText("Add website links distracting you from work. "
                + "LockIn will warn you off the sites and keep you productive.", body(Font.PLAIN, 15), TEXT);
        add(col(desc, 52));
        add(Box.createVerticalStrut(18));


        // add site bar
        add(col(buildAddBar(), 50));
        add(Box.createVerticalStrut(20));


        // restricted sites
        restrictedLabel = sectionLabel("RESTRICTED SITES");
        add(col(sectionHeader(restrictedLabel, null), 24));
        add(Box.createVerticalStrut(6));
        add(col(buildListBox(listPanel), 148));
        add(Box.createVerticalStrut(20));


        // tracked sites (with a Clear link on the right)
        trackedLabel = sectionLabel("TRACKED SITES");
        add(col(sectionHeader(trackedLabel, buildClearLink()), 24));
        add(Box.createVerticalStrut(6));
        add(col(buildListBox(trackedPanel), 148));


        add(Box.createVerticalGlue());


        refreshLists(); // shows the empty-state hints from the start
    }


    // ---------- building the screen ----------


    // gives a component a fixed size and centers it in the column
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


    private JPanel sectionHeader(JLabel label, JComponent right) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 4, 0, 4));
        p.add(label, BorderLayout.WEST);
        if (right != null) {
            p.add(right, BorderLayout.EAST);
        }
        return p;
    }


    private JButton buildClearLink() {
        JButton clear = new JButton("Clear");
        clear.setFont(body(Font.PLAIN, 14));
        clear.setForeground(TEXT);
        clear.setBorderPainted(false);
        clear.setContentAreaFilled(false);
        clear.setFocusPainted(false);
        clear.setMargin(new Insets(0, 0, 0, 0));
        clear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clear.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { clear.setForeground(DANGER); }


            @Override
            public void mouseExited(MouseEvent e) { clear.setForeground(TEXT); }
        });
        clear.addActionListener(e -> {
            trackedSites.clear();
            refreshLists();
        });
        return clear;
    }


    private JPanel buildAddBar() {
        addBar = new RoundedPanel(FIELD_BG, 14, true);
        addBar.setBaseOutline(BAR_BG);
        addBar.setLayout(new BorderLayout(10, 0));
        addBar.setBorder(new EmptyBorder(6, 14, 10, 6)); // extra bottom space is for the shadow


        JLabel globe = new JLabel(new GlobeIcon());


        input = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) { // hint text
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(MUTED);
                    g2.setFont(body(Font.ITALIC, 16));
                    FontMetrics fm = g2.getFontMetrics();
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString("e.g. youtube.com", getInsets().left, y);
                    g2.dispose();
                }
            }
        };
        input.setOpaque(false);
        input.setBorder(new EmptyBorder(0, 2, 0, 4));
        input.setFont(body(Font.PLAIN, 17));
        input.setForeground(ROW_TEXT);
        input.setCaretColor(TITLE);
        input.addActionListener(e -> addSite(input.getText())); // Enter key


        // outline glows while typing
        input.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) { addBar.setOutline(ACCENT); }


            @Override
            public void focusLost(FocusEvent e) { addBar.setOutline(null); }
        });


        PillButton add = new PillButton("Add");
        add.addActionListener(e -> {
            if (input.getText().trim().isEmpty()) {
                input.requestFocusInWindow(); // nothing typed yet: just focus the box
            } else {
                addSite(input.getText());
            }
        });


        addBar.add(globe, BorderLayout.WEST);
        addBar.add(input, BorderLayout.CENTER);
        addBar.add(add, BorderLayout.EAST);
        addBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                input.requestFocusInWindow(); // clicking the bar focuses the text box
            }
        });
        return addBar;
    }


    // a rounded tan card with a scrollable list and a slim scrollbar
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
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);


        JScrollBar vbar = scroll.getVerticalScrollBar();
        vbar.setUI(new SlimScrollBarUI());
        vbar.setPreferredSize(new Dimension(10, 0));
        vbar.setOpaque(false);
        vbar.setUnitIncrement(12);


        RoundedPanel box = new RoundedPanel(BOX_BG, 16, true);
        box.setBaseOutline(BAR_BG);
        box.setLayout(new BorderLayout());
        box.setBorder(new EmptyBorder(8, 8, 12, 8)); // extra bottom space is for the shadow
        box.add(scroll, BorderLayout.CENTER);
        return box;
    }


    private JButton iconButton(Icon icon) {
        JButton b = new JButton(icon);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setRolloverEnabled(true); // lets the icons react to the mouse
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }


    // restricted = true -> trash icon (removes it from the block list)
    // restricted = false -> x icon (just dismisses it from the tracked log)
    private JPanel buildRow(String site, boolean restricted) {
        HoverRow row = new HoverRow();
        row.setLayout(new BorderLayout(10, 0));
        row.setBorder(new EmptyBorder(0, 8, 0, 6));
        row.setPreferredSize(new Dimension(100, 36));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));


        Badge badge = new Badge(site.substring(0, 1).toUpperCase(), restricted ? TITLE : CORAL);


        JLabel label = new JLabel(site);
        label.setFont(body(Font.PLAIN, 16));
        label.setForeground(ROW_TEXT);


        JButton action = iconButton(restricted ? new TrashIcon() : new CrossIcon());
        action.setToolTipText(restricted ? "Remove from restricted sites" : "Dismiss");
        action.addActionListener(e -> {
            if (restricted) {
                restrictedSites.remove(site);
            } else {
                trackedSites.remove(site);
            }
            refreshLists();
        });


        row.add(badge, BorderLayout.WEST);
        row.add(label, BorderLayout.CENTER);
        row.add(action, BorderLayout.EAST);
        row.attachHover(badge, label, action);
        return row;
    }


    private JLabel emptyHint(String line1, String line2) {
        JLabel hint = new JLabel("<html><div style='text-align:center'>" + line1
                + "<br><span style='font-size:11px'>" + line2 + "</span></div></html>",
                SwingConstants.CENTER);
        hint.setFont(body(Font.ITALIC, 16));
        hint.setForeground(MUTED);
        hint.setPreferredSize(new Dimension(100, 110));
        hint.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        return hint;
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
            flashError(); // empty or duplicate: red flash
            return;
        }
        restrictedSites.add(site);
        input.setText("");
        refreshLists();
    }


    // briefly flashes the add bar red
    private void flashError() {
        addBar.setOutline(DANGER);
        Timer t = new Timer(400, e -> addBar.setOutline(input.hasFocus() ? ACCENT : null));
        t.setRepeats(false);
        t.start();
    }


    private void refreshLists() {
        listPanel.removeAll();
        if (restrictedSites.isEmpty()) {
            listPanel.add(emptyHint("No sites yet", "Type a site above and press Enter"));
        }
        for (String site : restrictedSites) {
            listPanel.add(buildRow(site, true));
        }


        trackedPanel.removeAll();
        if (trackedSites.isEmpty()) {
            trackedPanel.add(emptyHint("Nothing caught yet", "Visits to restricted sites will show up here"));
        }
        for (String site : trackedSites) {
            trackedPanel.add(buildRow(site, false));
        }


        restrictedLabel.setText(restrictedSites.isEmpty() ? "RESTRICTED SITES"
                : "RESTRICTED SITES  \u00B7  " + restrictedSites.size());
        trackedLabel.setText(trackedSites.isEmpty() ? "TRACKED SITES"
                : "TRACKED SITES  \u00B7  " + trackedSites.size());


        listPanel.revalidate();
        listPanel.repaint();
        trackedPanel.revalidate();
        trackedPanel.repaint();
    }


    // Jhenica's server and the warning call this.
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


    // Call this when a restricted site is caught during Work, so it shows under "Tracked sites".
    // Safe to call from another thread (like the server): the screen update runs on the UI thread.
    public void logTrackedSite(String site) {
        final String cleaned = clean(site);
        SwingUtilities.invokeLater(() -> {
            if (!cleaned.isEmpty() && !trackedSites.contains(cleaned)) {
                trackedSites.add(cleaned);
                refreshLists();
            }
        });
    }


    // =========================================================
    // WARNING
    // Pop-up version of the LockIn warning, uses the message picked in Settings.
    // =========================================================
    public void showWarning(String site) {

        final String message = getWarningMessage();

        SwingUtilities.invokeLater(() -> {

            JOptionPane.showMessageDialog(
                    this,
                    message + "\n\n" + clean(site),
                    "LockIn Warning",
                    JOptionPane.WARNING_MESSAGE
            );

        });
    }


    // ---------- small drawing helpers ----------


    // list row that lights up when the mouse is over it (or over its badge/label/button)
    private static class HoverRow extends JPanel {
        private boolean hover;


        HoverRow() {
            setOpaque(false);
            addMouseListener(hoverListener());
        }


        private MouseAdapter hoverListener() {
            return new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { setHover(true); }


                @Override
                public void mouseExited(MouseEvent e) {
                    // moving onto a child still counts as being inside the row
                    Point p = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), HoverRow.this);
                    setHover(contains(p));
                }
            };
        }


        void attachHover(Component... children) {
            MouseAdapter m = hoverListener();
            for (Component c : children) {
                c.addMouseListener(m);
            }
        }


        void setHover(boolean h) {
            if (hover != h) {
                hover = h;
                repaint();
            }
        }


        @Override
        protected void paintComponent(Graphics g) {
            if (hover) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ROW_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }


    // rounded card with an optional soft shadow (the bottom 4px are reserved for it)
    private static class RoundedPanel extends JPanel {
        private final Color color;
        private final int radius;
        private final boolean shadow;
        private Color outline;     // changes while focusing / flashing
        private Color baseOutline; // always-on thin outline


        RoundedPanel(Color color, int radius, boolean shadow) {
            this.color = color;
            this.radius = radius;
            this.shadow = shadow;
            setOpaque(false);
        }


        void setOutline(Color c) {
            this.outline = c;
            repaint();
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
            int h = getHeight() - (shadow ? 4 : 0);


            if (shadow) {
                for (int i = 0; i < 4; i++) {
                    g2.setColor(new Color(0x59, 0x13, 0x2C, 12));
                    g2.fillRoundRect(2, i + 1, w - 4, h, radius * 2, radius * 2);
                }
            }


            g2.setColor(color);
            g2.fillRoundRect(0, 0, w, h, radius * 2, radius * 2);


            Color o = outline != null ? outline : baseOutline;
            if (o != null) {
                g2.setColor(o);
                g2.setStroke(new BasicStroke(outline != null ? 2f : 1.2f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, radius * 2, radius * 2);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }


    // maroon "Add" pill button
    private static class PillButton extends JButton {
        PillButton(String text) {
            super(text);
            setFont(spaced(body(Font.BOLD, 14), 0.08f));
            setForeground(CREAM);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setMargin(new Insets(0, 16, 0, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(68, 30));
        }


        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color left = TITLE;
            Color right = ACCENT;
            if (getModel().isPressed()) {
                left = left.darker();
                right = right.darker();
            } else if (getModel().isRollover()) {
                left = ACCENT;
                right = ROSE;
            }
            g2.setPaint(new GradientPaint(0, 0, left, getWidth(), 0, right));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }


    // round badge with the first letter of the site
    private static class Badge extends JComponent {
        private final String letter;
        private final Color color;


        Badge(String letter, Color color) {
            this.letter = letter;
            this.color = color;
            setPreferredSize(new Dimension(26, 26));
        }


        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int d = 24;
            int x = (getWidth() - d) / 2;
            int y = (getHeight() - d) / 2;
            g2.setPaint(new GradientPaint(x, y, color, x + d, y + d, color.brighter()));
            g2.fillOval(x, y, d, d);
            g2.setColor(CREAM);
            g2.setFont(body(Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (d - fm.stringWidth(letter)) / 2;
            int ty = y + (d + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(letter, tx, ty);
            g2.dispose();
        }
    }


    // centered text that wraps to the width it actually has (no HTML, so it can never get cut off)
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


    // thin line with a diamond in the middle, fading out at both ends
    private static class OrnamentDivider extends JComponent {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int cx = w / 2;
            int cy = getHeight() / 2;
            Color solid = ROSE;
            Color clear = new Color(0xD1, 0x7D, 0x93, 0);


            g2.setPaint(new GradientPaint(cx - 150, 0, clear, cx - 14, 0, solid));
            g2.drawLine(cx - 150, cy, cx - 14, cy);
            g2.setPaint(new GradientPaint(cx + 14, 0, solid, cx + 150, 0, clear));
            g2.drawLine(cx + 14, cy, cx + 150, cy);


            g2.setColor(ACCENT);
            g2.fillPolygon(new int[]{cx, cx + 5, cx, cx - 5}, new int[]{cy - 5, cy, cy + 5, cy}, 4);
            g2.dispose();
        }
    }


    // slim rounded scrollbar: darker track, lighter thumb, no arrow buttons
    private static class SlimScrollBarUI extends BasicScrollBarUI {
        private static final Color THUMB = ROSE;


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


            Rectangle thumb = getThumbBounds();
            int y = thumb.height > 0 ? thumb.y : track.y;
            int h = thumb.height > 0 ? thumb.height : track.height / 3;
            g2.setColor(THUMB);
            g2.fillRoundRect(track.x, y, track.width, h, track.width, track.width);
            g2.dispose();
        }
    }


    private static boolean isHovered(Component c) {
        return c instanceof AbstractButton && ((AbstractButton) c).getModel().isRollover();
    }


    private static class GlobeIcon implements Icon {
        public int getIconWidth() { return 20; }
        public int getIconHeight() { return 20; }


        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(TEXT);
            g2.setStroke(new BasicStroke(1.3f));
            g2.drawOval(x + 1, y + 1, 18, 18);
            g2.drawOval(x + 6, y + 1, 8, 18);   // meridian
            g2.drawLine(x + 1, y + 10, x + 19, y + 10); // equator
            g2.dispose();
        }
    }


    private static class TrashIcon implements Icon {
        public int getIconWidth() { return 16; }
        public int getIconHeight() { return 16; }


        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isHovered(c) ? DANGER : TEXT); // red on hover
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(x + 1, y + 4, x + 15, y + 4);      // lid
            g2.drawRect(x + 6, y + 1, 4, 3);               // handle
            g2.drawRoundRect(x + 3, y + 5, 10, 10, 2, 2);  // body
            g2.drawLine(x + 6, y + 7, x + 6, y + 13);      // lines
            g2.drawLine(x + 10, y + 7, x + 10, y + 13);
            g2.dispose();
        }
    }


    private static class CrossIcon implements Icon {
        public int getIconWidth() { return 16; }
        public int getIconHeight() { return 16; }


        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isHovered(c) ? DANGER : TEXT);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x + 4, y + 4, x + 12, y + 12);
            g2.drawLine(x + 12, y + 4, x + 4, y + 12);
            g2.dispose();
        }
    }
}