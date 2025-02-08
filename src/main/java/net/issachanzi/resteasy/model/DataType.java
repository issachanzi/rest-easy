package net.issachanzi.resteasy.model;

import jakarta.json.JsonObject;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataType {
    private static final Map<Class <?>, DataType> dataTypes = new HashMap<> ();

    public static boolean isPrimitive (Class <?> clazz) {
        return dataTypes.containsKey (clazz);
    }

    public static DataType forClass (Class <?> clazz) {
        if (dataTypes.containsKey(clazz)) {
            return dataTypes.get(clazz);
        }
        else {
            throw new IllegalArgumentException (
                "Unsupported type " + clazz.getName()
            );
        }
    }

    private final String sqlType;
    private final String schemaType;
    private final MatchesType matchesType;
    private final FromString fromString;
    private final FromJson fromJson;

    public DataType (
        String sqlType,
        String schemaType,
        MatchesType matchesType,
        FromString fromString,
        FromJson fromJson
    ) {
        this.sqlType = sqlType;
        this.schemaType = schemaType;
        this.matchesType = matchesType;
        this.fromString = fromString;
        this.fromJson = fromJson;
    }

    public String sqlType () {
        return sqlType;
    }

    public String schemaType () {
        return schemaType;
    }

    public boolean matchesType(Class<?> clazz) {
        return matchesType.matchesType(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T> T fromJson(String fieldName, Connection db, JsonObject jsonObject) {
        return (T) fromJson.fromJson (fieldName, db, jsonObject);
    }

    @SuppressWarnings("unchecked")
    public <T> T fromString(String string) {
        return (T) fromString.fromString(string);
    }

    public interface MatchesType {
        boolean matchesType (Class <?> clazz);
    }

    public interface FromString {
        Object fromString (String string);
    }

    public interface FromJson {
        Object fromJson (String fieldName, Connection db, JsonObject jsonObject);
    }

    static {
        dataTypes.put(
            UUID.class,
            new DataType (
                "char(36)",
                "text",
                UUID.class::equals,
                UUID::fromString,
                (
                    fieldName,
                    db,
                    json
                ) -> UUID.fromString(json.getString(fieldName))
            )
        );
        dataTypes.put(
            String.class,
            new DataType(
                "varchar(255)",
                    "text",
                    String.class::equals,
                    String::new,
                    (
                        fieldName,
                        db,
                        json
                    ) -> json.getString(fieldName)
            )
        );
        dataTypes.put (
            boolean.class,
            new DataType(
                "boolean",
                "checkbox",
                either (boolean.class::equals, Boolean.class::equals),
                Boolean::valueOf,
                (
                    fieldName,
                    db,
                    json
                ) -> json.getBoolean(fieldName)
            )
        );
        dataTypes.put(Boolean.class, dataTypes.get (boolean.class));
        dataTypes.put(
            int.class,
            new DataType(
                "integer",
                "number",
                either (int.class::equals, Integer.class::equals),
                Integer::valueOf,
                (
                    fieldName,
                    db,
                    json
                ) -> json.getInt(fieldName)
            )
        );
        dataTypes.put (Integer.class, dataTypes.get(int.class));
        dataTypes.put (
            long.class,
            new DataType(
                "bigint",
                "number",
                either (long.class::equals, Long.class::equals),
                Long::valueOf,
                (
                    fieldName,
                    db,
                    json
                ) -> json.getJsonNumber(fieldName).longValue()
            )
        );
        dataTypes.put (Long.class, dataTypes.get(long.class));
        dataTypes.put(
            float.class,
            new DataType(
                "real",
                "number",
                either (float.class::equals, Float.class::equals),
                Float::valueOf,
                (
                        fieldName,
                        db,
                        json
                ) -> (float) json.getJsonNumber(fieldName).doubleValue()
            )
        );
        dataTypes.put (Float.class, dataTypes.get(float.class));
        dataTypes.put(
            double.class,
            new DataType(
                "double precision",
                "number",
                either (double.class::equals, Double.class::equals),
                Double::valueOf,
                (
                    fieldName,
                    db,
                    json
                ) -> json.getJsonNumber(fieldName).doubleValue()
            )
        );
        dataTypes.put (Double.class, dataTypes.get(double.class));
        dataTypes.put (
            Date.class,
            new DataType (
                "date",
                "date",
                Date.class::equals,
                string -> new Date (Long.parseLong(string)),
                (
                        fieldName,
                        db,
                        json
                ) -> new Date (json.getJsonNumber(fieldName).longValue())
            )
        );
        dataTypes.put (
            Time.class,
            new DataType(
                "time",
                "time",
                Time.class::equals,
                string -> new Time (Long.parseLong(string)),
                (
                    fieldName,
                    db,
                    json
                ) -> new Time (json.getJsonNumber(fieldName).longValue())
            )
        );
        dataTypes.put(
            Timestamp.class,
            new DataType(
                "timestamp",
                "datetime-local",
                Timestamp.class::equals,
                string -> new Timestamp (Long.parseLong(string)),
                (
                    fieldName,
                    db,
                    json
                ) -> new Time (json.getJsonNumber(fieldName).longValue())
            )
        );
    }

    private static MatchesType either (MatchesType a, MatchesType b) {
        return clazz -> a.matchesType(clazz) || b.matchesType(clazz);
    }
}
