package net.issachanzi.resteasy.view;

import net.issachanzi.resteasy.model.EasyModel;

import java.lang.reflect.Array;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;

import jakarta.json.Json;
import jakarta.json.JsonValue;

/**
 * A view to display EasyModel instances as JSON, for a REST API
 */
public class EasyView {
    private final JsonValue json;

    /**
     * Constructs an EasyView for a single model instance
     *
     * @param model The model instance to display in the view
     * @param <M> The type of model to be displayed
     */
    public <M extends EasyModel> EasyView (M model) {
        this (model, null);
    }

    /**
     * Constructs an EasyView for a collection of multiple model instances
     *
     * @param models The model instances to display in the view
     * @param <M> The type of model to be displayed
     */
    public <M extends EasyModel> EasyView(Collection<M> models) {
        var builder = Json.createArrayBuilder();

        for (var model : models) {
            var view = new EasyView (model);
            builder.add(view.json ());
        }

        this.json = builder.build();
    }

    /**
     * Constructs an EasyView for a single model instance
     *
     * <p>
     *     This version is meant to filter fields to be displayed, based on an
     *     authorisation. This is not implemented yet.
     * </p>
     *
     * @param model The model instance to display in the view
     * @param authorization The value of the {@code Authorization} HTTP header
     * @param <M> The type of model to be displayed
     */
    public <M extends EasyModel> EasyView(M model, String authorization) {
        var fieldValues = model.httpFieldValues(authorization);

        var builder = Json.createObjectBuilder();
        for (var fieldName : fieldValues.keySet()) {
            Object value = fieldValues.get (fieldName);

            builder.add(fieldName, jsonValue(value));
        }

        this.json = builder.build();
    }

    private JsonValue jsonValue(Object value) {
        JsonValue jsonValue;

        // TODO - refactor into data type classes
        if (value == null) {
            jsonValue = JsonValue.NULL;
        }
        else if (value instanceof Boolean) {
            jsonValue = (boolean) value ? JsonValue.TRUE : JsonValue.FALSE;
        }
        else if (value instanceof Integer) {
            jsonValue = Json.createValue((int) value);
        }
        else if (value instanceof Long) {
            jsonValue = Json.createValue ((long) value);
        }
        else if (value instanceof Float) {
            jsonValue = Json.createValue ((float) value);
        }
        else if (value instanceof Double) {
            jsonValue = Json.createValue ((double) value);
        }
        else if (value.getClass().isArray()) {
            jsonValue = arrayJsonValue(value);
        }
        else if (EasyModel.class.isAssignableFrom(value.getClass())) {
            jsonValue = Json.createValue(((EasyModel) value).id.toString());
        }
        else if (value instanceof Date) {
            jsonValue = Json.createValue(((Date) value).getTime());
        }
        else if (value instanceof Time) {
            jsonValue = Json.createValue(((Time) value).getTime());
        }
        else if (value instanceof Timestamp) {
            jsonValue = Json.createValue(((Timestamp) value).getTime());
        }
        else {
            jsonValue = Json.createValue (value.toString());
        }
        return jsonValue;
    }

    private JsonValue arrayJsonValue(Object values) {
        var builder = Json.createArrayBuilder();

        var length = Array.getLength(values);
        for (int i = 0; i < length; i++) {
            var value = Array.get(values, i);

            builder.add(jsonValue (value));
        }

        return builder.build();
    }

    /**
     * Gets the content of this view as JSON
     *
     * @return This view rendered as JSON
     */
    JsonValue json () {
        return this.json;
    }

    public String toString () {
        return this.json.toString();
    }
}
