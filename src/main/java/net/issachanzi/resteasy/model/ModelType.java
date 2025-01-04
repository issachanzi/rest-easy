package net.issachanzi.resteasy.model;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import net.issachanzi.resteasy.controller.exception.HttpErrorStatus;
import net.issachanzi.resteasy.model.annotation.CustomMethod;
import net.issachanzi.resteasy.model.annotation.NoHttp;
import net.issachanzi.resteasy.model.annotation.NoPersist;
import net.issachanzi.resteasy.model.association.Association;

import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.lang.reflect.Field;

import static net.issachanzi.resteasy.model.HttpField.isPublic;

public class ModelType {
    private static final Map<Class <? extends EasyModel>, ModelType> modelTypes
            = new HashMap <> ();

    private final Class <? extends EasyModel> clazz;
    private Association [] associations;
    private Map <Field, HttpField <?>>  httpFields = new HashMap<>();
    private Map <String, Method> customMethods = new HashMap<> ();

    private ModelType (Class <? extends EasyModel> clazz) {
        // TODO
        this.clazz = clazz;

        setupHttpFields();
        setupCustomMethods();

        modelTypes.put (clazz, this);
    }

    public static ModelType get (Class <? extends EasyModel> clazz) {
        if (modelTypes.containsKey (clazz)) {
            return modelTypes.get (clazz);
        }
        else {
            return new ModelType (clazz);
        }
    }

    Collection <HttpField <?>> httpFields() {
        // Return all fields that **do not** have a @NoHttp annotation
        return Collections.unmodifiableCollection(httpFields.values());
    }

    List<Field> persistentFields() {
        List<Field> result = new LinkedList<>();
        Set<Field> fields = new HashSet<> (List.of(clazz.getFields()));
        fields.addAll(List.of(clazz.getDeclaredFields()));

        for (var field : fields) {
            if (isPersistent(field)) {
                result.add(field);
            }
        }

        return result;
    }

    private static boolean isPersistent(Field field) {
        if (field.getAnnotation(NoPersist.class) != null) {
            return false;
        }
        else if ((field.getModifiers() & Modifier.STATIC) != 0) {
            // Static fields are not persistent.
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

    /**
     * Instantiate a model based on its data in JSON format
     *
     * @param db Database connection to use to perform queries
     * @param json The JSON data to populate the model instance with
     * @return The new model, populated with the supplied JSON data
     */
    public EasyModel fromJson (
            Connection db,
            String json
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

        return fromJson (db, jsonObject);
    }

    /**
     * Instantiate a model based on its data in JSON format
     *
     * @param db Database connection to use to perform queries
     * @param jsonObject The JSON data to populate the model instance with
     * @return The new model, populated with the supplied JSON data
     */
    private EasyModel fromJson (
            Connection db,
            JsonObject jsonObject
    ) throws
            NoSuchMethodException,
            InvocationTargetException,
            InstantiationException,
            IllegalAccessException,
            SQLException,
            HttpErrorStatus
    {
        EasyModel model = clazz.getDeclaredConstructor().newInstance();

        model.init(db, jsonObject);

        return model;
    }

    /**
     * Sets up the database table for a specified model class
     *
     * @param db Database connection to use
     */
    public void sync (
            Connection db
    ) {
        ModelType.get(clazz).setupHttpFields();

        BasicDao dao = new BasicDao(
                db,
                clazz.getSimpleName(),
                columnTypes()
        );

        try {
            dao.createTable();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> columnTypes() {
        Map<String, String> result = new HashMap<>();

        for (var field : primitivePersistentFields()) {
            String fieldName = field.getName();
            String columnType = SqlDatatypes.forClass(field.getType());

            result.put(fieldName, columnType);
        }

        return result;
    }

    public List<Field> primitivePersistentFields() {
        List<Field> result = new LinkedList<>();
        var fields = persistentFields();

        for (var field : fields) {
            if (SqlDatatypes.isPrimitive (field.getType())) {
                result.add(field);
            }
        }

        return result;
    }

    /**
     * Sets up the associations for a specified model in the database
     *
     * @param db Database connection to use
     */
    public void syncAssociations(
            Connection db
    ) {
        var modelAssociations = new Vector<Association>();

        for (var field : persistentFields()) {
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

        this.associations = modelAssociations.toArray(new Association [0]);
    }

    private void setupHttpFields () {
        Set<Field> fields = new HashSet<>(Arrays.asList(clazz.getFields()));
        fields.addAll(List.of(clazz.getDeclaredFields()));

        for (var field : fields) {
            if (Modifier.isStatic (field.getModifiers())) {
                continue;
            }

            var httpField = HttpField.forField(clazz, field, field.getType());

            if (httpField.canGet() || httpField.canSet()) {
                httpFields.put(field, httpField);
            }
        }

        var declaredFields = clazz.getDeclaredFields();
        for (var field : declaredFields) {
            if (Modifier.isStatic (field.getModifiers())) {
                continue;
            }

            if (!isPublic (field)) {
                var httpField = HttpField.forField(clazz, field, field.getType());

                if (httpField.canGet() || httpField.canSet()) {
                    httpFields.put(field, httpField);
                }
            }
        }
    }

    private void setupCustomMethods() {
        Arrays.stream(clazz.getDeclaredMethods())
            .filter(method -> method.getAnnotation(CustomMethod.class) != null)
            .forEach(method -> this.customMethods.put(method.getName(), method));
    }

    public Method customMethod (String methodName) {
        return customMethods.get(methodName);
    }

    public Collection <Method> customMethods () {
        return Collections.unmodifiableCollection(customMethods.values());
    }

    @SuppressWarnings ("unchecked")
    public <T> HttpField <T> httpField (Field field) {
        return (HttpField <T>) httpFields.get (field);
    }

    public Association[] associations() {
        return associations;
    }

    public Class<? extends EasyModel> modelClass() {
        return clazz;
    }
}
