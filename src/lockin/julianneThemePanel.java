package lockin;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class julianneThemePanel extends JPanel
        implements julianneThemeManager.ThemeChangeListener {


    // FIXED UI COLORS (same as the Tracker panel)

    private static final Color PANEL_BACKGROUND =
            new Color(0xF2E6DC);

    private static final Color BOX_BG =
            new Color(0xF2E6DC);

    private static final Color TITLE =
            new Color(0x4A0F2A);

    private static final Color ORNAMENT_LINE =
            new Color(0xB8, 0x9A, 0x6B);

    private static final Color MAROON =
            new Color(0x59132C);

    private static final Color WHITE =
            Color.WHITE;

    // THEME GRID

    private static final int COLUMNS = 4;

    // CHANGE THIS to make the circles bigger or smaller
    private static final int CIRCLE_SIZE = 64;

    // CHANGE THIS to space the circles out more or less
    // (space between two circles = GAP, on top of their size)
    private static final int GAP = 16;

    // width of the centered content column (same as the Tracker panel)
    private static final int COLUMN_W = 440;

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

    private long checkmarkStartedAt;

    private final Timer checkmarkTimer;

    // =========================================================
    // SMALL HELPERS
    // =========================================================

    // same font with extra letter spacing (matches the Tracker title)
    private static Font spaced(Font f, float tracking) {
        Map<TextAttribute, Object> attrs = new HashMap<>();
        attrs.put(TextAttribute.TRACKING, tracking);
        return f.deriveFont(attrs);
    }

    // gives a component a fixed height and centers it in the column
    private <T extends JComponent> T fixed(T c, int height) {
        c.setPreferredSize(new Dimension(COLUMN_W, height));
        c.setMaximumSize(new Dimension(COLUMN_W, height));
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
        return c;
    }

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public julianneThemePanel() {

        setBackground(
                PANEL_BACKGROUND
        );

        setOpaque(true);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // same padding as the Tracker panel
        setBorder(new EmptyBorder(24, 48, 24, 48));

        // pushes everything to the vertical middle
        add(Box.createVerticalGlue());

        // -----------------------------------------------------
        // HEADER (same as the Tracker panel)
        // -----------------------------------------------------

        JLabel title = new JLabel("Themes", SwingConstants.CENTER);
        title.setFont(spaced(new Font("Times New Roman", Font.BOLD, 32), 0.04f));
        title.setForeground(TITLE);
        add(fixed(title, 42));
        add(Box.createVerticalStrut(4));

        add(fixed(new OrnamentDivider(), 16));
        add(Box.createVerticalStrut(6));

        JLabel subtitle = new JLabel("Choose your gradient", SwingConstants.CENTER);
        subtitle.setFont(new Font("Times New Roman", Font.PLAIN, 15));
        subtitle.setForeground(TITLE);
        subtitle.setVerticalAlignment(SwingConstants.TOP);
        add(fixed(subtitle, 52));
        add(Box.createVerticalStrut(18));

        // -----------------------------------------------------
        // CREATE THE 4 x 4 GRID
        // -----------------------------------------------------

        JPanel gridPanel =
                new JPanel(
                        new GridBagLayout()
                );

        gridPanel.setOpaque(false);

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.weightx = 0.0;
        gbc.weighty = 0.0;

        gbc.anchor =
                GridBagConstraints.CENTER;

        gbc.fill =
                GridBagConstraints.NONE;

        gbc.insets =
                new Insets(
                        GAP / 2,
                        GAP / 2,
                        GAP / 2,
                        GAP / 2
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

        // rounded box around the grid (no shadow)
        RoundedPanel card = new RoundedPanel(BOX_BG, 14);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(8, 8, 8, 8));
        card.add(gridPanel, BorderLayout.CENTER);
        add(fixed(card, 374));
        add(Box.createVerticalStrut(20));

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

        add(fixed(bottomPanel, 52));

        add(Box.createVerticalGlue());

        // -----------------------------------------------------
        // CHECKMARK ANIMATION TIMER
        // -----------------------------------------------------

        checkmarkTimer =
                new Timer(
                        16,
                        e -> {

                            float rawProgress =
                                    Math.min(
                                            1.0f,
                                            (System.nanoTime()
                                                    - checkmarkStartedAt)
                                                    / 200_000_000.0f
                                    );

                            checkmarkProgress =
                                    rawProgress
                                            * rawProgress
                                            * (
                                                    3.0f
                                                    - 2.0f
                                                    * rawProgress
                                            );

                            if (
                                    rawProgress >=
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

        checkmarkStartedAt =
                System.nanoTime();

        if (
                !checkmarkTimer.isRunning()
        ) {

            checkmarkTimer.start();
        }

        repaint();
    }

    // =========================================================
    // BOX + ORNAMENT (same look as the Tracker panel)
    // =========================================================

    // plain rounded box, no shadow
    private static class RoundedPanel extends JPanel {
        private final Color color;
        private final int radius;

        RoundedPanel(Color color, int radius) {
            this.color = color;
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius * 2, radius * 2);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // thin line with a diamond in the middle, fading out at both ends
    private static class OrnamentDivider extends JComponent {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            Color clear = new Color(0xB8, 0x9A, 0x6B, 0);

            g2.setPaint(new GradientPaint(cx - 150, 0, clear, cx - 14, 0, ORNAMENT_LINE));
            g2.drawLine(cx - 150, cy, cx - 14, cy);
            g2.setPaint(new GradientPaint(cx + 14, 0, ORNAMENT_LINE, cx + 150, 0, clear));
            g2.drawLine(cx + 14, cy, cx + 150, cy);

            g2.setColor(TITLE);
            g2.fillPolygon(new int[]{cx, cx + 5, cx, cx - 5}, new int[]{cy - 5, cy, cy + 5, cy}, 4);
            g2.dispose();
        }
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

            if (hoverProgress > 0.0f) {
                g2.setColor(new Color(255, 255, 255,
                        (int) (18 * hoverProgress)));
                g2.fill(circle);
            }

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

                g2.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, scale));
                g2.setColor(WHITE);

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
 
 
 