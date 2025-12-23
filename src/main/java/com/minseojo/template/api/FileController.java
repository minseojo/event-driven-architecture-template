package com.minseojo.template.api;

import com.minseojo.template.application.file.FileDeleteUseCase;
import com.minseojo.template.application.file.FileDownloadUseCase;
import com.minseojo.template.application.file.FileListUseCase;
import com.minseojo.template.application.file.FileUploadUseCase;
import com.minseojo.template.domain.file.FileRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 파일 API 컨트롤러 (API Layer / Infrastructure Layer의 Inbound Adapter)
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileUploadUseCase fileUploadUseCase;
    private final FileListUseCase fileListUseCase;
    private final FileDownloadUseCase fileDownloadUseCase;
    private final FileDeleteUseCase fileDeleteUseCase;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Long fileId = fileUploadUseCase.execute(file);
            return ResponseEntity.ok(Map.of(
                    "fileId", fileId,
                    "message", "File uploaded successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Upload failed",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping
    public ResponseEntity<?> listFiles() {
        try {
            List<FileRecord> files = fileListUseCase.execute();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "List files failed",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        try {
            FileDownloadUseCase.FileDownloadResult result = fileDownloadUseCase.execute(id);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(result.getContentType()));
            
            // 한글 파일명 인코딩 처리 (RFC 5987)
            // filename에는 ASCII-safe한 fallback 사용, filename*에 UTF-8 인코딩 사용
            String asciiFilename = toAsciiSafeFilename(result.getFilename());
            String encodedFilename = encodeFilename(result.getFilename());
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + asciiFilename + "\"; filename*=UTF-8''" + encodedFilename);
            headers.setContentLength(result.getContent().length);
            
            return new ResponseEntity<>(result.getContent(), headers, HttpStatus.OK);
        } catch (FileDownloadUseCase.FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 파일명을 ASCII-safe한 문자열로 변환 (fallback용)
     */
    private String toAsciiSafeFilename(String filename) {
        if (filename == null) {
            return "download";
        }
        // 한글 등의 비ASCII 문자를 '_'로 치환
        return filename.replaceAll("[^\\x00-\\x7F]", "_");
    }
    
    /**
     * RFC 5987에 따라 파일명을 UTF-8로 인코딩
     */
    private String encodeFilename(String filename) {
        try {
            return java.net.URLEncoder.encode(filename, "UTF-8").replace("+", "%20");
        } catch (java.io.UnsupportedEncodingException e) {
            return filename;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable Long id) {
        try {
            fileDeleteUseCase.execute(id);
            return ResponseEntity.ok(Map.of(
                    "message", "File deleted successfully"
            ));
        } catch (FileDeleteUseCase.FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Delete failed",
                    "message", e.getMessage()
            ));
        }
    }
}

