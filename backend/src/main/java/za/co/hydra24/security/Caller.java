package za.co.hydra24.security;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.container.ContainerRequestContext;

import java.util.UUID;

/** The signed-in user making a request, taken from a verified ID token. */
public record Caller(String sub, String email, boolean admin) {

    private static final String PROPERTY = Caller.class.getName();

    void attachTo(ContainerRequestContext request) {
        request.setProperty(PROPERTY, this);
    }

    /** The caller's Cognito sub, the key to their users row. */
    public UUID subject() {
        return UUID.fromString(sub);
    }

    /** The caller of this request; 401 when it carried no bearer token. */
    public static Caller of(ContainerRequestContext request) {
        if (request.getProperty(PROPERTY) instanceof Caller caller) {
            return caller;
        }
        throw new NotAuthorizedException("Bearer");
    }
}
