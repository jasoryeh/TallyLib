package tk.jasonho.tally.core.bukkit.pseudo;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;

import java.util.Locale;

public interface TranslatableComponent {
    BaseComponent translate(Locale var1);

    default BaseComponent translate(CommandSender viewer) {
        return this.translate(viewer.getLocale());
    }
}
