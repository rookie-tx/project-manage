package cn.tx.aiagent.file.api;

import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.api.Result;
import cn.tx.aiagent.common.exception.BusinessException;
import cn.tx.aiagent.file.FileStorageProperties;
import cn.tx.aiagent.file.domain.UploadFile;
import cn.tx.aiagent.file.domain.UploadFileRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    );

    private final UploadFileRepository uploadFileRepository;
    private final FileStorageProperties fileStorageProperties;

    public FileController(UploadFileRepository uploadFileRepository,
                          FileStorageProperties fileStorageProperties) {
        this.uploadFileRepository = uploadFileRepository;
        this.fileStorageProperties = fileStorageProperties;
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(getUploadRoot());
    }

    @PostMapping("/upload")
    public Result<UploadFile> upload(@RequestParam("file") MultipartFile file,
                                     @RequestParam(value = "businessType", required = false) String businessType) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "文件为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }

        try {
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String storedName = UUID.randomUUID() + ext;

            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            Path dir = getUploadRoot().resolve(businessType != null ? businessType : "common").resolve(datePath);
            Files.createDirectories(dir);

            Path target = dir.resolve(storedName);
            file.transferTo(target);

            String relativePath = (businessType != null ? businessType : "common") + "/" + datePath + "/" + storedName;

            UploadFile uploadFile = UploadFile.create(originalName, relativePath, file.getSize(), contentType);
            UploadFile saved = uploadFileRepository.save(uploadFile);

            if (businessType != null) {
                uploadFileRepository.updateBusinessId(saved.getId(), businessType, null);
                saved = uploadFileRepository.findById(saved.getId()).orElse(saved);
            }

            return Result.success(saved);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        UploadFile file = uploadFileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));

        Path filePath = getUploadRoot().resolve(file.getStoredPath());
        Resource resource = new FileSystemResource(filePath);
        if (!resource.exists()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        MediaType mediaType = MediaType.parseMediaType(
                file.getContentType() != null ? file.getContentType() : "application/octet-stream");

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getOriginalName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        UploadFile file = uploadFileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));

        try {
            Path filePath = getUploadRoot().resolve(file.getStoredPath());
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
            // 文件已不存在，继续删除数据库记录
        }

        uploadFileRepository.delete(id);
        return Result.success();
    }

    private Path getUploadRoot() {
        return Paths.get(fileStorageProperties.getDir()).toAbsolutePath();
    }
}
