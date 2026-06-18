package cn.tx.aiagent.purchasemanagement.infrastructure;

import cn.tx.aiagent.purchasemanagement.domain.PurchaseOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PurchaseOrderPoMapper {

    default PurchaseOrder toDomain(PurchaseOrderPO po) {
        if (po == null) return null;
        return PurchaseOrder.fromPersistence(
                po.getId(), po.getPurchaseRequestId(), po.getSupplierName(),
                po.getSupplierPhone(), po.getUnitPrice(), po.getTotalAmount(),
                po.getPurchaseDate(), po.getExpectedArrivalDate(),
                po.getStatus(), po.getRemark(), po.getIsDeleted(),
                po.getCreatedBy(), po.getPurchaserId(), po.getInboundOperatorId(),
                po.getCreatedAt(), po.getUpdatedAt());
    }

    @Mapping(target = "status", expression = "java(po.getStatus() != null ? po.getStatus().getCode() : 0)")
    PurchaseOrderPO toPO(PurchaseOrder po);
}
