package com.minseojo.template.infrastructure.adapter.storage;

import com.minseojo.template.domain.file.port.StoragePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * SeaweedFS 스토리지 어댑터 (Infrastructure Layer)
 * Domain의 StoragePort를 SeaweedFS Filer로 구현
 */
@Slf4j
@Component
public class SeaweedFsStorageAdapter implements StoragePort {
    
    private final String filerUrl;
    private final String basePath;
    
    public SeaweedFsStorageAdapter(
            @Value("${seaweedfs.filer.url:http://localhost:8888}") String filerUrl,
            @Value("${seaweedfs.filer.base-path:/files}") String basePath) {
        this.filerUrl = filerUrl;
        this.basePath = basePath;
    }
    
    @Override
    public String upload(byte[] fileContent, String filename, String contentType) {
        HttpURLConnection connection = null;
        try {
            // SeaweedFS Filer에 파일 업로드
            // 경로: /{basePath}/{uniqueId}/{filename}
            String uniqueId = UUID.randomUUID().toString();
            String filePath = basePath + "/" + uniqueId + "/" + filename;
            String encodedPath = encodePath(filePath);
            String uploadUrl = filerUrl + encodedPath;
            
            log.debug("Uploading file to SeaweedFS Filer: {}", uploadUrl);
            
            URL url = new URL(uploadUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", contentType != null ? contentType : "application/octet-stream");
            connection.setRequestProperty("Content-Length", String.valueOf(fileContent.length));
            
            // 파일 내용 전송
            try (OutputStream os = connection.getOutputStream()) {
                os.write(fileContent);
                os.flush();
            }
            
            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                log.info("File uploaded successfully to SeaweedFS: {}", filePath);
                return filePath; // 파일 경로를 storageKey로 사용
            } else {
                throw new RuntimeException("Failed to upload file to SeaweedFS. Status: " + responseCode);
            }
        } catch (Exception e) {
            log.error("Error uploading file to SeaweedFS", e);
            throw new RuntimeException("File upload failed: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    @Override
    public byte[] download(String storageKey) {
        HttpURLConnection connection = null;
        try {
            log.debug("Downloading file from SeaweedFS Filer: {}", storageKey);
            
            // storageKey가 이미 절대 경로로 시작하는지 확인
            // 이전 버전의 데이터 호환성을 위해 /files로 시작하지 않으면 추가
            String filePath = storageKey;
            if (!storageKey.startsWith("/")) {
                // 상대 경로인 경우 basePath 추가
                filePath = basePath + "/" + storageKey;
            } else if (!storageKey.startsWith(basePath)) {
                // 다른 경로로 시작하는 경우 basePath 추가
                // (이전 데이터 호환성: fid_로 시작하는 경우 등)
                filePath = basePath + storageKey;
            }
            
            String encodedPath = encodePath(filePath);
            String downloadUrl = filerUrl + encodedPath;
            
            log.debug("Download URL: {}", downloadUrl);
            
            URL url = new URL(downloadUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 5초 타임아웃
            connection.setReadTimeout(30000);   // 30초 읽기 타임아웃
            
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                String errorMessage = "File not found in SeaweedFS: " + storageKey + " (Status: " + responseCode + ")";
                log.warn(errorMessage);
                throw new RuntimeException(errorMessage);
            }
            
            // 응답 데이터 읽기
            try (InputStream is = connection.getInputStream();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                
                byte[] fileContent = baos.toByteArray();
                log.info("File downloaded successfully from SeaweedFS: {} ({} bytes)", storageKey, fileContent.length);
                return fileContent;
            }
        } catch (java.net.ConnectException e) {
            log.error("Connection refused to SeaweedFS Filer at {}: {}", filerUrl, e.getMessage());
            throw new RuntimeException("Cannot connect to SeaweedFS Filer. Please check if the Filer is running at " + filerUrl, e);
        } catch (Exception e) {
            log.error("Error downloading file from SeaweedFS: {}", storageKey, e);
            throw new RuntimeException("File download failed: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    @Override
    public void delete(String storageKey) {
        HttpURLConnection connection = null;
        try {
            log.debug("Deleting file from SeaweedFS Filer: {}", storageKey);
            
            // storageKey 경로 처리 (download와 동일한 로직)
            String filePath = storageKey;
            if (!storageKey.startsWith("/")) {
                filePath = basePath + "/" + storageKey;
            } else if (!storageKey.startsWith(basePath)) {
                filePath = basePath + storageKey;
            }
            
            String encodedPath = encodePath(filePath);
            String deleteUrl = filerUrl + encodedPath;
            
            log.debug("Delete URL: {}", deleteUrl);
            
            URL url = new URL(deleteUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            
            int responseCode = connection.getResponseCode();
            
            // 에러 응답인 경우 에러 메시지 읽기
            if (responseCode >= 400 && responseCode != 404) {
                String errorMessage = readErrorResponse(connection);
                log.error("Failed to delete file from SeaweedFS: {} (Status: {}, Message: {})", 
                        storageKey, responseCode, errorMessage);
                throw new RuntimeException("Failed to delete file from SeaweedFS. Status: " + responseCode + ", Message: " + errorMessage);
            }
            
            // 200 (OK), 202 (Accepted), 204 (No Content), 404 (Not Found) 모두 성공으로 처리
            // 404는 이미 삭제된 경우이므로 성공으로 간주
            log.info("File deleted successfully from SeaweedFS: {} (Status: {})", storageKey, responseCode);
        } catch (java.net.ConnectException e) {
            log.error("Connection refused to SeaweedFS Filer at {}: {}", filerUrl, e.getMessage());
            throw new RuntimeException("Cannot connect to SeaweedFS Filer. Please check if the Filer is running at " + filerUrl, e);
        } catch (Exception e) {
            log.error("Error deleting file from SeaweedFS: {}", storageKey, e);
            throw new RuntimeException("File delete failed: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    /**
     * 에러 응답 메시지를 읽는다.
     */
    private String readErrorResponse(HttpURLConnection connection) {
        try (InputStream errorStream = connection.getErrorStream()) {
            if (errorStream != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = errorStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                return baos.toString(StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.debug("Failed to read error response", e);
        }
        return "Unknown error";
    }
    
    /**
     * URL 경로를 인코딩 (파일명에 특수문자가 포함될 수 있음)
     */
    private String encodePath(String path) {
        try {
            // 경로를 세그먼트별로 나누어 인코딩
            String[] segments = path.split("/");
            StringBuilder encoded = new StringBuilder();
            for (String segment : segments) {
                if (!segment.isEmpty()) {
                    encoded.append("/").append(URLEncoder.encode(segment, StandardCharsets.UTF_8));
                }
            }
            return encoded.length() > 0 ? encoded.toString() : path;
        } catch (Exception e) {
            log.warn("Failed to encode path: {}", path, e);
            return path;
        }
    }
}
