package tk.jasonho.tally.api.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Data;
import lombok.SneakyThrows;
import tk.jasonho.tally.api.TallyStatsManager;
import tk.jasonho.tally.api.models.helpers.MapsTo;
import tk.jasonho.tally.api.models.helpers.Model;

@Data
public class Instance extends Model {
    @MapsTo("id")
    private Integer id;
    @MapsTo("reporter")
    private Integer reporter;
    @MapsTo("selfid")
    private String selfid;
    @MapsTo("host")
    private String host;

    public Instance() {}

    public static Instance of(TallyStatsManager mgr, String selfID, String host) {
        Instance instance = new Instance();
        instance.setSelfid(selfID);
        instance.setHost(host);
        instance.save(mgr);
        return instance;
    }

    @SneakyThrows
    public void save(TallyStatsManager manager) {
        JsonObject instance = manager.connectionBuilder("instance")
                .post()
                .writeJson(Model.serialize(this))
                .verifyJsonThrowing()
                .getReadJson()
                .getAsJsonObject()
                .get("data")
                .getAsJsonObject();
        this.updateWith(Model.deserialize(Instance.class, instance));
    }
}
