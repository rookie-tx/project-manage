package cn.tx.aiagent.file.infrastructure;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UploadFilePO {
    private Long id;
    private String businessType;
    private Long businessId;
    private String originalName;
    private String storedPath;
    private long fileSize;
    private String contentType;
    private LocalDateTime createdAt;
}
