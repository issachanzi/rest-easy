package net.issachanzi.resteasy.demoapp;

import net.issachanzi.resteasy.RestEasy;

import java.sql.Connection;
import java.sql.DriverManager;

public class Main {
    private static final String dbUrl  = "jdbc:postgresql://localhost:5432/app";;

    public static void main(String[] args) {
        try {
            Connection db = DriverManager.getConnection(dbUrl);
            var app = new RestEasy ();

            app.init (db);

//            var walkTheDog = new TodoItem();
//            walkTheDog.text = "Walk the dog";
//            walkTheDog.done = false;
//            walkTheDog.save (db);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}