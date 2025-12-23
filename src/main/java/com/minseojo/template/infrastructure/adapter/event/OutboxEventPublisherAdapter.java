package com.minseojo.template.infrastructure.adapter.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minseojo.template.domain.file.FileRecord;
import com.minseojo.template.domain.file.port.EventPublisherPort;
import com.minseojo.template.infrastructure.outbox.OutboxEvent;
import com.minseojo.template.infrastructure.outbox.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Outbox 이벤트 발행 어댑터 (Infrastructure Layer)
 * Domain의 EventPublisherPort를 Outbox 패턴으로 구현
 */
@Component
@RequiredArgsConstructor
public class OutboxEventPublisherAdapter implements EventPublisherPort {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void publishFileUploadedEvent(FileRecord fileRecord) {
        try {
            // 이벤트 페이로드 생성 (Downstream 시스템이 필요로 하는 정보)
            String payload = objectMapper.writeValueAsString(Map.of(
                    "fileId", fileRecord.getId(),
                    "storageKey", fileRecord.getStorageKey(),
                    "contentType", fileRecord.getContentType()
            ));

            OutboxEvent event = OutboxEvent.builder()
                    .id(UUID.randomUUID())
                    .aggregateType("FILE")
                    .aggregateId(String.valueOf(fileRecord.getId()))
                    .eventType("FILE_UPLOADED")
                    .payload(payload)
                    .build();

            outboxRepository.save(event);
        } catch (Exception e) {
            throw new RuntimeException("Event serialization failed", e);
        }
    }
}

