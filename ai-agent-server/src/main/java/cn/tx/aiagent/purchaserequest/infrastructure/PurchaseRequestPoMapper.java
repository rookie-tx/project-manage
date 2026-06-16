package cn.tx.aiagent.purchaserequest.infrastructure;

import cn.tx.aiagent.purchaserequest.domain.PurchaseRequest;
import cn.tx.aiagent.purchaserequest.domain.PurchaseRequestStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface PurchaseRequestPoMapper {

    default PurchaseRequest toDomain(PurchaseRequestPO po) {
        if (po == null) return null;
        return PurchaseRequest.fromPersistence(
                po.getId(), po.getCode(), po.getProjectId(), po.getMaterialId(),
                po.getName(), po.getSpecification(), po.getBrand(),
                po.getQuantity() != null ? BigDecimal.valueOf(po.getQuantity()) : null,
                po.getUnit(),
                po.getArrivalDate(),
                po.getStatus() != null ? PurchaseRequestStatus.fromCode(po.getStatus()) : null,
                po.getRemark(), po.getIsDeleted(), po.getCreatedBy(),
                po.getCreatedAt(), po.getUpdatedAt()
        );
    }

    @Mapping(target = "quantity", expression = "java(pr.getQuantity() != null ? pr.getQuantity().doubleValue() : null)")
    @Mapping(target = "unit", expression = "java(pr.getUnit())")
    @Mapping(target = "status", expression = "java(pr.getStatus() != null ? pr.getStatus().getCode() : null)")
    PurchaseRequestPO toPO(PurchaseRequest pr);
}
