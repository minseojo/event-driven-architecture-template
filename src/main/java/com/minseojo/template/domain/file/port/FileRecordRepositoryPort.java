package com.minseojo.template.domain.file.port;

import com.minseojo.template.domain.file.FileRecord;

import java.util.List;
import java.util.Optional;

public interface FileRecordRepositoryPort {
    FileRecord save(FileRecord fileRecord);
    Optional<FileRecord> findById(Long id);
    List<FileRecord> findAll();
    void deleteById(Long id);
}

