package net.issachanzi.resteasy.demoapp;

import jakarta.json.Json;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class TodoTest {
    static TestDb testDb = new TestDb ();
    static Thread appThread;
    
    static final String TODO_TEXT = "Walk the dog";
    
    @BeforeAll
    static void setup() throws IOException, InterruptedException {
        
        
        Thread.sleep(10_000);
        
        appThread = new Thread(() -> Main.main(new String[]{}));
        appThread.start();
        
        Thread.sleep(10_000);
    }
    
    @AfterAll
    static void cleanup() throws IOException {
        testDb.destroy();
    }
    
    @Test
    public void test () throws IOException {
        String id = createTodo ();
        readTodo(id, false);
        markTodoDone(id);
        readTodo(id, true);
    }
    
    private String createTodo () throws IOException {
        var todo = Json.createObjectBuilder()
            .add("text", TODO_TEXT)
            .build();
        
        var resultStream = Request.Post("http://localhost:7070/api/TodoItem")
            .bodyString(
                todo.toString(),
                ContentType.parse("application/json")
            )
            .execute()
            .returnContent()
            .asStream();
        
        var resultModel = Json.createReader(resultStream).readObject();
        
        // Assert
        String resultId = resultModel.getString("id");
        String resultText = resultModel.getString("text");
        boolean resultDone = resultModel.getBoolean("done");
        
        assertNotNull(resultId);
        assertEquals(TODO_TEXT, resultText);
        assertFalse(resultDone);
        
        return resultId;
    }
    
    private void readTodo (String id, boolean shouldBeDone) throws IOException {
        // Arrange
        //  (nothing to do)
        
        // Act
        var resultStream =
            Request.Get("http://localhost:7070/api/TodoItem/" + id)
            .execute()
            .returnContent()
            .asStream();
        var resultModel = Json.createReader(resultStream).readObject();
        
        // Assert
        String resultId = resultModel.getString("id");
        String resultText = resultModel.getString("text");
        boolean resultDone = resultModel.getBoolean("done");
        
        assertEquals(id, resultId);
        assertEquals(TODO_TEXT, resultText);
        assertEquals(shouldBeDone, resultDone);
    }
    
    private void markTodoDone (String id) throws IOException {
        // Arrange
        var body = Json.createObjectBuilder()
            .add("done", true)
            .build();
        
        // Act
        int resultStatus =
            Request.Put("http://localhost:7070/api/TodoItem/" + id)
                .bodyString(body.toString(), ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse()
                .getStatusLine()
                .getStatusCode();
        
        // Assert
        assertEquals(204, resultStatus);
    }
}
