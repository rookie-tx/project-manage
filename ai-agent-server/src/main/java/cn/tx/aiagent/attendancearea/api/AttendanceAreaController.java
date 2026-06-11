package cn.tx.aiagent.attendancearea.api;

import cn.tx.aiagent.attendancearea.application.AttendanceAreaApplicationService;
import cn.tx.aiagent.attendancearea.domain.AttendanceArea;
import cn.tx.aiagent.common.api.Result;
import cn.tx.aiagent.common.security.RequirePermission;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AttendanceAreaController {

    private final AttendanceAreaApplicationService applicationService;

    public AttendanceAreaController(AttendanceAreaApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /** 查询项目打卡区域列表 */
    @GetMapping("/projects/{projectId}/attendance-areas")
    public Result<List<AttendanceArea>> listByProject(@PathVariable Long projectId) {
        return Result.success(applicationService.listByProject(projectId));
    }

    /** 新增打卡区域 */
    @RequirePermission("attendance:area-manage")
    @PostMapping("/projects/{projectId}/attendance-areas")
    public Result<AttendanceArea> create(@PathVariable Long projectId,
                                          @Valid @RequestBody CreateAreaRequest request) {
        return Result.success(applicationService.create(
                projectId, request.getName(), request.getCenterLat(),
                request.getCenterLng(), request.getRadius()));
    }

    /** 更新打卡区域 */
    @RequirePermission("attendance:area-manage")
    @PutMapping("/attendance-areas/{id}")
    public Result<Void> update(@PathVariable Long id,
                                @Valid @RequestBody UpdateAreaRequest request) {
        applicationService.update(id, request.getName(), request.getCenterLat(),
                request.getCenterLng(), request.getRadius());
        return Result.success();
    }

    /** 删除打卡区域 */
    @RequirePermission("attendance:area-manage")
    @DeleteMapping("/attendance-areas/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        applicationService.delete(id);
        return Result.success();
    }

    @Data
    public static class CreateAreaRequest {
        @NotBlank
        private String name;
        @NotNull
        @DecimalMin("-90.0") @DecimalMax("90.0")
        private BigDecimal centerLat;
        @NotNull
        @DecimalMin("-180.0") @DecimalMax("180.0")
        private BigDecimal centerLng;
        @NotNull
        @Min(50) @Max(5000)
        private Integer radius;
    }

    @Data
    public static class UpdateAreaRequest {
        @NotBlank
        private String name;
        @NotNull
        @DecimalMin("-90.0") @DecimalMax("90.0")
        private BigDecimal centerLat;
        @NotNull
        @DecimalMin("-180.0") @DecimalMax("180.0")
        private BigDecimal centerLng;
        @NotNull
        @Min(50) @Max(5000)
        private Integer radius;
    }
}
