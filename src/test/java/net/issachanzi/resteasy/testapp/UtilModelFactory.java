package net.issachanzi.resteasy.testapp;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import net.issachanzi.resteasy.testapp.model.Model;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.UUID;

import static net.issachanzi.resteasy.testapp.UtilModelFactory.WithId.WITH_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilModelFactory {
    
    public void assertModelsEqual(
        JsonObject expected,
        JsonObject actual,
        WithId withId
    ) {
        if (withId == WITH_ID) {
            assertEquals(expected.getString("uuid"), actual.getString("id"));
        }
        assertEquals(expected.getString("uuid"), actual.getString("uuid"));
        assertEquals(expected.getString("string"), actual.getString("string"));
        assertEquals(
            expected.getBoolean("primitiveBoolean"),
            actual.getBoolean("primitiveBoolean")
        );
        assertEquals(
            expected.getBoolean("wrappedBoolean"),
            actual.getBoolean("wrappedBoolean")
        );
        assertEquals(
            expected.getJsonNumber("primitiveLong").longValue(),
            actual.getJsonNumber("primitiveLong").longValue()
        );
        assertEquals(
            expected.getJsonNumber("wrappedLong").longValue(),
            actual.getJsonNumber("wrappedLong").longValue()
        );
        assertEquals(
            expected.getJsonNumber("primitiveFloat").numberValue().floatValue(),
            actual.getJsonNumber("primitiveFloat").numberValue().floatValue()
        );
        assertEquals(
            expected.getJsonNumber("wrappedFloat").numberValue().floatValue(),
            actual.getJsonNumber("wrappedFloat").numberValue().floatValue()
        );
        assertEquals(
            expected.getJsonNumber("primitiveDouble").doubleValue(),
            actual.getJsonNumber("primitiveDouble").doubleValue()
        );
        assertEquals(
            expected.getJsonNumber("wrappedDouble").doubleValue(),
            actual.getJsonNumber("wrappedDouble").doubleValue()
        );
    }
    
    public enum WithId {
        WITH_ID,
        NO_ID
    }
    
    public Model createModel() {
        long millis = System.currentTimeMillis();
        Model model = new Model();
        
        model.uuid = UUID.fromString("908d65c5-299f-424a-9d48-a6a569ba70f0");
        model.string = "foobar";
        model.primitiveBoolean = true;
        model.wrappedBoolean = true;
        model.primitiveLong = 42L;
        model.wrappedLong = 42L;
        model.primitiveFloat = 3.14F;
        model.wrappedFloat = 3.14F;
        model.primitiveDouble = 3.14D;
        model.wrappedDouble = 3.14D;
        model.date = new Date(millis);
        model.time = new Time(millis);
        model.timestamp = new Timestamp(millis);
        
        return model;
    }
    
    public JsonObject createJson() {
        Model model = createModel();
        
        return modelToJsonObject(model);
    }
    
    public void assertModelsEqual(
        Model expected,
        JsonObject actual,
        WithId withId
    ) {
        if (withId == WITH_ID) {
            assertEquals(expected.id, UUID.fromString(actual.getString("id")));
        }
        assertEquals(expected.uuid, UUID.fromString(actual.getString("uuid")));
        assertEquals(expected.string, actual.getString("string"));
        assertEquals(
            expected.primitiveBoolean,
            actual.getBoolean("primitiveBoolean")
        );
        assertEquals(
            expected.wrappedBoolean,
            actual.getBoolean("wrappedBoolean")
        );
        assertEquals(
            expected.primitiveLong,
            actual.getJsonNumber("primitiveLong").longValue()
        );
        assertEquals(
            expected.wrappedLong.longValue(),
            actual.getJsonNumber("wrappedLong").longValue()
        );
        assertEquals(
            expected.primitiveFloat,
            actual.getJsonNumber("primitiveFloat").numberValue().floatValue()
        );
        assertEquals(
            expected.wrappedFloat.floatValue(),
            actual.getJsonNumber("wrappedFloat").numberValue().floatValue()
        );
        assertEquals(
            expected.primitiveDouble,
            actual.getJsonNumber("primitiveDouble").doubleValue()
        );
        assertEquals(
            expected.wrappedDouble.doubleValue(),
            actual.getJsonNumber("wrappedDouble").doubleValue()
        );
    }
    
    public JsonObject modelToJsonObject(Model model) {
        
        return Json.createObjectBuilder()
            .add("uuid", model.uuid.toString())
            .add("string", model.string)
            .add("primitiveBoolean", model.primitiveBoolean)
            .add("wrappedBoolean", model.wrappedBoolean)
            .add("primitiveLong", model.primitiveLong)
            .add("wrappedLong", model.wrappedLong)
            .add("primitiveFloat", model.primitiveFloat)
            .add("wrappedFloat", model.wrappedFloat)
            .add("primitiveDouble", model.primitiveDouble)
            .add("wrappedDouble", model.wrappedDouble)
            .add("date", model.date.getTime())
            .add("time", model.time.getTime())
            .add("timestamp", model.timestamp.getTime())
            .build();
    }
}
