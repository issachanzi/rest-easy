package net.issachanzi.resteasy.model.association;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.Vector;

/**
 * Data access object for performing database queries on associations where one side
 * of the association is a {@link BelongsTo} association.
 *
 * <p>
 *     These associations are represented in the database by adding a foreign
 *     key to the main table of one of the models involved
 * </p>
 */
public class BelongsToDao {
    private final Connection db;
    private final String tableName;
    private final String columnName;

    /**
     * Construct a DAO for querying a specific pair of associations
     *
     * @param db The database connection to use
     * @param tableName The name of the table containing the foreign key
     * @param columnName The name of the column containing the foreign key
     */
    public BelongsToDao(Connection db, String tableName, String columnName) {
        this.db = db;
        this.tableName = tableName;
        this.columnName = columnName;
    }

    /**
     * Adds the foreign key column to the table, as specified in the
     * constructor.
     *
     * @throws SQLException If a database query fails
     */
    public void init () throws SQLException {
        String sql  = "ALTER TABLE " + tableName + " "
                + "ADD COLUMN IF NOT EXISTS " + columnName + " char(36)";

        var query = db.createStatement();

        System.out.println(sql);
        query.execute(sql);
    }

    /**
     * Updates the foreign key stored in a row of the table, given the primary
     * key of that row.
     *
     * <p>
     *     Used to set or clear associations
     * </p>
     *
     * @param primaryKey The primary key of the row to set
     * @param foreignKey The foreign key to set in the row
     * @throws SQLException If a query fails
     */
    public void setForeignByPrimary (
            UUID primaryKey,
            UUID foreignKey
    ) throws SQLException {
        String sql  = "UPDATE " + tableName + " "
                + "SET " + columnName + " = ? "
                + "WHERE id = ?";

        var query = db.prepareStatement(sql);
        query.setObject(1, idString(foreignKey));
        query.setObject(2, idString(primaryKey));

        System.out.println(sql);
        query.executeUpdate();
    }

    /**
     * Converts a UUID to a string, while propagating a {@code null} value
     *
     * @return The UUID in string format, or {@code null} if {@code uuid} is
     * null
     */
    private static String idString(UUID uuid) {
        if (uuid != null) {
            return uuid.toString();
        }
        else {
            return null;
        }
    }

    /**
     * Clears the foreign key column of all rows containing a given foreign key
     *
     * @param foreignKey The foreign key to remove associations for
     * @throws SQLException If a query fails
     */
    public void clearAssociationByForeign (UUID foreignKey)
            throws SQLException {
        String sql  = "UPDATE " + tableName + " "
                + "SET " + columnName + " = NULL "
                + "WHERE " + columnName + " = ?";

        var query = db.prepareStatement(sql);
        query.setObject(1, idString(foreignKey));

        System.out.println(sql);
        query.executeUpdate();
    }

    /**
     * Retrieves the foreign key stored in a given row
     *
     * @param primaryKey The primary key of the row to query
     * @return The foreign key stored in that row
     * @throws SQLException If a query fails
     */
    public UUID getForeignByPrimary (UUID primaryKey) throws SQLException {
        String sql  = "SELECT " + columnName + " "
                + "FROM " + tableName + " "
                + "WHERE id = ?";

        var query = db.prepareStatement(sql);
        query.setObject(1, idString(primaryKey));

        System.out.println(sql);
        var result = query.executeQuery();

        if (result.next()) {
            String uuidStr = result.getString(columnName);

            return uuidFromString(uuidStr);
        }
        else {
            return null;
        }
    }

    /**
     * Converts a string to a UUID, while propagating a {@code null} value
     *
     * @return The UUID of the string, or {@code null} if {@code uuidStr} is
     * null
     */
    private static UUID uuidFromString(String uuidStr) {
        if (uuidStr != null) {
            return UUID.fromString(uuidStr);
        }
        else {
            return null;
        }
    }

    /**
     * Gets the primary key of the row containing a given foreign key
     *
     * <p>
     *     If there are multiple rows containing the given foreign key, gets
     *     the first one.
     * </p>
     *
     * <p>
     *     This method is intended for one-to-one associations. For one-to-many
     *     associations, see {@link #getAllPrimaryByForeign(UUID)}.
     * </p>
     *
     * @param foreignKey The foreign key to search for
     * @return The primary key of the matching row, or {@code null} if no rows
     * contain the given foreign key
     * @throws SQLException If the query fails
     */
    public UUID getPrimaryByForeign (UUID foreignKey) throws SQLException {
        try (var result = queryPrimaryByForeign(foreignKey)) {
            if (result.next()) {
                String uuidStr = result.getString("id");

                return uuidFromString(uuidStr);
            }
            else {
                return null;
            }
        }
    }

    /**
     * Gets the primary keys of all rows containing a given foreign key
     *
     * <p>
     *     This method is intended for one-to-many associations. For one-to-one
     *     associations, see {@link #getPrimaryByForeign(UUID)}.
     * </p>
     *
     * @param foreignKey The foreign key to search for
     * @return The primary keys of the matching rows, or an empty array if no
     * rows contain the given foreign key
     * @throws SQLException If the query fails
     */
    public UUID [] getAllPrimaryByForeign (UUID foreignKey)
            throws SQLException {
        Vector<UUID> primaryKeys;

        try (var result = queryPrimaryByForeign(foreignKey)) {
            primaryKeys = new Vector<UUID>();

            while (result.next()) {
                String uuidStr = result.getString("id");

                primaryKeys.add(uuidFromString(uuidStr));
            }
        }

        return primaryKeys.toArray(new UUID[0]);
    }

    private ResultSet queryPrimaryByForeign(UUID foreignKey) throws SQLException {
        String sql  = "SELECT id "
                + "FROM " + tableName + " "
                + "WHERE " + columnName + " = ?";

        var query = db.prepareStatement(sql);
        query.setObject(1, idString(foreignKey));

        System.out.println(sql);
        return query.executeQuery();
    }
}
