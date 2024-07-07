package com.cloud.encryptor.controller;

import com.cloud.encryptor.model.FileMetadata;
import com.cloud.encryptor.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class
FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<FileMetadata> uploadFile(@RequestParam("file") MultipartFile file,
                                                   @RequestParam("userId") Long userId) throws Exception {
        return ResponseEntity.ok(fileService.storeFile(file, userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FileMetadata>> getUserFiles(@PathVariable Long userId) {
        return ResponseEntity.ok(fileService.getFilesByUserId(userId));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId) throws Exception {
        FileMetadata fileMetadata = fileService.getFileMetadata(fileId).orElseThrow(() -> new Exception("File not found"));
        byte[] data = fileService.retrieveFile(fileId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileMetadata.getFileName() + "\"")
                .body(data);
    }
}
