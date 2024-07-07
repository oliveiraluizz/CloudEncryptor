package com.cloud.encryptor.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "file_metadata")
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String fileType;
    private String filePath;
    private String encryptionKey;
    private Long userId;
}
