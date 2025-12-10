package tk.jasonho.tally.api.models.helpers;

import com.google.gson.*;
import lombok.SneakyThrows;
import lombok.ToString;
import tk.jasonho.tally.api.TallyConfiguration;
import tk.jasonho.tally.api.TallyStatsManager;
import tk.jasonho.tally.api.models.*;
import tk.jasonho.tally.api.util.TallyLogger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@ToString
public abstract class Model {

    public Model() {

    }

    @SneakyThrows
    protected void updateWith(Model dummy) {
        for (Field declaredField : this.getClass().getDeclaredFields()) {
            if (!declaredField.isAnnotationPresent(MapsTo.class)) {
                continue;
            }
            declaredField.setAccessible(true);
            declaredField.set(this, declaredField.get(dummy));
        }
    }

    public static JsonElement objectToJsonElement(Object value) throws Exception {
        if (value.getClass().isAssignableFrom(ModelSerializable.class)) {
            return (JsonElement) value.getClass()
                    .getDeclaredMethod("serialize")
                    .invoke(value);
        } else if (value instanceof Model) {
            return serialize(((Model) value));
        } else if (value instanceof Number) {
            return new JsonPrimitive((Number) value);
        } else if (value instanceof String) {
            return new JsonPrimitive((String) value);
        } else if (value instanceof Boolean) {
            return new JsonPrimitive((Boolean) value);
        } else if (value instanceof Character) {
            return new JsonPrimitive((Character) value);
        } else if (value == null) {
            return JsonNull.INSTANCE;
        } else if (value instanceof JsonElement) {
            return ((JsonElement) value);
        } else if (value instanceof Collection) {
            JsonArray jsonArray = new JsonArray();
            ((Collection<?>) value).forEach(new Consumer<Object>() {
                @SneakyThrows
                @Override
                public void accept(Object v) {
                    jsonArray.add(objectToJsonElement(v));
                }
            });
            return jsonArray;
        } else if (value instanceof Map) {
            JsonObject jsonObject = new JsonObject();
            ((Map<?, ?>) value).forEach(new BiConsumer<Object, Object>() {
                @SneakyThrows
                @Override
                public void accept(Object k, Object v) {
                    if (k == null) {
                        throw new Exception("Mapped key cannot be null!");
                    }
                    jsonObject.add(k.toString(), objectToJsonElement(v));
                }
            });
            return jsonObject;
        } else {
            TallyLogger.optionalLog("      ...could not convert to Json Element");
            throw new Exception("Serialization of '" + value.getClass().getCanonicalName() + "' is not currently supported!");
        }
    }

    public static <T extends Model> JsonObject serialize(T model) throws Exception {
        TallyLogger.optionalLog("Serializing " + model.getClass().getName());
        JsonObject jsonObject = new JsonObject();
        Class<? extends Model> clazz = model.getClass();
        for (Field declaredField : clazz.getDeclaredFields()) {
            TallyLogger.optionalLog("  ..." + declaredField.getName());
            declaredField.setAccessible(true);
            MapsTo[] annotationsByType = declaredField.getAnnotationsByType(MapsTo.class);

            if (annotationsByType.length <= 0) {
                // continue early so we don't convert an unused (and potentially unsupported field)
                continue;
            }

            Object value = declaredField.get(model);
            TallyLogger.optionalLog("    " + annotationsByType.length + "x@MapsTo");

            JsonElement jsonValue;
            try {
                jsonValue = objectToJsonElement(value);
                TallyLogger.optionalLog("      ...converted");
            } catch(Exception e) {
                TallyLogger.optionalLog("      ...could not convert, " + e.getMessage());
                throw new Exception("Serialization of this object is not currently supported!: " + value.getClass().getCanonicalName(), e);
            }

            for (MapsTo mapsTo : annotationsByType) {
                for (String mapping : mapsTo.value()) {
                    TallyLogger.optionalLog("    ...@MapsTo=" + mapping);
                    jsonObject.add(mapping, jsonValue);
                    TallyLogger.optionalLog("      ...mapped");
                }
            }
        }
        TallyLogger.optionalLog("Serialized: " + jsonObject.toString());
        return jsonObject;
    }

    public static <T extends Model> T deserialize(Class<T> clazz, JsonObject json) throws Exception {
        TallyLogger.optionalLog("Deserializing " + json.toString() + " to a " + clazz.getName());
        HashMap<String, Field> fieldMaps = new HashMap<>();
        for (Field declaredField : clazz.getDeclaredFields()) {
            TallyLogger.optionalLog("Field: " + declaredField.getName());
            MapsTo[] annotationsByType = declaredField.getAnnotationsByType(MapsTo.class);

            for (MapsTo mapsTo : annotationsByType) {
                TallyLogger.optionalLog("  MapsTo...");
                for (String maps : mapsTo.value()) {
                    TallyLogger.optionalLog("      ..." + mapsTo.value());
                    TallyLogger.optionalLog("      ..." + Arrays.toString(mapsTo.value()));
                    fieldMaps.put(maps, declaredField);
                }
            }
        }

        Constructor<T> constructor = null;
        for (Constructor<?> clazzConstructor : clazz.getDeclaredConstructors()) {
            if (clazzConstructor.getParameterCount() <= (clazz.isLocalClass() ? 1 : 0)) {
                constructor = (Constructor<T>) clazzConstructor;
            }
        }

        if (constructor == null) {
            throw new Exception("Model Class " + clazz.getName() + " must have a valid no-args constructor.");
        }

        if (clazz.isLocalClass()) {
            Class<?> enclosingClass = clazz.getEnclosingClass();
            if (!Arrays.stream(enclosingClass.getConstructors()).anyMatch((cons) -> cons.getParameterCount() <= 0)) {
                throw new Exception("Model classes nested deeper than one local class level are not supported.");
            }
        }

        T t = clazz.isLocalClass()
                ? constructor.newInstance(clazz.getEnclosingClass().getConstructor().newInstance())
                : constructor.newInstance();

        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            TallyLogger.optionalLog("Deserializing " + key);
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
                        TallyLogger.optionalLog("  failed deserializing unsupported primitive " + key);
                        throw new UnsupportedOperationException("Cannot deserialize to other primitive types yet!");
                        // todo: deserialize to other types
                    }
                } else {
                    if (Model.class.isAssignableFrom(field.getType())) {
                        if (!entry.getValue().isJsonObject()) {
                            throw new Exception("Malformed JSON, cannot deserialize non-json object to a nested model!");
                        }
                        field.set(t, Model.deserialize((Class<T>) field.getType(), entry.getValue().getAsJsonObject()));
                    } else {
                        TallyLogger.optionalLog("  failed deserializing unsupported type " + key);
                        throw new Exception("Cannot currently deserialize: at " + key + "; type: " + field.getType().getCanonicalName());
                    }

                }
            }
        }
        TallyLogger.optionalLog("Deserialized to " + t.toString());
        return t;
    }

    @SneakyThrows
    public static void main(String[] args) {
        TallyConfiguration config = new TallyConfiguration(
                "http://localhost/tally/api",
                "testtoken",
                new ArrayList<>()
        );
        config.setTestRoute("");

        TallyStatsManager mgr = new TallyStatsManager(config);

        for (Game game : Game.all(mgr)) {
            System.out.println(game);
        }

        Instance instance = Instance.of(mgr, "test-id", "123.0.0.1");
        System.out.println(instance);

        Game mcj = Game.ofTag(mgr, "mc-java");
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
        System.out.println(stat1);
        StatLink link1 = stat1.causalLink(mgr, player1, "role1");
        System.out.println("causal:");
        System.out.println(link1);
        StatLink link2 = stat1.ownsLink(mgr, player2, "role2");
        System.out.println("owns:");
        System.out.println(link2);
        System.out.println("-------------------------------labelinks");
        LabelLink label1 = stat1.link(mgr, test, true);
        System.out.println("primary label:");
        System.out.println(label1);
        LabelLink label2 = stat1.link(mgr, test1, false);
        System.out.println("nonprimary label1:");
        System.out.println(label2);
        LabelLink label3 = stat1.link(mgr, test2, false);
        System.out.println("nonprimary label2:");
        System.out.println(label3);
    }
}
