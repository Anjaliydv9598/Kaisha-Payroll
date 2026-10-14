package com.kaisha.payroll.config;

import com.kaisha.payroll.attendance.service.AttendanceService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class AttendanceSchedulerConfig {

    private final AttendanceService attendanceService;

    public AttendanceSchedulerConfig(
            AttendanceService attendanceService
    ) {
        this.attendanceService = attendanceService;
    }

    /*
     * Runs every day at 11:00 PM India time.
     */
    @Scheduled(
            cron = "0 0 23 * * *",
            zone = "Asia/Kolkata"
    )
    public void processMissingPunches() {

        int updated =
                attendanceService
                        .markMissingPunchesAbsent();

        System.out.println(
                "Attendance scheduler completed. " +
                        "Records marked absent: " +
                        updated
        );
    }
}