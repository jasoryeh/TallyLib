package tk.jasonho.tally.api.models.helpers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @aiAuthor Jetbrains AI **/
public class ModelTest {

    @BeforeEach
    void setUp() {
        tk.jasonho.tally.api.util.TallyLogger.verbose = true;
    }

    @Test
    void testUpdateWith_updatesAllAnnotatedFields() {
        @Getter
        @Setter
        class MockModel extends Model {
            @MapsTo("fieldOne")
            public String fieldOne;

            @MapsTo("fieldTwo")
            public Integer fieldTwo;

            @MapsTo("fieldThree")
            public Boolean fieldThree;

            public MockModel(String fieldOne, Integer fieldTwo, Boolean fieldThree) {
                this.fieldOne = fieldOne;
                this.fieldTwo = fieldTwo;
                this.fieldThree = fieldThree;
            }

            public MockModel() {
            }
        }

        MockModel initialModel = new MockModel("initialValue", 42, true);
        MockModel newModel = new MockModel("updatedValue", 99, false);

        initialModel.updateWith(newModel);

        assertEquals("updatedValue", initialModel.fieldOne);
        assertEquals(99, initialModel.fieldTwo);
        assertEquals(false, initialModel.fieldThree);
    }

    @Test
    void testUpdateWith_doesNotUpdateNonAnnotatedFields() {
        @Getter
        @Setter
        class NonAnnotatedModel extends Model {
            public String nonAnnotatedField;

            @MapsTo("fieldOne")
            public String fieldOne;

            public NonAnnotatedModel(String nonAnnotatedField, String fieldOne) {
                this.nonAnnotatedField = nonAnnotatedField;
                this.fieldOne = fieldOne;
            }

            public NonAnnotatedModel() {
            }
        }

        NonAnnotatedModel initialModel = new NonAnnotatedModel("nonAnnotatedValue", "initialValue");
        NonAnnotatedModel newModel = new NonAnnotatedModel("updatedNonAnnotatedValue", "updatedValue");

        initialModel.updateWith(newModel);

        assertEquals("updatedValue", initialModel.fieldOne);
        assertEquals("nonAnnotatedValue", initialModel.nonAnnotatedField);
    }

    @Test
    void testUpdateWith_ignoresFieldsWithoutMapsToAnnotation() {
        @Getter
        @Setter
        class PartialModel extends Model {
            @MapsTo("fieldOne")
            public String fieldOne;

            public Integer ignoredField;

            public PartialModel(String fieldOne, Integer ignoredField) {
                this.fieldOne = fieldOne;
                this.ignoredField = ignoredField;
            }

            public PartialModel() {
            }
        }

        PartialModel initialModel = new PartialModel("initialValue", 42);
        PartialModel newModel = new PartialModel("updatedValue", 99);

        initialModel.updateWith(newModel);

        assertEquals("updatedValue", initialModel.fieldOne);
        assertEquals(42, initialModel.ignoredField);
    }


    @Test
    void testSerialize_correctlySerializesAnnotatedFields() throws Exception {
        @Getter
        @Setter
        class SerializableModel extends Model {
            @MapsTo("fieldString")
            private String fieldString;

            @MapsTo("fieldInt")
            private Integer fieldInt;

            @MapsTo("fieldBool")
            private Boolean fieldBool;

            public SerializableModel(String fieldString, Integer fieldInt, Boolean fieldBool) {
                this.fieldString = fieldString;
                this.fieldInt = fieldInt;
                this.fieldBool = fieldBool;
            }

            public SerializableModel() {
            }
        }

        SerializableModel model = new SerializableModel("stringValue", 123, true);
        JsonObject jsonObject = Model.serialize(model);

        assertEquals("stringValue", jsonObject.get("fieldString").getAsString());
        assertEquals(123, jsonObject.get("fieldInt").getAsInt());
        assertEquals(true, jsonObject.get("fieldBool").getAsBoolean());
    }

    @Test
    void testDeserialize_correctlyDeserializesJsonObject() throws Exception {
        @Getter
        @Setter
        class DeserializableModel extends Model {
            @MapsTo("fieldString")
            private String fieldString;

            @MapsTo("fieldInt")
            private Integer fieldInt;

            @MapsTo("fieldBool")
            private Boolean fieldBool;

            public DeserializableModel(String fieldString, Integer fieldInt, Boolean fieldBool) {
                this.fieldString = fieldString;
                this.fieldInt = fieldInt;
                this.fieldBool = fieldBool;
            }

            public DeserializableModel() {
            }
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("fieldString", "deserializedValue");
        jsonObject.addProperty("fieldInt", 789);
        jsonObject.addProperty("fieldBool", false);

        DeserializableModel model = Model.deserialize(DeserializableModel.class, jsonObject);

        assertEquals("deserializedValue", model.getFieldString());
        assertEquals(789, model.getFieldInt());
        assertEquals(false, model.getFieldBool());
    }

    @Test
    void testDeserialize_withInvalidFieldsThrowsException() {
        @Getter
        @Setter
        class InvalidModel extends Model {
            @MapsTo("existingField")
            private String existingField;

            public InvalidModel(String existingField) {
                this.existingField = existingField;
            }

            public InvalidModel() {
            }
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("nonExistentField", "value");
        assertDoesNotThrow(() -> {
            Model.deserialize(InvalidModel.class, jsonObject);
        });
    }

    @Test
    void testSerialize_handlesNestedStructures() throws Exception {
        @Getter
        @Setter
        class NestedModel extends Model {
            @MapsTo("fieldList")
            private List<String> fieldList;

            @MapsTo("fieldMap")
            private Map<String, Integer> fieldMap;

            public NestedModel(List<String> fieldList, Map<String, Integer> fieldMap) {
                this.fieldList = fieldList;
                this.fieldMap = fieldMap;
            }

            public NestedModel() {
            }
        }

        List<String> list = Arrays.asList("item1", "item2", "item3");
        HashMap<String, Integer> map = new HashMap<>();
        map.put("key1", 1);
        map.put("key2", 2);

        NestedModel model = new NestedModel(list, map);
        JsonObject jsonObject = Model.serialize(model);

        JsonArray serializedList = jsonObject.getAsJsonArray("fieldList");
        assertEquals(list.size(), serializedList.size());
        assertEquals("item1", serializedList.get(0).getAsString());

        JsonObject serializedMap = jsonObject.getAsJsonObject("fieldMap");
        assertEquals(1, serializedMap.get("key1").getAsInt());
        assertEquals(2, serializedMap.get("key2").getAsInt());
    }
}