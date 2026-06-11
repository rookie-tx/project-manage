package cn.tx.aiagent.constructionwaste.infrastructure;

import cn.tx.aiagent.constructionwaste.domain.ConstructionWaste;
import cn.tx.aiagent.constructionwaste.domain.ConstructionWasteRepository;
import cn.tx.aiagent.file.domain.UploadFileRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class ConstructionWasteRepositoryImpl implements ConstructionWasteRepository {
    private final ConstructionWasteMapper mapper;
    private final ConstructionWastePoMapper poMapper;
    private final UploadFileRepository uploadFileRepository;

    public ConstructionWasteRepositoryImpl(ConstructionWasteMapper mapper,
                                            ConstructionWastePoMapper poMapper,
                                            UploadFileRepository uploadFileRepository) {
        this.mapper = mapper;
        this.poMapper = poMapper;
        this.uploadFileRepository = uploadFileRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConstructionWaste save(ConstructionWaste waste) {
        ConstructionWastePO po = poMapper.toPO(waste);
        mapper.insert(po);
        return poMapper.toDomain(mapper.selectById(po.getId()));
    }

    @Override
    public Optional<ConstructionWaste> findById(Long id) {
        ConstructionWastePO po = mapper.selectById(id);
        if (po == null || po.getIsDeleted() == 1) return Optional.empty();
        return Optional.of(poMapper.toDomain(po));
    }

    @Override
    public List<ConstructionWaste> search(Long projectId, String wasteType,
                                           LocalDate startDate, LocalDate endDate,
                                           int offset, int limit) {
        return mapper.search(projectId, wasteType, startDate, endDate, offset, limit)
                .stream().map(poMapper::toDomain).toList();
    }

    @Override
    public long count(Long projectId, String wasteType,
                      LocalDate startDate, LocalDate endDate) {
        return mapper.count(projectId, wasteType, startDate, endDate);
    }

    @Override
    public void update(ConstructionWaste waste) {
        // 当前设计不需要更新，创建后不可变
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void softDelete(Long id) {
        mapper.softDelete(id);
        // 同时删除关联文件
        var files = uploadFileRepository.findByBusiness("construction_waste", id);
        for (var file : files) {
            uploadFileRepository.delete(file.getId());
        }
    }
}
