package cn.tx.aiagent.supplier.application;

import cn.tx.aiagent.supplier.api.SupplierResponse;
import cn.tx.aiagent.supplier.domain.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SupplierDtoMapper {

    @Mapping(target = "status", expression = "java(s.getStatus() != null ? s.getStatus().getCode() : null)")
    SupplierResponse toResponse(Supplier s);
}
