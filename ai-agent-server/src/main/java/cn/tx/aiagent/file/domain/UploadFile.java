package cn.tx.aiagent.file.domain;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class UploadFile {
    private Long id;
    private String businessType;
    private Long businessId;
    private String originalName;
    private String storedPath;
    private long fileSize;
    private String contentType;
    private LocalDateTime createdAt;

    private UploadFile() {}

    public static UploadFile create(String originalName, String storedPath, long fileSize, String contentType) {
        UploadFile f = new UploadFile();
        f.originalName = originalName;
        f.storedPath = storedPath;
        f.fileSize = fileSize;
        f.contentType = contentType;
        return f;
    }

    public static UploadFile fromPersistence(Long id, String businessType, Long businessId,
                                              String originalName, String storedPath,
                                              long fileSize, String contentType,
                                              LocalDateTime createdAt) {
        UploadFile f = new UploadFile();
        f.id = id;
        f.businessType = businessType;
        f.businessId = businessId;
        f.originalName = originalName;
        f.storedPath = storedPath;
        f.fileSize = fileSize;
        f.contentType = contentType;
        f.createdAt = createdAt;
        return f;
    }

    public void bindBusiness(String businessType, Long businessId) {
        this.businessType = businessType;
        this.businessId = businessId;
    }
}
