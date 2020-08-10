package tk.jasonho.tally.core.bukkit.pseudo;

import com.google.common.base.Preconditions;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.joda.time.Duration;
import org.joda.time.Seconds;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.List;

public class Strings {
    private static PeriodFormatter periodFormatter = (new PeriodFormatterBuilder()).appendDays().appendSuffix("d").appendHours().appendSuffix("h").appendMinutes().appendSuffix("m").appendSeconds().appendSuffix("s").appendSeconds().toFormatter();

    public Strings() {
    }

    public static String addColors(String message) {
        return message == null ? null : ChatColor.translateAlternateColorCodes('^', message);
    }

    public static String removeColors(String message) {
        return message == null ? null : ChatColor.stripColor(message);
    }

    public static ChatColor toChatColor(DyeColor color) {
        switch(color) {
            case WHITE:
                return ChatColor.WHITE;
            case ORANGE:
                return ChatColor.GOLD;
            case MAGENTA:
                return ChatColor.LIGHT_PURPLE;
            case LIGHT_BLUE:
                return ChatColor.BLUE;
            case YELLOW:
                return ChatColor.YELLOW;
            case LIME:
                return ChatColor.GREEN;
            case PINK:
                return ChatColor.LIGHT_PURPLE;
            case GRAY:
                return ChatColor.DARK_GRAY;
            case SILVER:
                return ChatColor.GRAY;
            case CYAN:
                return ChatColor.DARK_AQUA;
            case PURPLE:
                return ChatColor.DARK_PURPLE;
            case BLUE:
                return ChatColor.BLUE;
            case BROWN:
                return ChatColor.GOLD;
            case GREEN:
                return ChatColor.DARK_GREEN;
            case RED:
                return ChatColor.DARK_RED;
            case BLACK:
                return ChatColor.GRAY;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static ChatColor toChatColor(double proportion) {
        Preconditions.checkArgument(proportion >= 0.0D && proportion <= 1.0D);
        if (proportion <= 0.15D) {
            return ChatColor.RED;
        } else {
            return proportion <= 0.5D ? ChatColor.YELLOW : ChatColor.GREEN;
        }
    }

    public static Duration toDuration(String format) {
        String text = format.toLowerCase().replace(" ", "");
        if (text.equals("oo")) {
            return Seconds.MAX_VALUE.toStandardDuration();
        } else {
            return text.equals("-oo") ? Seconds.MIN_VALUE.toStandardDuration() : periodFormatter.parsePeriod(text).toStandardDuration();
        }
    }

    public static String padChatMessage(BaseComponent message, String padChar, ChatColor padColor, ChatColor messageColor) {
        return padChatMessage(message.toPlainText(), padChar, padColor, messageColor);
    }

    public static BaseComponent padChatComponent(BaseComponent message, String padChar, ChatColor padColor, ChatColor messageColor) {
        return new TextComponent(padChatMessage(message.toPlainText(), padChar, padColor, messageColor));
    }

    public static BaseComponent padTextComponent(BaseComponent message, String padChar, ChatColor padColor, ChatColor messageColor) {
        return padTextComponent(message, padChar, padColor.toString(), messageColor);
    }

    public static BaseComponent padTextComponent(BaseComponent message, String padChar, String padColor, ChatColor messageColor) {
        String pad = paddingFor(message.toPlainText(), padChar);
        TextComponent component = new TextComponent(padColor + pad + ChatColor.RESET);
        BaseComponent copy = Components.copyStyle(message, new TextComponent(' ' + message.toPlainText() + ' '));
        copy.setColor(messageColor.asBungee());
        component.addExtra(copy);
        component.addExtra(ChatColor.RESET.toString() + padColor + pad);
        return component;
    }

    public static String padChatMessage(String message, String padChar, ChatColor padColor, ChatColor messageColor) {
        message = " " + message + " ";
        String pad = paddingFor(message, padChar);
        return padColor + pad + ChatColor.RESET + messageColor + message + ChatColor.RESET + padColor + pad;
    }

    private static String paddingFor(String text, String padChar) {
        return com.google.common.base.Strings.repeat(padChar, (55 - ChatColor.stripColor(text).length() - 2) / (padChar.length() * 2));
    }

    public static BaseComponent blankLine(ChatColor color) {
        TextComponent line = new TextComponent(com.google.common.base.Strings.repeat(" ", 59));
        line.setStrikethrough(true);
        line.setColor(color.asBungee());
        return line;
    }

    public static void wrapLoreCopyColors(List<String> lore, int length, String string) {
        String previous = "";
        String[] var4 = WordUtils.wrap(string, length).split(SystemUtils.LINE_SEPARATOR);
        int var5 = var4.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            String wrapped = var4[var6];
            if (!previous.isEmpty()) {
                previous = ChatColor.getLastColors(previous);
            }

            lore.add(previous + wrapped);
            previous = wrapped;
        }

    }
}
