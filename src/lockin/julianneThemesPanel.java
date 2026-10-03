package lockin;

public class julianneThemesPanel {
}package lockin;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JPanel;
import javax.swing.Timer;

public class julianneThemesPanel extends JPanel
        implements julianneThemeManager.ThemeChangeListener {

    // ============================================================
    // FIXED COLORS
    // ============================================================

    private static final Color CREAM =
            Color.decode("#FFF5E4");

    private static final Color BURGUNDY =
            Color.decode("#59132C");

    // ============================================================
    // REFERENCE DIMENSIONS
    // ============================================================

    private static final int PANEL_WIDTH = 486;

    private static final int CIRCLE_SIZE = 80;

    // 4 columns.
    private static final int[] CIRCLE_X = {
        91,
        193,
        295,
        398
    };

    // 4 rows.
    private static final int[] CIRCLE_Y = {
        206,
        306,
        405,
        505
    };

    // ============================================================
    // STATE
    // ============================================================

    private int selectedTheme;

    private int hoveredTheme = -1;

    private final float[] hoverScales = new float[16];

    private AnimationUtils.Animation hoverAnimation;

    private AnimationUtils.Animation entranceAnimation;

    private float contentAlpha = 1.0f;

    // ============================================================
    // CHECKMARK ANIMATION
    // ============================================================

    private float checkAlpha = 1.0f;

    private float checkScale = 1.0f;

    private Timer checkAnimation;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public julianneThemesPanel() {

        setOpaque(true);

        setBackground(CREAM);

        setPreferredSize(
                new Dimension(
                        PANEL_WIDTH,
                        810
                )
        );

        setMinimumSize(
                new Dimension(
                        PANEL_WIDTH,
                        1
                )
        );

        selectedTheme =
                julianneThemeManager
                        .getSelectedTheme();

        for (int i = 0; i < hoverScales.length; i++) {
            hoverScales[i] = 1.0f;
        }

        julianneThemeManager
                .addThemeChangeListener(this);

        setupMouseListeners();
    }

    // ============================================================
    // CLEANUP
    // ============================================================

    public void dispose() {

        julianneThemeManager
                .removeThemeChangeListener(this);

        if (checkAnimation != null) {
            checkAnimation.stop();
        }
        if (hoverAnimation != null) {
            hoverAnimation.stop();
        }
        if (entranceAnimation != null) {
            entranceAnimation.stop();
        }
    }

    public void playEntranceAnimation() {
        if (entranceAnimation != null) {
            entranceAnimation.stop();
        }

        contentAlpha = 0.0f;
        entranceAnimation = AnimationUtils.animate(
                240,
                AnimationUtils::easeInOut,
                progress -> {
                    contentAlpha = (float) progress;
                    repaint();
                },
                () -> {
                    contentAlpha = 1.0f;
                    repaint();
                });
    }

    // ============================================================
    // MOUSE
    // ============================================================

    private void setupMouseListeners() {

        addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mousePressed(
                            MouseEvent e
                    ) {

                        int theme =
                                getThemeAt(
                                        e.getX(),
                                        e.getY()
                                );

                        if (theme >= 0) {

                            julianneThemeManager
                                    .setTheme(theme);

                            return;
                        }

                        if (isResetButton(
                                e.getX(),
                                e.getY()
                        )) {

                            julianneThemeManager
                                    .resetToDefault();
                        }
                    }

                    @Override
                    public void mouseExited(
                            MouseEvent e
                    ) {

                        setHoveredTheme(-1);
                    }
                }
        );

        addMouseMotionListener(
                new MouseMotionAdapter() {

                    @Override
                    public void mouseMoved(
                            MouseEvent e
                    ) {

                        int newHover =
                                getThemeAt(
                                        e.getX(),
                                        e.getY()
                                );

                        setHoveredTheme(newHover);
                    }
                }
        );
    }

    // ============================================================
    // THEME CHANGED
    // ============================================================

    @Override
    public void themeChanged(
            Color[] colors
    ) {

        int newTheme =
                julianneThemeManager
                        .getSelectedTheme();

        if (newTheme != selectedTheme) {

            selectedTheme =
                    newTheme;

            startCheckmarkAnimation();
        }

        repaint();
    }

    // ============================================================
    // CHECKMARK ANIMATION
    // ============================================================

    private void startCheckmarkAnimation() {

        if (checkAnimation != null &&
                checkAnimation.isRunning()) {

            checkAnimation.stop();
        }

        checkAlpha = 0.0f;
        checkScale = 0.72f;

        final long start =
                System.currentTimeMillis();

        checkAnimation =
                new Timer(
                        15,
                        e -> {

                            long elapsed =
                                    System.currentTimeMillis()
                                            - start;

                            float progress =
                                    Math.min(
                                            1.0f,
                                            elapsed / 200.0f
                                    );

                            float eased =
                                    progress *
                                    progress *
                                    (3.0f -
                                    2.0f *
                                    progress);

                            checkAlpha =
                                    eased;

                            checkScale =
                                    0.72f +
                                    0.28f * eased;

                            repaint();

                            if (progress >= 1.0f) {

                                checkAlpha = 1.0f;
                                checkScale = 1.0f;

                                ((Timer) e.getSource())
                                        .stop();
                            }
                        }
                );

        checkAnimation.start();
    }

    // ============================================================
    // PAINT
    // ============================================================

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

        g2.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );

        g2.setComposite(
                AlphaComposite.SrcOver.derive(contentAlpha)
        );

        paintTitle(g2);

        paintThemeGrid(g2);

        paintResetButton(g2);

        g2.dispose();
    }

    // ============================================================
    // TITLE
    // ============================================================

    private void paintTitle(
            Graphics2D g2
    ) {

        String title =
                "T H E M E S";

        g2.setFont(
                new Font(
                        "Times New Roman",
                        Font.BOLD,
                        34
                )
        );

        g2.setColor(BURGUNDY);

        int textWidth =
                g2.getFontMetrics()
                        .stringWidth(title);

        int x =
                (getWidth() -
                        textWidth) / 2;

        g2.drawString(
                title,
                x,
                93
        );
    }

    // ============================================================
    // GRID
    // ============================================================

    private void paintThemeGrid(
            Graphics2D g2
    ) {

        for (int i = 0;
                i < 16;
                i++) {

            int row = i / 4;
            int column = i % 4;

            int x =
                    CIRCLE_X[column];

            int y =
                    CIRCLE_Y[row];

            boolean hovered =
                    hoveredTheme == i;

            double centerX = x + CIRCLE_SIZE / 2.0;
            double centerY = y + CIRCLE_SIZE / 2.0;
            Graphics2D circleGraphics = (Graphics2D) g2.create();
            circleGraphics.translate(centerX, centerY);
            circleGraphics.scale(hoverScales[i], hoverScales[i]);
            circleGraphics.translate(-centerX, -centerY);

            paintGradientCircle(
                    circleGraphics,
                    i,
                    x,
                    y,
                    CIRCLE_SIZE
            );

            if (selectedTheme == i) {

                paintCheckmark(
                        circleGraphics,
                        x,
                        y,
                        CIRCLE_SIZE
                );
            }

            if (hovered) {

                paintHoverBorder(
                        circleGraphics,
                        x,
                        y,
                        CIRCLE_SIZE
                );
            }

            circleGraphics.dispose();
        }
    }

    private void setHoveredTheme(int newHover) {
        if (newHover == hoveredTheme) {
            return;
        }

        hoveredTheme = newHover;
        if (hoverAnimation != null) {
            hoverAnimation.stop();
        }

        float[] startScales = hoverScales.clone();
        float[] targetScales = new float[hoverScales.length];
        for (int i = 0; i < targetScales.length; i++) {
            targetScales[i] = i == hoveredTheme ? 1.06f : 1.0f;
        }

        hoverAnimation = AnimationUtils.animate(
                180,
                AnimationUtils::easeInOut,
                progress -> {
                    for (int i = 0; i < hoverScales.length; i++) {
                        hoverScales[i] = (float) AnimationUtils.interpolate(
                                startScales[i], targetScales[i], progress);
                    }
                    repaint();
                },
                () -> {
                    System.arraycopy(targetScales, 0, hoverScales, 0, hoverScales.length);
                    repaint();
                });
    }

    // ============================================================
    // GRADIENT CIRCLE
    // ============================================================

    private void paintGradientCircle(
            Graphics2D g2,
            int themeIndex,
            int x,
            int y,
            int size
    ) {

        Color[] colors =
                julianneThemeManager
                        .getTheme(themeIndex);

        float[] fractions =
                new float[colors.length];

        for (int i = 0;
                i < colors.length;
                i++) {

            fractions[i] =
                    i /
                    (float)
                    (colors.length - 1);
        }

        LinearGradientPaint gradient =
                new LinearGradientPaint(
                        x,
                        y,
                        x + size,
                        y + size,
                        fractions,
                        colors
                );

        g2.setPaint(gradient);

        g2.fillOval(
                x,
                y,
                size,
                size
        );
    }

    // ============================================================
    // CHECKMARK
    // ============================================================

    private void paintCheckmark(
            Graphics2D g2,
            int x,
            int y,
            int size
    ) {

        Graphics2D check =
                (Graphics2D) g2.create();

        int centerX =
                x + size / 2;

        int centerY =
                y + size / 2;

        check.translate(
                centerX,
                centerY
        );

        check.scale(
                checkScale,
                checkScale
        );

        check.setComposite(
                AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER,
                        checkAlpha
                )
        );

        check.setColor(Color.WHITE);

        check.setStroke(
                new BasicStroke(
                        5f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                )
        );

        check.drawLine(
                -17,
                0,
                -5,
                13
        );

        check.drawLine(
                -5,
                13,
                22,
                -17
        );

        check.dispose();
    }

    // ============================================================
    // HOVER BORDER
    // ============================================================

    private void paintHoverBorder(
            Graphics2D g2,
            int x,
            int y,
            int size
    ) {

        g2.setColor(
                new Color(
                        255,
                        255,
                        255,
                        180
                )
        );

        g2.setStroke(
                new BasicStroke(2.5f)
        );

        g2.drawOval(
                x,
                y,
                size,
                size
        );
    }

    // ============================================================
    // RESET BUTTON
    // ============================================================

    private void paintResetButton(
            Graphics2D g2
    ) {

        int width = 210;
        int height = 45;

        int x =
                (getWidth() - width) / 2;

        int y = 615;

        g2.setColor(BURGUNDY);

        g2.setStroke(
                new BasicStroke(2f)
        );

        g2.drawRoundRect(
                x,
                y,
                width,
                height,
                18,
                18
        );

        String text =
                "RESET TO DEFAULT";

        g2.setFont(
                new Font(
                        "Times New Roman",
                        Font.PLAIN,
                        20
                )
        );

        int textWidth =
                g2.getFontMetrics()
                        .stringWidth(text);

        int textX =
                x +
                (width - textWidth) / 2;

        int textY =
                y +
                (height -
                g2.getFontMetrics()
                        .getHeight()) / 2 +
                g2.getFontMetrics()
                        .getAscent();

        g2.drawString(
                text,
                textX,
                textY
        );
    }

    // ============================================================
    // RESET HITBOX
    // ============================================================

    private boolean isResetButton(
            int mouseX,
            int mouseY
    ) {

        int width = 210;
        int height = 45;

        int x =
                (getWidth() - width) / 2;

        int y = 615;

        return mouseX >= x &&
                mouseX <= x + width &&
                mouseY >= y &&
                mouseY <= y + height;
    }

    // ============================================================
    // FIND THEME UNDER MOUSE
    // ============================================================

    private int getThemeAt(
            int mouseX,
            int mouseY
    ) {

        for (int i = 0;
                i < 16;
                i++) {

            int row = i / 4;
            int column = i % 4;

            int x =
                    CIRCLE_X[column];

            int y =
                    CIRCLE_Y[row];

            int centerX =
                    x + CIRCLE_SIZE / 2;

            int centerY =
                    y + CIRCLE_SIZE / 2;

            int radius =
                    CIRCLE_SIZE / 2;

            int dx =
                    mouseX - centerX;

            int dy =
                    mouseY - centerY;

            if ((dx * dx + dy * dy)
                    <= radius * radius) {

                return i;
            }
        }

        return -1;
    }
}
