package net.issachanzi.resteasy.controller;

import net.issachanzi.resteasy.controller.exception.Forbidden;
import net.issachanzi.resteasy.controller.exception.HttpErrorStatus;
import net.issachanzi.resteasy.controller.exception.InternalServerError;
import net.issachanzi.resteasy.model.AccessType;
import net.issachanzi.resteasy.model.EasyModel;
import net.issachanzi.resteasy.view.EasyView;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A controller for {@link EasyModel} models
 */
public class EasyController implements Controller {
    private final Class<? extends EasyModel> modelClazz;
    private final Connection db;

    /**
     * Constructs an EasyController for a given model class
     *
     * @param modelClazz The model class to create a controller for
     * @param db The database connection to use
     */
    public EasyController(
            Class<? extends EasyModel> modelClazz,
            Connection db
    ) {
        this.modelClazz = modelClazz;
        this.db = db;
    }


    @Override
    public String get(String authorization) throws HttpErrorStatus {
        try {
            var models = EasyModel.all(db, modelClazz).stream().filter(
                    model -> model.authorize(authorization, AccessType.READ)
            ).collect(Collectors.toList());

            return new EasyView(models).toString();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new InternalServerError (ex);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new InternalServerError(ex);
        }
    }

    @Override
    public String get(
            Map<String, String> params,
            String authorization
    ) throws HttpErrorStatus {
        try {
            var models = EasyModel.where(db, params, modelClazz)
                    .stream().filter(model -> model.authorize(
                            authorization,
                            AccessType.READ
                    )).collect(Collectors.toList());

            return new EasyView(models).toString();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new InternalServerError(e);
        }
    }

    @Override
    public String get(UUID id, String authorization) throws HttpErrorStatus {
        try {
            var model = EasyModel.byId(db, id, modelClazz);

            if (!model.authorize(authorization, AccessType.READ)) {
                throw new Forbidden();
            }

            return new EasyView(model).toString();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new InternalServerError(e);
        }
    }

    @Override
    public String post(String body, String authorization) throws HttpErrorStatus {
        try {
            var model = EasyModel.fromJson(db, body, modelClazz);

            if (!model.authorize(authorization, AccessType.CREATE)) {
                throw new Forbidden();
            }

            model.save(db);

            return new EasyView(model).toString();
        } catch (
                NullPointerException |
                SQLException |
                InvocationTargetException |
                NoSuchMethodException |
                InstantiationException |
                IllegalAccessException e
        ) {
            e.printStackTrace();
            throw new InternalServerError(e);
        }
    }

    @Override
    public String post(
            UUID id,
            String methodName,
            String body,
            String authorization
    ) throws HttpErrorStatus {
        // TODO
        var ex = new UnsupportedOperationException("Method endpoints not implemented");
        ex.printStackTrace();
        throw new InternalServerError(ex);
    }

    @Override
    public void put(UUID id, String body, String authorization) throws HttpErrorStatus {
        // TODO
        var ex = new UnsupportedOperationException("Put method not implemented");
        ex.printStackTrace();
        throw new InternalServerError(ex);
    }

    @Override
    public void delete(UUID id, String authorization) throws HttpErrorStatus {
        try {
            var model = EasyModel.byId(db, id, modelClazz);

            if (!model.authorize(authorization, AccessType.DELETE)) {
                throw new Forbidden();
            }

            model.delete(db);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new InternalServerError();
        }
    }
}
