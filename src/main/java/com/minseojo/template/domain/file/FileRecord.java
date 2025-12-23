package com.minseojo.template.domain.file;

import lombok.*;
import java.time.LocalDateTime;

/**
 * 파일 레코드 도메인 엔티티 (Domain Layer)
 * 헥사고날 아키텍처의 핵심 비즈니스 객체
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FileRecord {
    private Long id;
    private String originalFilename;
    private String storageKey; // 스토리지 URL 또는 Key
    private String contentType;
    private long sizeBytes;
    private LocalDateTime uploadedAt;

    /**
     * 파일 레코드 생성 팩토리 메서드
     */
    public static FileRecord create(String originalFilename, String storageKey, 
                                   String contentType, long sizeBytes) {
        return FileRecord.builder()
                .originalFilename(originalFilename)
                .storageKey(storageKey)
                .contentType(contentType)
                .sizeBytes(sizeBytes)
                .uploadedAt(LocalDateTime.now())
                .build();
    }
}
