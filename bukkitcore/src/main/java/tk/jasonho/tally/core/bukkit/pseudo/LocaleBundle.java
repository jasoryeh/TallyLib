package tk.jasonho.tally.core.bukkit.pseudo;

import java.util.*;

public class LocaleBundle {
    private List<LocaleStrings> locales;

    public LocaleBundle() {
        this(new ArrayList());
    }

    public LocaleBundle(List<LocaleStrings> locales) {
        this.locales = locales;
    }

    public LocaleBundle(List<LocaleStrings> locales, LocaleStrings defaultStrings) {
        this.locales = locales;
        this.locales.remove(defaultStrings);
        this.locales.add(0, defaultStrings);
    }

    public Optional<Locale> getDefaultLocale() {
        Optional<LocaleStrings> strings = this.getDefaultStrings();
        return strings.isPresent() ? Optional.of(((LocaleStrings)strings.get()).getLocale()) : Optional.empty();
    }

    public Optional<LocaleStrings> getDefaultStrings() {
        return this.locales.size() > 0 ? Optional.of(this.locales.get(0)) : Optional.empty();
    }

    public Optional<LocaleStrings> getStringsRoughly(Locale locale) {
        LocaleStrings match = null;
        Iterator var3 = this.locales.iterator();

        while(var3.hasNext()) {
            LocaleStrings test = (LocaleStrings)var3.next();
            if (test.getLocale().equals(locale)) {
                return Optional.of(test);
            }

            if (test.getLocale().getLanguage().equals(locale.getLanguage())) {
                match = test;
                break;
            }
        }

        return match != null ? Optional.of(match) : this.getDefaultStrings();
    }

    public void add(LocaleStrings strings) {
        this.locales.add(strings);
    }

    public Optional<String> get(Locale locale, String key) {
        Optional<LocaleStrings> strings = this.getStringsRoughly(locale);
        if (!strings.isPresent()) {
            return Optional.empty();
        } else {
            Optional<String> result = ((LocaleStrings)strings.get()).get(key);
            if (result.isPresent()) {
                return Optional.of(((String)result.get()).replace("\n", ""));
            } else {
                Optional<LocaleStrings> defStrings = this.getDefaultStrings();
                return defStrings.isPresent() && !strings.equals(this.getDefaultStrings()) ? this.get((Locale)this.getDefaultLocale().get(), key) : Optional.empty();
            }
        }
    }

    public boolean has(Locale locale, String key) {
        return this.get(locale, key).isPresent();
    }

    public LocalizedFormat getFormat(String key) {
        return new LocalizedFormat(this, key);
    }

    public LocalizedText getText(String key, Localizable... arguments) {
        return new LocalizedText(this, key, arguments);
    }
}
