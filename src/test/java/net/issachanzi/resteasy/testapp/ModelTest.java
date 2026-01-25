package net.issachanzi.resteasy.testapp;

import jakarta.json.Json;
import net.issachanzi.resteasy.Postgres;
import net.issachanzi.resteasy.RestEasy;
import net.issachanzi.resteasy.testapp.model.Model;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

import static net.issachanzi.resteasy.testapp.Main.db;
import static net.issachanzi.resteasy.testapp.UtilModelFactory.WithId.NO_ID;
import static net.issachanzi.resteasy.testapp.UtilModelFactory.WithId.WITH_ID;
import static org.junit.jupiter.api.Assertions.*;

public class ModelTest {
    private static final Logger log = LoggerFactory.getLogger(ModelTest.class);
    private static RestEasy app;
    private static final Postgres pg = new Postgres();
    private static final UtilModelFactory modelFactory = new UtilModelFactory();
    
    @BeforeAll
    static void setup() throws IOException, InterruptedException {
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
    static void cleanup() throws Exception {
        pg.stopPostgres();
        app.stop();
        
        Thread.sleep(10_000);
    }
    
    @Test
    void create() throws IOException, SQLException {
        var requestModel = modelFactory.createJson();
        
        // System.out.println(requestModel.toString());
        
        var resultStream = Request.Post("http://localhost:7070/api/Model")
            .bodyString(
                requestModel.toString(),
                ContentType.parse("application/json")
            )
            .execute()
            .returnContent()
            .asStream();
        
        var resultModel = Json.createReader(resultStream).readObject();
        
        assertTrue(resultModel.containsKey("id"));
        modelFactory.assertModelsEqual(requestModel, resultModel, NO_ID);
        
        UUID id = UUID.fromString(resultModel.getString("id"));
        Model model = Model.byId(db, id, Model.class);
        assertNotNull(model);
        modelFactory.assertModelsEqual(model, resultModel, WITH_ID);
    }
    
    @Test
    void read() throws IOException, SQLException {
        // Arrange
        Model model = modelFactory.createModel();
        model.save(db);
        
        // Act
        var resultStream = Request.Get("http://localhost:7070/api/Model/"
                + model.id)
            .execute()
            .returnContent()
            .asStream();
        var resultModel = Json.createReader(resultStream).readObject();
        
        // Assert
        modelFactory.assertModelsEqual(model, resultModel, WITH_ID);
    }
    
    @Test
    void read2() throws SQLException, IOException {
        // Arrange
        Model model = modelFactory.createModel();
        model.save(db);
        
        // Act
        var resultStream = Request.Get("http://localhost:7070/api/Model/")
            .execute()
            .returnContent()
            .asStream();
        var resultArr = Json.createReader(resultStream).readArray();
        
        // Assert
        var resultJsonObj = resultArr.stream().filter(
            m -> m.asJsonObject().getString("id").equals(model.id.toString())
        ).findAny();
        
        assertTrue(resultJsonObj.isPresent());
        modelFactory.assertModelsEqual(
            model,
            resultJsonObj.get().asJsonObject(),
            WITH_ID
        );
    }
    
    @Test
    void update () throws IOException, SQLException {
        // Arrange
        Model model = modelFactory.createModel();
        model.string = "foo";
        model.save(db);
        
        var requestModel = Json.createObjectBuilder()
            .add ("string", "bar")
            .build();
        
        // Act
        var result =
            Request.Put(
                "http://localhost:7070/api/Model/" + model.id
            )
            .bodyString(
                requestModel.toString(),
                ContentType.parse("application/json")
            )
            .execute();
        
        int statusCode = result
            .returnResponse()
            .getStatusLine()
            .getStatusCode();
        
        // Assert
        assertEquals(204, statusCode);
        
        model = Model.byId(db, model.id, Model.class);
        assertNotNull(model);
        assertEquals("bar", model.string);
    }
    
    @Test
    void delete () throws SQLException, IOException {
        // Arrange
        Model model = modelFactory.createModel();
        model.save(db);
        
        // Act
        var resultStream = Request.Delete(
            "http://localhost:7070/api/Model/" + model.id
            )
            .execute();
        
        // Assert
        var modelStillThere = Model.byId (db, model.id, Model.class);
        assertNull(modelStillThere);
    }
    
    @Test
    void unicode () throws SQLException, IOException {
        // Arrange
        Model model = modelFactory.createModel();
        final String tane = "T\u0101ne";
        model.string = tane;
        model.save(db);
        
        // Act
        var resultStream = Request.Get(
                "http://localhost:7070/api/Model/" + model.id
            )
            .execute()
            .returnContent()
            .asStream();
        
        // Assert
        var resultModel = Json.createReader(resultStream).readObject();
        String string = resultModel.getString("string");
        assertEquals(tane, string);
    }
}
