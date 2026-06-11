package cn.tx.aiagent.file.infrastructure;

import cn.tx.aiagent.file.domain.UploadFile;
import cn.tx.aiagent.file.domain.UploadFileRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UploadFileRepositoryImpl implements UploadFileRepository {
    private final UploadFileMapper mapper;
    private final UploadFilePoMapper poMapper;

    public UploadFileRepositoryImpl(UploadFileMapper mapper, UploadFilePoMapper poMapper) {
        this.mapper = mapper;
        this.poMapper = poMapper;
    }

    @Override
    public UploadFile save(UploadFile file) {
        UploadFilePO po = poMapper.toPO(file);
        mapper.insert(po);
        return poMapper.toDomain(mapper.selectById(po.getId()));
    }

    @Override
    public Optional<UploadFile> findById(Long id) {
        UploadFilePO po = mapper.selectById(id);
        return po == null ? Optional.empty() : Optional.of(poMapper.toDomain(po));
    }

    @Override
    public List<UploadFile> findByBusiness(String businessType, Long businessId) {
        return mapper.selectByBusiness(businessType, businessId).stream()
                .map(poMapper::toDomain).toList();
    }

    @Override
    public void updateBusinessId(Long id, String businessType, Long businessId) {
        mapper.updateBusinessId(id, businessType, businessId);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
