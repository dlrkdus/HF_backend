package com.hf.healthfriend.global.file.s3;

import com.hf.healthfriend.global.file.FileUploader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
@Profile("prod")
public class AwsS3FileUploader implements FileUploader {
    private final S3Client s3Client;
    private final String bucketName;

    public AwsS3FileUploader(S3Client s3Client, @Value("${spring.cloud.aws.s3.bucket}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public void uploadFile(String filePath, MultipartFile multipartFile) throws IOException {
        uploadFile(filePath, multipartFile.getInputStream());
    }

    @Override
    public void uploadFile(String filePath, byte[] bytes) throws IOException {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(this.bucketName)
                .contentType(extractContentType(filePath))
                .key(trimLeadingSlash(filePath))
                .build();

        RequestBody requestBody = RequestBody.fromBytes(bytes);
        this.s3Client.putObject(putObjectRequest, requestBody);
    }


    private String extractContentType(String filePath) {
        return "image/" + filePath.substring(filePath.lastIndexOf(".") + 1);
    }

    private String trimLeadingSlash(String filePath) {
        if (filePath.startsWith("/")) {
            return filePath.substring(1);
        }
        return filePath;
    }

    @Override
    public void uploadFile(String filePath, InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            int len;
            byte[] buffer = new byte[2048];
            while ((len = is.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
        } finally {
            is.close();
            bos.close();
        }

        uploadFile(filePath, bos.toByteArray());
    }
}