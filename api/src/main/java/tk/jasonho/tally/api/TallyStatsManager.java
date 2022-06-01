package tk.jasonho.tally.api;

import java.util.UUID;
import lombok.Getter;
import tk.jasonho.tally.api.interfacing.TallyConnectionBuilder;
import tk.jasonho.tally.api.models.Instance;
import tk.jasonho.tally.api.util.TallyUtils;

@Getter
public class TallyStatsManager {
    public static final String __VERSION = "v2";

    private final TallyConfiguration configuration;
    private final Instance instance;

    public TallyStatsManager(TallyConfiguration configuration) {
        this.configuration = configuration;
        this.instance = Instance.of(this, UUID.randomUUID().toString(), TallyUtils.getSelfIP());

        if (!this.test()) {
            throw new IllegalStateException("Failed to validate statistics endpoint at " + this.configuration.getHost());
        }
    }

    /**
     * Creates a configured connection builder to the route.
     * @param route Route this connection should send the request to.
     * @return Connection builder
     */
    public TallyConnectionBuilder connectionBuilder(String route) {
        return new TallyConnectionBuilder(
                this.configuration.ofRoute(route)
        ).authBearer(this.configuration.getAuth());
    }

    public boolean test() {
        return this.connectionBuilder(this.configuration.getTestRoute())
                .get()
                .readIn()
                .verifyJson();
    }


}
