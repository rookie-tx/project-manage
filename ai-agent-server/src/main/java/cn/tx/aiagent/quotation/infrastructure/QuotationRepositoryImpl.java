package cn.tx.aiagent.quotation.infrastructure;

import cn.tx.aiagent.quotation.domain.Quotation;
import cn.tx.aiagent.quotation.domain.QuotationRepository;
import cn.tx.aiagent.quotation.domain.QuotationStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 报价仓储实现
 */
@Component
public class QuotationRepositoryImpl implements QuotationRepository {

    private final QuotationMapper mapper;
    private final QuotationPoMapper poMapper;

    public QuotationRepositoryImpl(QuotationMapper mapper, QuotationPoMapper poMapper) {
        this.mapper = mapper;
        this.poMapper = poMapper;
    }

    @Override
    public void save(Quotation quotation) {
        QuotationPO po = poMapper.toPO(quotation);
        mapper.insert(po);
        quotation.setId(po.getId());
    }

    @Override
    public Optional<Quotation> findById(Long id) {
        QuotationPO po = mapper.selectById(id);
        return po == null ? Optional.empty() : Optional.of(poMapper.toDomain(po));
    }

    @Override
    public Optional<Quotation> findByOrderAndSupplier(Long purchaseOrderId, Long supplierId) {
        QuotationPO po = mapper.selectByOrderAndSupplier(purchaseOrderId, supplierId);
        return po == null ? Optional.empty() : Optional.of(poMapper.toDomain(po));
    }

    @Override
    public List<Quotation> findByPage(Long purchaseOrderId, int offset, int size) {
        return mapper.selectByPage(purchaseOrderId, offset, size).stream()
                .map(poMapper::toDomain).toList();
    }

    @Override
    public long countByPurchaseOrder(Long purchaseOrderId) {
        return mapper.countByPurchaseOrder(purchaseOrderId);
    }

    @Override
    public List<Quotation> findByCreatedBy(Long createdBy, int offset, int size) {
        return mapper.selectByCreatedBy(createdBy, offset, size).stream()
                .map(poMapper::toDomain).toList();
    }

    @Override
    public long countByCreatedBy(Long createdBy) {
        return mapper.countByCreatedBy(createdBy);
    }

    @Override
    public void update(Quotation quotation) {
        mapper.update(poMapper.toPO(quotation));
    }

    @Override
    public void updateStatus(Long id, QuotationStatus status) {
        mapper.updateStatus(id, status.getCode());
    }

    @Override
    public void softDelete(Long id) {
        mapper.softDelete(id);
    }
}
