
package com.example.ticketexpress;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemeHelper {
    private static final String PREF_NAME = "theme_prefs";
    private static final String KEY_THEME = "current_theme";

    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;

    public static void applyTheme(Context context) {
        int theme = getSavedTheme(context);
        if (theme == THEME_DARK) {
            context.setTheme(R.style.Theme_TicketExpressDarkMode);
        } else {
            context.setTheme(R.style.Theme_TicketExpress);
        }
    }

    public static void toggleTheme(Context context) {
        int currentTheme = getSavedTheme(context);
        int newTheme = (currentTheme == THEME_DARK) ? THEME_LIGHT : THEME_DARK;
        saveTheme(context, newTheme);
    }

    public static int getSavedTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_THEME, THEME_LIGHT); // Default: Light
    }

    public static void saveTheme(Context context, int theme) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME, theme).apply();
    }
}
