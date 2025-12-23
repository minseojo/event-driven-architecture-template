package com.minseojo.template.infrastructure.persistence.jpa;

import com.minseojo.template.domain.file.FileRecord;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * JPA Entity (Infrastructure Layer)
 * Domain Entity와 매핑하는 어댑터 역할
 */
@Entity
@Table(name = "file_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FileRecordJpaEntity {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_filename")
    private String originalFilename;
    
    @Column(name = "storage_key")
    private String storageKey;
    
    @Column(name = "content_type")
    private String contentType;
    
    @Column(name = "size_bytes")
    private long sizeBytes;
    
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    void prePersist() { 
        this.uploadedAt = LocalDateTime.now(); 
    }

    /**
     * Domain Entity로 변환
     */
    public FileRecord toDomain() {
        return FileRecord.builder()
                .id(this.id)
                .originalFilename(this.originalFilename)
                .storageKey(this.storageKey)
                .contentType(this.contentType)
                .sizeBytes(this.sizeBytes)
                .uploadedAt(this.uploadedAt)
                .build();
    }

    /**
     * Domain Entity로부터 생성
     */
    public static FileRecordJpaEntity fromDomain(FileRecord fileRecord) {
        return FileRecordJpaEntity.builder()
                .id(fileRecord.getId())
                .originalFilename(fileRecord.getOriginalFilename())
                .storageKey(fileRecord.getStorageKey())
                .contentType(fileRecord.getContentType())
                .sizeBytes(fileRecord.getSizeBytes())
                .uploadedAt(fileRecord.getUploadedAt() != null 
                    ? fileRecord.getUploadedAt() 
                    : LocalDateTime.now())
                .build();
    }
}

