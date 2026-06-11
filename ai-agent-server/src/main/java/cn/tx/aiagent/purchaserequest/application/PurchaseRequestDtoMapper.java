package cn.tx.aiagent.purchaserequest.application;

import cn.tx.aiagent.purchaserequest.api.CreatePurchaseRequestRequest;
import cn.tx.aiagent.purchaserequest.api.PurchaseRequestResponse;
import cn.tx.aiagent.purchaserequest.api.UpdatePurchaseRequestRequest;
import cn.tx.aiagent.purchaserequest.domain.PurchaseRequest;
import cn.tx.aiagent.purchaserequest.domain.Unit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PurchaseRequestDtoMapper {

    @Mapping(target = "unitCode", expression = "java(pr.getUnit() != null ? pr.getUnit().getCode() : null)")
    @Mapping(target = "unitLabel", expression = "java(pr.getUnit() != null ? pr.getUnit().getLabel() : null)")
    @Mapping(target = "statusCode", expression = "java(pr.getStatus() != null ? pr.getStatus().getCode() : null)")
    @Mapping(target = "statusLabel", expression = "java(pr.getStatus() != null ? pr.getStatus().getLabel() : null)")
    @Mapping(target = "projectCode", ignore = true)
    @Mapping(target = "projectName", ignore = true)
    @Mapping(target = "createdByName", ignore = true)
    @Mapping(target = "currentApprovalNode", ignore = true)
    PurchaseRequestResponse toResponse(PurchaseRequest pr);

    default PurchaseRequest toDomain(CreatePurchaseRequestRequest request) {
        if (request == null) return null;
        Unit unit = request.getUnit() != null ? Unit.fromCode(request.getUnit()) : null;
        return PurchaseRequest.create(
                request.getProjectId(), request.getName(), request.getSpecification(),
                request.getBrand(), request.getQuantity(), unit,
                request.getArrivalDate(), request.getRemark(), null);
    }

    default PurchaseRequest toDomain(UpdatePurchaseRequestRequest request) {
        if (request == null) return null;
        Unit unit = request.getUnit() != null ? Unit.fromCode(request.getUnit()) : null;
        return PurchaseRequest.create(
                request.getProjectId(), request.getName(), request.getSpecification(),
                request.getBrand(), request.getQuantity(), unit,
                request.getArrivalDate(), request.getRemark(), null);
    }
}
