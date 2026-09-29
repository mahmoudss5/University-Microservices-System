package com.unisystem.academic_core_service.application.port.out;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;

public interface ObjectStoragePort {
    PresignedUpload createUploadUrl(UploadRequest request);

    URI createDownloadUrl(String objectKey, Duration expiration);

    StoredObjectMetadata headObject(String objectKey);

    void deleteObject(String objectKey);

    record UploadRequest(
            String objectKey,
            String contentType,
            Duration expiration
    ) {
    }

    public record PresignedUpload(
            String objectKey,
            URI url,
            Instant expiresAt
    ) {
    }

    record StoredObjectMetadata(
            long size,
            String contentType,
            String eTag
    ) {
    }

}
