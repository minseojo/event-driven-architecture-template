package com.minseojo.template.application.file;

import com.minseojo.template.domain.file.port.FileRecordRepositoryPort;
import com.minseojo.template.domain.file.port.StoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 파일 삭제 유즈케이스 (Application Layer)
 */
@Service
@RequiredArgsConstructor
public class FileDeleteUseCase {

    private final FileRecordRepositoryPort fileRecordRepositoryPort;
    private final StoragePort storagePort;

    @Transactional
    public void execute(Long fileId) {
        Optional<com.minseojo.template.domain.file.FileRecord> fileRecordOpt = fileRecordRepositoryPort.findById(fileId);
        
        if (fileRecordOpt.isEmpty()) {
            throw new FileNotFoundException("File not found with id: " + fileId);
        }
        
        com.minseojo.template.domain.file.FileRecord fileRecord = fileRecordOpt.get();
        
        // 1. 스토리지에서 물리 파일 삭제 (실패 시 예외 발생하여 트랜잭션 롤백)
        try {
            storagePort.delete(fileRecord.getStorageKey());
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(FileDeleteUseCase.class)
                    .error("Failed to delete file from storage: {}", fileRecord.getStorageKey(), e);
            throw new RuntimeException("Failed to delete file from storage: " + e.getMessage(), e);
        }
        
        // 2. DB에서 메타데이터 삭제 (스토리지 삭제 성공 후에만 실행)
        fileRecordRepositoryPort.deleteById(fileId);
    }
    
    public static class FileNotFoundException extends RuntimeException {
        public FileNotFoundException(String message) {
            super(message);
        }
    }
}

