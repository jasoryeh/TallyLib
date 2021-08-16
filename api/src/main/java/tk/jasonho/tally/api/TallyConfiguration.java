package tk.jasonho.tally.api;

import java.util.Map;
import lombok.Getter;
import lombok.SneakyThrows;
import tk.jasonho.tally.api.exceptions.UnknownAPIHostException;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import tk.jasonho.tally.api.util.TallyLogger;

public class TallyConfiguration {

    @Getter
    private final String host;
    @Getter
    private final String auth;
    @Getter
    private final List<String> labels;

    public static final String DEFAULT_STATS_HOST = "https://stats.jasoryeh.tk/";

    public TallyConfiguration(String auth) {
        this(null, auth, new ArrayList<>());
    }

    @SneakyThrows
    public TallyConfiguration(String host, String auth, List<?> labels) {
        this.host = host == null ? DEFAULT_STATS_HOST : (host.endsWith("/") ? host : host + "/");
        this.auth = auth;
        this.labels = new ArrayList<>();

        labels.forEach(l -> {
            try {
                this.labels.add(l.toString());
            } catch (Exception e) {
                // pass
            }
        });

        Map<String, String> env = System.getenv();
        if (env.containsKey("TALLY_LABELS")) {
            TallyLogger.say("Found TALLY_LABELS system variable...");
            for (String label : env.get("TALLY_LABELS").split(" ")) {
                TallyLogger.say("Adding label: " + label);
                this.labels.add(label);
            }
        }

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
