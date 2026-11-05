package za.co.hydra24.api;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import za.co.hydra24.db.ForeignKeyViolationException;

import java.util.Map;

/** Errors the site can show as they are: a status and {"error": message}. */
public final class ApiErrors {

    private ApiErrors() {
    }

    public static WebApplicationException badRequest(String message) {
        return new WebApplicationException(response(Response.Status.BAD_REQUEST, message));
    }

    public static WebApplicationException conflict(String message) {
        return new WebApplicationException(response(Response.Status.CONFLICT, message));
    }

    @ServerExceptionMapper
    public Response foreignKeyViolation(ForeignKeyViolationException e) {
        return response(Response.Status.CONFLICT, "That refers to something that no longer exists.");
    }

    private static Response response(Response.Status status, String message) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", message))
                .build();
    }
}
