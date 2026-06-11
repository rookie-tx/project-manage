package cn.tx.aiagent.file.infrastructure;

import cn.tx.aiagent.file.domain.UploadFile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UploadFilePoMapper {

    default UploadFile toDomain(UploadFilePO po) {
        if (po == null) return null;
        return UploadFile.fromPersistence(
                po.getId(), po.getBusinessType(), po.getBusinessId(),
                po.getOriginalName(), po.getStoredPath(),
                po.getFileSize(), po.getContentType(),
                po.getCreatedAt()
        );
    }

    UploadFilePO toPO(UploadFile file);
}
