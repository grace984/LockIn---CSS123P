package lockin;

import javax.swing.*;
import java.awt.*;

public class TimerPanel extends JPanel {

    private int workMinutes = 25;
    private int breakMinutes = 5;

    private int timeLeft;
    private TimerState currentState = TimerState.WORK;
    private boolean timerRunning = false;

    private JLabel workTimeLabel;
    private JLabel breakTimeLabel;
    private JLabel timerLabel;
    private JLabel stateLabel;

    private JButton workMinusButton;
    private JButton workPlusButton;
    private JButton workDoneButton;

    private JButton breakMinusButton;
    private JButton breakPlusButton;
    private JButton breakDoneButton;

    private JButton workModeButton;
    private JButton breakModeButton;

    private JButton startButton;
    private JButton pauseButton;
    private JButton resetButton;
    private JButton skipButton;

    private Timer timer;

    public TimerPanel() {
        timeLeft = workMinutes * 60;

        createComponents();
        createLayout();
        createTimer();
        createButtonActions();

        updateDisplay();
    }

    private void createComponents() {
        workTimeLabel = new JLabel("25:00");
        breakTimeLabel = new JLabel("05:00");
        timerLabel = new JLabel("25:00");
        stateLabel = new JLabel("WORK");

        workMinusButton = new JButton("-");
        workPlusButton = new JButton("+");
        workDoneButton = new JButton("Done");

        breakMinusButton = new JButton("-");
        breakPlusButton = new JButton("+");
        breakDoneButton = new JButton("Done");

        workModeButton = new JButton("Work");
        breakModeButton = new JButton("Break");

        startButton = new JButton("Start");
        pauseButton = new JButton("Pause");
        resetButton = new JButton("Reset");
        skipButton = new JButton("Skip");
    }

    private void createLayout() {
        setLayout(new BorderLayout(20, 20));

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(
                new BoxLayout(settingsPanel, BoxLayout.Y_AXIS)
        );

        JLabel workLabel = new JLabel("Work");

        JPanel workPanel = new JPanel();
        workPanel.add(workMinusButton);
        workPanel.add(workTimeLabel);
        workPanel.add(workPlusButton);

        JPanel workDonePanel = new JPanel();
        workDonePanel.add(workDoneButton);

        JLabel breakLabel = new JLabel("Break");

        JPanel breakPanel = new JPanel();
        breakPanel.add(breakMinusButton);
        breakPanel.add(breakTimeLabel);
        breakPanel.add(breakPlusButton);

        JPanel breakDonePanel = new JPanel();
        breakDonePanel.add(breakDoneButton);

        settingsPanel.add(workLabel);
        settingsPanel.add(workPanel);
        settingsPanel.add(workDonePanel);

        settingsPanel.add(
                Box.createVerticalStrut(20)
        );

        settingsPanel.add(breakLabel);
        settingsPanel.add(breakPanel);
        settingsPanel.add(breakDonePanel);

        add(settingsPanel, BorderLayout.WEST);

        JPanel timerPanel = new JPanel();

        timerPanel.setLayout(
                new BoxLayout(
                        timerPanel,
                        BoxLayout.Y_AXIS
                )
        );

        JPanel modePanel = new JPanel();
        modePanel.add(workModeButton);
        modePanel.add(breakModeButton);

        JPanel displayPanel = new JPanel();
        displayPanel.add(stateLabel);
        displayPanel.add(timerLabel);

        JPanel controlPanel = new JPanel();
        controlPanel.add(startButton);
        controlPanel.add(pauseButton);
        controlPanel.add(resetButton);
        controlPanel.add(skipButton);

        timerPanel.add(modePanel);
        timerPanel.add(displayPanel);
        timerPanel.add(controlPanel);

        add(timerPanel, BorderLayout.CENTER);
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

        workMinusButton.addActionListener(e -> {

            if (workMinutes > 1) {
                workMinutes--;

                workTimeLabel.setText(
                        String.format(
                                "%02d:00",
                                workMinutes
                        )
                );
            }
        });

        workPlusButton.addActionListener(e -> {

            if (workMinutes < 180) {
                workMinutes++;

                workTimeLabel.setText(
                        String.format(
                                "%02d:00",
                                workMinutes
                        )
                );
            }
        });

        workDoneButton.addActionListener(e -> {

            if (!timerRunning) {
                timeLeft = workMinutes * 60;

                if (currentState == TimerState.WORK) {
                    updateDisplay();
                }
            }
        });

        breakMinusButton.addActionListener(e -> {

            if (breakMinutes > 1) {
                breakMinutes--;

                breakTimeLabel.setText(
                        String.format(
                                "%02d:00",
                                breakMinutes
                        )
                );
            }
        });

        breakPlusButton.addActionListener(e -> {

            if (breakMinutes < 60) {
                breakMinutes++;

                breakTimeLabel.setText(
                        String.format(
                                "%02d:00",
                                breakMinutes
                        )
                );
            }
        });

        breakDoneButton.addActionListener(e -> {

            if (!timerRunning) {
                timeLeft = breakMinutes * 60;

                if (currentState == TimerState.BREAK) {
                    updateDisplay();
                }
            }
        });

        workModeButton.addActionListener(e -> {

            if (!timerRunning) {
                currentState = TimerState.WORK;
                timeLeft = workMinutes * 60;
                updateDisplay();
            }
        });

        breakModeButton.addActionListener(e -> {

            if (!timerRunning) {
                currentState = TimerState.BREAK;
                timeLeft = breakMinutes * 60;
                updateDisplay();
            }
        });

        startButton.addActionListener(e -> {
            startTimer();
        });

        pauseButton.addActionListener(e -> {
            pauseTimer();
        });

        resetButton.addActionListener(e -> {
            resetTimer();
        });

        skipButton.addActionListener(e -> {
            skipSession();
        });
    }

    public void startTimer() {
        timer.start();
        timerRunning = true;
    }

    public void pauseTimer() {
        timer.stop();
        timerRunning = false;
    }

    public void resetTimer() {
        timer.stop();
        timerRunning = false;

        if (currentState == TimerState.WORK) {
            timeLeft = workMinutes * 60;
        } else {
            timeLeft = breakMinutes * 60;
        }

        updateDisplay();
    }

    public void skipSession() {
        switchSession();
    }

    private void switchSession() {
        timer.stop();
        timerRunning = false;

        if (currentState == TimerState.WORK) {
            currentState = TimerState.BREAK;
            timeLeft = breakMinutes * 60;
        } else {
            currentState = TimerState.WORK;
            timeLeft = workMinutes * 60;
        }

        updateDisplay();
    }

    private void updateDisplay() {
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;

        timerLabel.setText(
                String.format(
                        "%02d:%02d",
                        minutes,
                        seconds
                )
        );

        stateLabel.setText(
                currentState.toString()
        );
    }

    public TimerState getTimerState() {
        return currentState;
    }

    public boolean isWorkSession() {
        return currentState == TimerState.WORK;
    }

    public boolean isBreakSession() {
        return currentState == TimerState.BREAK;
    }

    public boolean isTimerRunning() {
        return timerRunning;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public int getWorkDuration() {
        return workMinutes * 60;
    }

    public int getBreakDuration() {
        return breakMinutes * 60;
    }
}