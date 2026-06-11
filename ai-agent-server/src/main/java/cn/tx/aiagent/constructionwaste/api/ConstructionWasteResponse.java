package cn.tx.aiagent.constructionwaste.api;

import cn.tx.aiagent.file.domain.UploadFile;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ConstructionWasteResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private String wasteType;
    private double quantity;
    private String unit;
    private String vehiclePlate;
    private String driverName;
    private String destination;
    private LocalDate removalDate;
    private String remark;
    private Long operatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<FileInfo> files;

    @Data
    public static class FileInfo {
        private Long id;
        private String originalName;
        private String contentType;
        private String url;

        public static FileInfo from(UploadFile f) {
            FileInfo info = new FileInfo();
            info.id = f.getId();
            info.originalName = f.getOriginalName();
            info.contentType = f.getContentType();
            info.url = "/api/files/" + f.getId();
            return info;
        }
    }
}
