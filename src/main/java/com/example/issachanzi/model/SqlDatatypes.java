package com.example.issachanzi.model;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class SqlDatatypes {
    private SqlDatatypes () {}

    public static boolean isPrimitive (Class<?> type) {
        return forClass(type) == null;
    }

    public static String forClass(Class<?> type) {
        if (type == String.class) {
            return "varchar(255)";
        } else if (type == boolean.class || type == Boolean.class) {
            return "boolean";
        } else if (type == byte.class || type == Byte.class) {
            return "tinyint";
        } else if (type == short.class || type == Short.class) {
            return "smallint";
        } else if (type == int.class || type == Integer.class) {
            return "integer";
        } else if (type == long.class || type == Long.class) {
            return "bigint";
        } else if (type == float.class || type == Float.class) {
            return "real";
        } else if (type == double.class || type == Double.class) {
            return "double";
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
}