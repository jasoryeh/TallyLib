package tk.jasonho.tally.api;

import lombok.Getter;
import lombok.SneakyThrows;
import tk.jasonho.tally.api.exceptions.UnknownAPIHostException;

import java.net.URL;

public class TallyConfiguration {

    @Getter
    private final String host;
    @Getter
    private final String auth;

    public static final String DEFAULT_STATS_HOST = "https://stats.jasoryeh.tk/";

    public TallyConfiguration(String auth) {
        this(null, auth);
    }

    @SneakyThrows
    public TallyConfiguration(String host, String auth) {
        this.host = host == null ? DEFAULT_STATS_HOST : (host.endsWith("/") ? host : host + "/");
        this.auth = auth;

        if(!this.getHost().startsWith("http") && !this.getHost().endsWith("/")) {
            // doesn't seem to be an actual url... so we test it!
            try {
                URL url = new URL(this.getHost());
                url.openConnection().connect();
            } catch(Exception e) {
                throw new UnknownAPIHostException(String.format("The host %s does not seem to be valid. We tested it an this error was given: %s", this.getHost(), e.getMessage()));
            }
        }
    }

}
