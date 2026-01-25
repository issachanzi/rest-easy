package net.issachanzi.resteasy.testapp;

import net.issachanzi.resteasy.RestEasy;

import java.sql.Connection;

public class MainManager extends Main {
    public static Connection db () {
        return db;
    }
    
    public static RestEasy app () {
        return app;
    }
    
}
