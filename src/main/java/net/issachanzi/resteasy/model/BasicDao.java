package net.issachanzi.resteasy.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A data access object for a table representing an EasyModel class
 *
 * Each method performs a different database query
 *
 */
public class BasicDao {
    private final Connection db;
    private final String tableName;
    private final Map<String, String> columnTypes;

    /**
     * Constructs the DAO for a particular model's table
     *
     * @param db The database connection to use
     * @param tableName The name of the table to query
     * @param columnTypes A map containing a key of each column's name with a
     *                    value of that column's SQL data type.
     */
    public BasicDao (
            Connection db,
            String tableName,
            Map<String, String> columnTypes
    ) {
        this.db = db;
        this.tableName = "\"" + tableName + "\"";
        this.columnTypes = columnTypes;
    }

    /**
     * Executes a query to create the table specified by this model, if it does
     * not already exist
     *
     * @throws SQLException if the underlying database query encounters an
     *                      error
     */
    public void createTable() throws SQLException {
        String columns = mapJoinColumns(
                colName -> colName + " " + columnTypes.get(colName),
                ", "
        );

        String sql  = "CREATE TABLE IF NOT EXISTS " + tableName + " "
                    + "(" + columns + ");";

        Statement query = db.createStatement();

        System.out.println(sql);
        query.execute(sql);
        System.out.println (sql);
    }

    /**
     * Executes a query to select all rows from this DAO's table
     *
     * @return  All rows in the table in a Collection. Each row is returned as
     *          a Map containing a key of each column name with the value of
     *          that column's value.
     * @throws SQLException if the underlying database query encounters an
     *                      error
     */
    public Collection<Map<String, Object>> select() throws SQLException {
        return this.where("1=1", new Object[] {});
    }

    /**
     * Executes a query to select one row by its id column
     *
     * @param id The id of the column to select
     * @return  The selected row as a Map containing a key of each column name
     *          with the value of that column's value.
     * @throws SQLException if the underlying database query encounters an
     *                      error
     */
    public Map<String, Object> select(UUID id) throws SQLException {
        String whereSql = "id = ?";
        var params = new Object[] {id.toString()};
        var results = this.where(whereSql, params);

        return results.stream().findFirst().get();
    }

    /**
     * Executes a query to select rows from this table by the values of certain
     * columns.
     *
     * <p>
     * Each specified column in the table must match the value in the
     * {@code filter} param for a row to be returned.
     * </p>
     *
     * @param filter A map containing a key of the name of each column to
     *               filter on, with the value that must match for a row to be
     *               selected
     * @return  Selected rows in a Collection. Each row is returned as a Map
     *          containing a key of each column name with the value of that
     *          column's value.
     * @throws SQLException if the underlying database query encounters an
     *                      error
     */
    public Collection<Map<String, Object>> where (
            Map<String, Object> filter
    ) throws SQLException {
        List<String> columns = filter
                .keySet()
                .stream()
                .map(colName -> "\"" + colName + "\"")
                .toList();
        String whereSql = mapJoin(
                columns,
                colName -> colName + " = ?",
                " AND "
        );
        Object[] params = map(columns, filter::get).toArray();

        return where(whereSql, params);
    }

    /**
     * Executes a query to select rows from this table based on an arbitrary
     * SQL {@code WHERE} clause
     *
     * <p>
     *     <b>Never</b> interpolate user input or untrusted data of any kind
     *     into SQL queries (such as the {@code whereSql} param). Always use
     *     parameterised queries, which should be hard coded wherever possible.
     *</p>
     *
     * <p>
     *     <a href="https://www.cloudflare.com/learning/security/threats/sql-injection/">
     *         What is SQL injection - Cloudflare
     *     </a>
     * </p>
     *
     * @param whereSql The SQL {@code WHERE} clause. {@code ?} characters can
     *                 be used for parameter placeholders. This parameter must
     *                 never contain untrusted data such as user input.
     * @return  Selected rows in a Collection. Each row is returned as a Map
     *          containing a key of each column name with the value of that
     *          column's value.
     * @throws SQLException if the underlying database query encounters an
     *                      error
     */
    public Collection<Map<String, Object>> where (
            String whereSql,
            Object[] params
    ) throws SQLException {
        String sql = "SELECT * FROM " + tableName + " WHERE " + whereSql + ";";
        PreparedStatement query = db.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            query.setObject(i + 1, params[i]);
        }

        System.out.println(sql);
        ResultSet rs = query.executeQuery();
        Collection<Map<String, Object>> results = new LinkedList<>();
        while (rs.next()) {
            Map<String, Object> resultMap = new HashMap<>();
            for (var colName : columnTypes.keySet()) {
                resultMap.put(colName, rs.getObject(colName));
            }
            results.add(resultMap);
        }

        return results;
    }

    /**
     * Executes a query to add a row to this table
     *
     * @param values A map containing a key of each column's name with a value
     *               of the value to set for that column
     * @throws SQLException if the underlying database query encounters an
     *                      error
     */
    public void insert (Map<String, Object> values) throws SQLException {
        List<String> columns = values.keySet().stream().toList();
        String columnsSql = mapJoin(columns, k -> k, ", ");
        String valuesSql = mapJoin(values.keySet(), k -> "?", ", ");
        String sql  = "INSERT INTO " + tableName + " (" + columnsSql + ") "
                    + "VALUES (" + valuesSql + ");";

        PreparedStatement query = db.prepareStatement(sql);
        for (int i = 0; i < columns.size(); i++) {
            String colName = columns.get(i);
            query.setObject(i + 1, values.get(colName));
        }

        query.execute();
    }

    /**
     * Executes a query to update a row in this table by its id column
     *
     * @param id The value of the id column for the row to update
     * @param values The values to update in the row, as a map with keys of
     *               column names with the value to be set in that column
     * @throws SQLException if the underlying database query encounters an
     *                      error
     */
    public void update (
            UUID id,
            Map<String, Object> values
    ) throws SQLException {
        List<String> columns = values.keySet().stream().toList();
        String setSql = mapJoin(
                columns,
                colName -> colName + " = " + values.get(colName),
                ", "
        );
        String sql  = "UPDATE " + tableName + " "
                    + "SET " + setSql + " WHERE id = ?;";

        PreparedStatement query = db.prepareStatement(sql);
        for(int i = 0; i < columns.size(); i++) {
            var colName = columns.get(i);
            query.setObject(i + 1, values.get(colName));
        }
        query.setString(columns.size() + 1, id.toString());

        query.execute();
    }

    /**
     * Executes a query to delete a row from this table by its id column
     *
     * @param id The value of the id column for the row to delete
     * @throws SQLException if the underlying database query encounters an
     *                      error
     */
    public void delete (UUID id) throws SQLException {
        String sql = "DELETE FROM " + tableName + "WHERE id = ?;";

        PreparedStatement query = db.prepareStatement(sql);
        query.setString(1, id.toString());

        query.execute();
    }

    @FunctionalInterface
    interface MapFunc<I, O> {
        O map (I i);
    }
    private <I, O> List<O> map (Collection<I> collection, MapFunc<I, O> mapFunc) {
        List<O> result = new LinkedList<>();

        for (var v : collection) {
            result.add(mapFunc.map(v));
        }

        return result;
    }
    private <T> String mapJoin (
            Collection<T> collection,
            MapFunc<T, String> mapFunc,
            String separator
    ) {
        List<String> mapped = collection.stream().map(mapFunc::map).toList();

        return String.join(separator, mapped);
    }
    private String mapJoinColumns(
            MapFunc<String, String> mapFunc,
            String separator
    ) {
        List<String> columnNames = columnTypes
                .keySet ()
                .stream ()
                .map (colName -> "\"" + colName + "\"")
                .toList ();

        return mapJoin(columnNames, mapFunc, separator);
    }
}
