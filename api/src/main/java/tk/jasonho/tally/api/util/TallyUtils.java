package tk.jasonho.tally.api.util;

import tk.jasonho.tally.api.interfacing.TallyConnectionBuilder;

public class TallyUtils {

    private static String IP_ADDRESS;

    static {
        try {
            IP_ADDRESS = new TallyConnectionBuilder("http://checkip.amazonaws.com").get().readIn().getRead();
        } catch(Exception e) {
            e.printStackTrace();
            IP_ADDRESS = "could not determine";
        }
    }

    public static String getSelfIP() {
        return IP_ADDRESS;
    }

}
