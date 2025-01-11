package net.issachanzi.resteasy.model.association;

import net.issachanzi.resteasy.model.EasyModel;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Stack;
import java.util.UUID;

/**
 * One side of a many-to-one association. The other side should be a
 * {@link BelongsTo}.
 */
public class HasMany extends Association {
    private final String tableName;
    private final  String columnName;

    /**
     * Create a new {@code HasMany} association.
     *
     * @param clazz The model class this association belongs to
     * @param field The field this association is to save
     */
    public HasMany(Class <? extends EasyModel> clazz, Field field) {
        this.field = field;

        this.tableName = getOtherType(field).getSimpleName();
        this.columnName = Arrays.stream(
            getComponentType(field).getFields()
        )
            .filter(f -> f.getType() == clazz)
            .findAny()
            .map (Field::getName)
            .orElse(clazz.getSimpleName());

        if (!EasyModel.class.isAssignableFrom(getOtherType (field))) {
            throw new IllegalArgumentException(
                    "Field must be a subclass of EasyModel"
            );
        }
    }

    @Override
    public void init (Connection db) throws SQLException {
        var dao = getDao(db);

        dao.init();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void load (
            Connection db,
            EasyModel model,
            Stack<EasyModel> chain
    ) throws SQLException {
        var dao = getDao(db);

        UUID [] uuids = dao.getAllPrimaryByForeign(model.id);

        loadManyByUuid(db, model, chain, uuids);
    }

    @Override
    public void save (Connection db, EasyModel model) throws SQLException {
        try {
            var dao = getDao(db);
            var value = field.get (model);

            dao.clearAssociationByForeign(model.id);

            if (value != null) {
                int valueLength = Array.getLength(value);
                for (int i = 0; i < valueLength; i++) {
                    var v = (EasyModel) Array.get(value, i);

                    dao.setForeignByPrimary(v.id, model.id);
                }
            }
            else {
                dao.clearAssociationByForeign(model.id);
            }
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private BelongsToDao getDao(Connection db) {
        return new BelongsToDao(db, tableName, columnName);
    }
}
