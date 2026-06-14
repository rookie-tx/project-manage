package cn.tx.aiagent.supplier.application;

import cn.tx.aiagent.supplier.api.SupplierResponse;
import cn.tx.aiagent.supplier.domain.Supplier;
import cn.tx.aiagent.supplier.domain.SupplierDomainService;
import cn.tx.aiagent.supplier.domain.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupplierApplicationService {

    private final SupplierRepository supplierRepository;
    private final SupplierDomainService domainService;
    private final SupplierDtoMapper dtoMapper;

    public SupplierApplicationService(SupplierRepository supplierRepository,
                                       SupplierDomainService domainService,
                                       SupplierDtoMapper dtoMapper) {
        this.supplierRepository = supplierRepository;
        this.domainService = domainService;
        this.dtoMapper = dtoMapper;
    }

    public List<SupplierResponse> list(String keyword, Integer status, int page, int size) {
        int offset = (page - 1) * size;
        return supplierRepository.findAll(keyword, status, offset, size).stream()
                .map(dtoMapper::toResponse).toList();
    }

    public long count(String keyword, Integer status) {
        return supplierRepository.count(keyword, status);
    }

    public SupplierResponse getById(Long id) {
        Supplier s = supplierRepository.findById(id)
                .orElseThrow(() -> new cn.tx.aiagent.common.exception.BusinessException(
                        cn.tx.aiagent.common.api.ErrorCode.SUPPLIER_NOT_FOUND, id));
        return dtoMapper.toResponse(s);
    }

    @Transactional(rollbackFor = Exception.class)
    public SupplierResponse create(String username, String rawPassword,
                                    String companyName, String licenseNo,
                                    String contactPerson, String contactPhone,
                                    String address) {
        Supplier supplier = domainService.createByAdmin(username, rawPassword,
                companyName, licenseNo, contactPerson, contactPhone, address);
        return dtoMapper.toResponse(supplier);
    }

    public void update(Long id, String companyName, String licenseNo,
                        String contactPerson, String contactPhone,
                        String address, String remark, Integer statusCode) {
        domainService.updateSupplier(id, companyName, licenseNo,
                contactPerson, contactPhone, address, remark, statusCode);
    }

    public void softDelete(Long id) {
        domainService.softDelete(id);
    }

    public SupplierResponse getMyProfile(Long userId) {
        Supplier s = supplierRepository.findByUserId(userId)
                .orElseThrow(() -> new cn.tx.aiagent.common.exception.BusinessException(
                        cn.tx.aiagent.common.api.ErrorCode.SUPPLIER_NOT_FOUND, userId));
        return dtoMapper.toResponse(s);
    }

    public void updateMyProfile(Long userId, String contactPerson, String contactPhone, String address) {
        domainService.updateProfile(userId, contactPerson, contactPhone, address);
    }
}
