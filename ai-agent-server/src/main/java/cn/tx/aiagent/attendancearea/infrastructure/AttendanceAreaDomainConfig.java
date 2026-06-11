package cn.tx.aiagent.attendancearea.infrastructure;

import cn.tx.aiagent.attendancearea.application.AttendanceAreaApplicationService;
import cn.tx.aiagent.attendancearea.domain.AttendanceAreaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AttendanceAreaDomainConfig {

    @Bean
    public AttendanceAreaApplicationService attendanceAreaApplicationService(
            AttendanceAreaRepository areaRepository) {
        return new AttendanceAreaApplicationService(areaRepository);
    }
}
