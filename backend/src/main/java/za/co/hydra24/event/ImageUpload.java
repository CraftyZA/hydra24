package za.co.hydra24.event;

import io.quarkus.runtime.annotations.RegisterForReflection;

/** Where to PUT an image, with which Content-Type, and the key to attach afterwards. */
@RegisterForReflection
public record ImageUpload(String uploadUrl, String key, String contentType) {
}
