package tk.jasonho.tally.api.models.helpers;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.SneakyThrows;
import lombok.ToString;
import tk.jasonho.tally.api.TallyConfiguration;
import tk.jasonho.tally.api.TallyStatsManager;
import tk.jasonho.tally.api.models.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@ToString
public abstract class Model {
    public Model() {

    }

    @SneakyThrows
    protected void updateWith(Model dummy) {
        for (Field declaredField : this.getClass().getDeclaredFields()) {
            declaredField.setAccessible(true);
            declaredField.set(this, declaredField.get(dummy));
        }
    }

    public static <T extends Model> JsonObject serialize(T model) throws Exception {
        JsonObject jsonObject = new JsonObject();
        Class<? extends Model> clazz = model.getClass();
        for (Field declaredField : clazz.getDeclaredFields()) {
            declaredField.setAccessible(true);
            MapsTo[] annotationsByType = declaredField.getAnnotationsByType(MapsTo.class);
            Object value = declaredField.get(model);

            for (MapsTo mapsTo : annotationsByType) {
                for (String mapping : mapsTo.value()) {
                    if (value instanceof Number) {
                        jsonObject.addProperty(mapping, ((Number) value));
                    } else if (value instanceof String) {
                        jsonObject.addProperty(mapping, ((String) value));
                    } else if (value instanceof Boolean) {
                        jsonObject.addProperty(mapping, ((Boolean) value));
                    } else if (value instanceof Character) {
                        jsonObject.addProperty(mapping, ((Character) value));
                    } else if (value == null) {
                        jsonObject.add(mapping, JsonNull.INSTANCE);
                    } else {
                        throw new Exception("Serialization of non-primitive mapping is not currently supported!");
                    }
                }
            }
        }
        return jsonObject;
    }

    public static <T extends Model> T deserialize(Class<T> clazz, JsonObject json) throws Exception {
        HashMap<String, Field> fieldMaps = new HashMap<>();
        for (Field declaredField : clazz.getDeclaredFields()) {
            MapsTo[] annotationsByType = declaredField.getAnnotationsByType(MapsTo.class);

            for (MapsTo mapsTo : annotationsByType) {
                for (String maps : mapsTo.value()) {
                    fieldMaps.put(maps, declaredField);
                }
            }
        }

        Constructor<T> constructor = clazz.getConstructor();
        T t = constructor.newInstance();

        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            if (fieldMaps.containsKey(key)) {
                Field field = fieldMaps.get(key);
                field.setAccessible(true);

                if (entry.getValue().isJsonNull()) {
                    field.set(t, null);
                } else if (entry.getValue().isJsonPrimitive()) {
                    JsonPrimitive asJsonPrimitive = entry.getValue().getAsJsonPrimitive();

                    if (asJsonPrimitive.isString()) {
                        field.set(t, asJsonPrimitive.getAsString());
                    } else if (asJsonPrimitive.isBoolean()) {
                        field.set(t, asJsonPrimitive.getAsBoolean());
                    } else if (asJsonPrimitive.isNumber()) {
                        field.set(t, asJsonPrimitive.getAsInt());
                    } else {
                        throw new UnsupportedOperationException("Cannot deserialize to other primitive types yet!");
                        // todo: deserialize to other types
                    }
                } else {
                    throw new Exception("Cannot currently deserialize non-primitives and non-nulls: at " + key);
                }
            }
        }
        return t;
    }

    @SneakyThrows
    public static void main(String[] args) {
        TallyConfiguration config = new TallyConfiguration(
                "https://faas-nyc1-2ef2e6cc.doserverless.co/api/v1/web/fn-98055e97-4e6f-47d5-8b7c-0b99eb9a7add/tally",
                "testtoken",
                new ArrayList<>()
        );
        config.setTestRoute("index");

        TallyStatsManager mgr = new TallyStatsManager(config);

        for (Game game : Game.all(mgr)) {
            System.out.println(game);
        }

        Instance instance = Instance.of(mgr, "test-id", "123.0.0.1");
        System.out.println(instance);

        Game mcj = Game.ofTag(mgr, "mcj");
        System.out.println(mcj);

        Label test = Label.of(mgr, "test");
        System.out.println(test);
        Label test1 = Label.of(mgr, "test1");
        System.out.println(test1);
        Label test2 = Label.of(mgr, "test2");
        System.out.println(test2);

        Player player1 = Player.of(mgr, mcj, "id1");
        System.out.println(player1);
        Player player2 = Player.of(mgr, mcj, "id2");
        System.out.println(player2);

        Statistic stat1 = Statistic.of(mgr, mcj, "some score here", instance);
        System.out.println("-------------------------------links");
        stat1.causalLink(mgr, player1, "role1");
        stat1.ownsLink(mgr, player2, "role2");
        System.out.println(stat1);

        System.out.println("-------------------------------labelinks");
        stat1.link(mgr, test, true);
        stat1.link(mgr, test1, false);
        stat1.link(mgr, test2, false);
    }
}
