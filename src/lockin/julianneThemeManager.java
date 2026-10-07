package lockin;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public final class julianneThemeManager {

    
    // THEME CHANGE LISTENER
    

    public interface ThemeChangeListener {
        void themeChanged(Color[] colors);
    }

    
    // THEME DATA
    

    private static final List<Color[]> THEMES = new ArrayList<>();

    private static final List<ThemeChangeListener> LISTENERS =
            new ArrayList<>();

    // Theme 1 is the default theme.
    private static int selectedTheme = 0;

    
    // 16 THEMES
    

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
                "#376DA2",
                "#1E9EC5"
        );

        // Theme 3
        addTheme(
                "#dbbb1a",
                "#f2c73c",
                "#D47400",
                "#D14E00"
        );

        // Theme 4
        addTheme(
                "#972828",
                "#E45742",
                "#C96320",
                "#D98B20"
        );

        // Theme 5
        addTheme(
                "#7C444F",
                "#9F5255",
                "#BD513D",
                "#D07B3D"
        );

        // Theme 6
        addTheme(
                "#C4773F",
                "#8C5A3C",
                "#A06C3E",
                "#BFAE9B"
        );

        // Theme 7
        addTheme(
                "#35155D",
                "#592D90",
                "#365FA8",
                "#6286D4"
        );

        // Theme 8
        addTheme(
                "#B7143C",
                "#D65464",
                "#C8BEB3"
        );

        // Theme 9
        addTheme(
                "#5E7E98",
                "#7C9D87",
                "#D1B5B5"
        );

        // Theme 10
        addTheme(
                "#6C76A2",
                "#A38BB4",
                "#D2ADA3"
        );

        // Theme 11
        addTheme(
                "#83B2D4",
                "#D49B9B",
                "#D47398"
        );

        // Theme 12
        addTheme(
                "#D2C5B4",
                "#D2B8B6",
                "#D2A8A8",
                "#D46F6F"
        );

        // Theme 13
        addTheme(
                "#84B179",
                "#8BAF76",
                "#9EB793",
                "#BDC997"
        );

        // Theme 14
        addTheme(
                "#D06B6B",
                "#D4A388",
                "#D0BE8C",
                "#C8CC8C"
        );

        // Theme 15
        addTheme(
                "#4682A9",
                "#5D89B3",
                "#80B3CC",
                "#A9D1D4"
        );

        // Theme 16
        addTheme(
                "#000000",
                "#1F150C",
                "#412D15",
                "#BEB9A8"
        );
    }

    
    // PRIVATE CONSTRUCTOR
    

    private julianneThemeManager() {
        // Prevent creating instances.
    }

    
    // ADD THEME
    

    private static void addTheme(String... hexColors) {

        Color[] colors =
                new Color[hexColors.length];

        for (int i = 0; i < hexColors.length; i++) {

            colors[i] =
                    Color.decode(hexColors[i]);
        }

        THEMES.add(colors);
    }

    
    // GET NUMBER OF THEMES
    

    // Returns the number of themes available to the user.
    public static int getThemeCount() {
        return THEMES.size();
    }

    
    // GET SELECTED THEME INDEX
    

    // Returns the index of the selected theme.
    public static int getSelectedTheme() {
        return selectedTheme;
    }

    
    // GET THEME COLORS
    

    // Gets a copy of one theme's colors by index.
    public static Color[] getTheme(int index) {

        if (index < 0 || index >= THEMES.size()) {
            index = 0;
        }

        return THEMES
                .get(index)
                .clone();
    }

    
    // GET CURRENTLY SELECTED COLORS
    

    // Gets the colors for the currently selected theme.
    public static Color[] getSelectedColors() {
        return getTheme(selectedTheme);
    }

    
    // SELECT THEME
    

    // Selects a theme and notifies listening UI components.
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

    
    // RESET TO DEFAULT
    

    // Returns the selected theme to Theme 1.
    public static void resetToDefault() {

        selectedTheme = 0;

        notifyListeners();
    }

    
    // ADD LISTENER
    

    // Registers a UI component for theme-change updates.
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

    
    // REMOVE LISTENER
    

    // Removes a UI component from theme-change updates.
    public static void removeListener(
            ThemeChangeListener listener
    ) {

        LISTENERS.remove(listener);
    }

    
    // NOTIFY LISTENERS
    

    // Tells registered panels that the theme changed.
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

    
    // COLOR INTERPOLATION
    

    // Blends two colors by a chosen amount.
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

    
    // INTERPOLATE COMPLETE THEME
    

    // Blends matching colors from two themes.
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

    
    // COLOR TO HEX
    

    // Converts a Color into hexadecimal text.
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