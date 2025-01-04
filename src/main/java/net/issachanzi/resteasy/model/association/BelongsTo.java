package net.issachanzi.resteasy.model.association;

import net.issachanzi.resteasy.model.EasyModel;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

/**
 * One side of a one-to-one or one-to-many association.
 */
public class BelongsTo extends Association {
    private final Field field;

    private final String tableName;
    private final  String columnName;

    /**
     * Create a new {@code BelongsTo} association.
     *
     * @param clazz The model class this association belongs to
     * @param field The field this association is to save
     */
    public BelongsTo (Class <? extends EasyModel> clazz, Field field) {
        this.field = field;

        this.tableName = clazz.getSimpleName();
        this.columnName = field.getType().getSimpleName();

        if (! EasyModel.class.isAssignableFrom(field.getType())) {
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
    public void load(Connection db, EasyModel model, EasyModel chainSource)
            throws SQLException {
        var dao = getDao(db);

        UUID uuid = dao.getForeignByPrimary(model.id);
        if (uuid != null) {
            try {
                EasyModel value = EasyModel.byId(
                        db,
                        uuid,
                        (Class<? extends EasyModel>) field.getType(),
                        chainSource
                );

                field.setAccessible(true);
                field.set(model, value);
                field.setAccessible(false);
            } catch (IllegalAccessException | ClassCastException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void save(Connection db, EasyModel model) throws SQLException {
        var dao = getDao(db);

        try {
            field.setAccessible(true);
            var value = (EasyModel) field.get(model);
            field.setAccessible(false);

            if (value != null) {
                dao.setForeignByPrimary(model.id, value.id);
            }
            else {
                dao.setForeignByPrimary(model.id, null);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private BelongsToDao getDao(Connection db) {
        return new BelongsToDao(db, tableName, columnName);
    }
}
