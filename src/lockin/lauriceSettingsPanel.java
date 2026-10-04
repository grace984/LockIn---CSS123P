package lockin;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class lauriceSettingsPanel extends JPanel {

    // FIXED UI COLORS
    private static final Color PANEL_BACKGROUND = new Color(0xF2E6DC);
    private static final Color BOX_BG = new Color(0xE0CEB3);
    private static final Color TITLE = new Color(0x4A0F2A);
    private static final Color ORNAMENT_LINE = new Color(0xB8, 0x9A, 0x6B);
    private static final Color ROW_HOVER = new Color(0xEBDFC9);

    private static final int COLUMN_W = 440;
    private static final int ROW_H = 36;

    private static final String PRIVACY_TEXT =
            "LockIn only monitors websites added to your distraction tracker during "
            + "active Work sessions. No account or personal information is required. "
            + "Your settings and preferences are stored locally on your device.";

    // =========================================================
    // SAVED SETTINGS
    // =========================================================

    private static final Preferences SETTINGS =
            Preferences.userNodeForPackage(lauriceSettingsPanel.class);

    private static final String SOUND_KEY = "selectedSound";
    private static final int DEFAULT_SOUND = 2;

    private OptionButton[] soundButtons;
    private OptionButton[] warningButtons;

    public lauriceSettingsPanel() {
        createComponents();
        createLayout();
    }

    // =========================================================
    // COMPONENTS
    // =========================================================

    private void createComponents() {

        // TIMER SOUND
        soundButtons = new OptionButton[]{
                new OptionButton("Sound 1"),
                new OptionButton("Sound 2")
        };

        int savedSound = getSavedSound();

        if (savedSound == 1) {
            soundButtons[0].setSelected(true);
        } else {
            soundButtons[1].setSelected(true);
        }

        ButtonGroup soundGroup = new ButtonGroup();

        for (int i = 0; i < soundButtons.length; i++) {

            final int soundNumber = i + 1;

            soundGroup.add(soundButtons[i]);

            soundButtons[i].addActionListener(
                    e -> saveSelectedSound(soundNumber)
            );
        }

        // TRACKER WARNING MESSAGE
        warningButtons = new OptionButton[]{
                new OptionButton("Version 1"),
                new OptionButton("Version 2"),
                new OptionButton("Version 3")
        };

        warningButtons[0].setSelected(true);

        ButtonGroup warningGroup = new ButtonGroup();

        for (OptionButton b : warningButtons) {
            warningGroup.add(b);
        }
    }

    // =========================================================
    // SOUND SETTINGS
    // =========================================================

    private int getSavedSound() {

        int sound = SETTINGS.getInt(
                SOUND_KEY,
                DEFAULT_SOUND
        );

        if (sound != 1 && sound != 2) {
            return DEFAULT_SOUND;
        }

        return sound;
    }

    private void saveSelectedSound(int sound) {

        if (sound == 1 || sound == 2) {

            SETTINGS.putInt(
                    SOUND_KEY,
                    sound
            );
        }
    }

    // =========================================================
    // PUBLIC SOUND PREFERENCE
    // Used by jhenicaApiServer
    // =========================================================

    public static int getSavedSoundPreference() {
        return SETTINGS.getInt(
                SOUND_KEY,
                DEFAULT_SOUND
        );
    }

    // =========================================================
    // LAYOUT
    // =========================================================

    private void createLayout() {

        setBackground(PANEL_BACKGROUND);
        setOpaque(true);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setBorder(new EmptyBorder(24, 48, 24, 48));

        add(Box.createVerticalGlue());

        JLabel title = new JLabel("Settings", SwingConstants.CENTER);

        title.setFont(
                spaced(
                        new Font(
                                "Times New Roman",
                                Font.BOLD,
                                32
                        ),
                        0.04f
                )
        );

        title.setForeground(TITLE);

        add(fixed(title, 42));

        add(Box.createVerticalStrut(4));

        add(fixed(new OrnamentDivider(), 16));

        add(Box.createVerticalStrut(6));

        JLabel subtitle = new JLabel(
                "Customize your LockIn settings",
                SwingConstants.CENTER
        );

        subtitle.setFont(
                new Font(
                        "Times New Roman",
                        Font.PLAIN,
                        15
                )
        );

        subtitle.setForeground(TITLE);
        subtitle.setVerticalAlignment(SwingConstants.TOP);

        add(fixed(subtitle, 52));

        add(Box.createVerticalStrut(18));

        add(
                fixed(
                        sectionLabel("TIMER SOUND"),
                        24
                )
        );

        add(Box.createVerticalStrut(6));

        add(buildOptionCard(soundButtons));

        add(Box.createVerticalStrut(16));

        add(
                fixed(
                        sectionLabel(
                                "TRACKER WARNING MESSAGE"
                        ),
                        24
                )
        );

        add(Box.createVerticalStrut(6));

        add(buildOptionCard(warningButtons));

        add(Box.createVerticalStrut(16));

        add(
                fixed(
                        sectionLabel("PRIVACY STATEMENT"),
                        24
                )
        );

        add(Box.createVerticalStrut(6));

        add(buildPrivacyCard());

        add(Box.createVerticalGlue());
    }

    private JLabel sectionLabel(String text) {

        JLabel l = new JLabel(text);

        l.setFont(
                spaced(
                        new Font(
                                "Times New Roman",
                                Font.BOLD,
                                13
                        ),
                        0.18f
                )
        );

        l.setForeground(TITLE);

        l.setBorder(
                new EmptyBorder(
                        0,
                        4,
                        0,
                        4
                )
        );

        return l;
    }

    private JPanel buildOptionCard(
            OptionButton[] buttons
    ) {

        RoundedPanel card =
                new RoundedPanel(
                        BOX_BG,
                        14
                );

        card.setLayout(
                new BoxLayout(
                        card,
                        BoxLayout.Y_AXIS
                )
        );

        card.setBorder(
                new EmptyBorder(
                        8,
                        8,
                        8,
                        8
                )
        );

        for (OptionButton b : buttons) {

            b.setAlignmentX(
                    Component.LEFT_ALIGNMENT
            );

            b.setPreferredSize(
                    new Dimension(
                            100,
                            ROW_H
                    )
            );

            b.setMaximumSize(
                    new Dimension(
                            Integer.MAX_VALUE,
                            ROW_H
                    )
            );

            card.add(b);
        }

        return fixed(
                card,
                buttons.length * ROW_H + 16
        );
    }

    private JPanel buildPrivacyCard() {

        RoundedPanel card =
                new RoundedPanel(
                        BOX_BG,
                        14
                );

        card.setLayout(
                new BorderLayout()
        );

        card.setBorder(
                new EmptyBorder(
                        14,
                        18,
                        14,
                        18
                )
        );

        card.add(
                new WrappedText(
                        PRIVACY_TEXT,
                        new Font(
                                "Times New Roman",
                                Font.ITALIC,
                                15
                        ),
                        TITLE
                ),
                BorderLayout.CENTER
        );

        return fixed(card, 112);
    }

    // =========================================================
    // SMALL HELPERS
    // =========================================================

    private static Font spaced(
            Font f,
            float tracking
    ) {

        Map<TextAttribute, Object> attrs =
                new HashMap<>();

        attrs.put(
                TextAttribute.TRACKING,
                tracking
        );

        return f.deriveFont(attrs);
    }

    private <T extends JComponent> T fixed(
            T c,
            int height
    ) {

        c.setPreferredSize(
                new Dimension(
                        COLUMN_W,
                        height
                )
        );

        c.setMaximumSize(
                new Dimension(
                        COLUMN_W,
                        height
                )
        );

        c.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        return c;
    }

    // =========================================================
    // WHAT THE REST OF THE APP READS
    // =========================================================

    public int getSelectedSound() {

        for (
                int i = 0;
                i < soundButtons.length;
                i++
        ) {

            if (
                    soundButtons[i].isSelected()
            ) {

                return i + 1;
            }
        }

        return getSavedSound();
    }

    public int getWarningVersion() {

        for (
                int i = 0;
                i < warningButtons.length;
                i++
        ) {

            if (
                    warningButtons[i].isSelected()
            ) {

                return i + 1;
            }
        }

        return 1;
    }

    // =========================================================
    // OLD METHODS
    // =========================================================

    public boolean isSoundOn() {
        return true;
    }

    public boolean isWarningOn() {
        return true;
    }

    public int getWorkMinutes() {
        return 25;
    }

    public int getBreakMinutes() {
        return 5;
    }

    // =========================================================
    // RADIO OPTION
    // =========================================================

    private static class OptionButton
            extends JRadioButton {

        OptionButton(String text) {

            super(text);

            setFont(
                    new Font(
                            "Times New Roman",
                            Font.PLAIN,
                            17
                    )
            );

            setForeground(TITLE);

            setIcon(new RadioIcon());

            setIconTextGap(10);

            setOpaque(false);

            setContentAreaFilled(false);

            setBorderPainted(false);

            setFocusPainted(false);

            setRolloverEnabled(true);

            setBorder(
                    new EmptyBorder(
                            0,
                            16,
                            0,
                            0
                    )
            );

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );
        }

        @Override
        protected void paintComponent(
                Graphics g
        ) {

            if (
                    getModel().isRollover()
            ) {

                Graphics2D g2 =
                        (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setColor(ROW_HOVER);

                g2.fillRoundRect(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        12,
                        12
                );

                g2.dispose();
            }

            super.paintComponent(g);
        }
    }

    // =========================================================
    // RADIO ICON
    // =========================================================

    private static class RadioIcon
            implements Icon {

        public int getIconWidth() {
            return 18;
        }

        public int getIconHeight() {
            return 18;
        }

        public void paintIcon(
                Component c,
                Graphics g,
                int x,
                int y
        ) {

            AbstractButton b =
                    (AbstractButton) c;

            boolean selected =
                    b.isSelected();

            boolean hot =
                    b.getModel().isRollover();

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(TITLE);

            if (selected) {

                g2.fillOval(
                        x + 1,
                        y + 1,
                        16,
                        16
                );

            } else {

                g2.setStroke(
                        new BasicStroke(
                                hot
                                        ? 2.2f
                                        : 1.6f
                        )
                );

                g2.drawOval(
                        x + 2,
                        y + 2,
                        14,
                        14
                );
            }

            g2.dispose();
        }
    }

    // =========================================================
    // ROUNDED PANEL
    // =========================================================

    private static class RoundedPanel
            extends JPanel {

        private final Color color;
        private final int radius;

        RoundedPanel(
                Color color,
                int radius
        ) {

            this.color = color;
            this.radius = radius;

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

            g2.setColor(color);

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    radius * 2,
                    radius * 2
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // =========================================================
    // WRAPPED TEXT
    // =========================================================

    private static class WrappedText
            extends JComponent {

        private final String text;

        WrappedText(
                String text,
                Font font,
                Color color
        ) {

            this.text = text;

            setFont(font);

            setForeground(color);
        }

        @Override
        protected void paintComponent(
                Graphics g
        ) {

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON
            );

            g2.setFont(getFont());

            g2.setColor(getForeground());

            FontMetrics fm =
                    g2.getFontMetrics();

            int y = fm.getAscent();

            StringBuilder line =
                    new StringBuilder();

            for (
                    String word :
                    text.split(" ")
            ) {

                String test =
                        line.length() == 0
                                ? word
                                : line + " " + word;

                if (
                        line.length() > 0
                                && fm.stringWidth(test)
                                > getWidth()
                ) {

                    g2.drawString(
                            line.toString(),
                            0,
                            y
                    );

                    y += fm.getHeight();

                    line =
                            new StringBuilder(word);

                } else {

                    line =
                            new StringBuilder(test);
                }
            }

            if (line.length() > 0) {

                g2.drawString(
                        line.toString(),
                        0,
                        y
                );
            }

            g2.dispose();
        }
    }

    // =========================================================
    // ORNAMENT DIVIDER
    // =========================================================

    private static class OrnamentDivider
            extends JComponent {

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

            int cx =
                    getWidth() / 2;

            int cy =
                    getHeight() / 2;

            Color clear =
                    new Color(
                            0xB8,
                            0x9A,
                            0x6B,
                            0
                    );

            g2.setPaint(
                    new GradientPaint(
                            cx - 150,
                            0,
                            clear,
                            cx - 14,
                            0,
                            ORNAMENT_LINE
                    )
            );

            g2.drawLine(
                    cx - 150,
                    cy,
                    cx - 14,
                    cy
            );

            g2.setPaint(
                    new GradientPaint(
                            cx + 14,
                            0,
                            ORNAMENT_LINE,
                            cx + 150,
                            0,
                            clear
                    )
            );

            g2.drawLine(
                    cx + 14,
                    cy,
                    cx + 150,
                    cy
            );

            g2.setColor(TITLE);

            g2.fillPolygon(
                    new int[]{
                            cx,
                            cx + 5,
                            cx,
                            cx - 5
                    },
                    new int[]{
                            cy - 5,
                            cy,
                            cy + 5,
                            cy
                    },
                    4
            );

            g2.dispose();
        }
    }
}