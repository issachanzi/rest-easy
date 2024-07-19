package net.issachanzi.resteasy.model.association;

import net.issachanzi.resteasy.model.EasyModel;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

/**
 * One side of a many-to-one association. The other side should be a
 * {@link BelongsTo}.
 */
public class HasOne extends Association {
    private final Field field;

    private final String tableName;
    private final  String columnName;

    /**
     * Create a new {@code HasOne} association.
     *
     * @param clazz The model class this association belongs to
     * @param field The field this association is to save
     */
    public HasOne (Class <? extends EasyModel> clazz, Field field) {
        this.field = field;

        this.tableName = field.getType().getSimpleName();
        this.columnName = clazz.getSimpleName();

        if (! EasyModel.class.isAssignableFrom(field.getType())) {
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
            EasyModel chainSource
    ) throws SQLException {
        var dao = getDao(db);

        UUID uuid = dao.getPrimaryByForeign(model.id);
        try {
            if (uuid != null) {
                EasyModel value = EasyModel.byId(
                        db,
                        uuid,
                        (Class<? extends EasyModel>) field.getType(),
                        chainSource
                );

                field.set(model, value);
            }
            else {
                field.set(model, null);
            }
        } catch (IllegalAccessException | ClassCastException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save (Connection db, EasyModel model) throws SQLException {
        try {
            var dao = getDao(db);
            var value = (EasyModel) field.get (model);

            dao.clearAssociationByForeign(model.id);
            if (value != null) {
                dao.setForeignByPrimary (value.id, model.id);
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
