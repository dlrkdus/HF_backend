package com.hf.healthfriend.global.file;

import com.hf.healthfriend.global.spec.ApiBasicResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/hf/files")
public class FileUploadController {
    private final FileUploader fileUploader;

    @PutMapping("/{*filePath}")
    public ResponseEntity<ApiBasicResponse<Void>> hello(@PathVariable("filePath") String filePath,
                                                        InputStream is) throws IOException {
        this.fileUploader.uploadFile(filePath, is);
        return ResponseEntity.ok(
                ApiBasicResponse.of(HttpStatus.OK)
        );
    }
}
