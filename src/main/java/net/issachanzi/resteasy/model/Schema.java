package net.issachanzi.resteasy.model;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;

/**
 * Utility class to record the types of fields in models, mainly for
 * associations
 *
 * <p>
 *     This is planned to be used for a frontend JavaScript library to
 *     abstract REST API requests for Rest Easy applications.
 * </p>
 */
public class Schema {
    private final JsonObject schema;

    /**
     * Constructs a {@code Schema}
     *
     * @param models The model classes to get the schema for
     */
    public Schema (Collection <Class <? extends EasyModel>> models) {
        this.schema = createSchema (models);
    }

    /**
     * Gets the generated schema as a {@link jakarta.json.JsonObject}
     *
     * @return The schema as a {@code JsonObject}
     */
    public JsonObject schema () {
        return schema;
    }

    private JsonObject createSchema(
            Collection<Class<? extends EasyModel>> models
    ) {
        var builder = Json.createObjectBuilder();

        for (var model : models) {
            String name = model.getSimpleName();
            JsonValue value = modelSchema(model);

            builder.add(name, value);
        }

        return builder.build();
    }

    private JsonObject modelSchema (Class <? extends EasyModel> model) {
        var builder = Json.createObjectBuilder();

        builder.add ("fields", fieldsSchema (model));
        builder.add ("methods", methodsSchema (model));

        return builder.build();
    }

    private JsonObject fieldsSchema (Class <? extends EasyModel> model) {
        var builder = Json.createObjectBuilder();

        var modelSchema = EasyModel.schema(model);
        for (var fieldName : modelSchema.keySet()) {
            String type = modelSchema.get(fieldName);
            boolean isAssociation = false;
            try {
                isAssociation = EasyModel.class.isAssignableFrom (
                        model.getDeclaredField(fieldName).getType()
                );
            } catch (NoSuchFieldException ignored) {}

            var fieldSchema = Json.createObjectBuilder();
            fieldSchema.add ("name", fieldName);
            if (type != null) {
                fieldSchema.add ("type", type);
            }
            else {
                fieldSchema.addNull ("type");
            }
            fieldSchema.add ("isAssociation", isAssociation);

            builder.add(fieldName, fieldSchema);
        }

        return builder.build();
    }

    private JsonObject methodsSchema (Class <? extends EasyModel> model) {
        ModelType modelType = ModelType.get(model);

        var methodsBuilder = Json.createObjectBuilder();

        for (Method cm : modelType.customMethods()) {
            var params = Json.createObjectBuilder();
            
            for (var param : cm.getParameters()) {
                if (param.getName().equals("authorization")
                        && param.getType() == String.class) {
                    continue;
                }
                else if (param.getType() == Connection.class) {
                    continue;
                }

                var paramBuilder = Json.createObjectBuilder();

                paramBuilder.add (
                    "name",
                    param.getName()
                );
                paramBuilder.add (
                    "isAssociation",
                    EasyModel.class.isAssignableFrom (param.getType())
                );
                paramBuilder.add (
                    "isDate",
                    param.getType() == Date.class
                            || param.getType() == Time.class
                            || param.getType() == Timestamp.class
                );

                params.add (param.getName(), paramBuilder.build());
            }

            methodsBuilder.add(cm.getName(), params.build());
        }

        return methodsBuilder.build();
    }
}
