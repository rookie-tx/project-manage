package cn.tx.aiagent.attendance.infrastructure;

import cn.tx.aiagent.attendance.domain.Attendance;
import cn.tx.aiagent.attendance.domain.AttendanceRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class AttendanceRepositoryImpl implements AttendanceRepository {
    private final AttendanceMapper mapper;
    private final AttendancePoMapper poMapper;

    public AttendanceRepositoryImpl(AttendanceMapper mapper, AttendancePoMapper poMapper) {
        this.mapper = mapper; this.poMapper = poMapper;
    }

    public Optional<Attendance> findTodayByWorker(Long workerId) {
        AttendancePO po = mapper.selectTodayByWorker(workerId); return po == null ? Optional.empty() : Optional.of(poMapper.toDomain(po));
    }
    public Attendance save(Attendance a) { AttendancePO po = poMapper.toPO(a); mapper.insert(po); return poMapper.toDomain(po); }
    public void update(Attendance a) { mapper.update(poMapper.toPO(a)); }
    public List<Attendance> findByWorkerMonth(Long workerId, String yearMonth) { return mapper.selectByWorkerMonth(workerId, yearMonth).stream().map(poMapper::toDomain).toList(); }
    public List<Attendance> findByProjectMonth(Long projectId, String yearMonth) { return mapper.selectByProjectMonth(projectId, yearMonth).stream().map(poMapper::toDomain).toList(); }
}
