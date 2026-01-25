package net.issachanzi.resteasy.demoapp;

import java.io.IOException;

public class TestDb {
    private Process postgresProc;
    
    public TestDb () {
        try {
            this.init();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @SuppressWarnings("deorecation")
    private void init () throws InterruptedException, IOException {
        postgresProc = Runtime.getRuntime().exec(
            "docker run --rm --name rest-easy-test-pg -d " +
                "-e POSTGRES_HOST_AUTH_METHOD=trust " +
                "-e POSTGRES_DB=app " +
                "-p 5432:5432 " +
                "postgres"
        );
        
        System.out.println("docker run --rm --name rest-easy-test-pg -d " +
            "-e POSTGRES_HOST_AUTH_METHOD=trust " +
            "-e POSTGRES_DB=app " +
            "-p 5432:5432 " +
            "postgres");
        
        postgresProc.waitFor();
    }
    
    @SuppressWarnings("deprecation")
    void destroy () {
        try {
            Runtime.getRuntime().exec("docker stop rest-easy-test-pg");
            Runtime.getRuntime().exec("docker rm rest-easy-test-pg");
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        postgresProc.destroy();
    }
}


