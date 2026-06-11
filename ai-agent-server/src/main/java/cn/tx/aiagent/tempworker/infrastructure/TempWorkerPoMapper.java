package cn.tx.aiagent.tempworker.infrastructure;

import cn.tx.aiagent.tempworker.domain.TempWorker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TempWorkerPoMapper {
    default TempWorker toDomain(TempWorkerPO po) {
        if (po == null) return null;
        return TempWorker.fromPersistence(po.getId(), po.getUserId(), po.getUsername(), po.getName(), po.getPhone(), po.getProjectId(), po.getWorkerType(),
                po.getEntryDate(), po.getExitDate(), po.getStatus(), po.getIsDeleted(), po.getCreatedAt(), po.getUpdatedAt());
    }
    @Mapping(target = "status", expression = "java(w.getStatus() != null ? w.getStatus().getCode() : null)")
    @Mapping(target = "isDeleted", expression = "java(w.getIsDeleted())")
    TempWorkerPO toPO(TempWorker w);
}
