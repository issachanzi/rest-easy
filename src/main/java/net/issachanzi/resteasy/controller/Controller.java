package net.issachanzi.resteasy.controller;

import net.issachanzi.resteasy.controller.exception.HttpErrorStatus;

import java.util.Map;
import java.util.UUID;

/**
 * A mapping between HTTP requests and actions performed by the model
 */
public interface Controller {
    /**
     * Handles an incoming GET request with no query parameters and no model
     * instance id given
     *
     * @param authorization The value of the HTTP {@code Authorization} header
     * @return The response body to return to the client
     * @throws HttpErrorStatus If the request is not successful
     */
    String get(String authorization) throws HttpErrorStatus;

    /**
     * Handles an incoming GET request with query parameters given
     *
     * @param params A map of the HTTP query parameters
     * @param authorization The value of the HTTP {@code Authorization} header
     * @return The response body to return to the client
     * @throws HttpErrorStatus If the request is not successful
     */
    String get(Map<String, String> params, String authorization)
            throws HttpErrorStatus;

    /**
     * Handles an incoming GET request with an id of a model instance given
     *
     * @param id The id of the model instance requested
     * @param authorization The value of the HTTP {@code Authorization} header
     * @return The response body to return to the client
     * @throws HttpErrorStatus If the request is not successful
     */
    String get(UUID id, String authorization) throws HttpErrorStatus;

    /**
     * Handles an incoming POST request
     *
     * @param body The request body
     * @param authorization The value of the HTTP {@code Authorization} header
     * @return The response body to return to the client
     * @throws HttpErrorStatus If the request is not successful
     */
    String post(String body, String authorization) throws HttpErrorStatus;

    /**
     * Handles an incoming POST request with an id and custom method name given
     *
     * @param id The id of the model instance to invoke a method on
     * @param methodName The name of the custom method to invoke
     * @param body The request body
     * @param authorization The value of the HTTP {@code Authorization} header
     * @return The response body to return to the client
     * @throws HttpErrorStatus If the request is not successful
     */
    String post(UUID id, String methodName, String body, String authorization)
            throws HttpErrorStatus;

    /**
     * Handles an incoming PUT request
     *
     * @param id The id of the model instance to update
     * @param body The request body
     * @param authorization The value of the HTTP {@code Authorization} header
     * @throws HttpErrorStatus If the request is not successful
     */
    void put(UUID id, String body, String authorization) throws HttpErrorStatus;

    /**
     * Handles an incoming DELETE request
     *
     * @param id The id of the model instance to update
     * @param authorization The value of the HTTP {@code Authorization} header
     * @throws HttpErrorStatus If the request is not successful
     */
    void delete(UUID id, String authorization) throws HttpErrorStatus;
}

