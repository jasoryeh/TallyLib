package tk.jasonho.tally.api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import tk.jasonho.tally.api.TallyStatsManager;
import tk.jasonho.tally.api.models.helpers.MapsTo;
import tk.jasonho.tally.api.models.helpers.Model;

@Data
@Builder
@AllArgsConstructor
public class LabelLink extends Model {
    @MapsTo("id")
    private Integer id;
    @MapsTo("label")
    private Integer label;
    @MapsTo("statistic")
    private Integer statistic;
    @Deprecated
    @MapsTo("primary")
    private Boolean primary;

    public LabelLink() {}

    @SneakyThrows
    public void save(TallyStatsManager mgr) {
        mgr.connectionBuilder("label/link")
                .post()
                .writeJson(
                        Model.serialize(this)
                )
                .verifyJsonThrowing();
    }
}
