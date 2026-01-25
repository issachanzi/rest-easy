package net.issachanzi.resteasy.testapp;

import net.issachanzi.resteasy.RestEasy;

import java.sql.Connection;
import java.sql.DriverManager;

public class Main {
    private static final String dbUrl  = "jdbc:postgresql://localhost:5432/app?user=postgres";

    protected static Connection db;
    protected static RestEasy app;

    public static void main(String[] args) {
        try {
            start();
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static RestEasy start() throws Exception {
        db = DriverManager.getConnection(dbUrl);
        app = new RestEasy(db);
        
        app.init ();
        
        return app;
    }
}