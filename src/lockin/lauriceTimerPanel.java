package lockin;

// Swing components: JPanel, JLabel, buttons, and the Swing Timer
import javax.swing.*;
// AWT classes for colors, fonts, layouts, and drawing
import java.awt.*;

// The timer screen: Work/Break buttons, the countdown, and Start/Pause/Reset controls
public class lauriceTimerPanel extends JPanel {

    // Cream color used for the text and button fills
    private static final Color CREAM = new Color(0xFFF0D8);
    // Brown color used for text on cream buttons
    private static final Color BROWN = new Color(0x5C3A12);

    // Length of a Work session, in seconds (25 minutes)
    private int workDuration = 25 * 60;
    // Length of a Break session, in seconds (5 minutes)
    private int breakDuration = 5 * 60;
    // Seconds remaining in the current session
    private int timeLeft = workDuration;

    // True when the current session is Work, false when it is Break
    private boolean workSession = true;
    // True once the user has pressed Start (decides which buttons to show)
    private boolean hasStarted = false;

    // The big countdown text (like 25:00)
    private JLabel timerLabel;

    // Control buttons shown depending on the timer state
    private JButton startButton;
    private JButton pauseButton;
    private JButton resumeButton;
    private JButton resetButton;

    // Work and Break toggle buttons at the top of the card
    private JToggleButton workButton;
    private JToggleButton breakButton;
    // Makes sure only one of Work/Break is selected at a time
    private ButtonGroup sessionButtons;

    // Holds the Start / Pause / Reset buttons
    private JPanel controlPanel;

    // Swing Timer that ticks once per second
    private Timer timer;

    // Builds the whole panel in order
    public lauriceTimerPanel() {
        // Create the buttons and label
        createComponents();
        // Arrange them on the card
        createLayout();
        // Set up the one-second countdown
        createTimer();
        // Connect the buttons to their actions
        createButtonActions();
        // Show the starting time and buttons
        updateDisplay();
    }

    // Creates the label and buttons (no layout yet)
    private void createComponents() {
        // Countdown label starts at 25:00
        timerLabel = new JLabel("25:00");

        // Big bold serif font for the countdown
        timerLabel.setFont(
                new Font(
                        "Times New Roman",
                        Font.BOLD,
                        186
                )
        );

        // Center the text inside the label
        timerLabel.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        // Cream text color
        timerLabel.setForeground(CREAM);
        // Center the label inside the vertical box layout
        timerLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        // Work and Break buttons with their widths
        workButton = new ModeButton("Work", 144);
        breakButton = new ModeButton("Break", 142);

        // Work is selected when the app opens
        workButton.setSelected(true);

        // Group them so selecting one deselects the other
        sessionButtons = new ButtonGroup();
        sessionButtons.add(workButton);
        sessionButtons.add(breakButton);

        // Big Start button
        startButton = new StartButton("Start");

        // Round icon buttons (the type picks which icon is drawn)
        pauseButton = new IconButton("pause");
        resumeButton = new IconButton("play");
        resetButton = new IconButton("reset");

        // Row that holds the control buttons, centered with 10px gaps
        controlPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.CENTER,
                        10,
                        0
                )
        );

        // Transparent so the card shows through
        controlPanel.setOpaque(false);

        // Clicking Work switches to a Work session
        workButton.addActionListener(
                e -> selectSession(true)
        );

        // Clicking Break switches to a Break session
        breakButton.addActionListener(
                e -> selectSession(false)
        );
    }

    // Arranges the components on the card
    private void createLayout() {
        setLayout(new BorderLayout());

        // Ideal card size, and the smallest it may shrink to
        setPreferredSize(new Dimension(638, 520));
        setMinimumSize(new Dimension(420, 420));

        // Transparent so the gradient behind shows (the card is drawn in paintComponent)
        setOpaque(false);

        // Inner panel that stacks everything top to bottom
        JPanel content = new JPanel();

        content.setOpaque(false);

        // Stack children vertically
        content.setLayout(
                new BoxLayout(
                        content,
                        BoxLayout.Y_AXIS
                )
        );

        // Padding: 75 top, 70 bottom
        content.setBorder(
                BorderFactory.createEmptyBorder(
                        75,
                        0,
                        70,
                        0
                )
        );

        // Row holding the Work and Break buttons, 40px apart
        JPanel modeRow = new JPanel(
                new FlowLayout(
                        FlowLayout.CENTER,
                        40,
                        0
                )
        );

        modeRow.setOpaque(false);

        modeRow.add(workButton);
        modeRow.add(breakButton);

        // Flexible space at the top, so the content sits in the middle
        content.add(
                Box.createVerticalGlue()
        );

        // Center the Work/Break row horizontally
        modeRow.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        content.add(modeRow);

        // Gap between the buttons and the countdown
        content.add(
                Box.createVerticalStrut(38)
        );

        content.add(timerLabel);

        // Gap between the countdown and the controls
        content.add(
                Box.createVerticalStrut(28)
        );

        // Center the control buttons horizontally
        controlPanel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        content.add(controlPanel);

        // Flexible space at the bottom
        content.add(
                Box.createVerticalGlue()
        );

        // Put the stacked content in the middle of the card
        add(
                content,
                BorderLayout.CENTER
        );

        // Rescale the countdown text whenever the card is resized
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                scaleTimerText();
            }
        });
    }

    // Resizes the countdown font to fit the card (186 is the full size)
    private void scaleTimerText() {
        // Largest size that fits the width
        float byWidth = (getWidth() - 80) / 2.4f;
        // Largest size that fits the height
        float byHeight = (getHeight() - 313) / 1.15f;
        // Use the smaller fit, kept between 48 and 186
        float size = Math.max(48f, Math.min(186f, Math.min(byWidth, byHeight)));
        timerLabel.setFont(timerLabel.getFont().deriveFont(size));
    }

    // Creates the timer that fires every 1000 ms
    private void createTimer() {

        timer = new Timer(
                1000,
                e -> {

                    // Time left: count down one second
                    if (timeLeft > 0) {

                        timeLeft--;

                        updateDisplay();

                    // Time is up: move to the next session
                    } else {

                        switchSession();
                    }
                }
        );
    }

    // Connects each control button to its action
    private void createButtonActions() {

        // Start: mark as started and begin counting
        startButton.addActionListener(
                e -> {

                    hasStarted = true;

                    startTimer();
                }
        );

        // Pause: stop counting
        pauseButton.addActionListener(
                e -> pauseTimer()
        );

        // Resume: keep counting from where it stopped
        resumeButton.addActionListener(
                e -> startTimer()
        );

        // Reset: go back to a fresh Work session
        resetButton.addActionListener(
                e -> resetTimer()
        );
    }

    // Starts (or resumes) the countdown
    public void startTimer() {

        timer.start();

        hasStarted = true;

        // Refresh the buttons (shows Pause and Reset)
        updateDisplay();
    }

    // Stops the countdown but keeps the time left
    public void pauseTimer() {

        timer.stop();

        // Refresh the buttons (shows Resume and Reset)
        updateDisplay();
    }

    // Stops the timer and goes back to a full Work session
    public void resetTimer() {

        timer.stop();

        workSession = true;

        timeLeft = workDuration;

        hasStarted = false;

        // Highlight the Work button again
        workButton.setSelected(true);

        updateDisplay();
    }

    // Jumps straight to the next session
    public void skipSession() {

        switchSession();
    }

    // Returns the time left as text like "12:31" (used by the warning popup)
    public String getRemainingText() {

        int minutes =
                timeLeft / 60;

        int seconds =
                timeLeft % 60;

        // %02d pads with zeros: 5 becomes 05
        return String.format(
                "%02d:%02d",
                minutes,
                seconds
        );
    }

    // Short alias for pauseTimer (can be called from other classes)
    public void pause() {

        pauseTimer();
    }

    // Short alias for startTimer (can be called from other classes)
    public void resume() {

        startTimer();
    }

    // Short alias for resetTimer (can be called from other classes)
    public void reset() {

        resetTimer();
    }

    // Swaps Work for Break (or Break for Work) and waits for Start
    private void switchSession() {

        timer.stop();

        // Work just ended: move to Break
        if (workSession) {

            workSession = false;

            timeLeft = breakDuration;

            breakButton.setSelected(true);

        // Break just ended: move to Work
        } else {

            workSession = true;

            timeLeft = workDuration;

            workButton.setSelected(true);
        }

        // The next session waits for the user to press Start
        hasStarted = false;

        updateDisplay();
    }

    // Refreshes the countdown text and picks which buttons to show
    private void updateDisplay() {

        int minutes =
                timeLeft / 60;

        int seconds =
                timeLeft % 60;

        // Show the time as MM:SS
        timerLabel.setText(
                String.format(
                        "%02d:%02d",
                        minutes,
                        seconds
                )
        );

        // Clear the old buttons before adding the right ones
        controlPanel.removeAll();

        // Not started yet: show only Start
        if (!hasStarted) {

            controlPanel.add(
                    startButton
            );

        // Running: show Pause and Reset
        } else if (timer.isRunning()) {

            controlPanel.add(
                    pauseButton
            );

            controlPanel.add(
                    resetButton
            );

        // Started but paused: show Resume and Reset
        } else {

            controlPanel.add(
                    resumeButton
            );

            controlPanel.add(
                    resetButton
            );
        }

        // Redraw the button row
        controlPanel.revalidate();
        controlPanel.repaint();
    }

    // Switches to Work or Break when its toggle button is clicked
    private void selectSession(
            boolean work
    ) {

        // Stop any running countdown first
        if (timer.isRunning()) {

            timer.stop();
        }

        hasStarted = false;

        workSession = work;

        // Load the full length of the chosen session
        timeLeft =
                work
                        ? workDuration
                        : breakDuration;

        updateDisplay();
    }

    /*
     * =========================================================
     * TIMER STATUS METHODS
     * Other classes (like the API server) use these to read the timer.
     * =========================================================
     */

    // True when the current session is Work
    public boolean isWorkSession() {

        return workSession;
    }

    // True when the current session is Break
    public boolean isBreakSession() {

        return !workSession;
    }

    // True while the countdown is ticking
    public boolean isTimerRunning() {

        return timer.isRunning();
    }

    /*
     * Used by the API server to decide if a restricted site should be flagged.
     *
     * TRUE only when the session is Work AND the timer is running.
     * FALSE when stopped, paused, reset, or in Break.
     */
    public boolean isWorkTimerActive() {

        return workSession
                && timer.isRunning();
    }

    // Seconds left in the current session
    public int getTimeLeft() {

        return timeLeft;
    }

    // Current Work length in seconds
    public int getWorkDuration() {

        return workDuration;
    }

    // Current Break length in seconds
    public int getBreakDuration() {

        return breakDuration;
    }

    // Sets a new Work length (minimum 60 seconds)
    public void setWorkDuration(
            int seconds
    ) {

        workDuration =
                Math.max(
                        60,
                        seconds
                );

        // If Work is showing and not running, show the new length right away
        if (
                workSession
                        && !timer.isRunning()
        ) {

            timeLeft = workDuration;

            hasStarted = false;

            updateDisplay();
        }
    }

    // Sets a new Break length (minimum 60 seconds)
    public void setBreakDuration(
            int seconds
    ) {

        breakDuration =
                Math.max(
                        60,
                        seconds
                );

        // If Break is showing and not running, show the new length right away
        if (
                !workSession
                        && !timer.isRunning()
        ) {

            timeLeft = breakDuration;

            hasStarted = false;

            updateDisplay();
        }
    }

    // Draws the translucent rounded card behind the timer
    @Override
    protected void paintComponent(
            Graphics g
    ) {

        super.paintComponent(g);

        // Copy of the graphics so our changes don't leak out
        Graphics2D g2 =
                (Graphics2D) g.create();

        // Smooth edges
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        // Cream with a low alpha (0x1C) so it looks see-through
        g2.setColor(
                new Color(
                        0xFFF5E4
                                | 0x1C000000,
                        true
                )
        );

        // Fill the whole card with large rounded corners
        g2.fillRoundRect(
                0,
                0,
                getWidth(),
                getHeight(),
                124,
                124
        );

        g2.dispose();
    }

    // Pill-shaped Work/Break toggle button
    private static class ModeButton
            extends JToggleButton {

        ModeButton(
                String label,
                int width
        ) {

            super(label);

            // Bold serif label
            setFont(
                    new Font(
                            "Times New Roman",
                            Font.BOLD,
                            26
                    )
            );

            // Lock the button to a fixed size
            Dimension size =
                    new Dimension(
                            width,
                            46
                    );

            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);

            // Turn off the default look, since we draw it ourselves
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
        }

        // Custom drawing: filled pill when selected, outline when not
        @Override
        protected void paintComponent(
                Graphics g
        ) {

            Graphics2D g2 =
                    (Graphics2D) g.create();

            // Smooth edges
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            // Is this button the selected one?
            boolean active =
                    isSelected();

            // Keeps the outline inside the button's edge
            int inset = 1;

            // Arc equal to the height makes fully round ends
            int arc =
                    getHeight()
                            - inset * 2;

            // Selected: solid cream pill
            if (active) {

                g2.setColor(CREAM);

                g2.fillRoundRect(
                        inset,
                        inset,
                        getWidth()
                                - inset * 2,
                        getHeight()
                                - inset * 2,
                        arc,
                        arc
                );

            // Not selected: cream outline only
            } else {

                g2.setColor(CREAM);

                g2.setStroke(
                        new BasicStroke(2f)
                );

                g2.drawRoundRect(
                        inset,
                        inset,
                        getWidth()
                                - inset * 2,
                        getHeight()
                                - inset * 2,
                        arc,
                        arc
                );
            }

            g2.setFont(getFont());

            // Brown text on the filled pill, cream text on the outline
            g2.setColor(
                    active
                            ? BROWN
                            : CREAM
            );

            FontMetrics fm =
                    g2.getFontMetrics();

            // x: center the text horizontally
            int x =
                    (
                            getWidth()
                                    - fm.stringWidth(
                                    getText()
                            )
                    ) / 2;

            // y: center the text vertically (baseline position)
            int y =
                    (
                            getHeight()
                                    - fm.getHeight()
                    ) / 2
                            + fm.getAscent();

            g2.drawString(
                    getText(),
                    x,
                    y
            );

            g2.dispose();
        }
    }

    // Big cream "Start" button
    private static class StartButton
            extends JButton {

        StartButton(String text) {

            super(text);

            // Large bold serif text
            setFont(
                    new Font(
                            "Times New Roman",
                            Font.BOLD,
                            36
                    )
            );

            setForeground(BROWN);

            // Lock the button to 174 x 56
            setPreferredSize(
                    new Dimension(
                            174,
                            56
                    )
            );

            setMinimumSize(
                    new Dimension(
                            174,
                            56
                    )
            );

            setMaximumSize(
                    new Dimension(
                            174,
                            56
                    )
            );

            // Turn off the default look, since we draw it ourselves
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
        }

        // Custom drawing: a cream pill with centered brown text
        @Override
        protected void paintComponent(
                Graphics g
        ) {

            Graphics2D g2 =
                    (Graphics2D) g.create();

            // Smooth edges
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(CREAM);

            // Pill shape: arc equals the height
            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    getHeight(),
                    getHeight()
            );

            g2.setFont(getFont());

            g2.setColor(BROWN);

            FontMetrics fm =
                    g2.getFontMetrics();

            // x: center the text horizontally
            int x =
                    (
                            getWidth()
                                    - fm.stringWidth(
                                    getText()
                            )
                    ) / 2;

            // y: center the text vertically (baseline position)
            int y =
                    (
                            getHeight()
                                    - fm.getHeight()
                    ) / 2
                            + fm.getAscent();

            g2.drawString(
                    getText(),
                    x,
                    y
            );

            g2.dispose();
        }
    }

    // Round cream button that draws a pause, play, or reset icon
    private static class IconButton
            extends JButton {

        // Which icon to draw: "pause", "play", or "reset"
        private final String type;

        IconButton(String type) {

            this.type = type;

            // Lock the button to 58 x 56
            setPreferredSize(
                    new Dimension(
                            58,
                            56
                    )
            );

            setMinimumSize(
                    new Dimension(
                            58,
                            56
                    )
            );

            setMaximumSize(
                    new Dimension(
                            58,
                            56
                    )
            );

            // Turn off the default look, since we draw it ourselves
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
        }

        // Custom drawing: a cream circle with the chosen icon on top
        @Override
        protected void paintComponent(
                Graphics g
        ) {

            Graphics2D g2 =
                    (Graphics2D) g.create();

            // Smooth edges
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(CREAM);

            // Cream pill background
            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    getHeight(),
                    getHeight()
            );

            // Icons are drawn in brown
            g2.setColor(BROWN);

            // Pause icon: two vertical bars
            if (
                    type.equals("pause")
            ) {

                g2.fillRoundRect(
                        21,
                        17,
                        6,
                        22,
                        4,
                        4
                );

                g2.fillRoundRect(
                        31,
                        17,
                        6,
                        22,
                        4,
                        4
                );
            }

            // Play icon: a triangle pointing right
            if (
                    type.equals("play")
            ) {

                Polygon triangle =
                        new Polygon();

                // Triangle corners (top-left, bottom-left, right tip)
                triangle.addPoint(
                        22,
                        14
                );

                triangle.addPoint(
                        22,
                        42
                );

                triangle.addPoint(
                        40,
                        28
                );

                g2.fillPolygon(
                        triangle
                );
            }

            // Reset icon: a circular arrow
            if (
                    type.equals("reset")
            ) {

                // Thick line with rounded ends
                g2.setStroke(
                        new BasicStroke(
                                3f,
                                BasicStroke.CAP_ROUND,
                                BasicStroke.JOIN_ROUND
                        )
                );

                // Circle drawn as an arc with a gap at the top right
                g2.drawArc(
                        18,
                        17,
                        22,
                        22,
                        45,
                        285
                );

                Polygon arrow =
                        new Polygon();

                // Small arrowhead at the end of the arc
                arrow.addPoint(
                        39,
                        16
                );

                arrow.addPoint(
                        39,
                        23
                );

                arrow.addPoint(
                        33,
                        20
                );

                g2.fillPolygon(
                        arrow
                );
            }

            g2.dispose();
        }
    }
}