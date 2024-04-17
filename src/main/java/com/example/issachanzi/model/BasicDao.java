package com.example.issachanzi.model;

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

public class BasicDao {
    private final Connection db;
    private final String tableName;
    private final Map<String, String> columnTypes;

    public BasicDao (
            Connection db,
            String tableName,
            Map<String, String> columnTypes
    ) {
        this.db = db;
        this.tableName = tableName;
        this.columnTypes = columnTypes;
    }

    public void createTable() throws SQLException {
        String columns = mapJoinColumns(
                colName -> colName + " " + columnTypes.get(colName),
                ", "
        );

        String sql  = "CREATE TABLE iF NOT EXISTS" + tableName + " "
                    + "(" + columns + ");";

        Statement query = db.createStatement();
        query.execute(sql);
    }

    public Map<String, Object> select(UUID id) throws SQLException {
        String whereSql = "id = ?";
        var params = new Object[] {id.toString()};
        var results = this.where(whereSql, params);

        return results.stream().toList().getFirst();
    }

    public Collection<Map<String, Object>> where (
            String whereSql,
            Object[] params
    ) throws SQLException {
        String sql = "SELECT * FROM " + tableName + "WHERE " + whereSql + ";";
        PreparedStatement query = db.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            query.setObject(i + 1, params[i]);
        }

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

    public void update (
            UUID id,
            Map<String, Object> values
    ) throws SQLException {
        List<String> columns = values.keySet().stream().toList();
        String setSql = mapJoin(
                columns,
                colName -> colName + " = " + values.get(colName),
                " = "
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

    public void delete (UUID id) throws SQLException {
        String sql = "DELETE FROM " + tableName + "WHERE id = ?;";

        PreparedStatement query = db.prepareStatement(sql);
        query.setString(1, id.toString());

        query.execute();
    }

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
        List<String> columnNames = columnTypes.keySet().stream().toList();

        return mapJoin(columnNames, mapFunc, separator);
    }
}
