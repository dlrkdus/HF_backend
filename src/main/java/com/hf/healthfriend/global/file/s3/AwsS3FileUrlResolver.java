package com.hf.healthfriend.global.file.s3;

import com.hf.healthfriend.global.file.FileUrlResolver;
import com.hf.healthfriend.global.file.image.ImageExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Component
@Profile("prod")
public class AwsS3FileUrlResolver implements FileUrlResolver {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;

    public AwsS3FileUrlResolver(S3Client s3Client, S3Presigner s3Presigner, @Value("${spring.cloud.aws.s3.bucket}") String bucketName) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
    }

    @Override
    public String generateFilePath(String filename, String... paths) {
        return String.join("/", paths) + (paths.length > 0 ? "/" : "") + filename;
    }

    @Override
    public String generateFilePathWithUuid(ImageExtension extension, String... paths) {
        return generateFilePath(UUID.randomUUID() + extension.value(), paths);
    }

    @Override
    public String resolveFileUrl(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return null;
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(this.bucketName)
                .key(filePath)
                .build();

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest
                .builder()
                .signatureDuration(Duration.ofMinutes(5))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedGetObjectRequest = this.s3Presigner.presignGetObject(getObjectPresignRequest);

        return presignedGetObjectRequest.url().toString();
    }

    @Override
    public String generateUploadUrl(String filename, String... paths) {
        return "https://api.healthfriend.site/hf/files/" + generateFilePath(filename, paths);
    }
}
