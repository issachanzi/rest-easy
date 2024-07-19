package net.issachanzi.resteasy;

import net.issachanzi.resteasy.controller.Controller;
import net.issachanzi.resteasy.controller.EasyController;
import net.issachanzi.resteasy.controller.ServletController;
import net.issachanzi.resteasy.model.EasyModel;
import net.issachanzi.resteasy.model.Loader;
import net.issachanzi.resteasy.model.Schema;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.Collection;

/**
 * Main class for a Rest Easy application
 */
public class RestEasy {

    public static final int LISTEN_PORT = 7070;
    public static final String SCHEMA_FILENAME = "schema.json";

    /**
     * Starts the Rest Easy application
     *
     * @param db The database connection to use
     * @throws Exception If an error occurs
     */
    public void init(Connection db) throws Exception {
        var models = Loader.load();

        initSchema (models);

        initModel(models, db);
        initController(models, db);
    }

    private void initModel (
            Collection<Class<? extends EasyModel>> models,
            Connection db
    ) {
        syncModels(models, db);
    }

    private void initController (
            Collection<Class<? extends EasyModel>> models,
            Connection db
    ) throws Exception {
        var server = new Server(LISTEN_PORT);
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
            EasyModel.sync (db, model);
        }
        for (var model : models) {
            EasyModel.syncAssociations (db, model);
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
