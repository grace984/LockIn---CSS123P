package lockin;

import javax.swing.*;

public class TimerTest {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("LockIn Timer");

            frame.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE
            );

            frame.setSize(800, 500);
            frame.setLocationRelativeTo(null);

            TimerPanel timerPanel = new TimerPanel();

            frame.add(timerPanel);
            frame.setVisible(true);
        });
    }
}