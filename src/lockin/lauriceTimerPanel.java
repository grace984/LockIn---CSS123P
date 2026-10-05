package lockin;

import javax.swing.*;
import java.awt.*;

public class lauriceTimerPanel extends JPanel {

    private static final Color CREAM = new Color(0xFFF0D8);
    private static final Color BROWN = new Color(0x5C3A12);

    private int workDuration = 25 * 60;
    private int breakDuration = 5 * 60;
    private int timeLeft = workDuration;

    private boolean workSession = true;
    private boolean hasStarted = false;

    private JLabel timerLabel;

    private JButton startButton;
    private JButton pauseButton;
    private JButton resumeButton;
    private JButton resetButton;

    private JToggleButton workButton;
    private JToggleButton breakButton;
    private ButtonGroup sessionButtons;

    private JPanel controlPanel;

    private Timer timer;

    public lauriceTimerPanel() {
        createComponents();
        createLayout();
        createTimer();
        createButtonActions();
        updateDisplay();
    }

    private void createComponents() {
        timerLabel = new JLabel("25:00");

        timerLabel.setFont(
                new Font(
                        "Times New Roman",
                        Font.BOLD,
                        186
                )
        );

        timerLabel.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        timerLabel.setForeground(CREAM);
        timerLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        workButton = new ModeButton("Work", 144);
        breakButton = new ModeButton("Break", 142);

        workButton.setSelected(true);

        sessionButtons = new ButtonGroup();
        sessionButtons.add(workButton);
        sessionButtons.add(breakButton);

        startButton = new StartButton("Start");

        pauseButton = new IconButton("pause");
        resumeButton = new IconButton("play");
        resetButton = new IconButton("reset");

        controlPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.CENTER,
                        10,
                        0
                )
        );

        controlPanel.setOpaque(false);

        workButton.addActionListener(
                e -> selectSession(true)
        );

        breakButton.addActionListener(
                e -> selectSession(false)
        );
    }

    private void createLayout() {
        setLayout(new BorderLayout());

        setPreferredSize(new Dimension(638, 520));
        setMinimumSize(new Dimension(420, 420));

        setOpaque(false);

        JPanel content = new JPanel();

        content.setOpaque(false);

        content.setLayout(
                new BoxLayout(
                        content,
                        BoxLayout.Y_AXIS
                )
        );

        content.setBorder(
                BorderFactory.createEmptyBorder(
                        75,
                        0,
                        70,
                        0
                )
        );

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

        content.add(
                Box.createVerticalGlue()
        );

        modeRow.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        content.add(modeRow);

        content.add(
                Box.createVerticalStrut(38)
        );

        content.add(timerLabel);

        content.add(
                Box.createVerticalStrut(28)
        );

        controlPanel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        content.add(controlPanel);

        content.add(
                Box.createVerticalGlue()
        );

        add(
                content,
                BorderLayout.CENTER
        );

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                scaleTimerText();
            }
        });
    }

    // shrinks or grows the big "25:00" to fit the card (186 is the original size)
    private void scaleTimerText() {
        float byWidth = (getWidth() - 80) / 2.4f;
        float byHeight = (getHeight() - 313) / 1.15f;
        float size = Math.max(48f, Math.min(186f, Math.min(byWidth, byHeight)));
        timerLabel.setFont(timerLabel.getFont().deriveFont(size));
    }

    private void createTimer() {

        timer = new Timer(
                1000,
                e -> {

                    if (timeLeft > 0) {

                        timeLeft--;

                        updateDisplay();

                    } else {

                        switchSession();
                    }
                }
        );
    }

    private void createButtonActions() {

        startButton.addActionListener(
                e -> {

                    hasStarted = true;

                    startTimer();
                }
        );

        pauseButton.addActionListener(
                e -> pauseTimer()
        );

        resumeButton.addActionListener(
                e -> startTimer()
        );

        resetButton.addActionListener(
                e -> resetTimer()
        );
    }

    public void startTimer() {

        timer.start();

        hasStarted = true;

        updateDisplay();
    }

    public void pauseTimer() {

        timer.stop();

        updateDisplay();
    }

    public void resetTimer() {

        timer.stop();

        workSession = true;

        timeLeft = workDuration;

        hasStarted = false;

        workButton.setSelected(true);

        updateDisplay();
    }

    public void skipSession() {

        switchSession();
    }

    public String getRemainingText() {

        int minutes =
                timeLeft / 60;

        int seconds =
                timeLeft % 60;

        return String.format(
                "%02d:%02d",
                minutes,
                seconds
        );
    }

    public void pause() {

        pauseTimer();
    }

    public void resume() {

        startTimer();
    }

    public void reset() {

        resetTimer();
    }

    private void switchSession() {

        timer.stop();

        if (workSession) {

            workSession = false;

            timeLeft = breakDuration;

            breakButton.setSelected(true);

        } else {

            workSession = true;

            timeLeft = workDuration;

            workButton.setSelected(true);
        }

        hasStarted = false;

        updateDisplay();
    }

    private void updateDisplay() {

        int minutes =
                timeLeft / 60;

        int seconds =
                timeLeft % 60;

        timerLabel.setText(
                String.format(
                        "%02d:%02d",
                        minutes,
                        seconds
                )
        );

        controlPanel.removeAll();

        if (!hasStarted) {

            controlPanel.add(
                    startButton
            );

        } else if (timer.isRunning()) {

            controlPanel.add(
                    pauseButton
            );

            controlPanel.add(
                    resetButton
            );

        } else {

            controlPanel.add(
                    resumeButton
            );

            controlPanel.add(
                    resetButton
            );
        }

        controlPanel.revalidate();
        controlPanel.repaint();
    }

    private void selectSession(
            boolean work
    ) {

        if (timer.isRunning()) {

            timer.stop();
        }

        hasStarted = false;

        workSession = work;

        timeLeft =
                work
                        ? workDuration
                        : breakDuration;

        updateDisplay();
    }

    /*
     * =========================================================
     * TIMER STATUS METHODS
     * =========================================================
     */

    public boolean isWorkSession() {

        return workSession;
    }

    public boolean isBreakSession() {

        return !workSession;
    }

    public boolean isTimerRunning() {

        return timer.isRunning();
    }

    /*
     * THIS METHOD IS USED BY THE API SERVER.
     *
     * TRUE ONLY WHEN:
     * 1. Current session is Work
     * 2. Timer is actually running
     *
     * FALSE WHEN:
     * - Work timer is not started
     * - Work timer is paused
     * - Work timer is reset
     * - Break session
     * - Break timer is running
     */
    public boolean isWorkTimerActive() {

        return workSession
                && timer.isRunning();
    }

    public int getTimeLeft() {

        return timeLeft;
    }

    public int getWorkDuration() {

        return workDuration;
    }

    public int getBreakDuration() {

        return breakDuration;
    }

    public void setWorkDuration(
            int seconds
    ) {

        workDuration =
                Math.max(
                        60,
                        seconds
                );

        if (
                workSession
                        && !timer.isRunning()
        ) {

            timeLeft = workDuration;

            hasStarted = false;

            updateDisplay();
        }
    }

    public void setBreakDuration(
            int seconds
    ) {

        breakDuration =
                Math.max(
                        60,
                        seconds
                );

        if (
                !workSession
                        && !timer.isRunning()
        ) {

            timeLeft = breakDuration;

            hasStarted = false;

            updateDisplay();
        }
    }

    @Override
    protected void paintComponent(
            Graphics g
    ) {

        super.paintComponent(g);

        Graphics2D g2 =
                (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setColor(
                new Color(
                        0xFFF5E4
                                | 0x1C000000,
                        true
                )
        );

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

    private static class ModeButton
            extends JToggleButton {

        ModeButton(
                String label,
                int width
        ) {

            super(label);

            setFont(
                    new Font(
                            "Times New Roman",
                            Font.BOLD,
                            26
                    )
            );

            Dimension size =
                    new Dimension(
                            width,
                            46
                    );

            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);

            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(
                Graphics g
        ) {

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            boolean active =
                    isSelected();

            int inset = 1;

            int arc =
                    getHeight()
                            - inset * 2;

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

            g2.setColor(
                    active
                            ? BROWN
                            : CREAM
            );

            FontMetrics fm =
                    g2.getFontMetrics();

            int x =
                    (
                            getWidth()
                                    - fm.stringWidth(
                                    getText()
                            )
                    ) / 2;

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

    private static class StartButton
            extends JButton {

        StartButton(String text) {

            super(text);

            setFont(
                    new Font(
                            "Times New Roman",
                            Font.BOLD,
                            36
                    )
            );

            setForeground(BROWN);

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

            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(
                Graphics g
        ) {

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(CREAM);

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

            int x =
                    (
                            getWidth()
                                    - fm.stringWidth(
                                    getText()
                            )
                    ) / 2;

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

    private static class IconButton
            extends JButton {

        private final String type;

        IconButton(String type) {

            this.type = type;

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

            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(
                Graphics g
        ) {

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(CREAM);

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    getHeight(),
                    getHeight()
            );

            g2.setColor(BROWN);

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

            if (
                    type.equals("play")
            ) {

                Polygon triangle =
                        new Polygon();

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

            if (
                    type.equals("reset")
            ) {

                g2.setStroke(
                        new BasicStroke(
                                3f,
                                BasicStroke.CAP_ROUND,
                                BasicStroke.JOIN_ROUND
                        )
                );

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