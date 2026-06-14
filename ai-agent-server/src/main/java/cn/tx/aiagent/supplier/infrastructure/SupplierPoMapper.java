package cn.tx.aiagent.supplier.infrastructure;

import cn.tx.aiagent.supplier.domain.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SupplierPoMapper {

    default Supplier toDomain(SupplierPO po) {
        if (po == null) return null;
        return Supplier.fromPersistence(
                po.getId(), po.getUserId(), po.getCompanyName(),
                po.getLicenseNo(), po.getContactPerson(), po.getContactPhone(),
                po.getAddress(), po.getStatus(), po.getRemark(),
                po.getIsDeleted(), po.getCreatedAt(), po.getUpdatedAt());
    }

    @Mapping(target = "status", expression = "java(s.getStatus() != null ? s.getStatus().getCode() : null)")
    @Mapping(target = "isDeleted", expression = "java(s.getIsDeleted())")
    SupplierPO toPO(Supplier s);
}
