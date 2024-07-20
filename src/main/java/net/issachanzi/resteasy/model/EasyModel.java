package net.issachanzi.resteasy.model;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;
import java.sql.Date;

import net.issachanzi.resteasy.controller.exception.HttpErrorStatus;
import net.issachanzi.resteasy.model.annotation.NoHttp;
import net.issachanzi.resteasy.model.annotation.NoPersist;
import net.issachanzi.resteasy.model.association.Association;
import jakarta.json.Json;
import jakarta.json.JsonObject;

import static net.issachanzi.resteasy.model.HttpField.isPublic;

/**
 * Base class for a model in a Rest Easy application
 *
 * <p>
 *     Rest Easy scans the classpath for all subclasses of EasyModel and
 *     exposes all models via a REST API.
 * </p>
 *
 * <p>
 *     A database table will be automatically created for each EasyModel
 *     subclass. All public fields will be automatically persisted when the
 *     {@link #save(Connection)} method is called, if they are of supported
 *     type. This can be overridden with the {@link NoPersist} annotation.
 * </p>
 *
 * <p>
 *     All public fields will be automatically be exposed for read and write
 *     access via a REST API by default. This can be overridden with the
 *     {@link NoHttp} annotation.
 * </p>
 *
 * <p>
 *     Authorisation checks can be done by overriding the
 *     {@link #authorize(String, AccessType)} method.
 * </p>
 */
public abstract class EasyModel {
    private static Map <Class <? extends EasyModel>, Association[]> associations
            = new HashMap <> ();
    private static Map <Class <? extends EasyModel>, Map <Field, HttpField <?>>>
            httpFields = new HashMap<>();

    /**
     * An ID field as a primary key for the database table.
     *
     * <p>
     *     It's usual for a model to have a unique ID, so it shouldn't have to
     *     be manually coded.
     * </p>
     */
    public UUID id = null;

    /**
     * Instantiate a model based on its data in JSON format
     *
     * @param db Database connection to use to perform queries
     * @param json The JSON data to populate the model instance with
     * @param clazz The specific subclass to instantiate
     * @return The new model, populated with the supplied JSON data
     * @param <T> The specific subclass to instantiate
     */
    public static <T extends EasyModel> T fromJson (
            Connection db,
            String json,
            Class<T> clazz
    ) throws
            InvocationTargetException,
            NoSuchMethodException,
            InstantiationException,
            IllegalAccessException,
            SQLException,
            HttpErrorStatus
    {
        var reader = Json.createReader (new StringReader(json));
        var jsonObject = reader.readObject();

        return fromJson (db, jsonObject, clazz);
    }

    /**
     * Instantiate a model based on its data in JSON format
     *
     * @param db Database connection to use to perform queries
     * @param jsonObject The JSON data to populate the model instance with
     * @param clazz The specific subclass to instantiate
     * @return The new model, populated with the supplied JSON data
     * @param <T> The specific subclass to instantiate
     */
    private static <T extends EasyModel> T fromJson (
           Connection db,
           JsonObject jsonObject,
           Class <T> clazz
    ) throws
            NoSuchMethodException,
            InvocationTargetException,
            InstantiationException,
            IllegalAccessException,
            SQLException,
            HttpErrorStatus
    {
        T model = clazz.getDeclaredConstructor().newInstance();

        model.init(db, jsonObject);

        return model;
    }

    /**
     * Initialises a newly created model instance.
     *
     * <p>
     *     Override this method to create custom init behaviour.
     * </p>
     *
     * <p>
     *     The default implementation populates all the fields in the model
     *     with values from the request body.
     * </p>
     *
     * @param db Database connection to use
     * @param jsonObject Request body parsed as JSON
     * @throws SQLException If a query fails
     */
    protected void init(
            Connection db,
            JsonObject jsonObject
    ) throws SQLException, HttpErrorStatus {
        var clazz = this.getClass();

        for (var field : persistentFields(this.getClass())) {
            if (jsonObject.containsKey(field.getName())) {
                var httpField = httpFields.get(clazz).get(field);
                try {
                    httpField.set(this, fieldFromJson(db, jsonObject, field));
                } catch (HttpErrorStatus ignored) {}
            }
        }
    }

    /**
     * Gets the value of a field based on JSON data
     *
     * @param db Database connection to use to perform queries
     * @param jsonObject The JSON data to populate the model instance with
     * @param field The field to find the value of
     *
     * @return The value to populate the field with
     * @throws IllegalArgumentException if the field is not a supported type
     * @throws SQLException if a query error is encountered while querying an
     *                      association
     */
    @SuppressWarnings("unchecked")
    private static Object fieldFromJson (
            Connection db,
            JsonObject jsonObject,
            Field field
    ) throws IllegalArgumentException, SQLException {
        var type = field.getType();
        var fieldName = field.getName();

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
            return Date.valueOf (jsonObject.getString(fieldName));
        }
        else if (type == Time.class) {
            return Time.valueOf (jsonObject.getString(fieldName));
        }
        else if (type == Timestamp.class) {
            return Timestamp.valueOf (jsonObject.getString(fieldName));
        }
        if (EasyModel.class.isAssignableFrom(type)) {
            return EasyModel.byId (
                    db,
                    UUID.fromString(jsonObject.getString(fieldName)),
                    (Class <? extends EasyModel>) type
            );
        }
        else if (jsonObject.isNull(fieldName)) {
            return null;
        }

        throw new IllegalArgumentException("Unsupported type " + type.getName());
    }

    /**
     * TODO
     * Updates the data of this model instance
     *
     * This method is WIP
     *
     * @param data Field data to update
     */
    public void update(Map<String, String> data) {
        for(var fieldName : data.keySet()) {
            try {
                var clazz = this.getClass();
                var field = clazz.getField(fieldName);
                var fieldType = field.getType();
                var fieldValueStr = data.get(fieldName);
                var fieldValue = SqlDatatypes.fromString(
                        fieldValueStr,
                        fieldType
                );

                var httpField = httpFields.get(this.getClass()).get(field);
                httpField.set(this, fieldValue);
            } catch (NoSuchFieldException |
                     HttpErrorStatus ignored) {}
        }
    }

    /**
     * Saves this model instance to the database
     *
     * @param db Database connection to use for queries
     * @throws SQLException If a query fails
     */
    public void save(Connection db) throws SQLException {
        BasicDao dao = new BasicDao(
                db,
                this.getClass().getSimpleName(),
                columnTypes()
        );

        if (this.id == null) {
            this.id = UUID.randomUUID();

            dao.insert(persistentFieldValues());
        } else {
            dao.update(id, persistentFieldValues());
        }

        saveAssociations (db);
    }

    private void saveAssociations(Connection db) throws SQLException {
        for (var association : associations.get(this.getClass())) {
            association.save(db, this);
        }
    }

    /**
     * Deletes this model instance from the database
     *
     * @param db Database connection to use
     * @throws SQLException If a query fails
     */
    public void delete(Connection db) throws SQLException {
        BasicDao dao = new BasicDao(
                db,
                this.getClass().getSimpleName(),
                columnTypes()
        );

        dao.delete(this.id);
    }

    /**
     * Retrieves a model instance from the database based on its id
     *
     * @param db Databbase connection to use
     * @param id The id of the model instance to find
     * @param clazz The class of the model instance to find
     *
     * @return The model instance with the specified id
     * @param <M> The class of the model instance to find
     * @throws SQLException If a database query fails
     */
    public static <M extends EasyModel> M byId(
            Connection db,
            UUID id,
            Class<M> clazz
    ) throws SQLException {
        boolean lazy = false;

        return byId(db, id, clazz, null);
    }

    /**
     * Retrieves a model instance from the database based on its id
     *
     * <p>
     *     This version of the method exists to break recursion loops caused by
     *     fetching associations. If the {@code id} parameter matches the id of
     *     the model instance specified by {@code chainSource}, then this
     *     method will simply return the value {@code chainSource} without
     *     querying the database or fetching any data.
     * </p>
     *
     * @param db Databbase connection to use
     * @param id The id of the model instance to find
     * @param clazz The class of the model instance to find
     * @param chainSource Model instance to break recursion on
     *
     * @return The model instance with the specified id
     * @param <M> The class of the model instance to find
     * @throws SQLException If a database query fails
     */
    @SuppressWarnings("unchecked")
    public static <M extends EasyModel> M byId(
            Connection db,
            UUID id,
            Class<M> clazz,
            EasyModel chainSource
    ) throws SQLException {
        if (
            chainSource != null
            && chainSource.getClass() == clazz
            && chainSource.id.equals(id)
        ) {
            return (M) chainSource;
        }
        else {
            BasicDao dao = new BasicDao(
                    db,
                    clazz.getSimpleName(),
                    columnTypes(clazz)
            );
            Map<String, Object> fieldValues = dao.select(id);

            return unfreezeModel(clazz, fieldValues, db, chainSource);
        }
    }

    /**
     * Retrieves all instances of a model from the database
     *
     * @param db The database connection to use
     * @param clazz The model class to return instances of
     *
     * @return A collection of all instances of the specified model
     * @param <M> The model class to return instances of
     * @throws SQLException If a query fails
     */
    public static <M extends EasyModel> Collection<M> all(
            Connection db,
            Class<M> clazz
    ) throws SQLException {
        BasicDao dao = new BasicDao(
                db,
                clazz.getSimpleName(),
                columnTypes(clazz)
        );

        Collection<M> results = new LinkedList<>();
        var rows = dao.select();
        for (var row : rows) {
            var model = unfreezeModel(clazz, row, db);
            results.add(model);
        }

        return results;
    }

    /**
     * Retrieves from the database instances of a specified model that matches
     * specified filter conditions
     *
     * <p>
     *     Models will be returned if, for each specified filter key, the
     *     corresponding model attribute matches the value given,
     * </p>
     *
     * @param db The database connection to use
     * @param strFilter The map of filter conditions to apply
     * @param clazz The model class to return instances of
     * @return A collection of model instances matching the filter criteria
     * @param <M> The model class to return instances of
     * @throws SQLException If a database query fails
     */
    public static <M extends EasyModel> Collection<M> where(
            Connection db,
            Map<String, String> strFilter,
            Class<M> clazz
    ) throws SQLException {
        BasicDao dao = new BasicDao(
                db,
                clazz.getSimpleName(),
                columnTypes(clazz)
        );

        Map<String, Object> filter = new HashMap<>();
        for(String key : strFilter.keySet()) {
            try {
                Class<?> type = clazz.getField(key).getType();
                String valueStr = strFilter.get(key);
                Object value = SqlDatatypes.fromString(valueStr, type);
                filter.put(key, value);
            } catch (NoSuchFieldException e) {
                continue;
            }
        }

        Collection<M> results = new LinkedList<>();
        var rows = dao.where(filter);
        for (var row : rows) {
            var model = unfreezeModel(clazz, row, db);
            results.add(model);
        }

        return results;
    }

    /**
     * Retrieves from the database instances of a specified model that match
     * an arbitrary SQL {@code WHERE} clauss
     *
     * <p>
     *     <b>Never</b> interpolate user input or untrusted data of any kind
     *     into SQL queries (such as the {@code whereSql} param). Always use
     *     parameterised queries, which should be hard coded wherever possible.
     *</p>
     *
     * <p>
     *     <a href="https://www.cloudflare.com/learning/security/threats/sql-injection/">
     *         What is SQL injection - Cloudflare
     *     </a>
     * </p>
     *
     * @param db The database connection to use
     * @param whereSql The SQL {@code WHERE} clause. {@code ?} characters can
     *                 be used for parameter placeholders. This parameter must
     *                 never contain untrusted data such as user input.
     * @param clazz The model class to return instances of
     * @return A collection of model instances matching the filter criteria
     * @param <M> The model class to return instances of
     * @throws SQLException If a database query fails
     */
    public static <M extends EasyModel> Collection<M> where(
            Connection db,
            String whereSql,
            Object[] params,
            Class<M> clazz
    ) throws SQLException {
        BasicDao dao = new BasicDao(
                db,
                clazz.getSimpleName(),
                columnTypes(clazz)
        );

        Collection<M> results = new LinkedList<>();
        var rows = dao.where(whereSql, params);
        for (var row : rows) {
            var model = unfreezeModel(clazz, row, db);
            results.add(model);
        }

        return results;
    }


    private static <M extends EasyModel> M unfreezeModel (
            Class <M> clazz,
            Map <String, Object> fieldValues,
            Connection db
    ) throws SQLException {
        return unfreezeModel(clazz, fieldValues, db, null);
    }

    private static <M extends EasyModel> M unfreezeModel(
            Class<M> clazz,
            Map<String, Object> fieldValues,
            Connection db,
            EasyModel chainSource
    ) throws SQLException {
        try {
            M model = clazz.getDeclaredConstructor().newInstance();

            for (var fieldName : fieldValues.keySet()) {
                var field = clazz.getField(fieldName);
                var value = fieldValues.get(fieldName);

                // Special case for UUID
                // TODO - refactor the data types code to be more elegant, with a class for each type
                if (field.getType() == UUID.class) {
                    value = UUID.fromString((String) value);
                }

                field.set(model, value);
            }

            // chainSource is required to avoid an infinite recursion loop
            //      with two models associated with each other
            if (chainSource == null) {
                chainSource = model;
            }
            loadAssociations(db, model, chainSource);

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

    private static void loadAssociations(
            Connection db,
            EasyModel model,
            EasyModel chainSource
    ) throws SQLException {
        for (var association : associations.get(model.getClass())) {
            association.load(db, model, chainSource);
        }
    }

    /**
     * Sets up the database table for a specified model class
     *
     * @param db Database connection to use
     * @param clazz The class to set up the table for
     */
    public static void sync (
            Connection db,
            Class <? extends EasyModel> clazz
    ) {
        setupHttpFields(clazz);

        BasicDao dao = new BasicDao(
                db,
                clazz.getSimpleName(),
                columnTypes(clazz)
        );

        try {
            dao.createTable();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets up the associations for a specified model in the database
     *
     * @param db Database connection to use
     * @param clazz The model class to set up associations for
     */
    public static void syncAssociations(
            Connection db,
            Class<? extends EasyModel> clazz
    ) {
        var modelAssociations = new Vector<Association>();

        for (var field : clazz.getFields()) {
            var association = Association.forField (clazz, field);

            if (association != null) {
                modelAssociations.add(association);

                try {
                    association.init(db);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        associations.put(clazz, modelAssociations.toArray(new Association [0]));
    }

    /**
     * Gets a Map of all fields in this model that can be saved in the
     * database.
     *
     * @return A Map of fields in this model with their values
     */
    public Map<String, Object> persistentFieldValues() {
        try {
            Map<String, Object> result = new HashMap<>();

            for (var field : primitiveFields()) {
                result.put(field.getName(), field.get(this));
            }

            return result;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets a Map of all fields in this model that can be exposed via the REST
     * API
     *
     * @return A Map of fields in this model with their values
     */
    public Map <String, Object> httpFieldValues (String authorization) {
        Map<String, Object> result = new HashMap<>();

        // TODO implement authorisation
        for (var field : httpFields()) {
            try {
                var fieldValue = httpFields
                        .get(this.getClass())
                        .get(field)
                        .get(this);
                result.put(field.getName(), fieldValue);
            } catch (HttpErrorStatus ignored) { }
        }

        return result;
    }

    private static void setupHttpFields (Class <? extends EasyModel> clazz) {
        Map <Field, HttpField <?>> httpFields = new HashMap<>();

        var publicFields = clazz.getFields();
        for (var field : publicFields) {
            var httpField = HttpField.forField(clazz, field, field.getType());

            if (httpField.canGet() || httpField.canSet()) {
                httpFields.put(field, httpField);
            }
        }

        var declaredFields = clazz.getDeclaredFields();
        for (var field : declaredFields) {
            if (!isPublic (field)) {
                var httpField = HttpField.forField(clazz, field, field.getType());

                if (httpField.canGet() || httpField.canSet()) {
                    httpFields.put(field, httpField);
                }
            }
        }

        EasyModel.httpFields.put(clazz, httpFields);
    }



    private Collection <Field> httpFields () {
        return httpFields(this.getClass());
    }

    private static Collection <Field> httpFields (
            Class <? extends EasyModel> clazz
    ) {
        // Return all fields that **do not** have a @NoHttp annotation
        return Arrays.stream(clazz.getFields())
                .filter(field -> field.getAnnotation(NoHttp.class) == null)
                .toList();
    }

    /**
     * Gets a Map of fields in this model exposed via the REST API with, if
     * they represent an association with another model, the name of the other
     * model.
     *
     * <p>
     *     This is planned to be used for a frontend JavaScript library to
     *     abstract REST API requests for Rest Easy applications.
     * </p>
     *
     * @param clazz The model class to get the schema for
     * @return Map of fields in this model exposed via the REST API with, if
     *         they represent an association with another model, the name of
     *         the other model, or null otherwise.
     */
    static Map <String, String> schema (
            Class <? extends EasyModel> clazz
    ) {
        Map <String, String> result = new HashMap <> ();

        for (var field : httpFields(clazz)) {
            String name = field.getName();
            String type = SqlDatatypes.schemaType(field.getType());

            result.put (name, type);
        }

        return result;
    }

    /**
     * Checks whether an attempt to access this model instance is allowed
     *
     * <p>
     *     This method should be overridden by subclasses of {@code EasyModel},
     *     if authorisation is desired.
     * </p>
     *
     * <p>
     *     The default implementation always returns true, allowing any user to
     *     create, read, update and delete {@code EasyModel} instances without
     *     authentication.
     * </p>
     *
     * @param authorization The value of the {@code Authorization} HTTP header
     * @param accessType The type of operation requested. Either CREATE, READ
     *                   UPDATE or DELETE.
     * @return {@code true} if the access is allowed. {@code false} if it is
     *         denied.
     */
    public boolean authorize (
            Connection db,
            String authorization,
            AccessType accessType
    ) throws HttpErrorStatus {
        return true;
    }

    private static boolean isPersistent(Field field) {
        if (field.getAnnotation(NoPersist.class) != null) {
            return false;
        }
        else if (SqlDatatypes.isPrimitive(field.getType())) {
            return true;
        }
        else if (EasyModel.class.isAssignableFrom(field.getType())) {
            return true;
        }
        else {
            return false;
        }
    }

    private Map<String, String> columnTypes() {
        return columnTypes(this.getClass());
    }

    private static Map<String, String> columnTypes(
            Class<? extends EasyModel> clazz
    ) {
        Map<String, String> result = new HashMap<>();

        for (var field : primitiveFields (clazz)) {
            String fieldName = field.getName();
            String columnType = SqlDatatypes.forClass(field.getType());

            result.put(fieldName, columnType);
        }

        return result;
    }

    private List<Field> primitiveFields() {
        return primitiveFields (this.getClass());
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

    private static List<Field> primitiveFields(
            Class<? extends EasyModel> clazz
    ) {
        List<Field> result = new LinkedList<>();
        var fields = clazz.getFields();

        for (var field : fields) {
            if (SqlDatatypes.isPrimitive (field.getType())) {
                result.add(field);
            }
        }

        return result;
    }
}

