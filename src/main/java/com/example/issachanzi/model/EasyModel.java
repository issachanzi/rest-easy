package com.example.issachanzi.model;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;

public abstract class EasyModel {

    // It's usual for a model to have a unique ID, so it shouldn't need to be
    //      manually coded.
    public UUID id = null;

    public void save(Connection db) throws SQLException {
        BasicDao dao = new BasicDao(
                db,
                this.getClass().getSimpleName(),
                columnTypes()
        );

        if (this.id == null) {
            this.id = UUID.randomUUID();

            dao.insert(fieldValues());
        } else {
            dao.update(id, fieldValues());
        }
    }

    public static <M extends EasyModel> M byId(
            Connection db,
            UUID id,
            Class<M> clazz
    ) throws
            SQLException {
        BasicDao dao = new BasicDao(
                db,
                clazz.getSimpleName(),
                columnTypes(clazz)
        );
        Map<String, Object> fieldValues = dao.select(id);

        return unfreezeModel(clazz, fieldValues);
    }

    private static <M extends EasyModel> M unfreezeModel(
            Class<M> clazz,
            Map<String, Object> fieldValues
    ) {
        try {
            M model = clazz.getDeclaredConstructor().newInstance();

            for (var fieldName : fieldValues.keySet()) {
                var field = clazz.getField(fieldName);
                var value = fieldValues.get(fieldName);

                field.set(model, value);
            }

            return model;
        } catch (
                InstantiationException |
                IllegalAccessException |
                InvocationTargetException |
                NoSuchMethodException |
                NoSuchFieldException e
        ) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> fieldValues() {
        try {
            Map<String, Object> result = new HashMap<>();

            for (var field : persistentFields()) {
                result.put(field.getName(), field.get(this));
            }

            return result;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isPersistent(Field field) {
        return SqlDatatypes.isPrimitive(field.getType());
    }

    private Map<String, String> columnTypes() {
        return columnTypes(this.getClass());
    }

    private static Map<String, String> columnTypes(
            Class<? extends EasyModel> clazz
    ) {
        Map<String, String> result = new HashMap<>();

        for (var field : persistentFields(clazz)) {
            String fieldName = field.getName();
            String columnType = SqlDatatypes.forClass(field.getType());

            result.put(fieldName, columnType);
        }

        return result;
    }

    private List<Field> persistentFields() {
        return persistentFields(this.getClass());
    }

    private static List<Field> persistentFields(
            Class<? extends EasyModel> clazz
    ) {
        List<Field> result = new LinkedList<>();
        var fields = clazz.getFields();

        for (var field : fields) {
            if (isPersistent(field)) {
                result.add(field);
            }
        }

        return result;
    }

}

