package com.minseojo.template.infrastructure.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {

    private final JdbcTemplate jdbcTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // 0.5초 주기 폴링
    @Scheduled(fixedDelay = 500)
    public void processOutbox() {
        // 1. 처리되지 않은 이벤트 조회 (다른 인스턴스가 잡은 row는 건너뜀)
        String fetchSql = """
            SELECT id, event_type, payload 
            FROM outbox_events 
            WHERE processed = FALSE 
            ORDER BY created_at ASC 
            LIMIT 50 
            FOR UPDATE SKIP LOCKED
        """;

        jdbcTemplate.query(fetchSql, (rs, rowNum) -> {
            UUID id = UUID.fromString(rs.getString("id"));
            String eventType = rs.getString("event_type");
            // JSONB는 PostgreSQL에서 String으로 자동 변환됨
            String payload = rs.getString("payload");
            
            publishEvent(id, eventType, payload);
            return id;
        });
    }

    private void publishEvent(UUID id, String eventType, String payload) {
        try {
            // 2. 실제 메시지 브로커 전송
            // 토픽명은 "domain.aggregate" 패턴 추천 (예: file-service.file)
            kafkaTemplate.send("file-service.events", eventType, payload)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        markAsProcessed(id);
                    } else {
                        log.error("Failed to publish event [id={}]", id, ex);
                    }
                });
        } catch (Exception e) {
            log.error("Error in event publishing loop", e);
        }
    }

    private void markAsProcessed(UUID id) {
        // 3. 처리 완료 마킹 (Hard Delete를 원하면 DELETE 쿼리로 변경)
        jdbcTemplate.update("UPDATE outbox_events SET processed = TRUE WHERE id = ?", id);
    }
}

