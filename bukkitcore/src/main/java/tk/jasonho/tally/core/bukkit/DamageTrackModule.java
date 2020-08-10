package tk.jasonho.tally.core.bukkit;

import com.google.common.util.concurrent.AtomicDouble;
import lombok.Getter;
import lombok.Setter;
import tk.jasonho.tally.core.bukkit.pseudo.Localizable;
import tk.jasonho.tally.core.bukkit.pseudo.Translations;
import tk.jasonho.tally.core.bukkit.pseudo.UnlocalizedText;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public abstract class DamageTrackModule extends TallyListener {
    /**
     * Anything non-player that we know, but generalized as the environment (including mobs)
     */
    public static final UUID ENVIRONMENT = new UUID(0, 0);
    /**
     * Non-entity unknown/unresolved person.
     */
    public static final UUID UNKNOWN = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Getter
    private final String id;

    @Getter
    private List<DamageExchange> damageExchanges;

    public DamageTrackModule(TallyOperationHandler opHandler, String id) {
        super(opHandler);
        this.id = id;

        this.damageExchanges = new ArrayList<>();

        Bukkit.getLogger().info("New damage tracking for: " + (id != null ? id : "nil"));
    }

    public void storeExchange(DamageExchange exchange) {
        this.damageExchanges.add(exchange);
    }

    public List<DamageExchange> getExchanges(UUID forUser) {
        List<DamageExchange> exchanges = new ArrayList<>();
        damageExchanges.forEach(e -> {
            if(e.getMe() == forUser) {
                exchanges.add(e);
            }
        });

        return exchanges;
    }

    public List<DamageExchange> getUntrackedExchanges(UUID forUser) {
        List<DamageExchange> exchanges = this.getExchanges(forUser);

        List<DamageExchange> untracked = new ArrayList<>();

        exchanges.forEach(e -> {
            if(!e.isTracked()) {
                untracked.add(e);
            }
        });

        return untracked;
    }

    public void trackUntracked(UUID forUser) {
        damageExchanges.forEach(e -> {
            if(e.getMe() == forUser) {
                e.setTracked(true);
            }
        });
    }

    public double sumOfExchanges(DamageExchange e) {
        return this.sumOfExchanges(e.getMe(), e.getYou());
    }

    public double sumOfExchanges(UUID me, UUID you) {
        double damage = 0.0;
        for (DamageExchange e : damageExchanges) {
            if (e.getMe() == me && e.getYou() == you) {
                damage += e.getAmount();
            }
        }

        return damage;
    }

    private final String SPACER_DOUBLE = "  ";

    /**
     * Builds chat message lines to send to player when they die
     * Recaps them on who hit who and how much
     * @param showTo Player that is getting the message
     * @return
     */
    public List<Localizable> getPlayerPVPRecap(Player showTo) {
        List<Localizable> result = new ArrayList<>();

        List<DamageExchange> exchanges = getUntrackedExchanges(showTo.getUniqueId());

        // Damage from the viewer to others, left is who it is to, right is <amount, hits>
        Map<UUID, Pair<AtomicDouble, AtomicLong>> damageFromViewer = new HashMap<>();
        // Damage taken from others to viewer, left is who it is from, right is <amount, hits>
        Map<UUID, Pair<AtomicDouble, AtomicLong>> damageToViewer = new HashMap<>();

        for (DamageExchange exchange : exchanges) {
            if(exchange.getDirection() == DamageDirection.GIVE) {
                if(damageFromViewer.containsKey(exchange.getYou())) {
                    Pair<AtomicDouble, AtomicLong> exEntry = damageFromViewer.get(exchange.getYou());
                    exEntry.getLeft().addAndGet(exchange.getAmount());
                    exEntry.getRight().addAndGet(1);
                } else {
                    damageFromViewer.put(exchange.getYou(), Pair.of(new AtomicDouble(exchange.getAmount()), new AtomicLong(1)));
                }
            } else if(exchange.getDirection() == DamageDirection.RECEIVE) {
                if(damageToViewer.containsKey(exchange.getYou())) {
                    Pair<AtomicDouble, AtomicLong> exEntry = damageToViewer.get(exchange.getYou());
                    exEntry.getLeft().addAndGet(exchange.getAmount());
                    exEntry.getRight().addAndGet(1);
                } else {
                    damageToViewer.put(exchange.getYou(), Pair.of(new AtomicDouble(exchange.getAmount()), new AtomicLong(1)));
                }
            }
        }

        //

        if(!damageFromViewer.isEmpty()) {
            result.add(new UnlocalizedText(""));
            result.add(Translations.STATS_RECAP_DAMAGE_DAMAGEGIVEN.with(ChatColor.GREEN));
            damageFromViewer.forEach((uuid, dmg) -> {
                result.add(
                        Translations.STATS_RECAP_DAMAGE_TO.with(
                                ChatColor.AQUA,
                                damageDisplay(dmg.getLeft().get(), dmg.getRight().get()),
                                (uuid == ENVIRONMENT) ?
                                        Translations.STATS_RECAP_DAMAGE_ENVIRONMENT.with(ChatColor.DARK_AQUA).translate(showTo).toLegacyText() :
                                        (Bukkit.getPlayer(uuid) != null) ? Bukkit.getPlayer(uuid).getDisplayName() : "unknown"
                        )
                );
            });
        }


        //
        if(!damageToViewer.isEmpty()) {
            result.add(new UnlocalizedText(""));
            result.add(Translations.STATS_RECAP_DAMAGE_DAMAGETAKEN.with(ChatColor.RED));
            damageToViewer.forEach((uuid, dmg) -> {
                result.add(
                        Translations.STATS_RECAP_DAMAGE_FROM.with(
                                ChatColor.AQUA,
                                damageDisplay(dmg.getLeft().get(), dmg.getRight().get()),
                                (uuid == ENVIRONMENT) ?
                                        Translations.STATS_RECAP_DAMAGE_ENVIRONMENT.with(ChatColor.DARK_AQUA).translate(showTo).toLegacyText() :
                                        (Bukkit.getPlayer(uuid) != null) ? Bukkit.getPlayer(uuid).getDisplayName() : "unknown"
                        )
                );
            });
        }

        if(result.isEmpty()) {
            // TODO: Translation
            UnlocalizedText nothing = new UnlocalizedText("No damage dealt or received was tracked.");
            nothing.style().bold(true).underlined(true);
            result.add(new UnlocalizedText(""));
            result.add(nothing);
            result.add(new UnlocalizedText(""));
        }

        return result;
    }

    /**
     * Renders damage amount
     * @param rawDmg Amount of total damage dealt
     * @param hits Amount of hits dealt
     * @return String with formatted damage and hits
     */
    private static String damageDisplay(Double rawDmg, long hits) {
        String display = (rawDmg / 2) + "";

        // cleanup
        display = display.contains(".0") ? display.replace(".0", "") : display;

        // heart char
        display = ChatColor.GOLD + display + ChatColor.RED + "❤" + ((rawDmg / 2) != 1.0 ? "s" : "")
                + ChatColor.DARK_GRAY + " x" + hits + ChatColor.RESET;

        return display;
    }

    /**
     * Single direction damage tracking, where attacker must be a player
     * @param attacker Player receiving damage
     * @param defender Can be environmental damage or a player damaging
     * @param damage amount of damage
     */
    protected void onDamage(UUID attacker, UUID defender, double damage, DamageDirection direction) {
        // Save the information
        DamageExchange de = new DamageExchange(attacker, defender == null ? ENVIRONMENT : defender, damage, direction);

        // Store both directions of damage given
        this.storeExchange(de);
        this.storeExchange(de.flip());
    }

    /**
     * Handles recap when player dies
     * @param dead player that dies
     */
    protected void onDeath(Player dead) {
        if (TallyPlugin.getInstance().getConfig().getBoolean("recap", false)) {
            List<Localizable> damage = this.getPlayerPVPRecap(dead);
            damage.forEach(m -> dead.sendMessage(m.translate(dead.getLocale())));
        }
    }



    public class DamageExchange {
        @Getter
        private final UUID me;
        @Getter
        private final UUID you;
        @Getter
        private final double amount;
        @Getter
        private final DamageDirection direction;

        /**
         * Tracked means it's been seen already//recapped already
         */
        @Getter
        @Setter
        private boolean tracked = false;

        /**
         * Helper variable to stop being rewarded multiple times for assists
         */
        @Getter
        @Setter
        private boolean isCreditRewarded = false;
        @Getter
        @Setter
        private boolean isExperienceRewarded = false;

        /**
         * Determines if both credit and experience is rewarded.
         * @return if both credit and experience is rewarded already
         */
        public boolean isRewarded() {
            return this.isCreditRewarded && this.isExperienceRewarded;
        }

        @Getter
        private Instant time;

        public DamageExchange(UUID me, UUID you, double amount, DamageDirection direction) {
            this.me = me;
            this.you = you;
            this.amount = amount;
            this.direction = direction;

            this.time = Instant.now();
        }

        private DamageExchange flip;

        public DamageExchange(UUID me, UUID you, double amount, DamageDirection direction, DamageExchange flippedPair) {
            this(me, you, amount, direction);
            this.flip = flippedPair;
        }

        /**
         * Get the other damage direction (used for internal purposes)
         * @return other direction
         */
        public DamageExchange flip() {
            DamageExchange flipped = this.flip != null ? this.flip : new DamageExchange(this.you, this.me, this.amount, this.direction.invert(), this);
            this.flip = flipped;
            return flipped;
        }

        @Override
        public String toString() {
            StringJoiner toString = new StringJoiner("");
            toString.add("DamageExchange")
                    .add("[");

            for (Field declaredField : this.getClass().getDeclaredFields()) {
                String name;
                Object value;
                try {
                    declaredField.setAccessible(true);
                    name = declaredField.getName();
                    value = declaredField.get(this);


                } catch(IllegalAccessException e) {
                    name = "unknown";
                    value = "unknown";
                }
                if(value instanceof DamageTrackModule || name.equalsIgnoreCase("flip")) {
                    // ignore "flip" value otherwise it will come right back to us.
                    continue;
                } else {
                    toString.add(name).add("=").add(value.toString()).add(",");
                }
            }
            toString.add("]");

            return toString.toString();
        }
    }

    /**
     * Represents direction of Damage.
     */
    public enum DamageDirection {
        RECEIVE,
        GIVE;

        public DamageDirection invert() {
            return this == RECEIVE ? GIVE : RECEIVE;
        }
    }

}
