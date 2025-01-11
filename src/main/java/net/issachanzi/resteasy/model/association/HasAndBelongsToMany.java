package net.issachanzi.resteasy.model.association;

import net.issachanzi.resteasy.model.EasyModel;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Stack;
import java.util.UUID;

/**
 * A many-to-many association
 */
public class HasAndBelongsToMany extends Association {
    private final String thisModelName;
    private final String otherModelName;
    private final String customTableName;

    /**
     * Create a new {@code HasAndBelongsToMany} association.
     *
     * @param clazz The model class this association belongs to
     * @param field The field this association is to save
     */
    public HasAndBelongsToMany(Class <? extends EasyModel> clazz, Field field) {
        this.field = field;

        Class <? extends EasyModel> otherType = getOtherType(field);

        if (otherType == clazz) {
            this.thisModelName = clazz.getSimpleName() + "_1";
            this.otherModelName = clazz.getSimpleName() + "_2";
            this.customTableName = clazz.getSimpleName() + "_" + field.getName();
        }
        else {
            this.thisModelName = clazz.getSimpleName();
            this.otherModelName = otherType.getSimpleName();
            this.customTableName = null;
        }

        if (!EasyModel.class.isAssignableFrom(getComponentType(field))) {
            throw new IllegalArgumentException(
                    "Field must be a subclass of EasyModel"
            );
        }
    }

    @Override
    public void init(Connection db) throws SQLException {
        var dao = getDao(db);

        dao.init();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(
            Connection db,
            EasyModel model,
            Stack<EasyModel> chain
    ) throws SQLException {
        var dao = getDao(db);

        UUID[] uuids = dao.getAssociations(model.id);

        loadManyByUuid(db, model, chain, uuids);
    }

    @Override
    public void save(Connection db, EasyModel model) throws SQLException {
        try {
            var dao = getDao(db);
            field.setAccessible(true);
            var value = field.get (model);
            field.setAccessible(false);

            dao.clearAssociations(model.id);

            if (value != null) {
                if (value instanceof Collection <?>) {
                    for (var v :(Iterable <? extends EasyModel>) value) {
                        dao.addAssociation(model.id, v.id);
                    }
                }
                else if (value.getClass().isArray()) {
                    int valueLength = Array.getLength(value);
                    for (int i = 0; i < valueLength; i++) {
                        var v = (EasyModel) Array.get(value, i);

                        dao.addAssociation(model.id, v.id);
                    }
                }
                else {
                    throw new RuntimeException ();
                }
            }
            else {
                dao.clearAssociations(model.id);
            }
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private JoinTableDao getDao(Connection db) {
        if (customTableName != null) {
            return new JoinTableDao(
                    db,
                    thisModelName,
                    otherModelName,
                    customTableName
            );
        }
        else {
            return new JoinTableDao(db, thisModelName, otherModelName);
        }
    }
}
