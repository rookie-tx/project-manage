package cn.tx.aiagent.attendancearea.application;

import cn.tx.aiagent.attendancearea.domain.AttendanceArea;
import cn.tx.aiagent.attendancearea.domain.AttendanceAreaRepository;
import cn.tx.aiagent.common.api.ErrorCode;
import cn.tx.aiagent.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** 打卡区域应用服务 */
@Service
public class AttendanceAreaApplicationService {

    private final AttendanceAreaRepository areaRepository;

    public AttendanceAreaApplicationService(AttendanceAreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }

    /** 查询项目下所有打卡区域 */
    public List<AttendanceArea> listByProject(Long projectId) {
        return areaRepository.findByProjectId(projectId);
    }

    /** 创建打卡区域 */
    @Transactional(rollbackFor = Exception.class)
    public AttendanceArea create(Long projectId, String name,
                                  BigDecimal centerLat, BigDecimal centerLng, Integer radius) {
        validateRadius(radius);
        AttendanceArea area = AttendanceArea.create(projectId, name, centerLat, centerLng, radius);
        return areaRepository.save(area);
    }

    /** 更新打卡区域 */
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, String name, BigDecimal centerLat, BigDecimal centerLng, Integer radius) {
        AttendanceArea area = areaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTENDANCE_AREA_NOT_FOUND, id));
        validateRadius(radius);
        area.update(name, centerLat, centerLng, radius);
        areaRepository.update(area);
    }

    /** 删除打卡区域（软删除） */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (areaRepository.findById(id).isEmpty()) {
            throw new BusinessException(ErrorCode.ATTENDANCE_AREA_NOT_FOUND, id);
        }
        areaRepository.softDelete(id);
    }

    private void validateRadius(Integer radius) {
        if (radius == null || radius < 50 || radius > 5000) {
            throw new BusinessException(ErrorCode.PARAM_INVALID);
        }
    }
}
