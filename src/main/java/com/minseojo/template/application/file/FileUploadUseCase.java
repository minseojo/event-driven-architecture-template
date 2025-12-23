package com.minseojo.template.application.file;

import com.minseojo.template.domain.file.FileRecord;
import com.minseojo.template.domain.file.port.EventPublisherPort;
import com.minseojo.template.domain.file.port.FileRecordRepositoryPort;
import com.minseojo.template.domain.file.port.StoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 업로드 유즈케이스 (Application Layer)
 * 헥사고날 아키텍처의 Application Layer에서 비즈니스 플로우를 조율
 */
@Service
@RequiredArgsConstructor
public class FileUploadUseCase {

    private final FileRecordRepositoryPort fileRecordRepositoryPort;
    private final StoragePort storagePort;
    private final EventPublisherPort eventPublisherPort;

    @Transactional
    public Long execute(MultipartFile file) {
        try {
            // 1. 물리 파일 업로드 (스토리지는 트랜잭션 외부 리소스)
            byte[] fileContent = file.getBytes();
            String storageKey = storagePort.upload(
                    fileContent,
                    file.getOriginalFilename(),
                    file.getContentType()
            );

            // 2. 메타데이터 저장 (DB)
            FileRecord fileRecord = FileRecord.create(
                    file.getOriginalFilename(),
                    storageKey,
                    file.getContentType(),
                    file.getSize()
            );
            
            FileRecord savedFileRecord = fileRecordRepositoryPort.save(fileRecord);

            // 3. Outbox 이벤트 발행 (DB 트랜잭션 묶음)
            eventPublisherPort.publishFileUploadedEvent(savedFileRecord);

            return savedFileRecord.getId();
        } catch (Exception e) {
            throw new RuntimeException("File upload failed", e);
        }
    }
}

