package net.issachanzi.resteasy;

import net.issachanzi.resteasy.controller.Controller;
import net.issachanzi.resteasy.controller.EasyController;
import net.issachanzi.resteasy.controller.ServletController;
import net.issachanzi.resteasy.model.EasyModel;
import net.issachanzi.resteasy.model.Loader;
import net.issachanzi.resteasy.model.ModelType;
import net.issachanzi.resteasy.model.Schema;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * Main class for a Rest Easy application
 */
public class RestEasy {

    public static final String DEFAULT_DB_URL = "jdbc:postgresql://localhost:5432/app?user=postgres";
    public static final int DEFAULT_LISTEN_PORT = 7070;
    public static final String SCHEMA_FILENAME = "schema.json";

    public final int listenPort;
    public final Connection db;

    /**
     * Constructs a Rest Easy application
     *
     * @throws SQLException if a database access error occurs
     */
    public RestEasy () throws SQLException {
        String dbUrl
            = Optional.ofNullable (System.getenv("DB_URL"))
            .orElse(DEFAULT_DB_URL);
        this.listenPort
            = Optional.ofNullable (System.getenv("LISTEN_PORT"))
            .map (Integer::valueOf)
            .orElse (DEFAULT_LISTEN_PORT);
        this.db = DriverManager.getConnection(dbUrl);
    }

    /**
     * Constructs a Rest Easy application
     *
     * @param db The database connection to use
     */
    public RestEasy (Connection db) {
        this.db = db;
        this.listenPort
            = Optional.ofNullable (System.getenv("LISTEN_PORT"))
            .map (Integer::valueOf)
            .orElse (DEFAULT_LISTEN_PORT);
    }

    /**
     * Constructs a Rest Easy application
     *
     * @param listenPort The port to listen for HTTP connections on
     */
    public RestEasy (int listenPort) throws SQLException {
        this.listenPort = listenPort;
        String dbUrl
                = Optional.ofNullable (System.getenv("DB_URL"))
                .orElse(DEFAULT_DB_URL);
        this.db = DriverManager.getConnection(dbUrl);
    }

    /**
     * Starts the Rest Easy application
     *
     * @throws Exception If an error occurs
     */
    public void init() throws Exception {
        var models = new Loader ().load();

        initSchema (models);

        initModel(models, db);
        initController(models, db, listenPort);
    }

    private void initModel (
            Collection<Class<? extends EasyModel>> models,
            Connection db
    ) {
        syncModels(models, db);
    }

    private void initController (
            Collection<Class<? extends EasyModel>> models,
            Connection db,
            int listenPort
    ) throws Exception {
        var server = new Server(listenPort);
        var connector = new ServerConnector (server);
        server.addConnector (connector);

        var context = new ServletContextHandler();
        context.setContextPath("/api");
        server.setHandler(context);

        for (var model : models) {
            String modelName = model.getSimpleName();
            Controller controller = new EasyController(model, db);
            var servlet = new ServletController(controller);

            context.addServlet(servlet, "/" + modelName + "/*");
        }

        server.start();
    }

    private static void syncModels(
            Collection <Class <? extends EasyModel>> models,
            Connection db
    ) {
        for (var model : models) {
            ModelType.get(model).sync (db);
        }
        for (var model : models) {
            ModelType.get (model).syncAssociations (db);
        }
    }

    private void initSchema (Collection<Class<? extends EasyModel>> models) {
        try {
            var schema = new Schema(models).schema();

            Path path = new File (SCHEMA_FILENAME).toPath();
            System.out.println("Schema path" + path);
            Files.writeString(path, schema.toString());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
