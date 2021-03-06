package tk.jasonho.tally.core.bukkit.pseudo;

/**
 * Represents a format that takes arguments for localization with {@link LocalizedText}s.
 */
public class LocalizedFormat implements LocalizableFormat<LocalizedText> {

  private final LocaleBundle bundle;
  private final String key;

  public LocalizedFormat(LocaleBundle bundle, String key) {
    this.bundle = bundle;
    this.key = key;
  }

  @Override
  public LocalizedText with(TextStyle style, Localizable... arguments) {
    return new LocalizedText(this.bundle, this.key, style, arguments);
  }

  @Override
  public LocalizedText with(Localizable... arguments) {
    return new LocalizedText(this.bundle, this.key, arguments);
  }
}
