package cn.tx.aiagent.quotation.application;

import cn.tx.aiagent.quotation.domain.Quotation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 报价 DTO ↔ Domain MapStruct 映射
 */
@Mapper(componentModel = "spring")
public interface QuotationDtoMapper {

    @Mapping(target = "statusCode", expression = "java(q.getStatus() != null ? q.getStatus().getCode() : 0)")
    @Mapping(target = "statusLabel", expression = "java(q.getStatus() != null ? q.getStatus().getLabel() : null)")
    @Mapping(target = "supplierName", ignore = true)
    @Mapping(target = "supplierCompanyName", ignore = true)
    @Mapping(target = "supplierContactPerson", ignore = true)
    @Mapping(target = "supplierContactPhone", ignore = true)
    @Mapping(target = "purchaseOrderInfo", ignore = true)
    @Mapping(target = "createdByName", ignore = true)
    QuotationResponse toResponse(Quotation q);
}
