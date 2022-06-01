package tk.jasonho.tally.api;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
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
    @Setter
    private String testRoute = "";

    @Getter
    private final List<String> labels = new ArrayList<>();

    public static final String DEFAULT_STATS_HOST = "https://stats.jasoryeh.tk/";

    @SneakyThrows
    public TallyConfiguration(String host, String auth, List<String> labels) {
        this.host = host == null ? DEFAULT_STATS_HOST : (host.endsWith("/") ? host : host + "/");
        this.auth = auth;

        this.labels.addAll(labels);
        this.importLabels();

        if (!this.testURL(this.getHost())) {
            throw new UnknownAPIHostException(
                    String.format("The host %s does not seem to be valid. We tested it and an error was given.",
                            this.getHost()));
        }
    }

    /**
     * Import labels from the environment, if present.
     */
    private void importLabels() {
        Map<String, String> env = System.getenv();
        if (env.containsKey("TALLY_LABELS")) {
            TallyLogger.say("Found TALLY_LABELS system variable...");
            for (String label : env.get("TALLY_LABELS").split(" ")) {
                TallyLogger.say("Adding label: " + label);
                this.labels.add(label);
            }
        }
    }

    private boolean testURL(String host) {
        try {
            new URL(host).openConnection().connect();
            return true;
        } catch(Exception e) {
            System.out.println("Failed to validate url: " + host);
            e.printStackTrace();
            return false;
        }
    }

    public String ofRoute(String route) {
        return this.getHost() + route;
    }

}
