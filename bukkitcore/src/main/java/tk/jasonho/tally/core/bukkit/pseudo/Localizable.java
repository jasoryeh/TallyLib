package tk.jasonho.tally.core.bukkit.pseudo;

/**
 * Represents anything that can be translated and sent to players.
 */
public interface Localizable extends TranslatableComponent {

  Localizable[] EMPTY = new Localizable[0];

  /**
   * Get the style of this.
   */
  TextStyle style();

  /**
   * Copy this and its styles.
   */
  Localizable duplicate();
}
