package cn.tx.aiagent.quotation.infrastructure;

import cn.tx.aiagent.quotation.domain.Quotation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 报价 PO ↔ Domain MapStruct 映射
 */
@Mapper(componentModel = "spring")
public interface QuotationPoMapper {

    default Quotation toDomain(QuotationPO po) {
        if (po == null) return null;
        return Quotation.fromPersistence(
                po.getId(), po.getPurchaseOrderId(), po.getSupplierId(),
                po.getUnitPrice(), po.getTotalAmount(),
                po.getDeliveryDate(), po.getRemark(),
                po.getStatus(), po.getIsDeleted(),
                po.getCreatedBy(), po.getCreatedAt(), po.getUpdatedAt());
    }

    @Mapping(target = "status", expression = "java(q.getStatus() != null ? q.getStatus().getCode() : 0)")
    QuotationPO toPO(Quotation q);
}
