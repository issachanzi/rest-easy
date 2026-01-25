package net.issachanzi.resteasy.demoapp;

import net.issachanzi.resteasy.RestEasy;

import java.sql.Connection;
import java.sql.DriverManager;

public class Main {
    public static void main(String[] args) {
        try {
            var app = new RestEasy();
            
            app.init();
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}