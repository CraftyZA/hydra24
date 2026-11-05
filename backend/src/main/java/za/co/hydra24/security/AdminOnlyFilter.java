package za.co.hydra24.security;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

/** 401 without a token, 403 with one that is not in the admin group. */
@Provider
@AdminOnly
public class AdminOnlyFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext request) {
        if (!Caller.of(request).admin()) {
            throw new ForbiddenException();
        }
    }
}
