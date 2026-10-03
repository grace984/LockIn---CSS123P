package lockin;

import javax.swing.*;
import java.awt.*;

public class lauriceTimerPanel extends JPanel {

    private int workDuration = 25 * 60;
    private int breakDuration = 5 * 60;
    private int timeLeft = workDuration;

    private boolean workSession = true;

    private JLabel stateLabel;
    private JLabel timerLabel;

    private JButton startButton;
    private JButton pauseButton;
    private JButton resetButton;
    private JButton skipButton;

    private Timer timer;

    public lauriceTimerPanel() {
        createComponents();
        createLayout();
        createTimer();
        createButtonActions();
        updateDisplay();
    }

    private void createComponents() {
        stateLabel = new JLabel("WORK");
        stateLabel.setFont(new Font("Arial", Font.BOLD, 28));
        stateLabel.setHorizontalAlignment(SwingConstants.CENTER);

        timerLabel = new JLabel("25:00");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 70));
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        startButton = new JButton("START");
        pauseButton = new JButton("PAUSE");
        resetButton = new JButton("RESET");
        skipButton = new JButton("SKIP");
    }

    private void createLayout() {
        setLayout(new BorderLayout(20, 20));

        setBorder(
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        );

        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(
                new BoxLayout(displayPanel, BoxLayout.Y_AXIS)
        );

        stateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        displayPanel.add(Box.createVerticalGlue());
        displayPanel.add(stateLabel);
        displayPanel.add(Box.createVerticalStrut(20));
        displayPanel.add(timerLabel);
        displayPanel.add(Box.createVerticalGlue());

        add(displayPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        buttonPanel.add(startButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(skipButton);

        add(buttonPanel, BorderLayout.SOUTH);
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
        startButton.addActionListener(e -> startTimer());
        pauseButton.addActionListener(e -> pauseTimer());
        resetButton.addActionListener(e -> resetTimer());
        skipButton.addActionListener(e -> skipSession());
    }

    public void startTimer() {
        timer.start();
    }

    public void pauseTimer() {
        timer.stop();
    }

    public void resetTimer() {
        timer.stop();
        workSession = true;
        timeLeft = workDuration;
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
        } else {
            workSession = true;
            timeLeft = workDuration;
        }

        updateDisplay();
    }

    private void updateDisplay() {
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;

        timerLabel.setText(
                String.format("%02d:%02d", minutes, seconds)
        );

        stateLabel.setText(
                workSession ? "WORK" : "BREAK"
        );
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
}