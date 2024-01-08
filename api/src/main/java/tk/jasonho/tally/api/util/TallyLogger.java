package tk.jasonho.tally.api.util;

public class TallyLogger {

    public static boolean verbose;

    public static void say(Object say) {
        System.out.print("[Tally] [Info Alt.] ");
        System.out.println(say);
    }

    public static void optionalLog(String string) {
        if(verbose) {
            System.out.println("[Tally] [Verbose Alt.] " + string);
        }
    }
}
