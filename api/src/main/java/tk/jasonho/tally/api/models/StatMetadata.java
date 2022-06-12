package tk.jasonho.tally.api.models;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import tk.jasonho.tally.api.TallyStatsManager;
import tk.jasonho.tally.api.models.helpers.MapsTo;
import tk.jasonho.tally.api.models.helpers.Model;

@Builder
@Data
@AllArgsConstructor
public class StatMetadata extends Model {
    @MapsTo("id")
    public int id;
    @MapsTo("statistic")
    public int statistic;
    @MapsTo("key")
    public String key;
    @MapsTo("value")
    public String value;

    public StatMetadata() {}

    @SneakyThrows
    public void save(TallyStatsManager manager) {
        JsonObject instance = manager.connectionBuilder("statistic/metadata")
                .post()
                .writeJson(Model.serialize(this))
                .verifyJsonThrowing()
                .getReadJson()
                .getAsJsonObject()
                .get("data")
                .getAsJsonObject();
        this.updateWith(Model.deserialize(StatMetadata.class, instance));
    }
}
