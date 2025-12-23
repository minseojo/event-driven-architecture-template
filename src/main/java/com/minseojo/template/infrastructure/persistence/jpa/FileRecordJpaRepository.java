package com.minseojo.template.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRecordJpaRepository extends JpaRepository<FileRecordJpaEntity, Long> {
}

