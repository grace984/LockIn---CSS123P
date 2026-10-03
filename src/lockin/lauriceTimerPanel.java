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

    private JLabel timerLabel;

    private JButton startButton;
    private JToggleButton workButton;
    private JToggleButton breakButton;
    private ButtonGroup sessionButtons;

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
        timerLabel.setFont(new Font("Times New Roman", Font.BOLD, 186));
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerLabel.setForeground(CREAM);
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        workButton = new ModeButton("Work");
        breakButton = new ModeButton("Break");
        workButton.setSelected(true);
        sessionButtons = new ButtonGroup();
        sessionButtons.add(workButton);
        sessionButtons.add(breakButton);

        startButton = new StartButton();
        startButton.addActionListener(e -> {
            if (timer.isRunning()) {
                pauseTimer();
            } else {
                startTimer();
            }
        });
        workButton.addActionListener(e -> selectSession(true));
        breakButton.addActionListener(e -> selectSession(false));
    }

    private void createLayout() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(638, 520));
        setMinimumSize(new Dimension(638, 520));
        setMaximumSize(new Dimension(638, 520));
        setOpaque(false);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(75, 0, 70, 0));

        JPanel modeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        modeRow.setOpaque(false);
        modeRow.add(workButton);
        modeRow.add(breakButton);

        content.add(Box.createVerticalGlue());
        modeRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(modeRow);
        content.add(Box.createVerticalStrut(38));
        content.add(timerLabel);
        content.add(Box.createVerticalStrut(28));
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(startButton);
        content.add(Box.createVerticalGlue());
        add(content, BorderLayout.CENTER);
    }

    private void createTimer() {
        timer = new Timer(1000, e -> {
            if (timeLeft > 0) {
                timeLeft--;
                updateDisplay();
            } else {
                switchSession();
            }
        });
    }

    private void createButtonActions() {
        // Actions are attached when each reference control is created.
    }

    public void startTimer() {
        timer.start();
        startButton.setText("Pause");
    }

    public void pauseTimer() {
        timer.stop();
        startButton.setText("Start");
    }

    public void resetTimer() {
        timer.stop();
        workSession = true;
        timeLeft = workDuration;
        workButton.setSelected(true);
        startButton.setText("Start");
        updateDisplay();
    }

    public void skipSession() {
        switchSession();
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

        startButton.setText("Start");
        updateDisplay();
    }

    private void updateDisplay() {
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;

        timerLabel.setText(
                String.format("%02d:%02d", minutes, seconds)
        );

    }

    private void selectSession(boolean work) {
        if (timer.isRunning()) {
            timer.stop();
            startButton.setText("Start");
        }
        workSession = work;
        timeLeft = work ? workDuration : breakDuration;
        updateDisplay();
    }

    public boolean isWorkSession() {
        return workSession;
    }

    public boolean isBreakSession() {
        return !workSession;
    }

    public boolean isTimerRunning() {
        return timer.isRunning();
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

    public void setWorkDuration(int seconds) {
        workDuration = Math.max(60, seconds);
        if (workSession && !timer.isRunning()) {
            timeLeft = workDuration;
            updateDisplay();
        }
    }

    public void setBreakDuration(int seconds) {
        breakDuration = Math.max(60, seconds);
        if (!workSession && !timer.isRunning()) {
            timeLeft = breakDuration;
            updateDisplay();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0xFFF5E4 | 0x1C000000, true));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 124, 124);
        g2.dispose();
    }

    private static class ModeButton extends JToggleButton {
        ModeButton(String label) {
            super(label);
            setFont(new Font("Times New Roman", Font.BOLD, 26));
            setPreferredSize(new Dimension(144, 48));
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            boolean active = isSelected();
            int arc = getHeight();
            if (active) {
                g2.setColor(CREAM);
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, arc, arc);
            } else {
                g2.setColor(CREAM);
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, arc, arc);
            }
            g2.setFont(getFont());
            g2.setColor(active ? BROWN : CREAM);
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    private static class StartButton extends JButton {
        StartButton() {
            super("Start");
            setFont(new Font("Times New Roman", Font.BOLD, 36));
            setForeground(BROWN);
            setPreferredSize(new Dimension(174, 56));
            setMinimumSize(new Dimension(174, 56));
            setMaximumSize(new Dimension(174, 56));
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(CREAM);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.setFont(getFont());
            g2.setColor(BROWN);
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }
}
