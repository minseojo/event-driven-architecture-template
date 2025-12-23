package com.minseojo.template.application.file;

import com.minseojo.template.domain.file.FileRecord;
import com.minseojo.template.domain.file.port.FileRecordRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 파일 목록 조회 유즈케이스 (Application Layer)
 */
@Service
@RequiredArgsConstructor
public class FileListUseCase {

    private final FileRecordRepositoryPort fileRecordRepositoryPort;

    public List<FileRecord> execute() {
        return fileRecordRepositoryPort.findAll();
    }
}

