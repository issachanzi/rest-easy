package net.issachanzi.resteasy.model;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;

import net.issachanzi.resteasy.controller.exception.HttpErrorStatus;
import net.issachanzi.resteasy.model.annotation.NoHttp;
import net.issachanzi.resteasy.model.annotation.NoPersist;
import jakarta.json.JsonObject;
import org.eclipse.jetty.http.ComplianceViolation;

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
 *     {@link #authorize(Connection, String, AccessType)} method.
 * </p>
 */
public abstract class EasyModel {
//    TODO delete this code
//    private static Map <Class <? extends EasyModel>, Association[]> associations
//            = new HashMap <> ();
//    private static Map <Class <? extends EasyModel>, Map <Field, HttpField <?>>>
//            httpFields = new HashMap<>();

    private ModelType modelType;

    /**
     * An ID field as a primary key for the database table.
     *
     * <p>
     *     It's usual for a model to have a unique ID, so it shouldn't have to
     *     be manually coded.
     * </p>
     */
    public UUID id = null;

    protected EasyModel () {
        this.modelType = ModelType.get(this.getClass());
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
        List<HttpField<?>> initFields = this.modelType
                .httpFields()
                .stream()
                .filter(HttpField::canSet)
                .toList();
        for (var field : initFields) {
            if (jsonObject.containsKey(field.name())) {
                try {
                    field.set(this, fieldFromJson(db, jsonObject, field));
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
            HttpField <?> field
    ) throws IllegalArgumentException, SQLException {
        var type = field.type();
        var fieldName = field.name();

        return SqlDatatypes.objectFromJson(fieldName, type, db, jsonObject);
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

                var httpField = this.modelType.httpField(field);
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
                modelType.columnTypes()
        );

        if (this.id == null) {
            this.id = UUID.randomUUID();

            dao.insert(primitivePersistentFieldValues());
        } else {
            dao.update(id, primitivePersistentFieldValues());
        }

        saveAssociations (db);
    }

    private void saveAssociations(Connection db) throws SQLException {
        for (var association : this.modelType.associations()) {
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
                modelType.columnTypes()
        );

        dao.delete(this.id);
    }

    /**
     * Retrieves a model instance from the database based on its id
     *
     * @param db Database connection to use
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

        return byId(db, id, clazz, new Stack<>());
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
     * @param db Database connection to use
     * @param id The id of the model instance to find
     * @param clazz The class of the model instance to find
     * @param chain Model instance to break recursion on
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
            Stack <EasyModel> chain
    ) throws SQLException {
        for (var model : chain) {
            if (
                model.getClass() == clazz
                && model.id.equals(id)
            ) {
                return (M) model;
            }
        }

        try {
            BasicDao dao = new BasicDao(
                    db,
                    clazz.getSimpleName(),
                    ModelType.get(clazz).columnTypes()
            );
            Map<String, Object> fieldValues = dao.select(id);

            return unfreezeModel(clazz, fieldValues, db, chain);
        } catch (NoSuchElementException ex) {
            return null;
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
                ModelType.get(clazz).columnTypes()
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
                ModelType.get(clazz).columnTypes()
        );

        Map<String, Object> filter = new HashMap<>();
        for(String key : strFilter.keySet()) {
            Field field = findField(clazz, key);
            if (field == null) {
                // TODO not sure what to do here
                throw new RuntimeException();
            }
            Class<?> type = field.getType();
            String valueStr = strFilter.get(key);
            Object value;
            if (EasyModel.class.isAssignableFrom(type)) {
                value = valueStr;
            }
            else {
                value = SqlDatatypes.fromString(valueStr, type);
            }

            filter.put(key, value);
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
     * an arbitrary SQL {@code WHERE} clause
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
                ModelType.get(clazz).columnTypes()
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
        return unfreezeModel(clazz, fieldValues, db, new Stack<>());
    }

    private static <M extends EasyModel> M unfreezeModel(
            Class<M> clazz,
            Map<String, Object> fieldValues,
            Connection db,
            Stack <EasyModel> chain
    ) throws SQLException {
        try {
            M model = clazz.getDeclaredConstructor().newInstance();

            for (var fieldName : fieldValues.keySet()) {
                Field field = findField(clazz, fieldName);
                if (field == null) {
                    continue;
                }
                var value = fieldValues.get(fieldName);

                // Special case for UUID
                // TODO - refactor the data types code to be more elegant, with a class for each type
                if (field.getType() == UUID.class) {
                    value = UUID.fromString((String) value);
                }

                field.setAccessible(true);
                field.set(model, value);
                field.setAccessible(false);
            }

            // chainSource is required to avoid an infinite recursion loop
            //      with two models associated with each other
            chain.push (model);
            loadAssociations(db, model, chain);
            chain.pop();

            return model;
        } catch (
                InstantiationException |
                IllegalAccessException |
                InvocationTargetException |
                NoSuchMethodException e
        ) {
            throw new RuntimeException(e);
        }
    }

    private static <M extends EasyModel> Field findField(Class<M> clazz, String fieldName) {
        Field field;
        try {
            field = clazz.getField(fieldName);
        } catch (NoSuchFieldException ex) {
            try {
                field = clazz.getDeclaredField(fieldName);
            }
            catch (NoSuchFieldException ex2) {
                return null;
            }
        }
        return field;
    }

    private static void loadAssociations(
            Connection db,
            EasyModel model,
            Stack <EasyModel> chain
    ) throws SQLException {
        for (var association : model.modelType.associations()) {
            association.load(db, model, chain);
        }
    }

    /**
     * Gets a Map of all fields in this model that can be saved in the
     * database.
     *
     * @return A Map of fields in this model with their values
     */
    public Map<String, Object> primitivePersistentFieldValues() {
        try {
            Map<String, Object> result = new HashMap<>();

            for (var field : modelType.primitivePersistentFields()) {
                field.setAccessible(true);
                result.put(field.getName(), field.get(this));
                field.setAccessible(false);
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
        for (var field : modelType.httpFields()) {
            if (field.canGet()) {
                try {

                    var fieldValue = field.get(this);
                    result.put(field.name(), fieldValue);
                } catch (HttpErrorStatus ignored) {
                }
            }
        }

        return result;
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

        for (var field : ModelType.get(clazz).httpFields()) {
            String name = field.name();
            String type = SqlDatatypes.schemaType(field.type());

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

    @Override
    public boolean equals (Object obj) {
        if (obj instanceof EasyModel) {
            return ((EasyModel) obj).id.equals(this.id);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode () {
        return this.id.hashCode();
    }

}

