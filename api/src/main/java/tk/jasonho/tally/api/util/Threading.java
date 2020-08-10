package tk.jasonho.tally.api.util;

import lombok.SneakyThrows;

public class Threading {

    @SneakyThrows
    public static void sleep(long millis) {
        Thread.sleep(millis);
    }

}
