package tk.jasonho.tally.api.models;

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
public class StatLink extends Model {
    @MapsTo("id")
    public int id;
    @MapsTo("statistic")
    public int statistic;
    @MapsTo("player")
    public int player;
    @MapsTo("role")
    public String role;

    public StatLink() {}

    @Deprecated
    @SneakyThrows
    public void saveAsCausal(TallyStatsManager manager) {
        this.role = "tally_causal" + (this.role != null ? " " + this.role : "");
        manager.connectionBuilder("statistic/link")
                .post()
                .writeJson(
                        Model.serialize(this)
                )
                .verifyJsonThrowing();
    }

    @Deprecated
    @SneakyThrows
    public void saveAsOwns(TallyStatsManager manager) {
        this.role = "tally_own" + (this.role != null ? " " + this.role : "");
        manager.connectionBuilder("statistic/link")
                .post()
                .writeJson(
                        Model.serialize(this)
                )
                .verifyJsonThrowing();
    }
}
