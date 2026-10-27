package za.co.hydra24.security;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * Attaches the {@link Caller} to requests with a valid bearer token. No token:
 * the request carries on anonymously. A token that does not verify: 401.
 */
@Provider
@PreMatching
public class BearerTokenFilter implements ContainerRequestFilter {

    private static final String BEARER = "Bearer ";

    @Inject
    JwtVerifier verifier;

    @Override
    public void filter(ContainerRequestContext request) {
        String header = request.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER)) {
            return;
        }
        verifier.verify(header.substring(BEARER.length()).trim()).ifPresentOrElse(
                caller -> caller.attachTo(request),
                () -> request.abortWith(Response.status(Response.Status.UNAUTHORIZED).build()));
    }
}
