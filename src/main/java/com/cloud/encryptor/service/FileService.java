package com.cloud.encryptor.service;

import com.cloud.encryptor.model.FileMetadata;
import com.cloud.encryptor.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final FileMetadataRepository fileMetadataRepository;
    private final EncryptionService encryptionService;

    public FileMetadata storeFile(MultipartFile file, Long userId) throws Exception {
        String fileName = file.getOriginalFilename();
        Path targetLocation = Paths.get(uploadDir).resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        String encryptionKey = encryptionService.generateKey();
        String encryptedFilePath = targetLocation.toString() + ".enc";
        String fileContent = new String(Files.readAllBytes(targetLocation));
        String encryptedContent = encryptionService.encrypt(fileContent, encryptionKey);
        Files.write(Paths.get(encryptedFilePath), encryptedContent.getBytes());

        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFileName(fileName);
        fileMetadata.setFileType(file.getContentType());
        fileMetadata.setFilePath(encryptedFilePath);
        fileMetadata.setEncryptionKey(encryptionKey);
        fileMetadata.setUserId(userId);

        Files.delete(targetLocation);  // Delete the original unencrypted file

        return fileMetadataRepository.save(fileMetadata);
    }

    public List<FileMetadata> getFilesByUserId(Long userId) {
        return fileMetadataRepository.findByUserId(userId);
    }

    public Optional<FileMetadata> getFileMetadata(Long fileId) {
        return fileMetadataRepository.findById(fileId);
    }

    public byte[] retrieveFile(Long fileId) throws Exception {
        FileMetadata fileMetadata = getFileMetadata(fileId).orElseThrow(() -> new Exception("File not found"));
        String encryptedContent = new String(Files.readAllBytes(Paths.get(fileMetadata.getFilePath())));
        return encryptionService.decrypt(encryptedContent, fileMetadata.getEncryptionKey()).getBytes();
    }
}
