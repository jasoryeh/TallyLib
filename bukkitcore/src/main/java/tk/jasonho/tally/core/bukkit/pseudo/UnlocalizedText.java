package tk.jasonho.tally.core.bukkit.pseudo;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A simple non-translatable string that can still take arguments and styling.
 */
public class UnlocalizedText implements Localizable {

  private final String text;
  private final List<Localizable> arguments;
  private final TextStyle style;

  public UnlocalizedText(String text, Localizable... arguments) {
    this(text, TextStyle.create(), arguments);
  }

  public UnlocalizedText(String text, TextStyle style, Localizable... arguments) {
    this(text, style, arguments.length == 0 ? Collections.emptyList()
        : new ArrayList<>(Arrays.asList(arguments)));
  }

  public UnlocalizedText(String text, ChatColor color) {
    this(text, TextStyle.ofColor(color));
  }

  public UnlocalizedText(String text, TextStyle style, List<Localizable> arguments) {
    this.text = text;
    this.arguments = arguments;
    this.style = style;
  }

  @Override
  public TextComponent translate(Locale locale) {
    String format = this.text;

    List<BaseComponent> parts = new ArrayList<>();

    for (int i = 0; i < this.arguments.size(); i++) {
      Localizable curr = this.arguments.get(i);

      if (format.contains("{" + i + "}")) {
        String[] split = format.split("\\{" + i + "\\}");

        if (split.length > 0) {
          parts.add(this.style.apply(split[0]));
        }

        // Allows null arguments to be converted to ""
        if (curr != null) {
          curr.style().inherit(this.style);
          parts.add(curr.translate(locale));
        }

        if (split.length > 1) {
          format = split[1];
        } else {
          format = "";
        }
      }
    }

    if (format.length() > 0) {
      parts.add(this.style.apply(format));
    }

    TextComponent result;

    if (parts.size() > 0) {
      result = new TextComponent(parts.get(0));
      for (int i = 1; i < parts.size(); i++) {
        result.addExtra(parts.get(i));
      }
    } else {
      result = new TextComponent("");
    }

    return result;
  }

  @Override
  public TextStyle style() {
    return this.style;
  }

  @Override
  public UnlocalizedText duplicate() {
    List<Localizable> arguments = this.arguments.stream()
        .map(Localizable::duplicate)
        .collect(Collectors.toList());

    return new UnlocalizedText(this.text, this.style.duplicate(), arguments);
  }
}
