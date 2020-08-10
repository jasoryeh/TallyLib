package tk.jasonho.tally.core.bukkit;

import tk.jasonho.tally.api.interfacing.TallyConnectionBuilder;

public class TallyUtils {

    public static String getSelfIP() {
        return new TallyConnectionBuilder("http://checkip.amazonaws.com").get().readIn().getRead();
    }

}
