package tk.jasonho.tally.api;

import java.util.UUID;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import tk.jasonho.tally.api.interfacing.TallyConnectionBuilder;
import tk.jasonho.tally.api.models.Instance;
import tk.jasonho.tally.api.util.TallyLogger;
import tk.jasonho.tally.api.util.TallyUtils;

@Getter
public class TallyStatsManager {
    public static final String __VERSION = "v2";

    private final TallyConfiguration configuration;
    private final Instance instance;

    @Getter
    protected String matchTag;
    public JsonObject matchData;

    @Getter
    @Setter
    protected boolean tagMatches = true;

    @Getter
    @Setter
    protected boolean tagHiddenMetadata = false; // whether to automatically add the hidden metadata tag to all statistics

    public TallyStatsManager(TallyConfiguration configuration) {
        this.configuration = configuration;
        this.instance = Instance.of(this, UUID.randomUUID().toString(), TallyUtils.getSelfIP());

        this.matchData = new JsonObject();
        this.matchTag = this.regenerateMatchTag();

        if (!this.test()) {
            throw new IllegalStateException("Failed to validate statistics endpoint at " + this.configuration.getHost());
        }
    }

    public String setMatchTag(String tag) {
        return this.matchTag = tag;
    }

    public String regenerateMatchTag() {
        return this.setMatchTag(
                UUID.randomUUID().toString()
        );
    }

    /**
     * Creates a configured connection builder to the route.
     * @param route Route this connection should send the request to.
     * @return Connection builder
     */
    public TallyConnectionBuilder connectionBuilder(String route) {
        TallyLogger.optionalLog("Create connection: " + route);
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
