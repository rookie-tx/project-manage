package cn.tx.aiagent.attendance.infrastructure;

import cn.tx.aiagent.attendance.application.AttendanceApplicationService;
import cn.tx.aiagent.attendance.domain.AttendanceRepository;
import cn.tx.aiagent.attendancearea.domain.AttendanceAreaRepository;
import cn.tx.aiagent.tempworker.domain.TempWorkerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AttendanceDomainConfig {

    @Bean
    public AttendanceApplicationService attendanceApplicationService(
            AttendanceRepository attendanceRepository,
            TempWorkerRepository tempWorkerRepository,
            AttendanceAreaRepository attendanceAreaRepository) {
        return new AttendanceApplicationService(attendanceRepository, tempWorkerRepository,
                attendanceAreaRepository);
    }
}
