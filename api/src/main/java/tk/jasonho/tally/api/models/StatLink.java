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

    @SneakyThrows
    public void saveAsCausal(TallyStatsManager manager) {
        manager.connectionBuilder("statistic/link/causal")
                .post()
                .writeJson(
                        Model.serialize(this)
                )
                .verifyJsonThrowing();
    }

    @SneakyThrows
    public void saveAsOwns(TallyStatsManager manager) {
        manager.connectionBuilder("statistic/link/owns")
                .post()
                .writeJson(
                        Model.serialize(this)
                )
                .verifyJsonThrowing();
    }
}
