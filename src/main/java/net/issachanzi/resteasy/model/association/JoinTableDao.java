package net.issachanzi.resteasy.model.association;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.Vector;

import static net.issachanzi.resteasy.model.BasicDao.escape;

/**
 * Data access object for performing database queries on many-to-many
 * associations.
 *
 * <p>
 *     These associations are represented in the database by adding join table
 *     containing foreign keys of both models in the association
 * </p>
 */
public class JoinTableDao {
    private final Connection db;
    private final String tableName;
    private final String thisModelName;
    private final String otherModelName;

    /**
     * Construct a DAO for querying a specific pair of associations
     *
     * @param db The database connection to use
     * @param thisModelName The name the model to query associations of
     * @param otherModelName The name of the other model in the association
     */
    public JoinTableDao(
            Connection db,
            String thisModelName,
            String otherModelName
    ) {
        this.db = db;
        this.thisModelName = thisModelName;
        this.otherModelName = otherModelName;

        if (thisModelName.compareTo(otherModelName) < 0) {
            this.tableName = escape (thisModelName + otherModelName);
        }
        else {
            this.tableName = escape (otherModelName + thisModelName);
        }
    }

    /**
     * Creates the join table for this association
     *
     * @throws SQLException If a database query fails
     */
    public void init () throws SQLException {
        String sql  = "CREATE TABLE IF NOT EXISTS " + tableName + " ( "
                    +   "" + escape (thisModelName) + " char(36), "
                    +   "" + escape (otherModelName) + " char(36) "
                    + ")";

        var query = db.createStatement();

        System.out.println(sql);
        query.execute(sql);
    }

    /**
     * Gets the keys of all rows where the column given by
     * {@code thisModelName} contains the primary key given by {@code thisId}.
     *
     * @param thisId The id of the model instance to query associations of
     * @return The ids of the model instances associated with this model
     * instance
     * @throws SQLException If a query fails
     */
    public UUID [] getAssociations (UUID thisId) throws SQLException {
        String sql  = "SELECT " + escape (otherModelName) + " "
                    + "FROM " + tableName + " "
                    + "WHERE " + escape (thisModelName) + " = ?";

        var query = db.prepareStatement(sql);
        query.setObject(1, idString(thisId));

        Vector<UUID> uuids;

        System.out.println(sql);
        try (var result = query.executeQuery()) {
            uuids = new Vector<UUID>();

            while (result.next()) {
                String uuidStr = result.getString(otherModelName);

                uuids.add(UUID.fromString(uuidStr));
            }
        }

        return uuids.toArray(new UUID[0]);
    }

    /**
     * Deletes rows from the join table where the column given by
     * {@code thisModelName} contains the id given by {@code thisId}
     *
     * @param thisId The id of the model instance to delete associations for
     * @throws SQLException If a query fails
     */
    public void clearAssociations (UUID thisId) throws SQLException {
        String sql  = "DELETE FROM " + tableName + " "
                    + "WHERE " + escape (thisModelName) + " = ?";

        var query = db.prepareStatement(sql);
        query.setObject(1, idString(thisId));

        System.out.println(sql);
        query.executeUpdate();
    }

    /**
     * Add a row to the join table. Set the column given by
     * {@code thisModelName} to {@code thisId}, and the column given by
     * {@code otherModelName} to {@code otherId}.
     *
     * @param thisId The id of the model instance to add an association for
     * @param otherId  The id of the model instance to add an association with
     * @throws SQLException If a query fails
     */
    public void addAssociation (UUID thisId, UUID otherId) throws SQLException {
        String sql  = "INSERT INTO " + tableName + " "
                    + "("
                    +   escape (thisModelName) + ", "
                    +   escape (otherModelName)
                    + ") "
                    + "VALUES (?, ?)";

        var query = db.prepareStatement(sql);
        query.setObject(1, idString(thisId));
        query.setObject(2, idString(otherId));

        System.out.println(sql);
        query.executeUpdate();
    }

    private static String idString(UUID uuid) {
        if (uuid != null) {
            return uuid.toString();
        }
        else {
            return null;
        }
    }
}
