package za.co.hydra24.event;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import za.co.hydra24.api.ApiErrors;
import za.co.hydra24.security.AdminOnly;

/**
 * An event's image, in two steps: POST /upload for a presigned URL, PUT the file
 * there, then PUT here with the key to show it. Replacing or removing an image
 * deletes the old file.
 */
@Path("/api/admin/events/{id}/image")
@AdminOnly
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventImageAdminResource {

    @RegisterForReflection
    public record UploadRequest(@NotNull Long size) {
    }

    @RegisterForReflection
    public record AttachRequest(@NotNull String key) {
    }

    @Inject
    EventRepository events;

    @Inject
    EventImages images;

    @POST
    @Path("/upload")
    public ImageUpload prepareUpload(@PathParam("id") long id, @Valid UploadRequest request) {
        requireEvent(id);
        return images.prepareUpload(id, request.size());
    }

    @PUT
    public void attach(@PathParam("id") long id, @Valid AttachRequest request) {
        requireEvent(id);
        if (!EventImages.isKeyFor(id, request.key())) {
            throw ApiErrors.badRequest("That is not an image uploaded for this event.");
        }
        replaceImage(id, request.key());
    }

    @DELETE
    public void remove(@PathParam("id") long id) {
        requireEvent(id);
        replaceImage(id, null);
    }

    private void replaceImage(long id, String newKey) {
        String oldKey = events.findImageKey(id).orElse(null);
        events.updateImageKey(id, newKey);
        if (oldKey != null && !oldKey.equals(newKey)) {
            images.delete(oldKey);
        }
    }

    private void requireEvent(long id) {
        if (events.find(id).isEmpty()) {
            throw new NotFoundException();
        }
    }
}
