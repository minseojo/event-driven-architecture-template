package com.minseojo.template.application.file;

import com.minseojo.template.domain.file.FileRecord;
import com.minseojo.template.domain.file.port.FileRecordRepositoryPort;
import com.minseojo.template.domain.file.port.StoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 파일 다운로드 유즈케이스 (Application Layer)
 */
@Service
@RequiredArgsConstructor
public class FileDownloadUseCase {

    private final FileRecordRepositoryPort fileRecordRepositoryPort;
    private final StoragePort storagePort;

    public FileDownloadResult execute(Long fileId) {
        Optional<FileRecord> fileRecord = fileRecordRepositoryPort.findById(fileId);
        
        if (fileRecord.isEmpty()) {
            throw new FileNotFoundException("File not found with id: " + fileId);
        }
        
        FileRecord record = fileRecord.get();
        byte[] fileContent = storagePort.download(record.getStorageKey());
        
        return new FileDownloadResult(
                record.getOriginalFilename(),
                record.getContentType(),
                fileContent
        );
    }
    
    public static class FileDownloadResult {
        private final String filename;
        private final String contentType;
        private final byte[] content;
        
        public FileDownloadResult(String filename, String contentType, byte[] content) {
            this.filename = filename;
            this.contentType = contentType;
            this.content = content;
        }
        
        public String getFilename() {
            return filename;
        }
        
        public String getContentType() {
            return contentType;
        }
        
        public byte[] getContent() {
            return content;
        }
    }
    
    public static class FileNotFoundException extends RuntimeException {
        public FileNotFoundException(String message) {
            super(message);
        }
    }
}

