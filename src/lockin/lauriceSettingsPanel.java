package lockin;

import javax.swing.*;
import java.awt.*;

public class lauriceSettingsPanel extends JPanel {

    private JCheckBox soundCheckBox;
    private JCheckBox warningCheckBox;
    private JSpinner workSpinner;
    private JSpinner breakSpinner;

    public lauriceSettingsPanel() {
        createComponents();
        createLayout();
    }

    private void createComponents() {
        soundCheckBox = new JCheckBox("Sound ON", true);
        warningCheckBox = new JCheckBox("Warning ON", true);

        workSpinner = new JSpinner(
                new SpinnerNumberModel(25, 1, 180, 1)
        );

        breakSpinner = new JSpinner(
                new SpinnerNumberModel(5, 1, 60, 1)
        );
    }

    private void createLayout() {
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Sound:"), gbc);

        gbc.gridx = 1;
        add(soundCheckBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Warning:"), gbc);

        gbc.gridx = 1;
        add(warningCheckBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Work Duration (min):"), gbc);

        gbc.gridx = 1;
        add(workSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Break Duration (min):"), gbc);

        gbc.gridx = 1;
        add(breakSpinner, gbc);
    }

    public boolean isSoundOn() {
        return soundCheckBox.isSelected();
    }

    public boolean isWarningOn() {
        return warningCheckBox.isSelected();
    }

    public int getWorkMinutes() {
        return (Integer) workSpinner.getValue();
    }

    public int getBreakMinutes() {
        return (Integer) breakSpinner.getValue();
    }
}