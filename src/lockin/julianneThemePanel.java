package lockin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;

public class julianneThemePanel extends JPanel
        implements julianneThemeManager.ThemeChangeListener {

    // =========================================================
    // FIXED UI COLORS
    // =========================================================

    private static final Color PANEL_BACKGROUND =
            new Color(0xF7E9DF);

    private static final Color MAROON =
            new Color(0x59132C);

    private static final Color WHITE =
            Color.WHITE;

    // =========================================================
    // THEME GRID
    // =========================================================

    private static final int COLUMNS = 4;

    private static final int CIRCLE_SIZE = 80;

    // =========================================================
    // THEME CIRCLES
    // =========================================================

    private final ThemeCircle[] themeCircles =
            new ThemeCircle[
                    julianneThemeManager.getThemeCount()
            ];

    // =========================================================
    // SELECTED THEME
    // =========================================================

    private int selectedTheme =
            julianneThemeManager.getSelectedTheme();

    // =========================================================
    // CHECKMARK ANIMATION
    // =========================================================

    private float checkmarkProgress = 1.0f;

    private final Timer checkmarkTimer;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public julianneThemePanel() {

        setBackground(
                PANEL_BACKGROUND
        );

        setOpaque(true);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // -----------------------------------------------------
        // CREATE THE 4 x 4 GRID
        // -----------------------------------------------------

        JPanel gridPanel =
                new JPanel(
                        new GridBagLayout()
                );

        gridPanel.setOpaque(false);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.weightx = 0.0;
        gbc.weighty = 1.0;

        gbc.anchor =
                GridBagConstraints.CENTER;

        gbc.fill =
                GridBagConstraints.NONE;

        gbc.insets =
                new Insets(
                        9,
                        11,
                        9,
                        11
                );

        for (
                int i = 0;
                i < julianneThemeManager.getThemeCount();
                i++
        ) {

            int row =
                    i / COLUMNS;

            int column =
                    i % COLUMNS;

            ThemeCircle circle =
                    new ThemeCircle(i);

            themeCircles[i] =
                    circle;

            gbc.gridx =
                    column;

            gbc.gridy =
                    row;

            gridPanel.add(
                    circle,
                    gbc
            );
        }

        JLabel title = new JLabel("THEMES");
        title.setFont(new Font("Times New Roman", Font.PLAIN, 34));
        title.setForeground(MAROON);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Choose your gradient");
        subtitle.setFont(new Font("Times New Roman", Font.PLAIN, 20));
        subtitle.setForeground(MAROON);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(96, 0, 0, 0));
        headerPanel.setPreferredSize(new Dimension(471, 199));
        headerPanel.setMinimumSize(new Dimension(471, 199));
        headerPanel.setMaximumSize(new Dimension(471, 199));
        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(subtitle);

        gridPanel.setPreferredSize(new Dimension(471, 392));
        gridPanel.setMinimumSize(new Dimension(471, 392));
        gridPanel.setMaximumSize(new Dimension(471, 392));

        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        gridPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(headerPanel);
        add(gridPanel);

        // -----------------------------------------------------
        // RESET BUTTON
        // -----------------------------------------------------

        JButton resetButton =
                createResetButton();

        JPanel bottomPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER,
                                0,
                                10
                        )
                );

        bottomPanel.setOpaque(false);

        bottomPanel.add(
                resetButton
        );

        bottomPanel.setPreferredSize(new Dimension(471, 52));
        bottomPanel.setMinimumSize(new Dimension(471, 52));
        bottomPanel.setMaximumSize(new Dimension(471, 52));
        bottomPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(Box.createVerticalStrut(2));
        add(bottomPanel);
        add(Box.createVerticalGlue());

        // -----------------------------------------------------
        // CHECKMARK ANIMATION TIMER
        // -----------------------------------------------------

        checkmarkTimer =
                new Timer(
                        16,
                        e -> {

                            checkmarkProgress +=
                                    0.10f;

                            if (
                                    checkmarkProgress >=
                                    1.0f
                            ) {

                                checkmarkProgress =
                                        1.0f;

                                // FIX:
                                // Get the Timer from the event
                                // instead of referencing the
                                // field while it is initializing.
                                ((Timer) e.getSource()).stop();
                            }

                            repaint();
                        }
                );

        // -----------------------------------------------------
        // LISTEN FOR THEME CHANGES
        // -----------------------------------------------------

        julianneThemeManager.addListener(
                this
        );
    }

    // =========================================================
    // RESET BUTTON
    // =========================================================

    private JButton createResetButton() {

        JButton button =
                new JButton(
                        "[  Reset to Default  ]"
                );

        button.setFont(
                new Font(
                        "Times New Roman",
                        Font.PLAIN,
                        22
                )
        );

        button.setForeground(
                MAROON
        );

        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.setPreferredSize(
                new Dimension(
                        260,
                        42
                )
        );

        button.addActionListener(
                e ->
                        julianneThemeManager
                                .resetToDefault()
        );

        return button;
    }

    // =========================================================
    // THEME CHANGE CALLBACK
    // =========================================================

    @Override
    public void themeChanged(
            Color[] colors
    ) {

        selectedTheme =
                julianneThemeManager
                        .getSelectedTheme();

        checkmarkProgress =
                0.0f;

        if (
                !checkmarkTimer.isRunning()
        ) {

            checkmarkTimer.start();
        }

        repaint();
    }

    // =========================================================
    // THEME CIRCLE
    // =========================================================

    private class ThemeCircle
            extends JComponent {

        private final int themeIndex;

        private boolean hovered =
                false;

        private float hoverProgress =
                0.0f;

        private final Timer hoverTimer;

        // -----------------------------------------------------
        // CONSTRUCTOR
        // -----------------------------------------------------

        ThemeCircle(
                int themeIndex
        ) {

            this.themeIndex =
                    themeIndex;

            setPreferredSize(
                    new Dimension(
                            CIRCLE_SIZE,
                            CIRCLE_SIZE
                    )
            );

            setMinimumSize(
                    new Dimension(
                            CIRCLE_SIZE,
                            CIRCLE_SIZE
                    )
            );

            setMaximumSize(
                    new Dimension(
                            CIRCLE_SIZE,
                            CIRCLE_SIZE
                    )
            );

            setOpaque(false);

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );

            // -------------------------------------------------
            // HOVER ANIMATION
            // -------------------------------------------------

            hoverTimer =
                    new Timer(
                            16,
                            e -> {

                                float target =
                                        hovered
                                                ? 1.0f
                                                : 0.0f;

                                float difference =
                                        target
                                        - hoverProgress;

                                if (
                                        Math.abs(
                                                difference
                                        ) < 0.08f
                                ) {

                                    hoverProgress =
                                            target;

                                    // FIX:
                                    // Don't reference hoverTimer
                                    // during its own initialization.
                                    ((Timer) e.getSource()).stop();

                                } else {

                                    hoverProgress +=
                                            difference
                                            * 0.25f;
                                }

                                repaint();
                            }
                    );

            // -------------------------------------------------
            // MOUSE EVENTS
            // -------------------------------------------------

            addMouseListener(
                    new MouseAdapter() {

                        @Override
                        public void mouseEntered(
                                MouseEvent e
                        ) {

                            hovered =
                                    true;

                            if (
                                    !hoverTimer.isRunning()
                            ) {

                                hoverTimer.start();
                            }
                        }

                        @Override
                        public void mouseExited(
                                MouseEvent e
                        ) {

                            hovered =
                                    false;

                            if (
                                    !hoverTimer.isRunning()
                            ) {

                                hoverTimer.start();
                            }
                        }

                        @Override
                        public void mouseClicked(
                                MouseEvent e
                        ) {

                            if (
                                    SwingUtilities
                                            .isLeftMouseButton(e)
                            ) {

                                julianneThemeManager
                                        .selectTheme(
                                                themeIndex
                                        );
                            }
                        }
                    }
            );
        }

        // -----------------------------------------------------
        // PAINT CIRCLE
        // -----------------------------------------------------

        @Override
        protected void paintComponent(
                Graphics g
        ) {

            super.paintComponent(g);

            Graphics2D g2 =
                    (Graphics2D)
                            g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );

            // -------------------------------------------------
            // GET THEME COLORS
            // -------------------------------------------------

            Color[] colors =
                    julianneThemeManager
                            .getTheme(
                                    themeIndex
                            );

            // -------------------------------------------------
            // CIRCLE SIZE
            // -------------------------------------------------

            int size =
                    (int) (
                            CIRCLE_SIZE
                            - 6
                            + (
                                    hoverProgress
                                    * 4
                            )
                    );

            int x =
                    (
                            getWidth()
                            - size
                    ) / 2;

            int y =
                    (
                            getHeight()
                            - size
                    ) / 2;

            // -------------------------------------------------
            // CREATE GRADIENT
            // -------------------------------------------------

            Paint gradient;

            if (colors.length == 3) {

                float[] fractions = {
                        0.0f,
                        0.5f,
                        1.0f
                };

                gradient =
                        new LinearGradientPaint(
                                0,
                                0,
                                size,
                                size,
                                fractions,
                                colors
                        );

            } else {

                float[] fractions = {
                        0.0f,
                        0.33f,
                        0.66f,
                        1.0f
                };

                gradient =
                        new LinearGradientPaint(
                                0,
                                0,
                                size,
                                size,
                                fractions,
                                colors
                        );
            }

            // -------------------------------------------------
            // CIRCLE
            // -------------------------------------------------

            Ellipse2D circle =
                    new Ellipse2D.Double(
                            x,
                            y,
                            size,
                            size
                    );

            g2.setPaint(
                    gradient
            );

            g2.fill(
                    circle
            );

            // -------------------------------------------------
            // HOVER BORDER
            // -------------------------------------------------

            if (
                    hoverProgress > 0.0f
            ) {

                int alpha =
                        (int) (
                                60
                                + (
                                        100
                                        * hoverProgress
                                )
                        );

                g2.setColor(
                        new Color(
                                MAROON.getRed(),
                                MAROON.getGreen(),
                                MAROON.getBlue(),
                                alpha
                        )
                );

                g2.setStroke(
                        new BasicStroke(
                                2.0f
                        )
                );

                g2.draw(
                        circle
                );
            }

            // -------------------------------------------------
            // SELECTED CHECKMARK
            // -------------------------------------------------

            if (
                    themeIndex ==
                    selectedTheme
            ) {

                float scale =
                        Math.min(
                                1.0f,
                                checkmarkProgress
                        );

                int centerX =
                        getWidth() / 2;

                int centerY =
                        getHeight() / 2;

                int checkWidth =
                        (int) (
                                28
                                * scale
                        );

                int checkHeight =
                        (int) (
                                20
                                * scale
                        );

                int startX =
                        centerX
                        - checkWidth / 2;

                int startY =
                        centerY
                        - checkHeight / 2;

                int x1 =
                        startX;

                int y1 =
                        startY
                        + checkHeight / 2;

                int x2 =
                        startX
                        + checkWidth / 3;

                int y2 =
                        startY
                        + checkHeight;

                int x3 =
                        startX
                        + checkWidth;

                int y3 =
                        startY;

                g2.setColor(
                        WHITE
                );

                g2.setStroke(
                        new BasicStroke(
                                4.0f,
                                BasicStroke.CAP_ROUND,
                                BasicStroke.JOIN_ROUND
                        )
                );

                g2.drawLine(
                        x1,
                        y1,
                        x2,
                        y2
                );

                g2.drawLine(
                        x2,
                        y2,
                        x3,
                        y3
                );
            }

            g2.dispose();
        }
    }
}
