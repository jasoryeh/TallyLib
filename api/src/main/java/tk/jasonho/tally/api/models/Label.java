package tk.jasonho.tally.api.models;

import com.google.gson.JsonObject;
import lombok.Data;
import lombok.SneakyThrows;
import tk.jasonho.tally.api.TallyStatsManager;
import tk.jasonho.tally.api.models.helpers.MapsTo;
import tk.jasonho.tally.api.models.helpers.Model;

@Data
public class Label extends Model {
    @MapsTo("id")
    private Integer id;
    @MapsTo("label")
    private String label;
    @MapsTo("name")
    private String name;

    public Label() {}

    @SneakyThrows
    public LabelLink link(TallyStatsManager mgr, Statistic statistic, boolean primary) {
        LabelLink build = LabelLink.builder()
                .label(this.id)
                .statistic(statistic.getId())
                .primary(primary)
                .build();
        build.save(mgr);
        return build;
    }

    @SneakyThrows
    public void save(TallyStatsManager manager) {
        JsonObject instance = manager.connectionBuilder("label")
                .post()
                .writeJson(Model.serialize(this))
                .verifyJsonThrowing()
                .getReadJson()
                .getAsJsonObject()
                .get("data")
                .getAsJsonObject();
        this.updateWith(Model.deserialize(Label.class, instance));
    }

    public static Label of(TallyStatsManager mgr, String label) {
        Label label1 = new Label();
        label1.setLabel(label);
        label1.save(mgr);
        return label1;
    }
}
