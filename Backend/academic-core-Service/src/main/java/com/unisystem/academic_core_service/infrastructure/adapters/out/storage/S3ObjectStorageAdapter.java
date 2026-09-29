package com.unisystem.academic_core_service.infrastructure.adapters.out.storage;

import com.unisystem.academic_core_service.application.port.out.ObjectStoragePort;
import com.unisystem.academic_core_service.domain.exceptions.AwsError;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class S3ObjectStorageAdapter implements ObjectStoragePort {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket:academic-core-service}")
    private String bucketName;

    @Override
    public PresignedUpload createUploadUrl(UploadRequest request) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(request.objectKey())
                    .contentType(request.contentType())
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .putObjectRequest(putObjectRequest)
                    .signatureDuration(request.expiration())
                    .build();

            var presigned = s3Presigner.presignPutObject(presignRequest);

            return new PresignedUpload(
                    request.objectKey(),
                    URI.create(presigned.url().toString()),
                    presigned.expiration()
            );
        } catch (S3Exception | SdkClientException ex) {
            throw new AwsError("Could not create S3 upload URL", ex);
        }
    }

    @Override
    public URI createDownloadUrl(String objectKey, Duration expiration) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(getObjectRequest)
                    .signatureDuration(expiration)
                    .build();

            var presigned = s3Presigner.presignGetObject(presignRequest);
            return URI.create(presigned.url().toString());
        } catch (S3Exception | SdkClientException ex) {
            throw new AwsError("Could not create S3 download URL", ex);
        }
    }

    @Override
    public StoredObjectMetadata headObject(String objectKey) {
        try {
            HeadObjectResponse response = s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .build()
            );

            return new StoredObjectMetadata(
                    response.contentLength(),
                    response.contentType(),
                    response.eTag()
            );
        } catch (S3Exception | SdkClientException ex) {
            throw new AwsError("Could not inspect S3 object", ex);
        }
    }

    @Override
    public void deleteObject(String objectKey) {
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .build()
            );
        } catch (S3Exception | SdkClientException ex) {
            throw new AwsError("Could not delete S3 object", ex);
        }
    }
}
