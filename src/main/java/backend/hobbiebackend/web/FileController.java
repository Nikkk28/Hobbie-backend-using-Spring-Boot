package backend.hobbiebackend.web;

import backend.hobbiebackend.service.impl.S3FileStorageServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "http://localhost:4200")
public class FileController {

    private final S3FileStorageServiceImpl s3FileStorageService;

    @Autowired
    public FileController(S3FileStorageServiceImpl s3FileStorageService) {
        this.s3FileStorageService = s3FileStorageService;
    }

    @GetMapping("/url")
    @Operation(summary = "Get S3 file URL")
    public ResponseEntity<String> getFileUrl(@RequestParam String fileName) {
        try {
            String url = s3FileStorageService.getS3Url(fileName);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}