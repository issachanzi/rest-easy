package net.issachanzi.resteasy.controller;

import net.issachanzi.resteasy.controller.exception.HttpErrorStatus;
import net.issachanzi.resteasy.controller.exception.BadRequest;

import java.util.Map;
import java.util.UUID;

/**
 * A mapping between the four main HTTP verbs and the more specific versions
 * specified by the {@link Controller} interface.
 */
public class HttpController {
    private final Controller controller;

    /**
     * Constructs a {@code HttpController} for a given {@link Controller}
     *
     * @param controller The {@code Controller} to pass requests to
     */
    public HttpController(Controller controller) {
        this.controller = controller;
    }

    /**
     * Handles all HTTP {@code GET} requests
     *
     * @param id The id of the model to get, or {@code null} if no id is given
     * @param query The query parameters, or {@code null} if none are given
     * @param authorization The value of the HTTP {@code Authorization header}
     * @return The response body to return to the client
     * @throws HttpErrorStatus If the request is not successful
     */
    public String get(
            UUID id,
            Map<String, String> query,
            String authorization
    ) throws HttpErrorStatus {
        if (id == null && query == null) {
            return controller.get(authorization);
        }
        else if (id == null) {
            return controller.get(query, authorization);
        }
        else if (query == null) {
            return controller.get(id,authorization);
        }
        else {
            throw new BadRequest();
        }
    }

    /**
     * Handles all HTTP {@code GET} requests
     *
     * @param id The id of the model instance to access, or {@code null} if no
     *           id is given
     * @param methodName The name of the custom method to invoke on the model
     *                   instance
     * @param body The request body
     * @param authorization The value of the HTTP {@code Authorization header}
     * @return The response body to return to the client
     * @throws HttpErrorStatus If the request is not successful
     */
    public String post(
            UUID id,
            String methodName,
            String body,
            String authorization
    ) throws HttpErrorStatus {
        if(methodName != null && body != null) {
            return controller.post(id, methodName, body, authorization);
        }
        else if (body != null) {
            return controller.post(body, authorization);
        }
        else {
            throw new BadRequest();
        }
    }

    /**
     * Handles all HTTP {@code PUT} requests
     *
     * @param id The id of the model instance to update
     * @param body The request body
     * @param authorization The value of the HTTP {@code Authorization header}
     * @return The response body to return to the client
     * @throws HttpErrorStatus If the request is not successful
     */
    public String put(
            UUID id,
            String body,
            String authorization
    ) throws HttpErrorStatus {
        if (id != null) {
            controller.put(id, body, authorization);

            return null;
        }
        else {
            throw new BadRequest("Missing ID in path");
        }
    }

    /**
     * Handles all HTTP {@code DELETE} requests
     *
     * @param id The id of the model instance to delete
     * @param authorization The value of the HTTP {@code Authorization header}
     * @return The response body to return to the client
     * @throws HttpErrorStatus If the request is not successful
     */
    public String delete(UUID id, String authorization) throws HttpErrorStatus {
        if(id != null) {
            controller.delete(id, authorization);

            return null;
        }
        else {
            throw new BadRequest("Missing ID in path");
        }
    }

}
