package net.issachanzi.resteasy.controller;

import net.issachanzi.resteasy.controller.exception.HttpErrorStatus;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A servlet to pass requests to a {@link HttpController}
 */
public class ServletController extends HttpServlet {
    // https://app.example.com/api/ModelName/id/method

    private HttpController controller;

    /**
     * Constructs a {@code ServletController} given a {@link HttpController}
     *
     * @param controller The controller to pass requests to
     */
    public ServletController (HttpController controller) {
        this.controller = controller;
    }

    /**
     * Constructs a {@code ServletController} given a {@link Controller}
     *
     * @param controller The controller to pass requests to
     */
    public ServletController (Controller controller) {
        this.controller = new HttpController (controller);
    }

    @Override
    public void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        UUID id = getId(request);
        Map<String, String> query = getQuery(request);

        var origin = request.getHeader("Origin");
        response.addHeader("Access-Control-Allow-Origin", origin);

        try {
            String authorization = request.getHeader("Authorization");
            String responseContent = controller.get(id, query, authorization);

            sendResponse(response, responseContent);
        } catch (HttpErrorStatus errorStatus) {
            sendError(response, errorStatus);
        }
    }

    @Override
    public void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        UUID id = getId(request);
        String methodName = getPathParam(request, 1);
        String body = getBody(request);

        var origin = request.getHeader("Origin");
        response.addHeader("Access-Control-Allow-Origin", origin);

        try {
            String authorization = request.getHeader("Authorization");
            String responseContent = controller.post(
                    id,
                    methodName,
                    body,
                    authorization
            );

            sendResponse(response, responseContent);
        }
        catch (HttpErrorStatus errorStatus) {
            sendError(response, errorStatus);
        }
    }

    @Override
    public void doPut (
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        UUID id = getId(request);
        String body = getBody(request);

        var origin = request.getHeader("Origin");
        response.addHeader("Access-Control-Allow-Origin", origin);

        try {
            String authorization = request.getHeader("Authorization");
            controller.put(id, body, authorization);

            sendResponse(response, null);
        }
        catch (HttpErrorStatus errorStatus) {
            sendError(response, errorStatus);
        }
    }

    @Override
    public void doDelete(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        UUID id = getId(request);

        var origin = request.getHeader("Origin");
        response.addHeader("Access-Control-Allow-Origin", origin);

        try {
            String authorization = request.getHeader("Authorization");
            controller.delete(id, authorization);

            sendResponse(response, null);
        } catch (HttpErrorStatus errorStatus) {
            sendError(response, errorStatus);
        }

    }

    @Override
    public void doOptions (
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        var requestMethod = request.getHeader("Access-Control-Request-Method");
        var requestHeaders = request.getHeader("Access-Control-Request-Headers");
        var origin = request.getHeader("Origin");

        response.addHeader("Access-Control-Allow-Origin", origin);
        response.addHeader(
                "Access-Control-Allow-Methods",
                "OPTIONS, GET, POST, PUT, PATCH, DELETE"
        );
        response.addHeader("Access-Control-Allow-Headers", requestHeaders);

        response.setStatus(204);
    }

    private static void sendError(
            HttpServletResponse response,
            HttpErrorStatus errorStatus
    ) {
        int statusCode = errorStatus.statusCode();
        String message = errorStatus.getMessage();

        try {
            response.sendError(statusCode, message);
        } catch (IOException e) {
            response.setStatus(500);
        }
    }

    private void sendResponse(
            HttpServletResponse response,
            String responseContent
    ) {
        try {
            if(responseContent != null && !responseContent.isEmpty()) {
                response.setStatus(200);
                response.getOutputStream().print(responseContent);
            }
            else {
                response.setStatus(204); // No content
            }
        } catch (IOException e) {
            response.setStatus(500);
        }
    }

    private String getBody(HttpServletRequest request) {
        try {
            var stream = request.getInputStream();
            byte[] bytes = stream.readAllBytes();

            String body = new String(bytes, StandardCharsets.UTF_8);

            return body;
        } catch (IOException e) {
            return null;
        }
    }

    private String getPathParam(HttpServletRequest request, int index) {
        String pathInfo = request.getPathInfo();
        if(pathInfo == null) {
            return null;
        }

        String[] path = pathInfo.split("/");
        if(path.length < index + 1) {
            return null;
        }

        return path[index + 1];
    }

    private UUID getId(HttpServletRequest request) {
        String uuid = getPathParam(request, 0);

        if (uuid == null) {
            return null;
        }
        else {
            return UUID.fromString(uuid);
        }
    }

    private Map<String, String> getQuery(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if(queryString == null) {
            return null;
        }

        Map<String, String> result = new HashMap<>();
        String[] queries = queryString.split("&");
        for (String query : queries) {
            if (query.contains("=")) {
                String[] splitQuery = query.split("=");

                String key = splitQuery[0];
                String value = splitQuery[1];

                result.put(key, value);
            }
        }

        return result;
    }
}
