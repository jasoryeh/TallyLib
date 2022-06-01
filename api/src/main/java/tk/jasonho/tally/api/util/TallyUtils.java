package tk.jasonho.tally.api.util;

import tk.jasonho.tally.api.interfacing.TallyConnectionBuilder;

public class TallyUtils {

    public static String getSelfIP() {
        return new TallyConnectionBuilder("http://checkip.amazonaws.com").get().readIn().getRead();
    }

}
