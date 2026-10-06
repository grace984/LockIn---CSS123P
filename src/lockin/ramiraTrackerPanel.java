// Package this class belongs to
package lockin;


// AWT classes: colors, fonts, layouts, drawing
import java.awt.*;
// Base class for reacting to focus changes
import java.awt.event.FocusAdapter;
// Describes a focus gained/lost event
import java.awt.event.FocusEvent;
// Base class for reacting to mouse events
import java.awt.event.MouseAdapter;
// Describes a mouse event
import java.awt.event.MouseEvent;
// Lets us change font attributes such as letter spacing
import java.awt.font.TextAttribute;
// Used to find the font file on disk
import java.io.File;
// Resizable list
import java.util.ArrayList;
// Used to hold font attributes
import java.util.HashMap;
// Generic key-value map
import java.util.Map;
// Thread-safe list (shared with the server thread)
import java.util.concurrent.CopyOnWriteArrayList;
// Saves small settings between app runs
import java.util.prefs.Preferences;
// Swing components (JPanel, JButton, etc.)
import javax.swing.*;
// Lets us add empty padding around a component
import javax.swing.border.EmptyBorder;
// Base class for customizing scrollbars
import javax.swing.plaf.basic.BasicScrollBarUI;


// The Tracker tab: lets the user add restricted sites and shows the sites that were caught
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
    // Background of the text field
    private static final Color FIELD_BG = new Color(0xFFFDFA);
    // Light cream for text on dark buttons
    private static final Color CREAM = new Color(0xFFF6EE);
    // Row highlight color on hover
    private static final Color ROW_HOVER = new Color(0xEBDFC9);
    // Badge color for tracked sites
    private static final Color CORAL = new Color(0x9C6B3C);
    // Red used for errors and delete hover
    private static final Color DANGER = new Color(0xC0392B);
    // Semi-transparent maroon for hint text
    private static final Color MUTED = new Color(0x4A, 0x0F, 0x2A, 130);
    // Dark color for site names
    private static final Color ROW_TEXT = new Color(0x2B1A14);


    // width of the centered content column
    private static final int COLUMN_W = 440;


    // ---------- fonts ----------
    // Newsreader 36pt for the title, Times New Roman for everything else

    // Load the header font once when the class loads
    private static final Font HEADER_FONT = loadHeaderFont();

    // Looks for the Newsreader font file in several folders and loads it
    private static Font loadHeaderFont() {
        // File name of the font
        String name = "Newsreader_36pt-Regular.ttf";

        // List of places to look for the font
        ArrayList<File> candidates = new ArrayList<>();

        // Look in the "fonts" folder next to where the app runs
        candidates.add(new File("fonts/" + name));
        // Look in the "fonts" folder one level up
        candidates.add(new File("../fonts/" + name));

        try {
            // .../LockIn/out -> .../LockIn/fonts
            // Find where the compiled classes are located
            File codeLocation = new File(
                    ramiraTrackerPanel.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI());

            // Look in a "fonts" folder next to the compiled classes
            candidates.add(
                    new File(codeLocation.getParentFile(), "fonts/" + name)
            );

        } catch (Exception e) {
            // ignore, the other paths may still work
        }

        // Try each location in order
        for (File f : candidates) {

            // Only try files that exist
            if (f.exists()) {

                try {
                    // Read the font from the file
                    Font font = Font.createFont(
                            Font.TRUETYPE_FONT,
                            f
                    );

                    // Make the font available to Java
                    GraphicsEnvironment
                            .getLocalGraphicsEnvironment()
                            .registerFont(font);

                    // Use the loaded font
                    return font;

                } catch (Exception e) {
                    // Print the error and try the next location
                    e.printStackTrace();
                }
            }
        }

        // Tell the developer the font was not found
        System.out.println(
                "Newsreader font file not found. Looked in: "
                        + candidates
        );

        // Fall back to a font with the same name (may use a default font)
        return new Font(
                "Newsreader 36pt",
                Font.PLAIN,
                12
        );
    }

    // Returns the header font at the given size
    public static Font header(float size) {
        return HEADER_FONT.deriveFont(Font.PLAIN, size);
    }

    // Returns Times New Roman with the given style and size
    public static Font body(int style, int size) {
        return new Font("Times New Roman", style, size);
    }


    // same font with extra letter spacing (matches the spaced-out sidebar tabs)
    private static Font spaced(Font f, float tracking) {
        // Map of font attributes to change
        Map<TextAttribute, Object> attrs = new HashMap<>();
        // Set the letter spacing
        attrs.put(TextAttribute.TRACKING, tracking);
        // Return a copy of the font with that spacing
        return f.deriveFont(attrs);
    }


    // ---------- warning message (picked in Settings) ----------

    // The three possible warning messages
    private static final String[] WARNING_MESSAGES = {
        "This website is on your distraction list.",   // Version 1
        "Go back to work!",                            // Version 2
        "Eyes on the task!"                            // Version 3
    };

    // Storage for saving the chosen message between runs
    private static final Preferences PREFS =
            Preferences.userNodeForPackage(ramiraTrackerPanel.class);

    // 0 = Version 1, 1 = Version 2, 2 = Version 3 (loaded from the saved choice)
    private static volatile int warningVersion =
            clampVersion(PREFS.getInt("warningVersion", 0));

    // Keeps a version number inside the valid range
    private static int clampVersion(int v) {
        return Math.max(0, Math.min(v, WARNING_MESSAGES.length - 1));
    }

    // Returns the selected warning version number
    public static int getWarningVersion() {
        return warningVersion;
    }

    // Changes and saves the selected warning version
    public static void setWarningVersion(int version) {
        // Keep the value in range
        warningVersion = clampVersion(version);
        PREFS.putInt("warningVersion", warningVersion); // saved, survives closing the app
    }

    // Returns the text of the selected warning message
    public static String getWarningMessage() {
        return WARNING_MESSAGES[warningVersion];
    }

    // all messages, so Settings can show the real text next to each radio button
    public static String[] getWarningMessages() {
        // Return a copy so outside code can't change the original
        return WARNING_MESSAGES.clone();
    }


    // restrictedSites is read by Jhenica's server (another thread) while the UI edits it,
    // so it uses a thread-safe list. It is static because the server reads it as
    // ramiraTrackerPanel.restrictedSites. trackedSites is only touched on the UI thread.
    public static final CopyOnWriteArrayList<String> restrictedSites = new CopyOnWriteArrayList<>();
    // Sites that were caught during Work sessions
    private final ArrayList<String> trackedSites = new ArrayList<>();
    // Holds the rows for restricted sites
    private final JPanel listPanel = new JPanel();
    // Holds the rows for tracked sites
    private final JPanel trackedPanel = new JPanel();
    // Text box where the user types a site
    private JTextField input;
    // Rounded bar that contains the text box and Add button
    private RoundedPanel addBar;
    // Heading for the restricted list
    private JLabel restrictedLabel;
    // Heading for the tracked list
    private JLabel trackedLabel;


    // Builds the whole Tracker screen
    public ramiraTrackerPanel() {
        // Set the background color
        setBackground(PANEL_BG);
        // Stack children vertically
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // Padding around the whole panel
        setBorder(new EmptyBorder(24, 48, 24, 48));


        add(Box.createVerticalGlue()); // pushes everything to the vertical middle


        // title, centered (Newsreader)
        // Title label
        JLabel title = new JLabel("Distraction Sites", SwingConstants.CENTER);
        // Bold Times New Roman with slight letter spacing
        title.setFont(spaced(body(Font.BOLD, 32), 0.04f));
        // Maroon title color
        title.setForeground(TITLE);
        // Add the title at a fixed size
        add(col(title, 42));
        // Small gap
        add(Box.createVerticalStrut(4));


        // little ornament under the title
        add(col(new OrnamentDivider(), 16));
        // Small gap
        add(Box.createVerticalStrut(6));


        // description, centered
        // Wrapped, centered description text
        CenteredText desc = new CenteredText("Add website links distracting you from work. "
                + "LockIn will warn you off the sites and keep you productive.", body(Font.PLAIN, 15), TEXT);
        // Add the description at a fixed size
        add(col(desc, 52));
        // Gap before the add bar
        add(Box.createVerticalStrut(18));


        // add site bar
        add(col(buildAddBar(), 50));
        // Gap before the restricted list
        add(Box.createVerticalStrut(20));


        // restricted sites
        // Heading for the restricted list
        restrictedLabel = sectionLabel("RESTRICTED SITES");
        // Add the heading row (no right-side button)
        add(col(sectionHeader(restrictedLabel, null), 24));
        // Small gap
        add(Box.createVerticalStrut(6));
        // Add the scrollable restricted list
        add(col(buildListBox(listPanel), 148));
        // Gap before the tracked list
        add(Box.createVerticalStrut(20));


        // tracked sites (with a Clear link on the right)
        // Heading for the tracked list
        trackedLabel = sectionLabel("TRACKED SITES");
        // Add the heading row with the Clear link
        add(col(sectionHeader(trackedLabel, buildClearLink()), 24));
        // Small gap
        add(Box.createVerticalStrut(6));
        // Add the scrollable tracked list
        add(col(buildListBox(trackedPanel), 148));


        // Bottom glue keeps everything centered vertically
        add(Box.createVerticalGlue());


        loadSites();    // brings back the sites saved last time
        refreshLists(); // shows the empty-state hints from the start
    }

    // ---------- building the screen ----------


    // gives a component a fixed size and centers it in the column
    private <T extends JComponent> T col(T c, int height) {
        // Preferred size
        c.setPreferredSize(new Dimension(COLUMN_W, height));
        // Maximum size (so it doesn't stretch)
        c.setMaximumSize(new Dimension(COLUMN_W, height));
        // Center horizontally
        c.setAlignmentX(CENTER_ALIGNMENT);
        // Return the same component
        return c;
    }


    // Creates a small spaced-out heading label
    private JLabel sectionLabel(String text) {
        // Label with the given text
        JLabel l = new JLabel(text);
        // Bold small font with wide letter spacing
        l.setFont(spaced(body(Font.BOLD, 13), 0.18f));
        // Maroon text
        l.setForeground(TITLE);
        // Return the label
        return l;
    }


    // Builds a heading row: label on the left, optional component on the right
    private JPanel sectionHeader(JLabel label, JComponent right) {
        // Row using BorderLayout
        JPanel p = new JPanel(new BorderLayout());
        // Transparent background
        p.setOpaque(false);
        // Small left/right padding
        p.setBorder(new EmptyBorder(0, 4, 0, 4));
        // Label on the left
        p.add(label, BorderLayout.WEST);
        // Only add the right component if there is one
        if (right != null) {
            // Right-side component (e.g. Clear link)
            p.add(right, BorderLayout.EAST);
        }
        // Return the row
        return p;
    }


    // Creates the "Clear" text link for the tracked list
    private JButton buildClearLink() {
        // Button with the text "Clear"
        JButton clear = new JButton("Clear");
        // Button font
        clear.setFont(body(Font.PLAIN, 14));
        // Normal text color
        clear.setForeground(TEXT);
        // Remove the border
        clear.setBorderPainted(false);
        // Remove the background
        clear.setContentAreaFilled(false);
        // Remove the focus outline
        clear.setFocusPainted(false);
        // Remove inner margins
        clear.setMargin(new Insets(0, 0, 0, 0));
        // Show a hand cursor on hover
        clear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Turn the text red while the mouse is over it
        clear.addMouseListener(new MouseAdapter() {
            // Mouse enters: turn red
            @Override
            public void mouseEntered(MouseEvent e) { clear.setForeground(DANGER); }


            // Mouse leaves: back to normal
            @Override
            public void mouseExited(MouseEvent e) { clear.setForeground(TEXT); }
        });
        // Clicking Clear empties the tracked list
        clear.addActionListener(e -> {
            // Remove all tracked sites
            trackedSites.clear();
            // Update the screen
            refreshLists();
        });
        // Return the button
        return clear;
    }


    // Builds the bar with the globe icon, text box, and Add button
    private JPanel buildAddBar() {
        // Rounded white bar with a shadow
        addBar = new RoundedPanel(FIELD_BG, 14, true);
        // Thin always-visible outline
        addBar.setBaseOutline(BAR_BG);
        // Lay out children left / center / right with a 10px gap
        addBar.setLayout(new BorderLayout(10, 0));
        addBar.setBorder(new EmptyBorder(6, 14, 10, 6)); // extra bottom space is for the shadow


        // Globe icon on the left
        JLabel globe = new JLabel(new GlobeIcon());


        // Text box that also draws a hint when empty
        input = new JTextField() {
            // Custom painting
            @Override
            protected void paintComponent(Graphics g) {
                // Draw the normal text box first
                super.paintComponent(g);
                if (getText().isEmpty()) { // hint text
                    // Copy the graphics object
                    Graphics2D g2 = (Graphics2D) g.create();
                    // Smooth text
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    // Faded hint color
                    g2.setColor(MUTED);
                    // Italic hint font
                    g2.setFont(body(Font.ITALIC, 16));
                    // Font measurements for vertical centering
                    FontMetrics fm = g2.getFontMetrics();
                    // Y position that centers the hint
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    // Draw the hint text
                    g2.drawString("e.g. youtube.com", getInsets().left, y);
                    // Release the graphics copy
                    g2.dispose();
                }
            }
        };
        // Transparent so the bar shows through
        input.setOpaque(false);
        // Small padding inside the box
        input.setBorder(new EmptyBorder(0, 2, 0, 4));
        // Text font
        input.setFont(body(Font.PLAIN, 17));
        // Text color
        input.setForeground(ROW_TEXT);
        // Cursor color
        input.setCaretColor(TITLE);
        input.addActionListener(e -> addSite(input.getText())); // Enter key


        // outline glows while typing
        input.addFocusListener(new FocusAdapter() {
            // Clicked in: show the accent outline
            @Override
            public void focusGained(FocusEvent e) { addBar.setOutline(ACCENT); }


            // Clicked away: remove the outline
            @Override
            public void focusLost(FocusEvent e) { addBar.setOutline(null); }
        });


        // The "Add" button
        PillButton add = new PillButton("Add");
        // What happens when Add is clicked
        add.addActionListener(e -> {
            // If the box is empty (ignoring spaces)...
            if (input.getText().trim().isEmpty()) {
                input.requestFocusInWindow(); // nothing typed yet: just focus the box
            } else {
                // Otherwise add the typed site
                addSite(input.getText());
            }
        });


        // Globe on the left
        addBar.add(globe, BorderLayout.WEST);
        // Text box in the middle
        addBar.add(input, BorderLayout.CENTER);
        // Add button on the right
        addBar.add(add, BorderLayout.EAST);
        // Listen for clicks anywhere on the bar
        addBar.addMouseListener(new MouseAdapter() {
            // On click...
            @Override
            public void mousePressed(MouseEvent e) {
                input.requestFocusInWindow(); // clicking the bar focuses the text box
            }
        });
        // Return the finished bar
        return addBar;
    }


    // a rounded tan card with a scrollable list and a slim scrollbar
    private JPanel buildListBox(JPanel list) {
        // Stack rows vertically
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        // Transparent background
        list.setOpaque(false);


        // Wrapper so the rows stay at the top
        JPanel wrapper = new JPanel(new BorderLayout());
        // Transparent background
        wrapper.setOpaque(false);
        wrapper.add(list, BorderLayout.NORTH); // keeps rows at the top


        // Scroll area around the list
        JScrollPane scroll = new JScrollPane(wrapper);
        // Transparent scroll pane
        scroll.setOpaque(false);
        // Transparent viewport
        scroll.getViewport().setOpaque(false);
        // No border
        scroll.setBorder(null);
        // Show the vertical scrollbar only when needed
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        // Never show a horizontal scrollbar
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);


        // The vertical scrollbar
        JScrollBar vbar = scroll.getVerticalScrollBar();
        // Use our slim custom look
        vbar.setUI(new SlimScrollBarUI());
        // Make it 10px wide
        vbar.setPreferredSize(new Dimension(10, 0));
        // Transparent background
        vbar.setOpaque(false);
        // Scroll speed per mouse wheel step
        vbar.setUnitIncrement(12);


        // Rounded tan card with a shadow
        RoundedPanel box = new RoundedPanel(BOX_BG, 16, true);
        // Thin outline
        box.setBaseOutline(BAR_BG);
        // Scroll area fills the card
        box.setLayout(new BorderLayout());
        box.setBorder(new EmptyBorder(8, 8, 12, 8)); // extra bottom space is for the shadow
        // Put the scroll area inside the card
        box.add(scroll, BorderLayout.CENTER);
        // Return the card
        return box;
    }


    // Creates a clear icon-only button
    private JButton iconButton(Icon icon) {
        // Button showing the icon
        JButton b = new JButton(icon);
        // Remove the border
        b.setBorderPainted(false);
        // Remove the background
        b.setContentAreaFilled(false);
        // Remove the focus outline
        b.setFocusPainted(false);
        b.setRolloverEnabled(true); // lets the icons react to the mouse
        // Remove inner margins
        b.setMargin(new Insets(0, 0, 0, 0));
        // Show a hand cursor on hover
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Return the button
        return b;
    }


    // restricted = true -> trash icon (removes it from the block list)
    // restricted = false -> x icon (just dismisses it from the tracked log)
    private JPanel buildRow(String site, boolean restricted) {
        // Row that highlights on hover
        HoverRow row = new HoverRow();
        // Badge, name, and button laid out with 10px gaps
        row.setLayout(new BorderLayout(10, 0));
        // Small side padding
        row.setBorder(new EmptyBorder(0, 8, 0, 6));
        // Row height is 36px
        row.setPreferredSize(new Dimension(100, 36));
        // Full width, fixed height
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));


        // Round badge with the site's first letter
        Badge badge = new Badge(site.substring(0, 1).toUpperCase(), restricted ? TITLE : CORAL);


        // Label with the site name
        JLabel label = new JLabel(site);
        // Label font
        label.setFont(body(Font.PLAIN, 16));
        // Label color
        label.setForeground(ROW_TEXT);


        // Trash icon for restricted rows, X icon for tracked rows
        JButton action = iconButton(restricted ? new TrashIcon() : new CrossIcon());
        // Tooltip text on hover
        action.setToolTipText(restricted ? "Remove from restricted sites" : "Dismiss");
        // What happens when the icon is clicked
        action.addActionListener(e -> {
            if (restricted) {
                // Remove from the restricted list
                restrictedSites.remove(site);
                    // Save the updated list to disk
                    saveSites();
            } else {
                // Remove from the tracked list
                trackedSites.remove(site);
            }
            // Update the screen
            refreshLists();
        });


        // Badge on the left
        row.add(badge, BorderLayout.WEST);
        // Name in the middle
        row.add(label, BorderLayout.CENTER);
        // Icon button on the right
        row.add(action, BorderLayout.EAST);
        // Keep the hover highlight when over the children
        row.attachHover(badge, label, action);
        // Return the finished row
        return row;
    }


    // Creates the centered "empty list" message
    private JLabel emptyHint(String line1, String line2) {
        // HTML label: main line plus a smaller second line
        JLabel hint = new JLabel("<html><div style='text-align:center'>" + line1
                + "<br><span style='font-size:11px'>" + line2 + "</span></div></html>",
                SwingConstants.CENTER);
        // Italic font
        hint.setFont(body(Font.ITALIC, 16));
        // Faded color
        hint.setForeground(MUTED);
        // Fixed height
        hint.setPreferredSize(new Dimension(100, 110));
        // Full width, fixed height
        hint.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        // Return the label
        return hint;
    }


    // ---------- the logic ----------


    // makes "https://www.YouTube.com/watch?v=1" become "youtube.com"
    private String clean(String site) {
        // Trim spaces and make lowercase
        site = site.trim().toLowerCase();
        // Remove the protocol and "www."
        site = site.replace("https://", "").replace("http://", "").replace("www.", "");
        // Find the first slash (start of the path)
        int slash = site.indexOf('/');
        // If there is a path...
        if (slash != -1) {
            // ...cut it off, keeping only the domain
            site = site.substring(0, slash);
        }
        // Return the cleaned domain
        return site;
    }


    // Adds a site to the restricted list
    private void addSite(String site) {
        // Clean the typed text into a domain
        site = clean(site);
        // Reject empty or already-added sites
        if (site.isEmpty() || restrictedSites.contains(site)) {
            flashError(); // empty or duplicate: red flash
            return;
        }
        // Add the site
        restrictedSites.add(site);
            // Save the list to disk
            saveSites();
        // Clear the text box
        input.setText("");
        // Update the screen
        refreshLists();
    }


    // briefly flashes the add bar red
    private void flashError() {
        // Turn the outline red
        addBar.setOutline(DANGER);
        // After 400 ms, restore the outline (accent if focused, none otherwise)
        Timer t = new Timer(400, e -> addBar.setOutline(input.hasFocus() ? ACCENT : null));
        // Run only once
        t.setRepeats(false);
        // Start the timer
        t.start();
    }


    // Rebuilds both lists and their headings from the current data
    private void refreshLists() {
        // Clear the restricted rows
        listPanel.removeAll();
        // If there are none...
        if (restrictedSites.isEmpty()) {
            // ...show the empty hint
            listPanel.add(emptyHint("No sites yet", "Type a site above and press Enter"));
        }
        // Add one row per restricted site
        for (String site : restrictedSites) {
            listPanel.add(buildRow(site, true));
        }


        // Clear the tracked rows
        trackedPanel.removeAll();
        // If there are none...
        if (trackedSites.isEmpty()) {
            // ...show the empty hint
            trackedPanel.add(emptyHint("Nothing caught yet", "Visits to restricted sites will show up here"));
        }
        // Add one row per tracked site
        for (String site : trackedSites) {
            trackedPanel.add(buildRow(site, false));
        }


        // Show the count in the restricted heading when the list isn't empty
        restrictedLabel.setText(restrictedSites.isEmpty() ? "RESTRICTED SITES"
                : "RESTRICTED SITES  \u00B7  " + restrictedSites.size());
        // Show the count in the tracked heading when the list isn't empty
        trackedLabel.setText(trackedSites.isEmpty() ? "TRACKED SITES"
                : "TRACKED SITES  \u00B7  " + trackedSites.size());


        // Recalculate the restricted list layout
        listPanel.revalidate();
        // Redraw the restricted list
        listPanel.repaint();
        // Recalculate the tracked list layout
        trackedPanel.revalidate();
        // Redraw the tracked list
        trackedPanel.repaint();
    }

    // saved in the user's home folder, so it never gets committed to git
// Path of the save file: ~/.lockin/restricted-sites.txt
private static final java.nio.file.Path SAVE_FILE =
        java.nio.file.Paths.get(System.getProperty("user.home"), ".lockin", "restricted-sites.txt");

// Writes the restricted sites to the save file
private void saveSites() {
    try {
        // Create the folder if it doesn't exist
        java.nio.file.Files.createDirectories(SAVE_FILE.getParent());
        // Write one site per line
        java.nio.file.Files.write(SAVE_FILE, restrictedSites,
                java.nio.charset.StandardCharsets.UTF_8);
    } catch (java.io.IOException e) {
        // Print the error if saving fails
        e.printStackTrace();
    }
}

// Reads the restricted sites back from the save file
private void loadSites() {
    try {
        // Only load if the file exists
        if (java.nio.file.Files.exists(SAVE_FILE)) {
            // Go through each saved line
            for (String line : java.nio.file.Files.readAllLines(SAVE_FILE,
                    java.nio.charset.StandardCharsets.UTF_8)) {
                // Clean the line into a domain
                String site = clean(line);
                // Skip blanks and duplicates
                if (!site.isEmpty() && !restrictedSites.contains(site)) {
                    // Add the site
                    restrictedSites.add(site);
                        // Save the list to disk
                        saveSites();
                }
            }
            // Update the screen
            refreshLists();
        }
    } catch (java.io.IOException e) {
        // Print the error if loading fails
        e.printStackTrace();
    }
}

    // Jhenica's server and the warning call this.
    // youtube.com is restricted -> m.youtube.com is too
    public boolean isRestricted(String site) {
        // Clean the input into a domain
        String host = clean(site);
        // Check against every restricted site
        for (String s : restrictedSites) {
            // Match the exact domain or any subdomain of it
            if (host.equals(s) || host.endsWith("." + s)) {
                return true;
            }
        }
        // No match found
        return false;
    }


    // Call this when a restricted site is caught during Work, so it shows under "Tracked sites".
    // Safe to call from another thread (like the server): the screen update runs on the UI thread.
    public void logTrackedSite(String site) {
        // Clean the site into a domain
        final String cleaned = clean(site);
        // Run the update on the UI thread
        SwingUtilities.invokeLater(() -> {
            // Add only if non-empty and not already tracked
            if (!cleaned.isEmpty() && !trackedSites.contains(cleaned)) {
                // Add to the tracked list
                trackedSites.add(cleaned);
                // Update the screen
                refreshLists();
            }
        });
    }


    // =========================================================
    // WARNING
    // Pop-up version of the LockIn warning, uses the message picked in Settings.
    // =========================================================
    public void showWarning(String site) {

        // Get the message chosen in Settings
        final String message = getWarningMessage();

        // Show the pop-up on the UI thread
        SwingUtilities.invokeLater(() -> {

            // Warning dialog: message, blank line, then the site
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
        // True while the mouse is over the row
        private boolean hover;


        // Sets up the row
        HoverRow() {
            // Transparent so only the highlight shows
            setOpaque(false);
            // Listen for the mouse entering/leaving the row
            addMouseListener(hoverListener());
        }


        // Creates a listener that turns the highlight on and off
        private MouseAdapter hoverListener() {
            return new MouseAdapter() {
                // Mouse entered: highlight on
                @Override
                public void mouseEntered(MouseEvent e) { setHover(true); }


                // Mouse left a component
                @Override
                public void mouseExited(MouseEvent e) {
                    // moving onto a child still counts as being inside the row
                    // Convert the mouse position to the row's coordinates
                    Point p = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), HoverRow.this);
                    // Highlight stays on only if still inside the row
                    setHover(contains(p));
                }
            };
        }


        // Makes the given children trigger the same hover highlight
        void attachHover(Component... children) {
            // One shared listener
            MouseAdapter m = hoverListener();
            // Attach it to each child
            for (Component c : children) {
                c.addMouseListener(m);
            }
        }


        // Turns the highlight on or off and redraws if it changed
        void setHover(boolean h) {
            if (hover != h) {
                hover = h;
                repaint();
            }
        }


        // Draws the highlight, then the children
        @Override
        protected void paintComponent(Graphics g) {
            // Only draw when hovered
            if (hover) {
                // Copy the graphics object
                Graphics2D g2 = (Graphics2D) g.create();
                // Smooth edges
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Highlight color
                g2.setColor(ROW_HOVER);
                // Rounded highlight covering the row
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                // Release the graphics copy
                g2.dispose();
            }
            // Paint the rest normally
            super.paintComponent(g);
        }
    }


    // rounded card with an optional soft shadow (the bottom 4px are reserved for it)
    private static class RoundedPanel extends JPanel {
        // Fill color
        private final Color color;
        // Corner roundness
        private final int radius;
        // Whether to draw the shadow
        private final boolean shadow;
        private Color outline;     // changes while focusing / flashing
        private Color baseOutline; // always-on thin outline


        // Stores the card's color, roundness, and shadow setting
        RoundedPanel(Color color, int radius, boolean shadow) {
            this.color = color;
            this.radius = radius;
            this.shadow = shadow;
            // Transparent so only our rounded shape shows
            setOpaque(false);
        }


        // Sets the temporary outline color (null removes it)
        void setOutline(Color c) {
            this.outline = c;
            repaint();
        }


        // Sets the permanent outline color
        void setBaseOutline(Color c) {
            this.baseOutline = c;
            repaint();
        }


        // Draws the shadow, card, and outline
        @Override
        protected void paintComponent(Graphics g) {
            // Copy the graphics object
            Graphics2D g2 = (Graphics2D) g.create();
            // Smooth edges
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Card width
            int w = getWidth();
            // Card height (minus the space reserved for the shadow)
            int h = getHeight() - (shadow ? 4 : 0);


            // Draw the shadow as 4 faint layers
            if (shadow) {
                for (int i = 0; i < 4; i++) {
                    // Very transparent maroon
                    g2.setColor(new Color(0x59, 0x13, 0x2C, 12));
                    // Shadow layer shifted down slightly
                    g2.fillRoundRect(2, i + 1, w - 4, h, radius * 2, radius * 2);
                }
            }


            // Card fill color
            g2.setColor(color);
            // Draw the card
            g2.fillRoundRect(0, 0, w, h, radius * 2, radius * 2);


            // Use the temporary outline if set, otherwise the base outline
            Color o = outline != null ? outline : baseOutline;
            // Draw an outline only if there is one
            if (o != null) {
                g2.setColor(o);
                // Thicker line for the temporary outline
                g2.setStroke(new BasicStroke(outline != null ? 2f : 1.2f));
                // Draw the outline
                g2.drawRoundRect(1, 1, w - 3, h - 3, radius * 2, radius * 2);
            }
            // Release the graphics copy
            g2.dispose();
            // Paint the children
            super.paintComponent(g);
        }
    }


    // maroon "Add" pill button
    private static class PillButton extends JButton {
        // Creates the button with its text and style
        PillButton(String text) {
            super(text);
            // Bold font with slight letter spacing
            setFont(spaced(body(Font.BOLD, 14), 0.08f));
            // Cream text
            setForeground(CREAM);
            // We paint the background ourselves
            setContentAreaFilled(false);
            // No border
            setBorderPainted(false);
            // No focus outline
            setFocusPainted(false);
            // Track mouse hover
            setRolloverEnabled(true);
            // Side margins
            setMargin(new Insets(0, 16, 0, 16));
            // Hand cursor on hover
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            // Button size
            setPreferredSize(new Dimension(68, 30));
        }


        // Draws the gradient pill, then the text
        @Override
        protected void paintComponent(Graphics g) {
            // Copy the graphics object
            Graphics2D g2 = (Graphics2D) g.create();
            // Smooth edges
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Normal gradient colors
            Color left = TITLE;
            Color right = ACCENT;
            // Darker when pressed
            if (getModel().isPressed()) {
                left = left.darker();
                right = right.darker();
            // Lighter when hovered
            } else if (getModel().isRollover()) {
                left = ACCENT;
                right = ROSE;
            }
            // Horizontal gradient from left to right
            g2.setPaint(new GradientPaint(0, 0, left, getWidth(), 0, right));
            // Draw the pill (arc = height gives round ends)
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            // Release the graphics copy
            g2.dispose();
            // Draw the button text
            super.paintComponent(g);
        }
    }


    // round badge with the first letter of the site
    private static class Badge extends JComponent {
        // Letter shown in the badge
        private final String letter;
        // Badge color
        private final Color color;


        // Stores the letter and color, and sets the size
        Badge(String letter, Color color) {
            this.letter = letter;
            this.color = color;
            setPreferredSize(new Dimension(26, 26));
        }


        // Draws the circle and the letter
        @Override
        protected void paintComponent(Graphics g) {
            // Copy the graphics object
            Graphics2D g2 = (Graphics2D) g.create();
            // Smooth shapes
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Smooth text
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            // Circle diameter
            int d = 24;
            // Top-left position that centers the circle
            int x = (getWidth() - d) / 2;
            int y = (getHeight() - d) / 2;
            // Gradient from the color to a brighter version
            g2.setPaint(new GradientPaint(x, y, color, x + d, y + d, color.brighter()));
            // Draw the circle
            g2.fillOval(x, y, d, d);
            // Letter color
            g2.setColor(CREAM);
            // Letter font
            g2.setFont(body(Font.BOLD, 14));
            // Font measurements for centering
            FontMetrics fm = g2.getFontMetrics();
            // Position that centers the letter
            int tx = x + (d - fm.stringWidth(letter)) / 2;
            int ty = y + (d + fm.getAscent() - fm.getDescent()) / 2;
            // Draw the letter
            g2.drawString(letter, tx, ty);
            // Release the graphics copy
            g2.dispose();
        }
    }


    // centered text that wraps to the width it actually has (no HTML, so it can never get cut off)
    private static class CenteredText extends JComponent {
        // The text to display
        private final String text;


        // Stores the text and sets its font and color
        CenteredText(String text, Font font, Color color) {
            this.text = text;
            setFont(font);
            setForeground(color);
        }


        // Draws the text, wrapping lines to fit the width
        @Override
        protected void paintComponent(Graphics g) {
            // Copy the graphics object
            Graphics2D g2 = (Graphics2D) g.create();
            // Smooth text
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            // Use the component's font
            g2.setFont(getFont());
            // Use the component's color
            g2.setColor(getForeground());
            // Font measurements for wrapping
            FontMetrics fm = g2.getFontMetrics();


            // Y position of the first line
            int y = fm.getAscent();
            // The line being built
            StringBuilder line = new StringBuilder();
            // Go through the text word by word
            for (String word : text.split(" ")) {
                // The line if this word were added
                String test = line.length() == 0 ? word : line + " " + word;
                // If it would be too wide, finish the current line
                if (line.length() > 0 && fm.stringWidth(test) > getWidth()) {
                    // Draw the finished line
                    drawCentered(g2, fm, line.toString(), y);
                    // Move down to the next line
                    y += fm.getHeight();
                    // Start a new line with this word
                    line = new StringBuilder(word);
                } else {
                    // Otherwise keep adding to the current line
                    line = new StringBuilder(test);
                }
            }
            // Draw the last line
            if (line.length() > 0) {
                drawCentered(g2, fm, line.toString(), y);
            }
            // Release the graphics copy
            g2.dispose();
        }


        // Draws one line of text horizontally centered
        private void drawCentered(Graphics2D g2, FontMetrics fm, String s, int y) {
            g2.drawString(s, (getWidth() - fm.stringWidth(s)) / 2, y);
        }
    }


    // thin line with a diamond in the middle, fading out at both ends
    private static class OrnamentDivider extends JComponent {
        // Draws the divider
        @Override
        protected void paintComponent(Graphics g) {
            // Copy the graphics object
            Graphics2D g2 = (Graphics2D) g.create();
            // Smooth edges
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Component width
            int w = getWidth();
            // Center point
            int cx = w / 2;
            int cy = getHeight() / 2;
            // Solid color in the middle
            Color solid = ROSE;
            // Fully transparent color at the ends
            Color clear = new Color(0xD1, 0x7D, 0x93, 0);


            // Left line fading in toward the center
            g2.setPaint(new GradientPaint(cx - 150, 0, clear, cx - 14, 0, solid));
            g2.drawLine(cx - 150, cy, cx - 14, cy);
            // Right line fading out away from the center
            g2.setPaint(new GradientPaint(cx + 14, 0, solid, cx + 150, 0, clear));
            g2.drawLine(cx + 14, cy, cx + 150, cy);


            // Diamond color
            g2.setColor(ACCENT);
            // Draw the diamond in the center
            g2.fillPolygon(new int[]{cx, cx + 5, cx, cx - 5}, new int[]{cy - 5, cy, cy + 5, cy}, 4);
            // Release the graphics copy
            g2.dispose();
        }
    }


    // slim rounded scrollbar: darker track, lighter thumb, no arrow buttons
    private static class SlimScrollBarUI extends BasicScrollBarUI {
        // Color of the draggable thumb
        private static final Color THUMB = ROSE;


        // No up/left arrow button
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return emptyButton();
        }


        // No down/right arrow button
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return emptyButton();
        }


        // Creates an invisible zero-size button
        private JButton emptyButton() {
            // Plain button
            JButton b = new JButton();
            // Zero size
            Dimension zero = new Dimension(0, 0);
            b.setPreferredSize(zero);
            b.setMinimumSize(zero);
            b.setMaximumSize(zero);
            // Return the button
            return b;
        }


        // Draws the track and thumb
        @Override
        public void paint(Graphics g, JComponent c) {
            // Copy the graphics object
            Graphics2D g2 = (Graphics2D) g.create();
            // Smooth edges
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


            // Area of the track
            Rectangle track = getTrackBounds();
            // Track color
            g2.setColor(BAR_BG);
            // Draw the rounded track
            g2.fillRoundRect(track.x, track.y, track.width, track.height, track.width, track.width);


            // Area of the thumb
            Rectangle thumb = getThumbBounds();
            // Thumb position (fallback to the track top if it has no size)
            int y = thumb.height > 0 ? thumb.y : track.y;
            // Thumb height (fallback to a third of the track)
            int h = thumb.height > 0 ? thumb.height : track.height / 3;
            // Thumb color
            g2.setColor(THUMB);
            // Draw the rounded thumb
            g2.fillRoundRect(track.x, y, track.width, h, track.width, track.width);
            // Release the graphics copy
            g2.dispose();
        }
    }


    // True if the mouse is currently over the given button
    private static boolean isHovered(Component c) {
        return c instanceof AbstractButton && ((AbstractButton) c).getModel().isRollover();
    }


    // Globe icon shown in the add bar
    private static class GlobeIcon implements Icon {
        // Icon width
        public int getIconWidth() { return 20; }
        // Icon height
        public int getIconHeight() { return 20; }


        // Draws the globe
        public void paintIcon(Component c, Graphics g, int x, int y) {
            // Copy the graphics object
            Graphics2D g2 = (Graphics2D) g.create();
            // Smooth edges
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Brown lines
            g2.setColor(TEXT);
            // Thin line
            g2.setStroke(new BasicStroke(1.3f));
            // Outer circle
            g2.drawOval(x + 1, y + 1, 18, 18);
            g2.drawOval(x + 6, y + 1, 8, 18);   // meridian
            g2.drawLine(x + 1, y + 10, x + 19, y + 10); // equator
            // Release the graphics copy
            g2.dispose();
        }
    }


    // Trash can icon for removing a restricted site
    private static class TrashIcon implements Icon {
        // Icon width
        public int getIconWidth() { return 16; }
        // Icon height
        public int getIconHeight() { return 16; }


        // Draws the trash can
        public void paintIcon(Component c, Graphics g, int x, int y) {
            // Copy the graphics object
            Graphics2D g2 = (Graphics2D) g.create();
            // Smooth edges
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isHovered(c) ? DANGER : TEXT); // red on hover
            // Line thickness
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(x + 1, y + 4, x + 15, y + 4);      // lid
            g2.drawRect(x + 6, y + 1, 4, 3);               // handle
            g2.drawRoundRect(x + 3, y + 5, 10, 10, 2, 2);  // body
            g2.drawLine(x + 6, y + 7, x + 6, y + 13);      // lines
            g2.drawLine(x + 10, y + 7, x + 10, y + 13);
            // Release the graphics copy
            g2.dispose();
        }
    }


    // X icon for dismissing a tracked site
    private static class CrossIcon implements Icon {
        // Icon width
        public int getIconWidth() { return 16; }
        // Icon height
        public int getIconHeight() { return 16; }


        // Draws the X
        public void paintIcon(Component c, Graphics g, int x, int y) {
            // Copy the graphics object
            Graphics2D g2 = (Graphics2D) g.create();
            // Smooth edges
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Red on hover, brown otherwise
            g2.setColor(isHovered(c) ? DANGER : TEXT);
            // Thick rounded lines
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // First diagonal of the X
            g2.drawLine(x + 4, y + 4, x + 12, y + 12);
            // Second diagonal of the X
            g2.drawLine(x + 12, y + 4, x + 4, y + 12);
            // Release the graphics copy
            g2.dispose();
        }
    }
}