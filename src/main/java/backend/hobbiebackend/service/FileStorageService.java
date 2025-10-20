package backend.hobbiebackend.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageService {
    String storeFile(MultipartFile file) throws IOException;

    void deleteFile(String fileName) throws IOException;

    Path loadFile(String fileName);

    void init() throws IOException;
}