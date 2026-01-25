package net.issachanzi.resteasy;

import java.io.IOException;

public class Postgres {
    private Process postgresProc;
    
    public void startPostgres() throws IOException, InterruptedException {
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
    
    public void stopPostgres() throws IOException {
        Runtime.getRuntime().exec("docker stop rest-easy-test-pg");
        Runtime.getRuntime().exec("docker rm rest-easy-test-pg");
        postgresProc.destroy();
    }
}
