package za.co.hydra24.event;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import za.co.hydra24.api.ApiErrors;

import java.time.Duration;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Event images in the images bucket, under event-images/&lt;event id&gt;/. The site
 * serves that folder at the same path, and shrinks every image to a JPEG before
 * uploading it straight to the bucket with a presigned PUT.
 */
@ApplicationScoped
public class EventImages {

    static final String CONTENT_TYPE = "image/jpeg";
    static final long MAX_BYTES = 2 * 1024 * 1024;

    private static final String FOLDER = "event-images";
    private static final Duration UPLOAD_WINDOW = Duration.ofMinutes(5);
    private static final Pattern UUID_JPG = Pattern.compile("[0-9a-f-]{36}\\.jpg");

    @Inject
    S3Client s3;

    @Inject
    S3Presigner presigner;

    @ConfigProperty(name = "images.bucket")
    String bucket;

    /** Where the site serves an image: a root-absolute path, or null for none. */
    public static String path(String key) {
        return key == null ? null : "/" + key;
    }

    /** True for a key this class could have handed out for the event. */
    static boolean isKeyFor(long eventId, String key) {
        String folder = FOLDER + "/" + eventId + "/";
        return key != null && key.startsWith(folder) && UUID_JPG.matcher(key.substring(folder.length())).matches();
    }

    /** A URL the browser can PUT exactly this many bytes of JPEG to, for a few minutes. */
    ImageUpload prepareUpload(long eventId, long size) {
        if (size <= 0 || size > MAX_BYTES) {
            throw ApiErrors.badRequest("The image must be under 2 MB.");
        }
        String key = FOLDER + "/" + eventId + "/" + UUID.randomUUID() + ".jpg";
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(CONTENT_TYPE)
                .contentLength(size)
                .build();
        String url = presigner.presignPutObject(PutObjectPresignRequest.builder()
                        .signatureDuration(UPLOAD_WINDOW)
                        .putObjectRequest(put)
                        .build())
                .url().toString();
        return new ImageUpload(url, key, CONTENT_TYPE);
    }

    /** Best effort: a leftover file costs little, a failed request after the database changed costs more. */
    void delete(String key) {
        try {
            s3.deleteObject(request -> request.bucket(bucket).key(key));
        } catch (SdkException e) {
            Log.warnf(e, "Could not delete image %s", key);
        }
    }
}
