package com.hf.healthfriend.global.file.s3.test;

import com.hf.healthfriend.global.file.FileUrlResolver;
import com.hf.healthfriend.global.file.image.ImageExtension;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Arrays;

@RestController
@RequiredArgsConstructor
@Slf4j
public class S3FileUploaderTestController {
    private final FileUrlResolver fileUrlResolver;
    private final S3Client s3Client;

    @GetMapping("/test/presigned-url")
    public String testPresignedUrl(@RequestParam String extension, @RequestParam("file-path") String[] path) {
        log.info("path={}", Arrays.toString(path));
        String filePath = this.fileUrlResolver.generateFilePathWithUuid(ImageExtension.valueOf(extension.toUpperCase()));
        return this.fileUrlResolver.generateUploadUrl(filePath, path);
    }

    @GetMapping("/test/read-object-url")
    public String testReadObjectUrl(@RequestParam("file-path") String filePath) {
        return this.fileUrlResolver.resolveFileUrl(filePath);
    }
//
//    @GetMapping("/test/sig-version")
//    public String testSignVersion() {
//        return this.s3Client.serviceClientConfiguration();
//    }
}
