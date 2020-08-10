package tk.jasonho.tally.core.bukkit.pseudo;

import org.bukkit.ChatColor;
import tk.jasonho.tally.core.bukkit.TallyPlugin;

import static tk.jasonho.tally.core.bukkit.pseudo.TranslationProvider.$NULL$;

public class Translations {

    // ----
    public static final LocalizedFormat TYPE_BOOLEAN_TRUE = $NULL$;
    public static final LocalizedFormat TYPE_BOOLEAN_FALSE = $NULL$;
    public static final LocalizedFormat STATS_RECAP_DAMAGE_DAMAGEGIVEN = $NULL$;
    public static final LocalizedFormat STATS_RECAP_DAMAGE_DAMAGETAKEN = $NULL$;
    public static final LocalizedFormat STATS_RECAP_DAMAGE_FROM = $NULL$;
    public static final LocalizedFormat STATS_RECAP_DAMAGE_TO = $NULL$;
    public static final LocalizedFormat STATS_RECAP_DAMAGE_ENVIRONMENT = $NULL$;
    // ----
    static final LocaleBundle BUNDLE;

    /**
     * Automatic mapping via static initialization block
     */
    static {
        /*
         * TODO: Add more languages
         * Add additional languages here! (List of languages)
         */
        BUNDLE = TranslationProvider.loadBundle(TallyPlugin.getInstance(), "en_US", "es_ES");

        // Maps the translations from the Translation files to the files above.
        TranslationProvider.map(Translations.class, BUNDLE);
    }

    private Translations() { }

    /**
     * Bundle containing all of the currently loaded Locales
     *
     * @return Bundle with currently loaded locales
     */
    public static LocaleBundle getBundle() {
        return BUNDLE;
    }

    /**
     * Returns a "enabled"(green) or "disabled"(red) when true or false.
     * GREEN|RED
     *
     * @param value Accepts, true for green "enabled", false for red "disabled"
     * @return Translation of each enabled/disabled message
     */
    public static LocalizedText bool(final boolean value) {
        return value ? TYPE_BOOLEAN_TRUE.with(ChatColor.GREEN) : TYPE_BOOLEAN_FALSE.with(ChatColor.RED);
    }

}
