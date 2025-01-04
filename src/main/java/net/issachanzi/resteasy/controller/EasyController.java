package net.issachanzi.resteasy.controller;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import net.issachanzi.resteasy.controller.exception.*;
import net.issachanzi.resteasy.model.AccessType;
import net.issachanzi.resteasy.model.EasyModel;
import net.issachanzi.resteasy.model.ModelType;
import net.issachanzi.resteasy.model.SqlDatatypes;
import net.issachanzi.resteasy.view.EasyView;

import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A controller for {@link EasyModel} models
 */
public class EasyController implements Controller {
//    private final Class<? extends EasyModel> modelClazz;
    private final ModelType modelType;
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
        this.modelType = ModelType.get (modelClazz);
        this.db = db;
    }


    @Override
    public String get(String authorization) throws HttpErrorStatus {
        try {
            var models = EasyModel.all(db, modelType.modelClass())
                    .stream().filter(model -> {
                        try {
                            return model.authorize(
                                    db,
                                    authorization,
                                    AccessType.READ
                            );
                        } catch (HttpErrorStatus e) {
                            return false;
                        }
                    }
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
            var models = EasyModel.where(db, params, modelType.modelClass())
                    .stream().filter(model -> {
                        try {
                            return model.authorize(
                                    db,
                                    authorization,
                                    AccessType.READ
                            );
                        } catch (HttpErrorStatus e) {
                            return false;
                        }
                    }).collect(Collectors.toList());

            return new EasyView(models).toString();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new InternalServerError(e);
        }
    }

    @Override
    public String get(UUID id, String authorization) throws HttpErrorStatus {
        try {
            var model = EasyModel.byId(db, id, modelType.modelClass());

            if (!model.authorize(db, authorization, AccessType.READ)) {
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
            var model = modelType.fromJson(db, body);

            if (!model.authorize(db, authorization, AccessType.CREATE)) {
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
        try {
            var reader = Json.createReader (new StringReader(body));
            JsonObject bodyJson = reader.readObject();
            Method customMethod = modelType.customMethod (methodName);
            EasyModel modelInstance = EasyModel.byId(
                    db,
                    id,
                    modelType.modelClass()
            );

            if (!modelInstance.authorize(
                    db,
                    authorization,
                    AccessType.CUSTOM_METHOD
            )) {
                throw new Forbidden();
            }

            var requiredParams = customMethod.getParameters();
            var args = new LinkedList<Object>();

            for (var param : requiredParams) {
                if (param.getName().equals("authorization")
                        && param.getType() == String.class) {
                    args.add (authorization);
                }
                else if (param.getType() == Connection.class) {
                    args.add (db);
                }
                else if (!bodyJson.containsKey(param.getName())) {
                    throw new BadRequest (param.getName() + " is required");
                }
                else {
                    try {
                        args.add(SqlDatatypes.objectFromJson(
                                param.getName(),
                                param.getType(),
                                db,
                                bodyJson
                        ));
                    } catch (IllegalArgumentException ex) {
                        throw new InternalServerError(ex);
                    }
                }
            }

            var result = customMethod.invoke (modelInstance, args);

            if (result instanceof EasyModel) {
                return new EasyView ((EasyModel) result).toString();
            }
            else if (result != null) {
                return result.toString();
            }
            else {
                return "";
            }

        }
        catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof HttpErrorStatus) {
                throw (HttpErrorStatus) ex.getCause();
            }
            else {
                throw new BadRequest(ex.getCause());
            }
        }
        catch (
                IllegalArgumentException |
                IllegalAccessException |
                SQLException ex
        ) {
            throw new BadRequest(ex);
        }
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
            var model = EasyModel.byId(db, id, modelType.modelClass());

            if (!model.authorize(db, authorization, AccessType.DELETE)) {
                throw new Forbidden();
            }

            model.delete(db);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new InternalServerError();
        }
    }
}
