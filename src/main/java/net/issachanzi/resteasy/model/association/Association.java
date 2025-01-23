package net.issachanzi.resteasy.model.association;

import net.issachanzi.resteasy.model.EasyModel;
import net.issachanzi.resteasy.model.annotation.NoPersist;

import java.lang.reflect.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * A connection between two EasyModel classes, based on a field in one or both
 * classes referencing the other
 *
 * <p>
 *     The following code section would result in one association between
 *     {@code Actor} and {@code Movie}.
 * </p>
 * <pre><code>
 *     class Actor extends EasyModel {
 *         Movie [] movies; // Association with Movie
 *     }
 *
 *     class Movie extends EasyModel {
 *         Actor [] actors; // Association with Actor
 *     }
 * </code></pre>
 */
public abstract class Association {
    protected Field field;

    @SuppressWarnings("unchecked")
    protected static Class <? extends EasyModel> getOtherType(Field field) {
        Type otherType = getComponentType(field);

        if (EasyModel.class.isAssignableFrom(field.getType())) {
            return (Class<? extends EasyModel>) field.getType();
        }
        else if (otherType == null) {
            throw new RuntimeException("Type is not valid");
        }

        return (Class<? extends EasyModel>) otherType;
    }

    static Class<?> getComponentType(Field field) {
        Type componentType;

        if (field.getType().isArray()) {
            componentType = field.getType().componentType();
        }
        else if (Collection.class.isAssignableFrom(field.getType())) {
            Type type = field.getGenericType();
            if (type instanceof ParameterizedType pType) {
                componentType = pType.getActualTypeArguments()[0];
            }
            else {
                throw new RuntimeException("Type is not valid");
            }
        }
        else {
            componentType = null;
        }

        return (Class<?>) componentType;
    }

    /**
     * Sets up the database to store the association.
     *
     * <p>
     *     This might include adding a foreign key to an existing table, or
     *     creating a new join table.
     * </p>
     *
     * @param db The database connection to use
     * @throws SQLException If a database query fails
     */
    public abstract void init (Connection db) throws SQLException;

    /**
     * For a given model instance, fetches the associated model instances from
     * the database and populates the given model instance with the retrieved
     * model instances.
     *
     * @param db The database connection to use
     * @param model The model instance to populate associations for
     * @param chain Model instances to break recursion on
     * @throws SQLException If a database query fails
     */
    public abstract void load (
            Connection db,
            EasyModel model,
            Stack<EasyModel> chain
    ) throws SQLException;

    @SuppressWarnings("unchecked")
    protected void loadManyByUuid(
        Connection db,
        EasyModel model,
        Stack<EasyModel> chain,
        UUID[] uuids
    ) throws SQLException {
        Class<? extends EasyModel> componentType
            = (Class<? extends EasyModel>) getComponentType(field);
        if (field.getType().isArray()) {

            Object value = Array.newInstance(componentType, uuids.length);
            for (int i = 0; i < uuids.length; i++) {
                EasyModel v = EasyModel.byId (
                        db,
                        uuids[i],
                        componentType,
                        chain
                );

                Array.set(value, i, v);
            }

            try {
                field.setAccessible(true);
                field.set(model, value);
                field.setAccessible(false);
            } catch (IllegalAccessException | ClassCastException e) {
                throw new RuntimeException(e);
            }
        }
        else if (Collection.class.isAssignableFrom(field.getType())) {
            try {
                field.setAccessible(true);
                var collection
                        = (Collection<? extends EasyModel>) field.get(model);
                field.setAccessible(false);

                collection.clear();
                for (UUID uuid : uuids) {
                    EasyModel element = EasyModel.byId(
                            db,
                            uuid,
                            componentType,
                            chain
                    );
                    // You can't add anything to a Collection with a wildcard in
                    // the type parameter, so I have to cast to a raw Collection
                    ((Collection) collection).add(element);

//                    // I thought I was so clever with this, but it turns out
                      // that there is a more elegant way of doing this
//                    Method addMethod = collection
//                            .getClass()
//                            .getMethod("add", componentType);
//                    addMethod.invoke(collection, element);
                }
            }
            catch (IllegalAccessException |
                   ClassCastException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Saves this association in the database
     *
     * @param db The database connection to use
     * @param model The model instance to save the association for
     * @throws SQLException If a database query fails
     */
    public abstract void save (Connection db, EasyModel model) throws SQLException;

    /**
     * Creates a suitable {@code Association} for a field of an EasyModel
     * subclass.
     *
     * @param clazz The model class to create an association in
     * @param field The field to create an association for
     * @return The new association, if one is supported for the parameters
     * given. Otherwise {@code null}.
     */
    public static Association forField (
            Class <? extends EasyModel> clazz,
            Field field
    ) {
        if (!isSupportedAssociation (field)) {
            return null;
        }

        boolean isMultiple = isMultiple(field);
        boolean isOtherMultiple = isOtherMultiple (clazz, field);
        if (isMultiple) {
            if (isOtherMultiple) {
                return new HasAndBelongsToMany (clazz, field);
            }
            else {
                return new HasMany (clazz, field);
            }
        }
        else {
            var className = clazz.getSimpleName();
            var otherClassName = field.getType().getSimpleName();
            if (isOtherMultiple) {
                return new BelongsTo(clazz,field);
            }
            else if (className.compareTo(otherClassName) <= 0) {
                return new BelongsTo(clazz, field);
            }
            else if (
                Arrays.stream(field.getType().getFields())
                    .anyMatch(f -> f.getType() == clazz)
            ) {
                return new HasOne(clazz, field);
            }
            else {
                return new BelongsTo (clazz, field);
            }
        }
    }

    private static boolean isMultiple(Field field) {
        if (field.getType().isArray()) {
            return true;
        }
        else if (Collection.class.isAssignableFrom(field.getType())) {
            return true;
        }
        else {
            return false;
        }
    }

    private static boolean isOtherMultiple(
            Class<? extends EasyModel> clazz,
            Field field
    ) {
        var otherField = findOtherField(clazz, field);

        if (otherField == null) {
            return false;
        }
        else {
            return isMultiple(otherField);
        }
    }

    public static boolean isSupportedAssociation(Field field) {
        var fieldType = field.getType();
        if (EasyModel.class.isAssignableFrom(fieldType)) {
            return true;
        }
        else if (fieldType.isArray()) {
            return EasyModel.class.isAssignableFrom(fieldType.componentType());
        }
        else if (Collection.class.isAssignableFrom(fieldType)) {
            Type type = field.getGenericType();
            if (type instanceof ParameterizedType pType) {
                if (
                    pType.getActualTypeArguments().length > 0
                        && pType.getActualTypeArguments() [0] instanceof Class
                ) {
                    return (
                            EasyModel.class.isAssignableFrom(
                                (Class <?>) pType.getActualTypeArguments()[0]
                            )
                    );
                }
                else {
                    return false;
                }
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    private static Field findOtherField (
            Class <? extends EasyModel> clazz,
            Field field
    ) {
        var otherClazz = getOtherType(field);
        var otherClazzFields = getFields(otherClazz);

        for (var f : otherClazzFields) {
            if (f.getType () == clazz || getComponentType(f) == clazz) {
                return f;
            }
        }

        return null;
    }

    private static Field[] getFields(Class<?> otherClazz) {
        var publicFields = otherClazz.getFields();
        var privateFields = Arrays.stream(otherClazz.getDeclaredFields())
                .filter(field -> (field.getModifiers() & Modifier.PUBLIC) == 0);
        var fields = Stream.concat(Arrays.stream(publicFields), privateFields)
                .filter(field -> Arrays.stream(field.getAnnotations())
                        .noneMatch(
                                annotation
                                        -> annotation.annotationType()
                                        == NoPersist.class
                        )
                )
                .toList()
                .toArray(new Field [0]);
        return fields;
    }
}
