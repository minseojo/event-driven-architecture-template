package com.minseojo.template.infrastructure.persistence;

import com.minseojo.template.domain.file.FileRecord;
import com.minseojo.template.domain.file.port.FileRecordRepositoryPort;
import com.minseojo.template.infrastructure.persistence.jpa.FileRecordJpaEntity;
import com.minseojo.template.infrastructure.persistence.jpa.FileRecordJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 파일 레코드 저장소 어댑터 (Infrastructure Layer)
 * Domain의 포트를 JPA Repository로 구현
 */
@Component
@RequiredArgsConstructor
public class FileRecordJpaRepositoryAdapter implements FileRecordRepositoryPort {

    private final FileRecordJpaRepository springDataRepository;

    @Override
    public FileRecord save(FileRecord fileRecord) {
        FileRecordJpaEntity entity = FileRecordJpaEntity.fromDomain(fileRecord);
        FileRecordJpaEntity saved = springDataRepository.save(entity);
        return saved.toDomain();
    }
    
    @Override
    public Optional<FileRecord> findById(Long id) {
        return springDataRepository.findById(id)
                .map(FileRecordJpaEntity::toDomain);
    }
    
    @Override
    public List<FileRecord> findAll() {
        return springDataRepository.findAll().stream()
                .map(FileRecordJpaEntity::toDomain)
                .toList();
    }
    
    @Override
    public void deleteById(Long id) {
        springDataRepository.deleteById(id);
    }
}

