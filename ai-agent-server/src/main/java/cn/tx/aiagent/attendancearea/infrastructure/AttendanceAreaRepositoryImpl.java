package cn.tx.aiagent.attendancearea.infrastructure;

import cn.tx.aiagent.attendancearea.domain.AttendanceArea;
import cn.tx.aiagent.attendancearea.domain.AttendanceAreaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AttendanceAreaRepositoryImpl implements AttendanceAreaRepository {
    private final AttendanceAreaMapper mapper;
    private final AttendanceAreaPoMapper poMapper;

    public AttendanceAreaRepositoryImpl(AttendanceAreaMapper mapper, AttendanceAreaPoMapper poMapper) {
        this.mapper = mapper;
        this.poMapper = poMapper;
    }

    @Override
    public List<AttendanceArea> findByProjectId(Long projectId) {
        return mapper.selectByProjectId(projectId).stream()
                .map(poMapper::toDomain).toList();
    }

    @Override
    public Optional<AttendanceArea> findById(Long id) {
        AttendanceAreaPO po = mapper.selectById(id);
        return po == null ? Optional.empty() : Optional.of(poMapper.toDomain(po));
    }

    @Override
    public AttendanceArea save(AttendanceArea area) {
        AttendanceAreaPO po = poMapper.toPO(area);
        mapper.insert(po);
        return poMapper.toDomain(po);
    }

    @Override
    public void update(AttendanceArea area) {
        mapper.update(poMapper.toPO(area));
    }

    @Override
    public void softDelete(Long id) {
        mapper.softDelete(id);
    }
}
