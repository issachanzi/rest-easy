package net.issachanzi.resteasy.model.association;

import net.issachanzi.resteasy.model.EasyModel;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

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
    private Field field;

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
     * @param chainSource Model instance to break recursion on
     * @throws SQLException If a database query fails
     */
    public abstract void load (
            Connection db,
            EasyModel model,
            EasyModel chainSource
    ) throws SQLException;

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

        boolean isMultiple = field.getType().isArray();
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
            else {
                return new HasOne(clazz, field);
            }
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
            return otherField.getType().isArray();
        }
    }

    private static boolean isSupportedAssociation(Field field) {
        var fieldType = field.getType();
        if (EasyModel.class.isAssignableFrom(fieldType)) {
            return true;
        }
        else if (!fieldType.isArray()) {
            return false;
        }
        else {
            return EasyModel.class.isAssignableFrom(fieldType.componentType());
        }
    }

    private static Field findOtherField (
            Class <? extends EasyModel> clazz,
            Field field
    ) {
        var otherClazz = field.getType();
        var otherClazzFields = otherClazz.getFields();

        for (var f : otherClazzFields) {
            if (field.getType () == clazz || f.getType().arrayType() == clazz) {
                return f;
            }
        }

        return null;
    }
}
