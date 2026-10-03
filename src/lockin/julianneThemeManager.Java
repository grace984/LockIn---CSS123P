package lockin;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public final class julianneThemeManager {

    // =========================================================
    // THEME CHANGE LISTENER
    // =========================================================

    public interface ThemeChangeListener {
        void themeChanged(Color[] colors);
    }

    // =========================================================
    // THEME DATA
    // =========================================================

    private static final List<Color[]> THEMES = new ArrayList<>();

    private static final List<ThemeChangeListener> LISTENERS =
            new ArrayList<>();

    // Theme 1 is the default theme.
    private static int selectedTheme = 0;

    // =========================================================
    // 16 THEMES
    // =========================================================

    static {

        // Theme 1
        addTheme(
                "#59132C",
                "#A42762",
                "#D17D93",
                "#FFBCED"
        );

        // Theme 2
        addTheme(
                "#0F2854",
                "#1C4D8D",
                "#4988C4",
                "#31C8F6"
        );

        // Theme 3
        addTheme(
                "#FFD400",
                "#FFC300",
                "#FF8C00",
                "#FF5F00"
        );

        // Theme 4
        addTheme(
                "#972828",
                "#E45742",
                "#EB7F31",
                "#FCAD38"
        );

        // Theme 5
        addTheme(
                "#7C444F",
                "#9F5255",
                "#E16A54",
                "#F39E60"
        );

        // Theme 6
        addTheme(
                "#F39E60",
                "#8C5A3C",
                "#C08552",
                "#E4CAB0"
        );

        // Theme 7
        addTheme(
                "#35155D",
                "#592D90",
                "#4477CE",
                "#8CABFF"
        );

        // Theme 8
        addTheme(
                "#B7143C",
                "#FF7888",
                "#F1E9DF"
        );

        // Theme 9
        addTheme(
                "#789DBC",
                "#9CC3A7",
                "#FFE3E3"
        );

        // Theme 10
        addTheme(
                "#8B96CA",
                "#C7AEDA",
                "#FCD8CD"
        );

        // Theme 11
        addTheme(
                "#AEDEFC",
                "#FFC1C1",
                "#FF99C2"
        );

        // Theme 12
        addTheme(
                "#FFF5E4",
                "#FFE3E1",
                "#FFD1D1",
                "#FF9494"
        );

        // Theme 13
        addTheme(
                "#84B179",
                "#A2CB8B",
                "#C7EABB",
                "#E8F5BD"
        );

        // Theme 14
        addTheme(
                "#F08787",
                "#FFC7A7",
                "#FEE2AD",
                "#F8FAB4"
        );

        // Theme 15
        addTheme(
                "#4682A9",
                "#7BA9D6",
                "#A6DFFB",
                "#DEFCFF"
        );

        // Theme 16
        addTheme(
                "#000000",
                "#1F150C",
                "#412D15",
                "#E1DCC9"
        );
    }

    // =========================================================
    // PRIVATE CONSTRUCTOR
    // =========================================================

    private julianneThemeManager() {
        // Prevent creating instances.
    }

    // =========================================================
    // ADD THEME
    // =========================================================

    private static void addTheme(String... hexColors) {

        Color[] colors =
                new Color[hexColors.length];

        for (int i = 0; i < hexColors.length; i++) {

            colors[i] =
                    Color.decode(hexColors[i]);
        }

        THEMES.add(colors);
    }

    // =========================================================
    // GET NUMBER OF THEMES
    // =========================================================

    public static int getThemeCount() {
        return THEMES.size();
    }

    // =========================================================
    // GET SELECTED THEME INDEX
    // =========================================================

    public static int getSelectedTheme() {
        return selectedTheme;
    }

    // =========================================================
    // GET THEME COLORS
    // =========================================================

    public static Color[] getTheme(int index) {

        if (index < 0 || index >= THEMES.size()) {
            index = 0;
        }

        return THEMES
                .get(index)
                .clone();
    }

    // =========================================================
    // GET CURRENTLY SELECTED COLORS
    // =========================================================

    public static Color[] getSelectedColors() {
        return getTheme(selectedTheme);
    }

    // =========================================================
    // SELECT THEME
    // =========================================================

    public static void selectTheme(int index) {

        if (index < 0 || index >= THEMES.size()) {
            return;
        }

        if (selectedTheme == index) {
            return;
        }

        selectedTheme = index;

        notifyListeners();
    }

    // =========================================================
    // RESET TO DEFAULT
    // =========================================================

    public static void resetToDefault() {

        selectedTheme = 0;

        notifyListeners();
    }

    // =========================================================
    // ADD LISTENER
    // =========================================================

    public static void addListener(
            ThemeChangeListener listener
    ) {

        if (listener == null) {
            return;
        }

        if (!LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    // =========================================================
    // REMOVE LISTENER
    // =========================================================

    public static void removeListener(
            ThemeChangeListener listener
    ) {

        LISTENERS.remove(listener);
    }

    // =========================================================
    // NOTIFY LISTENERS
    // =========================================================

    private static void notifyListeners() {

        Color[] selectedColors =
                getSelectedColors();

        // Make a copy so listeners can safely
        // modify themselves while notifying.
        List<ThemeChangeListener> snapshot =
                new ArrayList<>(LISTENERS);

        for (ThemeChangeListener listener : snapshot) {

            listener.themeChanged(
                    selectedColors.clone()
            );
        }
    }

    // =========================================================
    // COLOR INTERPOLATION
    // =========================================================

    public static Color interpolate(
            Color from,
            Color to,
            double amount
    ) {

        amount =
                Math.max(
                        0.0,
                        Math.min(
                                1.0,
                                amount
                        )
                );

        int red =
                (int) Math.round(
                        from.getRed()
                        + (
                                to.getRed()
                                - from.getRed()
                        ) * amount
                );

        int green =
                (int) Math.round(
                        from.getGreen()
                        + (
                                to.getGreen()
                                - from.getGreen()
                        ) * amount
                );

        int blue =
                (int) Math.round(
                        from.getBlue()
                        + (
                                to.getBlue()
                                - from.getBlue()
                        ) * amount
                );

        return new Color(
                red,
                green,
                blue
        );
    }

    // =========================================================
    // INTERPOLATE COMPLETE THEME
    // =========================================================

    public static Color[] interpolateColors(
            Color[] from,
            Color[] to,
            double amount
    ) {

        int count =
                Math.min(
                        from.length,
                        to.length
                );

        Color[] result =
                new Color[count];

        for (int i = 0; i < count; i++) {

            result[i] =
                    interpolate(
                            from[i],
                            to[i],
                            amount
                    );
        }

        return result;
    }

    // =========================================================
    // COLOR TO HEX
    // =========================================================

    public static String colorToHex(
            Color color
    ) {

        return String.format(
                "#%02X%02X%02X",
                color.getRed(),
                color.getGreen(),
                color.getBlue()
        );
    }
}