package net.issachanzi.resteasy.model.association;

import net.issachanzi.resteasy.model.EasyModel;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

/**
 * A many-to-many association
 */
public class HasAndBelongsToMany extends Association {
    private final Field field;
    private final String thisModelName;
    private final String otherModelName;

    /**
     * Create a new {@code HasAndBelongsToMany} association.
     *
     * @param clazz The model class this association belongs to
     * @param field The field this association is to save
     */
    public HasAndBelongsToMany(Class <? extends EasyModel> clazz, Field field) {
        this.field = field;

        this.thisModelName = clazz.getSimpleName();
        this.otherModelName = field.getType().componentType().getSimpleName();

        if (!EasyModel.class.isAssignableFrom(field.getType().componentType())) {
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
            EasyModel chainSource
    ) throws SQLException {
        var dao = getDao(db);

        UUID[] uuids = dao.getAssociations(model.id);
        var componentType
                = (Class <? extends EasyModel>) field.getType().componentType();

        var value = Array.newInstance(componentType, uuids.length);
        for (int i = 0; i < uuids.length; i++) {
            EasyModel v = EasyModel.byId (
                    db,
                    uuids [i],
                    componentType,
                    chainSource
            );

            Array.set(value, i, v);
        }

        try {
            field.set(model, value);
        } catch (IllegalAccessException | ClassCastException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Connection db, EasyModel model) throws SQLException {
        try {
            var dao = getDao(db);
            var value = field.get (model);

            dao.clearAssociations(model.id);

            if (value != null) {
                int valueLength = Array.getLength(value);
                for (int i = 0; i < valueLength; i++) {
                    var v = (EasyModel) Array.get(value, i);

                    dao.addAssociation(model.id, v.id);
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
        return new JoinTableDao(db, thisModelName, otherModelName);
    }
}
