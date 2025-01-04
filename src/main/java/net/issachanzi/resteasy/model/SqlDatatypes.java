package net.issachanzi.resteasy.model;

import jakarta.json.JsonObject;

import java.sql.*;
import java.util.UUID;

/**
 * Utility class to look up and convert between data types
 */
public class SqlDatatypes {
    // This code is so ugly. I'm sorry. I will maybe refactor it some time.

    private SqlDatatypes () {}

    /**
     * Look up whether a type can be stored directly in the database
     *
     * @param type The type to look up
     * @return {@code true} if that type can be stored directly in the
     *         database, {@code false} if it cannot.
     */
    public static boolean isPrimitive (Class<?> type) {
        return forClass(type) != null;
    }

    /**
     * Look up the appropriate SQL data type for a given java {@code Class}.
     *
     * @param type The Java type to look up
     * @return The corresponding SQL data type
     */
    public static String forClass(Class<?> type) {
        if (type == UUID.class) {
            return "char(36)";
        } else if (type == String.class) {
            return "varchar(255)";
        } else if (type == boolean.class || type == Boolean.class) {
            return "boolean";
        }
//          These were causing errors
//          I don't think it's necessary for an MVP to support these types
//          Disabling this code for now
//        // TODO - make small integer types
//        else if (type == byte.class || type == Byte.class) {
//            return "smallint";
//        } else if (type == short.class || type == Short.class) {
//            return "smallint";
//        }
        else if (type == int.class || type == Integer.class) {
            return "integer";
        } else if (type == long.class || type == Long.class) {
            return "bigint";
        } else if (type == float.class || type == Float.class) {
            return "real";
        } else if (type == double.class || type == Double.class) {
            return "double precision";
        } else if (type == Date.class) {
            return "date";
        } else if (type == Time.class) {
            return "time";
        } else if (type == Timestamp.class) {
            return "timestamp";
        } else {
            return null;
        }
    }

    /**
     * Convert a value from a {@code String} to a specified type
     *
     * @param string The string to convert
     * @param type The desired type
     * @return The converted value
     * @param <T> The desired type
     */
    @SuppressWarnings("unchecked") // Type must match for == comparison to be true
    public static <T> T fromString(String string, Class<T> type) {
        if (type == UUID.class) {
            return (T) UUID.fromString(string);
        }
        else if (type == String.class) {
            return (T) string;
        }
        else if (type == boolean.class || type == Boolean.class) {
            return (T) Boolean.valueOf(string);
        }
        else if (type == byte.class || type == Byte.class) {
            return (T) Byte.valueOf(string);
        }
        else if (type == short.class || type == Short.class) {
            return (T) Short.valueOf(string);
        }
        else if (type == int.class || type == Integer.class) {
            return (T) Integer.valueOf(string);
        }
        else if (type == long.class || type == Long.class) {
            return (T) Long.valueOf(string);
        }
        else if (type == float.class || type == Float.class) {
            return (T) Float.valueOf(string);
        }
        else if (type == double.class || type == Double.class) {
            return (T) Double.valueOf(string);
        }
        else if (type == Date.class) {
            return (T) new Date (Long.valueOf(string));
        }
        else if (type == Time.class) {
            return (T) new Time (Long.valueOf(string));
        }
        else if (type == Timestamp.class) {
            return (T) new Timestamp(Long.valueOf(string));
        }
        else {
            return null;
        }
    }

    /**
     * Gets the type as to be included in the schema
     *
     * <p>
     *     Effectively, if the {@code type} is a subclass of EasyModel, returns
     *     the name of the type, otherwise returns {@code null}.
     * </p>
     *
     * @param type The type to look up
     * @return The name of the model if {@code type} is a subclass of
     * {@link EasyModel}, or {@code null} otherwise.
     */
    public static String schemaType (Class <?> type) {
        if (EasyModel.class.isAssignableFrom(type)) {
            return type.getSimpleName();
        }
        else if (type == UUID.class) {
            return "text";
        } else if (type == String.class) {
            return "text";
        } else if (type == boolean.class || type == Boolean.class) {
            return "checkbox";
        }
        else if (type == int.class || type == Integer.class) {
            return "number";
        } else if (type == long.class || type == Long.class) {
            return "number";
        } else if (type == float.class || type == Float.class) {
            return "number";
        } else if (type == double.class || type == Double.class) {
            return "number";
        } else if (type == Date.class) {
            return "date";
        } else if (type == Time.class) {
            return "time";
        } else if (type == Timestamp.class) {
            return "datetime-local";
        } else {
            return null;
        }
    }

    public static Object objectFromJson(
            String fieldName,
            Class<?> type,
            Connection db,
            JsonObject jsonObject
    ) throws SQLException {
        if (type == UUID.class) {
            return UUID.fromString(jsonObject.getString(fieldName));
        }
        else if (type == String.class) {
            return jsonObject.getString(fieldName);
        }
        else if (type == boolean.class || type == Boolean.class) {
            return jsonObject.getBoolean(fieldName);
        }
        else if (type == byte.class || type == Byte.class) {
            return (byte) jsonObject.getInt(fieldName);
        }
        else if (type == short.class || type == Short.class) {
            return (short) jsonObject.getInt(fieldName);
        }
        else if (type == int.class || type == Integer.class) {
            return jsonObject.getInt(fieldName);
        }
        else if (type == long.class || type == Long.class) {
            return jsonObject.getJsonNumber(fieldName).longValue();
        }
        else if (type == float.class || type == Float.class) {
            return (float) jsonObject.getJsonNumber(fieldName).doubleValue();
        }
        else if (type == double.class || type == Double.class) {
            return jsonObject.getJsonNumber(fieldName).doubleValue();
        }
        else if (type == Date.class) {
            return new Date(jsonObject.getJsonNumber(fieldName).longValue());
        }
        else if (type == Time.class) {
            return new Time(jsonObject.getJsonNumber(fieldName).longValue());
        }
        else if (type == Timestamp.class) {
            return new Timestamp(jsonObject.getJsonNumber(fieldName).longValue());
        }
        if (EasyModel.class.isAssignableFrom(type)) {
            return EasyModel.byId(
                    db,
                    UUID.fromString(jsonObject.getString(fieldName)),
                    (Class<? extends EasyModel>) type
            );
        }
        else if (jsonObject.isNull(fieldName)) {
            return null;
        }

        throw new IllegalArgumentException("Unsupported type " + type.getName());
    }
}