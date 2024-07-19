package net.issachanzi.resteasy.model;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

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
        var builder = Json.createObjectBuilder(EasyModel.schema(model));

        return builder.build();
    }
}
