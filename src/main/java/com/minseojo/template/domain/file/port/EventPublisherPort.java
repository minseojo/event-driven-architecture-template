package com.minseojo.template.domain.file.port;

import com.minseojo.template.domain.file.FileRecord;

/**
 * 이벤트 발행 포트 (헥사고날 아키텍처)
 * Domain에서 외부 이벤트 시스템에 이벤트를 발행하기 위한 인터페이스
 */
public interface EventPublisherPort {
    /**
     * 파일 업로드 이벤트를 발행한다.
     * @param fileRecord 업로드된 파일 레코드
     */
    void publishFileUploadedEvent(FileRecord fileRecord);
}

