package com.minseojo.template.domain.file.port;

/**
 * 파일 스토리지 포트 (헥사고날 아키텍처)
 * Domain에서 외부 스토리지 시스템과 통신하기 위한 인터페이스
 */
public interface StoragePort {
    /**
     * 파일을 업로드하고 접근 가능한 Key(또는 URL)를 반환한다.
     * @param fileContent 파일 내용 (바이트 배열)
     * @param filename 원본 파일명
     * @param contentType MIME 타입
     * @return 스토리지 Key 또는 URL
     */
    String upload(byte[] fileContent, String filename, String contentType);
    
    /**
     * 스토리지에서 파일을 다운로드한다.
     * @param storageKey 스토리지에 저장된 파일의 Key
     * @return 파일 내용 (바이트 배열)
     */
    byte[] download(String storageKey);
    
    /**
     * 스토리지에서 파일을 삭제한다.
     * @param storageKey 스토리지에 저장된 파일의 Key
     * @throws RuntimeException 파일을 찾을 수 없거나 삭제에 실패할 경우
     */
    void delete(String storageKey);
}

