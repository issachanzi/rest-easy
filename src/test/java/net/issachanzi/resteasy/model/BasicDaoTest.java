package net.issachanzi.resteasy.model;

import net.issachanzi.resteasy.Postgres;
import net.issachanzi.resteasy.RestEasy;
import net.issachanzi.resteasy.testapp.Main;
import net.issachanzi.resteasy.testapp.MainManager;
import net.issachanzi.resteasy.testapp.UtilModelFactory;
import net.issachanzi.resteasy.testapp.model.Model;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BasicDaoTest {
    /*
     * I want to make sure that no SQL injection bugs exist in this code.
     */
    
    private static final Postgres pg = new Postgres();
    private static RestEasy app;
    
    @BeforeAll
    public static void setup () throws IOException, InterruptedException {
        pg.startPostgres();
        
        Thread.sleep(10_000);
        
        try {
            app = Main.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        Thread.sleep(10_000);
    }
    
    @AfterAll
    public static void cleanup () throws Exception {
        pg.stopPostgres();
        app.stop();
        
        Thread.sleep(10_000);
    }
    
    @Test
    public void selectByUuidExpectNoSqlInjection () throws SQLException {
        UUID uuid = UUID.randomUUID();
        
        Connection db = Mockito.spy (MainManager.db());
        
        BasicDao dao = new BasicDao(
            db,
            Model.class.getSimpleName(),
            ModelType.get(Model.class).columnTypes()
        );
        
        try {
            dao.select(uuid);
        }
        catch (NoSuchElementException ignored) {}
     
        Mockito.verify (db).prepareStatement(Mockito.argThat(sql -> {
            sql = sql.toLowerCase(Locale.ROOT);
            String uuidLwr = uuid.toString().toLowerCase(Locale.ROOT);
            
            boolean isSqlInjection = sql.contains(uuidLwr);
            
            assertFalse(isSqlInjection);
            
            return !isSqlInjection;
        }));
    }
    
    private static Statement mockStatement () {
        return Mockito.mock(Statement.class);
    }
    
    private static Connection mockConnection (Statement query) throws SQLException {
        Connection db = Mockito.mock (Connection.class);
        
        Mockito.when (db.createStatement()).thenReturn(query);
        
        return db;
    }
}
