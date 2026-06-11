package cn.tx.aiagent.file.domain;

import java.util.List;
import java.util.Optional;

public interface UploadFileRepository {
    UploadFile save(UploadFile file);
    Optional<UploadFile> findById(Long id);
    List<UploadFile> findByBusiness(String businessType, Long businessId);
    void updateBusinessId(Long id, String businessType, Long businessId);
    void delete(Long id);
}
