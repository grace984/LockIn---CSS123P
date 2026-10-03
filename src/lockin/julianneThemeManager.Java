package lockin;

public class julianneThemeManager {
}package lockin;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;

public final class julianneThemeManager {

    private julianneThemeManager() {
    }

    // ============================================================
    // 16 LOCKIN THEMES
    // ============================================================

    private static final String[][] THEME_COLORS = {

        // 1
        {"#59132C", "#A42762", "#D17D93", "#FFBCED"},

        // 2
        {"#0F2854", "#1C4D8D", "#4988C4", "#31C8F6"},

        // 3
        {"#FFD400", "#FFC300", "#FF8C00", "#FF5F00"},

        // 4
        {"#972828", "#E45742", "#EB7F31", "#FCAD38"},

        // 5
        {"#7C444F", "#9F5255", "#E16A54", "#F39E60"},

        // 6
        {"#F39E60", "#8C5A3C", "#C08552", "#E4CAB0"},

        // 7
        {"#35155D", "#592D90", "#4477CE", "#8CABFF"},

        // 8
        {"#B7143C", "#FF7888", "#F1E9DF"},

        // 9
        {"#789DBC", "#9CC3A7", "#FFE3E3"},

        // 10
        {"#8B96CA", "#C7AEDA", "#FCD8CD"},

        // 11
        {"#AEDEFC", "#FFC1C1", "#FF99C2"},

        // 12
        {"#FFF5E4", "#FFE3E1", "#FFD1D1", "#FF9494"},

        // 13
        {"#84B179", "#A2CB8B", "#C7EABB", "#E8F5BD"},

        // 14
        {"#F08787", "#FFC7A7", "#FEE2AD", "#F8FAB4"},

        // 15
        {"#4682A9", "#7BA9D6", "#A6DFFB", "#DEFCFF"},

        // 16
        {"#000000", "#1F150C", "#412D15", "#E1DCC9"}
    };

    // ============================================================
    // CURRENT THEME
    // ============================================================

    private static int selectedTheme = 0;

    private static Color[] currentColors =
            createColors(0);

    // ============================================================
    // LISTENERS
    // ============================================================

    private static final List<ThemeChangeListener> listeners =
            new ArrayList<>();

    public interface ThemeChangeListener {

        void themeChanged(Color[] colors);
    }

    // ============================================================
    // ANIMATION
    // ============================================================

    private static Timer transitionTimer;

    private static Color[] transitionStart;

    private static Color[] transitionTarget;

    private static final int TRANSITION_DURATION = 280;

    private static final int TRANSITION_DELAY = 15;

    // ============================================================
    // GETTERS
    // ============================================================

    public static int getThemeCount() {

        return THEME_COLORS.length;
    }

    public static int getSelectedTheme() {

        return selectedTheme;
    }

    public static Color[] getCurrentGradient() {

        return currentColors.clone();
    }

    public static Color[] getTheme(int index) {

        if (index < 0 ||
                index >= THEME_COLORS.length) {

            index = 0;
        }

        return createColors(index);
    }

    public static String[] getThemeHexCodes(int index) {

        if (index < 0 ||
                index >= THEME_COLORS.length) {

            index = 0;
        }

        return THEME_COLORS[index].clone();
    }

    // ============================================================
    // SET THEME
    // ============================================================

    public static void setTheme(int index) {

        if (index < 0 ||
                index >= THEME_COLORS.length) {

            return;
        }

        selectedTheme = index;

        Color[] target =
                createColors(index);

        if (sameColors(
                currentColors,
                target
        )) {

            currentColors =
                    target.clone();

            notifyListeners();

            return;
        }

        startTransition(target);
    }

    // ============================================================
    // RESET
    // ============================================================

    public static void resetToDefault() {

        setTheme(0);
    }

    // ============================================================
    // LISTENER MANAGEMENT
    // ============================================================

    public static void addThemeChangeListener(
            ThemeChangeListener listener
    ) {

        if (listener != null &&
                !listeners.contains(listener)) {

            listeners.add(listener);
        }
    }

    public static void removeThemeChangeListener(
            ThemeChangeListener listener
    ) {

        listeners.remove(listener);
    }

    // ============================================================
    // THEME TRANSITION
    // ============================================================

    private static void startTransition(
            Color[] target
    ) {

        if (transitionTimer != null &&
                transitionTimer.isRunning()) {

            transitionTimer.stop();
        }

        transitionStart =
                currentColors.clone();

        transitionTarget =
                target.clone();

        final long startTime =
                System.currentTimeMillis();

        transitionTimer =
                new Timer(
                        TRANSITION_DELAY,
                        e -> {

                            long elapsed =
                                    System.currentTimeMillis()
                                            - startTime;

                            float progress =
                                    Math.min(
                                            1.0f,
                                            elapsed /
                                            (float)
                                            TRANSITION_DURATION
                                    );

                            // Smooth ease-in-out.
                            float eased =
                                    progress *
                                    progress *
                                    (3.0f -
                                    2.0f *
                                    progress);

                            currentColors =
                                    interpolate(
                                            transitionStart,
                                            transitionTarget,
                                            eased
                                    );

                            notifyListeners();

                            if (progress >= 1.0f) {

                                currentColors =
                                        transitionTarget.clone();

                                notifyListeners();

                                ((Timer) e.getSource())
                                        .stop();
                            }
                        }
                );

        transitionTimer.start();
    }

    // ============================================================
    // COLOR INTERPOLATION
    // ============================================================

    private static Color[] interpolate(
            Color[] start,
            Color[] end,
            float amount
    ) {

        int count = end.length;

        Color[] result =
                new Color[count];

        for (int i = 0;
                i < count;
                i++) {

            float position = count == 1
                    ? 0.0f
                    : i / (float) (count - 1);
            Color startColor = sampleGradient(start, position);
            Color endColor = sampleGradient(end, position);

            int red =
                    interpolateValue(
                            startColor.getRed(),
                            endColor.getRed(),
                            amount
                    );

            int green =
                    interpolateValue(
                            startColor.getGreen(),
                            endColor.getGreen(),
                            amount
                    );

            int blue =
                    interpolateValue(
                            startColor.getBlue(),
                            endColor.getBlue(),
                            amount
                    );

            result[i] =
                    new Color(
                            red,
                            green,
                            blue
                    );
        }

        return result;
    }

    private static Color sampleGradient(Color[] colors, float position) {
        if (colors.length == 1) {
            return colors[0];
        }

        float scaledPosition = Math.max(0.0f, Math.min(1.0f, position)) * (colors.length - 1);
        int startIndex = Math.min((int) scaledPosition, colors.length - 2);
        float localProgress = scaledPosition - startIndex;
        Color from = colors[startIndex];
        Color to = colors[startIndex + 1];

        return new Color(
                interpolateValue(from.getRed(), to.getRed(), localProgress),
                interpolateValue(from.getGreen(), to.getGreen(), localProgress),
                interpolateValue(from.getBlue(), to.getBlue(), localProgress));
    }

    private static int interpolateValue(
            int start,
            int end,
            float amount
    ) {

        return Math.round(
                start +
                (end - start) * amount
        );
    }

    // ============================================================
    // CREATE COLORS
    // ============================================================

    private static Color[] createColors(
            int index
    ) {

        String[] hex =
                THEME_COLORS[index];

        Color[] colors =
                new Color[hex.length];

        for (int i = 0;
                i < hex.length;
                i++) {

            colors[i] =
                    Color.decode(hex[i]);
        }

        return colors;
    }

    // ============================================================
    // COMPARE COLORS
    // ============================================================

    private static boolean sameColors(
            Color[] a,
            Color[] b
    ) {

        if (a.length != b.length) {
            return false;
        }

        for (int i = 0;
                i < a.length;
                i++) {

            if (!a[i].equals(b[i])) {
                return false;
            }
        }

        return true;
    }

    // ============================================================
    // NOTIFY
    // ============================================================

    private static void notifyListeners() {

        Color[] snapshot =
                currentColors.clone();

        for (ThemeChangeListener listener :
                new ArrayList<>(listeners)) {

            listener.themeChanged(snapshot);
        }s
    }
}
