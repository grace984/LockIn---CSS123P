// Package this class belongs to
package lockin;

// Swing components (JFrame, JPanel, JButton, etc.)
import javax.swing.*;
// Lets us add empty padding around a component
import javax.swing.border.EmptyBorder;
// AWT classes for colors, fonts, layouts, and drawing
import java.awt.*;
// Lets us change font attributes such as letter spacing
import java.awt.font.TextAttribute;
// Used to draw custom shapes (the tab outline)
import java.awt.geom.Path2D;
// Used to hold font attributes
import java.util.HashMap;
// Map that remembers insertion order (keeps tabs in order)
import java.util.LinkedHashMap;
// Generic key-value map interface
import java.util.Map;

// Main app window: timer area on the right, sliding drawer with tabs on the left
public class MainFrame extends JFrame {

    // Background color of inactive tabs
    private static final Color TAB_BG =
            new Color(0xFFF5E4);

    // Background color of the selected tab
    private static final Color TAB_SELECTED =
            new Color(0xF7E9DF);

    // Background color of the drawer's content panels
    private static final Color PANEL_BG =
            new Color(0xF7E9DF);

    // Main dark text/outline color
    private static final Color MAROON =
            new Color(0x59132C);

    // Light cream color used for icons and filled buttons
    private static final Color CREAM =
            new Color(0xFFF5E4);

    // Layout that shows one content panel at a time
    private CardLayout cardLayout =
            new CardLayout();

    // Holds the Time / Tracker / Themes / Settings panels
    private JPanel contentPanel =
            new JPanel(cardLayout);

    // The sliding left drawer that contains tabs and content
    private JPanel drawer =
            new JPanel(new BorderLayout());

    // Tab buttons by key ("time", "tracker", ...), in order
    private Map<String, JButton> tabButtons =
            new LinkedHashMap<>();

    // Gradient background panel on the right side
    private GradientPanel timerArea;

    // Width of the tab column
    private static final int NAVIGATION_WIDTH = 238;

    // Width of the content panel next to the tabs
    private static final int CONTENT_WIDTH = 471;

    // Width of the selected tab (it sticks out over the content)
    private static final int ACTIVE_TAB_WIDTH = 276;

    // Total width of the drawer when fully open
    private static final int DRAWER_WIDTH =
            NAVIGATION_WIDTH + CONTENT_WIDTH;

    // Current drawer width (changes during the slide animation)
    private int displayedDrawerWidth = 0;

    // True when the drawer is open (or opening)
    private boolean drawerOpen = false;

    // The running drawer animation, so it can be stopped
    private AnimationUtils.Animation drawerAnimation;


    /*
     * =========================================================
     * CONSTRUCTOR
     * =========================================================
     *
     * IMPORTANT:
     * The SAME trackerPanel and timerPanel created in Main.java
     * are passed here.
     */
    public MainFrame(
            ramiraTrackerPanel trackerPanel,
            lauriceTimerPanel timerPanel
    ) {

        // Set the window title
        setTitle("LockIn");

        // Exit the app when the window is closed
        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        // Use BorderLayout for the main window
        setLayout(
                new BorderLayout()
        );


        // ---------- RIGHT SIDE: gradient area with hamburger button ----------

        // Get the currently selected theme colors
        Color[] themeColors =
                julianneThemeManager.getSelectedColors();

        // Create the gradient from the first to the last theme color
        timerArea =
                new GradientPanel(
                        themeColors[0],
                        themeColors[themeColors.length - 1]
                );

        // Set the preferred size of the timer area
        timerArea.setPreferredSize(
                new Dimension(731, 810)
        );

        // Use BorderLayout inside the timer area
        timerArea.setLayout(
                new BorderLayout()
        );

        // Update the gradient whenever the theme changes
        julianneThemeManager.addListener(
                colors ->
                        timerArea.setColors(
                                colors[0],
                                colors[colors.length - 1]
                        )
        );


        // Create the hamburger (menu) button with its icon
        JButton hamburger =
                new JButton(
                        new HamburgerIcon()
                );

        // Remove the button border
        hamburger.setBorderPainted(false);

        // Remove the button background
        hamburger.setContentAreaFilled(false);

        // Remove the focus outline
        hamburger.setFocusPainted(false);

        // Show a hand cursor on hover
        hamburger.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        // Open or close the drawer when clicked
        hamburger.addActionListener(
                e -> toggleDrawer()
        );


        // Top bar that holds the hamburger button (left-aligned)
        JPanel topBar =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                10,
                                10
                        )
                );

        // Make the top bar transparent
        topBar.setOpaque(false);

        // Put the hamburger button in the top bar
        topBar.add(hamburger);


        // Place the top bar at the top of the timer area
        timerArea.add(
                topBar,
                BorderLayout.NORTH
        );


        /*
         * IMPORTANT:
         * DO NOT create a new lauriceTimerPanel here.
         *
         * We use the timerPanel received from Main.java.
         *
         * This is the SAME timer that the API server uses.
         */

        // Panel that centers the timer
        JPanel timerCenter =
                new JPanel(
                        new GridBagLayout()
                );

        // Make it transparent so the gradient shows through
        timerCenter.setOpaque(false);

        // Bottom padding equal to the top bar height keeps the timer visually centered
        timerCenter.setBorder(
                BorderFactory.createEmptyBorder(
                        0,
                        0,
                        topBar.getPreferredSize().height,
                        0
                )
        );

        // Add the shared timer panel to the centered area
        timerCenter.add(timerPanel);


        // Place the centered timer in the middle of the timer area
        timerArea.add(
                timerCenter,
                BorderLayout.CENTER
        );


        // Add the timer area to fill the window's center
        add(
                timerArea,
                BorderLayout.CENTER
        );


        // ---------- LEFT SIDE: drawer = tabs + content ----------

        // Grid that stacks the 4 tab buttons vertically
        JPanel tabsGrid =
                new JPanel(
                        new GridLayout(4, 1)
                );

        // Make the tab grid transparent
        tabsGrid.setOpaque(false);

        // Set the size of the tab stack
        tabsGrid.setPreferredSize(
                new Dimension(
                        ACTIVE_TAB_WIDTH,
                        330
                )
        );


        // Add the TIME tab
        addTab(
                tabsGrid,
                "TIME",
                "time"
        );

        // Add the TRACKER tab
        addTab(
                tabsGrid,
                "TRACKER",
                "tracker"
        );

        // Add the THEMES tab
        addTab(
                tabsGrid,
                "THEMES",
                "themes"
        );

        // Add the SETTINGS tab
        addTab(
                tabsGrid,
                "SETTINGS",
                "settings"
        );


        // Background strip behind the tabs
        JPanel tabsHolder =
                new JPanel();

        // Fill it with the tab background color
        tabsHolder.setBackground(
                TAB_BG
        );

        // Make sure the color is actually painted
        tabsHolder.setOpaque(true);


        /*
         * Use the SAME timerPanel for the Time settings.
         */
        // Add the Time settings page
        contentPanel.add(
                createTimePanel(timerPanel),
                "time"
        );


        /*
         * Use the SAME trackerPanel that was given
         * to the API server.
         */
        // Add the shared tracker panel as the Tracker page
        contentPanel.add(
                trackerPanel,
                "tracker"
        );


        // Add the Themes page
        contentPanel.add(
                new julianneThemePanel(),
                "themes"
        );


        // Add the Settings page
        contentPanel.add(
                new lauriceSettingsPanel(),
                "settings"
        );


        // Set the size of the content area
        contentPanel.setPreferredSize(
                new Dimension(
                        CONTENT_WIDTH,
                        810
                )
        );


        // Container with manual (null) layout so the tabs can overlap the content
        JPanel columns =
                new JPanel(null) {

                    /*
                     * FIX: the tabs (276px wide) overlap the content panel
                     * (which starts at 238px). Swing normally assumes that
                     * children of a panel do not overlap, so when a panel
                     * like Themes repainted (hover / checkmark animation),
                     * it painted over the tab's rounded end.
                     *
                     * Returning false makes Swing repaint overlapping
                     * children in the correct order.
                     */
                    @Override
                    public boolean isOptimizedDrawingEnabled() {
                        return false;
                    }

                    // Manually position the three child components
                    @Override
                    public void doLayout() {

                        // Tab background strip on the far left
                        tabsHolder.setBounds(
                                0,
                                0,
                                NAVIGATION_WIDTH,
                                getHeight()
                        );

                        // Content panel starts right after the tab column
                        contentPanel.setBounds(
                                NAVIGATION_WIDTH,
                                0,
                                CONTENT_WIDTH,
                                getHeight()
                        );

                        // Tab buttons at the top-left, wide enough to overlap the content
                        tabsGrid.setBounds(
                                0,
                                0,
                                ACTIVE_TAB_WIDTH,
                                330
                        );
                    }
                };


        // Make the container transparent
        columns.setOpaque(false);

        // Set the full size of the drawer contents
        columns.setPreferredSize(
                new Dimension(
                        709,
                        810
                )
        );


        // Add the tab background strip
        columns.add(tabsHolder);

        // Add the content panel
        columns.add(contentPanel);

        // Add the tab buttons
        columns.add(tabsGrid);


        // Draw tabs on top (index 0 = front)
        columns.setComponentZOrder(
                tabsGrid,
                0
        );

        // Draw content in the middle
        columns.setComponentZOrder(
                contentPanel,
                1
        );

        // Draw the background strip at the back
        columns.setComponentZOrder(
                tabsHolder,
                2
        );


        // Start the drawer at its current width (0 = closed)
        drawer.setPreferredSize(
                new Dimension(
                        displayedDrawerWidth,
                        810
                )
        );

        // Allow the drawer to shrink all the way to zero width
        drawer.setMinimumSize(
                new Dimension(0, 0)
        );

        // Put the tabs and content inside the drawer
        drawer.add(
                columns,
                BorderLayout.CENTER
        );

        // Keep the drawer visible (its width controls open/closed)
        drawer.setVisible(true);


        // Place the drawer on the left side of the window
        add(
                drawer,
                BorderLayout.WEST
        );


        // Set the window's starting content size
        getContentPane().setPreferredSize(
                new Dimension(
                        1440,
                        810
                )
        );

        // Size the window to fit its contents
        pack();

        // Windows applies the minimum in real screen pixels, but the layout is scaled
                // by the display scaling (125%, 150%...), so multiply by that scale.
                // Get the screen's display scale factor
                double scale = getGraphicsConfiguration().getDefaultTransform().getScaleX();
                // Set the minimum window size, adjusted for display scaling
                setMinimumSize(new Dimension(
                        (int) Math.round(1150 * scale),   // drawer (709) + timer card (420) + borders
                        (int) Math.round(600 * scale)));

        // Center the window on the screen
        setLocationRelativeTo(null);


        // Start on the Time tab
        selectTab("time");
    }


    // ---------- helpers ----------

    // Opens the drawer if closed, closes it if open (animated)
    private void toggleDrawer() {

        // Flip the open/closed state
        drawerOpen = !drawerOpen;

        // Animation starts from the current width
        int start =
                displayedDrawerWidth;

        // Animation ends fully open or fully closed
        int target =
                drawerOpen
                        ? DRAWER_WIDTH
                        : 0;


        // If an animation is already running, stop it first
        if (drawerAnimation != null) {

            drawerAnimation.stop();
        }


        // Run a 300 ms ease-in-out slide animation
        drawerAnimation =
                AnimationUtils.animate(
                        300,
                        AnimationUtils::easeInOut,

                        // Runs on every frame; progress goes from 0 to 1
                        progress -> {

                            // Work out the drawer width for this frame
                            displayedDrawerWidth =
                                    (int) Math.round(
                                            AnimationUtils.interpolate(
                                                    start,
                                                    target,
                                                    progress
                                            )
                                    );

                            // Apply the new width to the drawer
                            drawer.setPreferredSize(
                                    new Dimension(
                                            displayedDrawerWidth,
                                            810
                                    )
                            );

                            // Recalculate the window layout
                            getContentPane()
                                    .revalidate();

                            // Redraw the window
                            getContentPane()
                                    .repaint();

                        },

                        // No callback when the animation finishes
                        null
                );
    }


    // Creates one tab button and adds it to the tab grid
    private void addTab(
            JPanel grid,
            String label,
            String key
    ) {

        // Base tab font: Times New Roman, size 30
        Font base =
                new Font(
                        "Times New Roman",
                        Font.PLAIN,
                        30
                );


        // Copy the font's attributes so we can modify them
        Map<TextAttribute, Object> attrs =
                new HashMap<TextAttribute, Object>(
                        base.getAttributes()
                );


        // Add extra letter spacing
        attrs.put(
                TextAttribute.TRACKING,
                0.25
        );


        // Create the custom tab button
        TabButton b =
                new TabButton(label);


        // Apply the font with letter spacing
        b.setFont(
                base.deriveFont(attrs)
        );


        // Remove the focus outline
        b.setFocusPainted(false);

        // Remove the default border
        b.setBorderPainted(false);

        // Remove the default background (we paint our own)
        b.setContentAreaFilled(false);

        // Make the button transparent
        b.setOpaque(false);


        // Set the tab's size
        b.setPreferredSize(
                new Dimension(
                        ACTIVE_TAB_WIDTH,
                        82
                )
        );


        // Show a hand cursor on hover
        b.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );


        // Switch to this tab's page when clicked
        b.addActionListener(
                e -> selectTab(key)
        );


        // Remember the button by its key
        tabButtons.put(
                key,
                b
        );


        // Add the button to the grid
        grid.add(b);
    }


    // Shows the page for the given tab and highlights that tab
    private void selectTab(
            String key
    ) {

        // Switch the visible content page
        cardLayout.show(
                contentPanel,
                key
        );


        // Loop through every tab button
        for (
                Map.Entry<String, JButton> entry :
                tabButtons.entrySet()
        ) {

            // Mark only the matching tab as selected
            ((TabButton) entry.getValue())
                    .setTabSelected(
                            entry.getKey().equals(key)
                    );
        }
    }


    // Builds the Time page with Work and Break duration editors
    private JPanel createTimePanel(
            lauriceTimerPanel timerPanel
    ) {

        // Panel using GridBagLayout to stack the two editors
        JPanel panel =
                new JPanel(
                        new GridBagLayout()
                );

        // Set the panel background color
        panel.setBackground(
                PANEL_BG
        );


        // Rules for positioning components in the grid
        GridBagConstraints constraints =
                new GridBagConstraints();


        // Use the first column
        constraints.gridx = 0;

        // Let the column take the full width
        constraints.weightx = 1.0;

        // Center horizontally
        constraints.anchor =
                GridBagConstraints.CENTER;

        // Add space below the Work editor
        constraints.insets =
                new Insets(
                        0,
                        0,
                        28,
                        0
                );

        // Work editor goes in the first row
        constraints.gridy = 0;

        // Let the row take half the height
        constraints.weighty = 1.0;

        // Push the Work editor toward the bottom of its row
        constraints.anchor =
                GridBagConstraints.SOUTH;


        // Add the Work duration editor
        panel.add(
                createDurationEditor(
                        "Work",
                        timerPanel.getWorkDuration(),
                        timerPanel::setWorkDuration
                ),
                constraints
        );


        // Break editor goes in the second row
        constraints.gridy = 1;

        // Let the row take half the height
        constraints.weighty = 1.0;

        // Add space above the Break editor
        constraints.insets =
                new Insets(
                        28,
                        0,
                        0,
                        0
                );

        // Push the Break editor toward the top of its row
        constraints.anchor =
                GridBagConstraints.NORTH;


        // Add the Break duration editor
        panel.add(
                createDurationEditor(
                        "Break",
                        timerPanel.getBreakDuration(),
                        timerPanel::setBreakDuration
                ),
                constraints
        );


        // Return the finished Time page
        return panel;
    }


    // Builds one editor: title, minus/plus buttons, time value, and Done button
    private JPanel createDurationEditor(
            String title,
            int seconds,
            java.util.function.IntConsumer update
    ) {

        // Container for the whole editor
        JPanel section =
                new JPanel();


        // Make it transparent
        section.setOpaque(false);

        // Stack children vertically
        section.setLayout(
                new BoxLayout(
                        section,
                        BoxLayout.Y_AXIS
                )
        );


        // Fix the editor to 360x220 (preferred, minimum, and maximum)
        section.setPreferredSize(
                new Dimension(
                        360,
                        220
                )
        );

        section.setMinimumSize(
                new Dimension(
                        360,
                        220
                )
        );

        section.setMaximumSize(
                new Dimension(
                        360,
                        220
                )
        );


        // Heading label ("Work" or "Break")
        JLabel heading =
                new JLabel(title);


        // Heading font
        heading.setFont(
                new Font(
                        "Times New Roman",
                        Font.PLAIN,
                        32
                )
        );


        // Heading color
        heading.setForeground(
                MAROON
        );


        // Center the heading horizontally
        heading.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );


        // Label showing the duration as MM:SS
        JLabel value =
                new JLabel(
                        formatDuration(seconds)
                );


        // Duration font (large)
        value.setFont(
                new Font(
                        "Times New Roman",
                        Font.PLAIN,
                        52
                )
        );


        // Duration color
        value.setForeground(
                MAROON
        );


        // One-element array so the click handlers can modify the value
        int[] duration =
                {seconds};


        // Button to decrease the time
        JButton minus =
                textButton(
                        "−",
                        46
                );


        // Button to increase the time
        JButton plus =
                textButton(
                        "+",
                        46
                );


        // Minus: subtract 1 minute
        minus.addActionListener(
                e -> {

                    // Subtract 60 seconds but never go below 60 (1 minute)
                    duration[0] =
                            Math.max(
                                    60,
                                    duration[0] - 60
                            );

                    // Refresh the displayed time
                    value.setText(
                            formatDuration(
                                    duration[0]
                            )
                    );
                }
        );


        // Plus: add 1 minute
        plus.addActionListener(
                e -> {

                    // Add 60 seconds
                    duration[0] += 60;

                    // Refresh the displayed time
                    value.setText(
                            formatDuration(
                                    duration[0]
                            )
                    );
                }
        );


        // Row holding minus, time, and plus (centered)
        JPanel row =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER,
                                8,
                                4
                        )
                );


        // Make the row transparent
        row.setOpaque(false);

        // Center the row horizontally
        row.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        // Limit the row's size
        row.setMaximumSize(
                new Dimension(
                        360,
                        74
                )
        );


        // Add the minus button
        row.add(minus);

        // Add the time label
        row.add(value);

        // Add the plus button
        row.add(plus);


        // Rounded outline "Done" button
        JButton done =
                new RoundedActionButton(
                        "Done",
                        142,
                        50,
                        false
                );


        // Done button font
        done.setFont(
                new Font(
                        "Times New Roman",
                        Font.PLAIN,
                        28
                )
        );


        // On click, save the chosen duration to the timer
        done.addActionListener(
                e -> update.accept(
                        duration[0]
                )
        );


        // Add the heading
        section.add(heading);

        // Gap between heading and row
        section.add(
                Box.createVerticalStrut(18)
        );

        // Add the minus/time/plus row
        section.add(row);

        // Gap between row and Done button
        section.add(
                Box.createVerticalStrut(22)
        );


        // Center the Done button horizontally
        done.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );


        // Add the Done button
        section.add(done);


        // Return the finished editor
        return section;
    }


    // Builds an older Settings page (manual layout with absolute positions)
    private JPanel createSettingsPanel() {

        // Panel with no layout manager (positions set by hand)
        JPanel panel =
                new JPanel(null);


        // Set the panel background color
        panel.setBackground(
                PANEL_BG
        );


        // Page title
        JLabel title =
                new JLabel(
                        "SETTINGS"
                );


        // Title font
        title.setFont(
                new Font(
                        "Times New Roman",
                        Font.PLAIN,
                        30
                )
        );


        // Title color
        title.setForeground(
                MAROON
        );


        // Center the title text
        title.setHorizontalAlignment(
                SwingConstants.CENTER
        );


        // Position and size of the title
        title.setBounds(
                0,
                80,
                471,
                40
        );


        // Subtitle under the title
        JLabel subtitle =
                new JLabel(
                        "Customize your LockIn settings"
                );


        // Subtitle font (italic)
        subtitle.setFont(
                new Font(
                        "Times New Roman",
                        Font.ITALIC,
                        18
                )
        );


        // Subtitle color
        subtitle.setForeground(
                MAROON
        );


        // Center the subtitle text
        subtitle.setHorizontalAlignment(
                SwingConstants.CENTER
        );


        // Position and size of the subtitle
        subtitle.setBounds(
                0,
                124,
                471,
                30
        );


        // Timer sound options (Sound 2 selected by default)
        JPanel soundSettings =
                settingsGroup(
                        "Timer Sound",
                        new String[]{
                                "Sound 1",
                                "Sound 2"
                        },
                        1
                );


        // Position and size of the sound group
        soundSettings.setBounds(
                42,
                195,
                410,
                136
        );


        // Tracker warning message options, saved when picked
        JPanel warningSettings =
                settingsGroup(
                        "Tracker Warning Message",
                        ramiraTrackerPanel.getWarningMessages(),   // was new String[]{"Version 1", ...}
                        ramiraTrackerPanel.getWarningVersion(),
                        ramiraTrackerPanel::setWarningVersion
                );


        // Position and size of the warning group
        warningSettings.setBounds(
                42,
                354,
                410,
                178
        );


        // Heading for the privacy section
        JLabel privacyTitle =
                new JLabel(
                        "Privacy Statement"
                );


        // Privacy heading font
        privacyTitle.setFont(
                new Font(
                        "Times New Roman",
                        Font.PLAIN,
                        20
                )
        );


        // Privacy heading color
        privacyTitle.setForeground(
                MAROON
        );


        // Position and size of the privacy heading
        privacyTitle.setBounds(
                58,
                547,
                350,
                30
        );


        // Privacy text (HTML lets us use line breaks)
        JLabel privacy =
                new JLabel(
                        "<html>LockIn only monitors websites added to your distraction<br>"
                        + "tracker during active Work sessions. No account or<br>"
                        + "personal information is required. Your settings and<br>"
                        + "preferences are stored locally on your device.</html>"
                );


        // Privacy text font (small italic)
        privacy.setFont(
                new Font(
                        "Times New Roman",
                        Font.ITALIC,
                        14
                )
        );


        // Privacy text color
        privacy.setForeground(
                MAROON
        );


        // Rounded card behind the privacy text
        JPanel privacyCard =
                new SoftPanel(
                        new Color(0xE9D6BF),
                        20
                );


        // Use BorderLayout inside the card
        privacyCard.setLayout(
                new BorderLayout()
        );


        // 12px padding inside the card
        privacyCard.setBorder(
                new EmptyBorder(
                        12,
                        12,
                        12,
                        12
                )
        );


        // Put the privacy text at the top of the card
        privacyCard.add(
                privacy,
                BorderLayout.NORTH
        );


        // Position and size of the privacy card
        privacyCard.setBounds(
                42,
                598,
                410,
                156
        );


        // Add the title
        panel.add(title);

        // Add the subtitle
        panel.add(subtitle);

        // Add the sound options
        panel.add(soundSettings);

        // Add the warning options
        panel.add(warningSettings);

        // Add the privacy heading
        panel.add(privacyTitle);

        // Add the privacy card
        panel.add(privacyCard);


        // Return the finished Settings page
        return panel;
    }


    // old 3-argument version still works (radios just don't save anything)
    private JPanel settingsGroup(String label, String[] values, int selectedIndex) {
        // Call the main version with a do-nothing callback
        return settingsGroup(label, values, selectedIndex, i -> { });
    }

    // Builds a labeled group of radio buttons; onPick receives the chosen index
    private JPanel settingsGroup(
            String label,
            String[] values,
            int selectedIndex,
            java.util.function.IntConsumer onPick
    ) {
        // Container with manual layout
        JPanel section =
                new JPanel(null);


        // Make it transparent
        section.setOpaque(false);


        // Group heading label
        JLabel heading =
                new JLabel(label);


        // Heading font
        heading.setFont(
                new Font(
                        "Times New Roman",
                        Font.PLAIN,
                        20
                )
        );


        // Heading color
        heading.setForeground(
                MAROON
        );


        // Position and size of the heading
        heading.setBounds(
                16,
                0,
                394,
                30
        );


        // Add the heading to the group
        section.add(heading);


        // Rounded card that holds the radio buttons
        JPanel options =
                new SoftPanel(
                        new Color(0xE9D6BF),
                        20
                );


        // Stack the radio buttons vertically
        options.setLayout(
                new BoxLayout(
                        options,
                        BoxLayout.Y_AXIS
                )
        );


        // Padding inside the card
        options.setBorder(
                new EmptyBorder(
                        6,
                        36,
                        6,
                        8
                )
        );


        // Makes sure only one radio button can be selected
        ButtonGroup group =
                new ButtonGroup();


        // Create one radio button per value
        for (
                int i = 0;
                i < values.length;
                i++
        ) {

            // Radio button, pre-selected if it matches selectedIndex
            JRadioButton option =
                    new JRadioButton(
                            values[i],
                            i == selectedIndex
                    );


            // Radio button font
            option.setFont(
                    new Font(
                            "Times New Roman",
                            Font.PLAIN,
                            18
                    )
            );


            // Radio button text color
            option.setForeground(
                    MAROON
            );


            // Make the background transparent
            option.setOpaque(false);


            // Custom icon for the unselected state
            option.setIcon(
                    new RadioCircleIcon(false)
            );


            // Custom icon for the selected state
            option.setSelectedIcon(
                    new RadioCircleIcon(true)
            );


            // Remove the focus outline
            option.setFocusPainted(false);


            // Preferred size of each option
            option.setPreferredSize(
                    new Dimension(
                            360,
                            34
                    )
            );


            // Allow full width but fix the height
            option.setMaximumSize(
                    new Dimension(
                            Integer.MAX_VALUE,
                            34
                    )
            );

            // Copy of i so the lambda can use it
            final int index = i;
            // When picked, tell the callback which option was chosen
            option.addActionListener(e -> onPick.accept(index));

            // Add to the group (one selection only)
            group.add(option);

            // Add to the card
            options.add(option);


            // If this is not the last option...
            if (
                    i + 1 < values.length
            ) {

                // ...add a small gap before the next one
                options.add(
                        Box.createVerticalStrut(6)
                );
            }
        }


        // Card height depends on whether there are 2 options or more
        options.setBounds(
                0,
                46,
                410,
                values.length == 2
                        ? 90
                        : 132
        );


        // Add the card to the group
        section.add(options);


        // Return the finished group
        return section;
    }


    // Converts seconds to a "MM:SS" string
    private static String formatDuration(
            int seconds
    ) {

        // Minutes = seconds / 60, remaining seconds = seconds % 60, both 2 digits
        return String.format(
                "%02d:%02d",
                seconds / 60,
                seconds % 60
        );
    }


    // Creates a plain text button (used for + and −)
    private JButton textButton(
            String text,
            int width
    ) {

        // Button showing the given text
        JButton button =
                new JButton(text);


        // Large font so the symbol is easy to see
        button.setFont(
                new Font(
                        "Times New Roman",
                        Font.PLAIN,
                        48
                )
        );


        // Symbol color
        button.setForeground(
                MAROON
        );


        // Button size
        button.setPreferredSize(
                new Dimension(
                        width,
                        66
                )
        );


        // Remove inner margins
        button.setMargin(
                new Insets(
                        0,
                        0,
                        0,
                        0
                )
        );


        // Remove the border
        button.setBorderPainted(false);

        // Remove the background
        button.setContentAreaFilled(false);

        // Remove the focus outline
        button.setFocusPainted(false);


        // Show a hand cursor on hover
        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );


        // Return the finished button
        return button;
    }


    // Draws the round radio button icon (filled when selected)
    private static class RadioCircleIcon
            implements Icon {

        // Whether this icon is the selected version
        private final boolean selected;


        // Stores whether the icon is for the selected state
        RadioCircleIcon(
                boolean selected
        ) {

            this.selected = selected;
        }


        // Icon width in pixels
        @Override
        public int getIconWidth() {

            return 15;
        }


        // Icon height in pixels
        @Override
        public int getIconHeight() {

            return 15;
        }


        // Draws the icon
        @Override
        public void paintIcon(
                Component component,
                Graphics g,
                int x,
                int y
        ) {

            // Copy the graphics object so we don't affect others
            Graphics2D g2 =
                    (Graphics2D) g.create();


            // Smooth edges
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );


            // Draw in maroon
            g2.setColor(
                    MAROON
            );


            if (selected) {

                // Selected: solid filled circle
                g2.fillOval(
                        x + 1,
                        y + 1,
                        13,
                        13
                );

            } else {

                // Unselected: thin outline
                g2.setStroke(
                        new BasicStroke(1f)
                );

                // Unselected: empty circle
                g2.drawOval(
                        x + 1,
                        y + 1,
                        13,
                        13
                );
            }


            // Release the graphics copy
            g2.dispose();
        }
    }


    // Changes the gradient colors of the timer area
    public void setThemeColors(
            Color top,
            Color bottom
    ) {

        // Animate the gradient to the new colors
        timerArea.setColors(
                top,
                bottom
        );
    }


    // ---------- small drawing classes ----------

    // Custom tab button with a rounded right end that grows when selected
    private static class TabButton
            extends JButton {

        // Whether this tab is currently selected
        private boolean selected;

        // Tab width when not selected
        private static final double INACTIVE_WIDTH =
                NAVIGATION_WIDTH;

        // Tab width when selected
        private static final double ACTIVE_WIDTH =
                ACTIVE_TAB_WIDTH;

        // Current drawn width (changes during animation)
        private double displayedWidth =
                INACTIVE_WIDTH;

        // The running width animation, so it can be stopped
        private AnimationUtils.Animation widthAnimation;


        // Creates the tab with its label
        TabButton(
                String label
        ) {

            // Pass the label to JButton
            super(label);

            // Center the text
            setHorizontalAlignment(
                    SwingConstants.CENTER
            );

            // Text color
            setForeground(
                    MAROON
            );
        }


        // Selects or deselects the tab and animates its width
        void setTabSelected(
                boolean selected
        ) {

            // Do nothing if the state is unchanged
            if (
                    this.selected == selected
            ) {

                return;
            }


            // Save the new state
            this.selected =
                    selected;


            // Animation starts from the current width
            double start =
                    displayedWidth;


            // Animation ends at the wide or narrow size
            double target =
                    selected
                            ? ACTIVE_WIDTH
                            : INACTIVE_WIDTH;


            // Stop any running animation first
            if (
                    widthAnimation != null
            ) {

                widthAnimation.stop();
            }


            // Run a 240 ms ease-in-out width animation
            widthAnimation =
                    AnimationUtils.animate(
                            240,
                            AnimationUtils::easeInOut,

                            // Runs on every frame
                            progress -> {

                                // Compute the width for this frame
                                displayedWidth =
                                        AnimationUtils.interpolate(
                                                start,
                                                target,
                                                progress
                                        );

                                // Redraw the tab
                                repaint();

                            },

                            // No callback when finished
                            null
                    );
        }


        // Custom drawing of the tab
        @Override
        protected void paintComponent(
                Graphics g
        ) {

            // Copy the graphics object
            Graphics2D g2 =
                    (Graphics2D) g.create();


            // Smooth edges
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );


            // Drawn width, never wider than the button itself
            int width =
                    Math.min(
                            getWidth(),
                            (int) Math.round(
                                    displayedWidth
                            )
                    );


            // Drawn height (2px less so the outline fits)
            int height =
                    getHeight() - 2;


            // Corner radius for the rounded right end
            int radius =
                    Math.min(
                            34,
                            height / 2
                    );


            // Shape of the tab
            Path2D shape =
                    new Path2D.Float();


            // Start at the top-left
            shape.moveTo(
                    0,
                    1
            );


            // Line along the top edge
            shape.lineTo(
                    width - radius,
                    1
            );


            // Curve for the top-right corner
            shape.quadTo(
                    width - 1,
                    1,
                    width - 1,
                    radius
            );


            // Line down the right edge
            shape.lineTo(
                    width - 1,
                    height - radius
            );


            // Curve for the bottom-right corner
            shape.quadTo(
                    width - 1,
                    height,
                    width - radius,
                    height
            );


            // Line along the bottom edge
            shape.lineTo(
                    0,
                    height
            );


            // Close the shape back to the start
            shape.closePath();


            // Fill color depends on whether the tab is selected
            g2.setColor(
                    selected
                            ? TAB_SELECTED
                            : TAB_BG
            );


            // Fill the tab shape
            g2.fill(shape);


            // Outline color (dark brown)
            g2.setColor(
                    new Color(0x4A3A34)
            );


            // Thin outline
            g2.setStroke(
                    new BasicStroke(1f)
            );


            // Draw the outline
            g2.draw(shape);


            // Font measurements, used to center the text
            FontMetrics fm =
                    g2.getFontMetrics(
                            getFont()
                    );


            // Width of the label text
            int textWidth =
                    fm.stringWidth(
                            getText()
                    );


            // Horizontal position that centers the text in the tab
            int x =
                    Math.max(
                            0,
                            (width - textWidth) / 2
                    );


            // Vertical position that centers the text in the tab
            int y =
                    (height - fm.getHeight()) / 2
                            + fm.getAscent();


            // Use the button's font
            g2.setFont(
                    getFont()
            );


            // Use the button's text color
            g2.setColor(
                    getForeground()
            );


            // Draw the label
            g2.drawString(
                    getText(),
                    x,
                    y
            );


            // Release the graphics copy
            g2.dispose();
        }
    }


    // Panel with a solid rounded-rectangle background
    private static class SoftPanel
            extends JPanel {

        // Background fill color
        private final Color fill;

        // Corner roundness
        private final int arc;


        // Stores the fill color and corner roundness
        SoftPanel(
                Color fill,
                int arc
        ) {

            // Save the fill color
            this.fill = fill;

            // Save the corner roundness
            this.arc = arc;

            // Transparent so only our rounded shape shows
            setOpaque(false);
        }


        // Draws the rounded background, then the children
        @Override
        protected void paintComponent(
                Graphics g
        ) {

            // Copy the graphics object
            Graphics2D g2 =
                    (Graphics2D) g.create();


            // Smooth edges
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );


            // Use the fill color
            g2.setColor(fill);


            // Fill a rounded rectangle covering the whole panel
            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    arc,
                    arc
            );


            // Release the graphics copy
            g2.dispose();


            // Let Swing paint the rest as normal
            super.paintComponent(g);
        }
    }


    // Pill-shaped button, either filled or outline-only
    private static class RoundedActionButton
            extends JButton {

        // True = filled with cream, false = outline only
        private final boolean filled;


        // Creates the button with its label, size, and style
        RoundedActionButton(
                String label,
                int width,
                int height,
                boolean filled
        ) {

            // Pass the label to JButton
            super(label);

            // Save the filled/outline style
            this.filled = filled;


            // Set the button size
            setPreferredSize(
                    new Dimension(
                            width,
                            height
                    )
            );


            // Text color (brown)
            setForeground(
                    new Color(0x5C3A12)
            );


            // Remove the default border
            setBorderPainted(false);

            // Remove the default background
            setContentAreaFilled(false);

            // Remove the focus outline
            setFocusPainted(false);
        }


        // Custom drawing of the button
        @Override
        protected void paintComponent(
                Graphics g
        ) {

            // Copy the graphics object
            Graphics2D g2 =
                    (Graphics2D) g.create();


            // Smooth edges
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );


            // Gap between the button edge and the shape
            int inset = 1;

            // Arc equals height, which makes fully round ends
            int arc = getHeight();


            // Only fill the background when "filled" is true
            if (filled) {

                // Fill color
                g2.setColor(
                        CREAM
                );


                // Draw the filled pill
                g2.fillRoundRect(
                        inset,
                        inset,
                        getWidth() - 2 * inset,
                        getHeight() - 2 * inset,
                        arc,
                        arc
                );
            }


            // Outline color: cream if filled, maroon otherwise
            g2.setColor(
                    filled
                            ? CREAM
                            : MAROON
            );


            // Outline thickness
            g2.setStroke(
                    new BasicStroke(1.2f)
            );


            // Draw the pill outline
            g2.drawRoundRect(
                    inset,
                    inset,
                    getWidth() - 2 * inset,
                    getHeight() - 2 * inset,
                    arc,
                    arc
            );


            // Use the button's font for the label
            g2.setFont(
                    getFont()
            );


            // Use the button's text color
            g2.setColor(
                    getForeground()
            );


            // Font measurements, used to center the text
            FontMetrics fm =
                    g2.getFontMetrics();


            // Horizontal position that centers the text
            int x =
                    (getWidth()
                            - fm.stringWidth(
                                    getText()
                            )) / 2;


            // Vertical position that centers the text
            int y =
                    (getHeight()
                            - fm.getHeight()) / 2
                            + fm.getAscent();


            // Draw the label
            g2.drawString(
                    getText(),
                    x,
                    y
            );


            // Release the graphics copy
            g2.dispose();
        }
    }


    // Panel with a diagonal two-color gradient background that animates between themes
    private static class GradientPanel
            extends JPanel {

        // Current top-left gradient color
        private Color top;

        // Current bottom-right gradient color
        private Color bottom;

        // The running color animation, so it can be stopped
        private AnimationUtils.Animation colorAnimation;


        // Creates the panel with starting colors
        GradientPanel(
                Color top,
                Color bottom
        ) {

            // Save the top color
            this.top = top;

            // Save the bottom color
            this.bottom = bottom;
        }


        // Smoothly changes the gradient to new colors
        void setColors(
                Color top,
                Color bottom
        ) {

            // Remember the current top color as the animation start
            Color startTop =
                    this.top;

            // Remember the current bottom color as the animation start
            Color startBottom =
                    this.bottom;


            // Stop any running animation first
            if (
                    colorAnimation != null
            ) {

                colorAnimation.stop();
            }


            // Run a 280 ms ease-in-out color animation
            colorAnimation =
                    AnimationUtils.animate(
                            280,
                            AnimationUtils::easeInOut,

                            // Runs on every frame
                            progress -> {

                                // Blend the top color toward the new one
                                this.top =
                                        AnimationUtils.interpolate(
                                                startTop,
                                                top,
                                                progress
                                        );


                                // Blend the bottom color toward the new one
                                this.bottom =
                                        AnimationUtils.interpolate(
                                                startBottom,
                                                bottom,
                                                progress
                                        );


                                // Redraw with the blended colors
                                repaint();

                            },

                            // No callback when finished
                            null
                    );
        }


        // Paints the gradient background
        @Override
        protected void paintComponent(
                Graphics g
        ) {

            // Do the normal panel painting first
            super.paintComponent(g);


            // Copy the graphics object
            Graphics2D g2 =
                    (Graphics2D) g.create();


            // Set a diagonal gradient from top-left to bottom-right
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


            // Fill the entire panel with the gradient
            g2.fillRect(
                    0,
                    0,
                    getWidth(),
                    getHeight()
            );


            // Release the graphics copy
            g2.dispose();
        }
    }


    // Icon with three horizontal lines (the menu button)
    private static class HamburgerIcon
            implements Icon {


        // Icon width in pixels
        @Override
        public int getIconWidth() {

            return 30;
        }


        // Icon height in pixels
        @Override
        public int getIconHeight() {

            return 24;
        }


        // Draws the three lines
        @Override
        public void paintIcon(
                Component c,
                Graphics g,
                int x,
                int y
        ) {

            // Copy the graphics object
            Graphics2D g2 =
                    (Graphics2D) g.create();


            // Draw in cream
            g2.setColor(
                    CREAM
            );


            // Thick lines
            g2.setStroke(
                    new BasicStroke(3f)
            );


            // Top line
            g2.drawLine(
                    x + 2,
                    y + 4,
                    x + 28,
                    y + 4
            );


            // Middle line
            g2.drawLine(
                    x + 2,
                    y + 12,
                    x + 28,
                    y + 12
            );


            // Bottom line
            g2.drawLine(
                    x + 2,
                    y + 20,
                    x + 28,
                    y + 20
            );


            // Release the graphics copy
            g2.dispose();
        }
    }
}