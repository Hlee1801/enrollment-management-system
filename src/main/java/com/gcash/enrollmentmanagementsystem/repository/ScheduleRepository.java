package com.gcash.enrollmentmanagementsystem.repository;

import com.gcash.enrollmentmanagementsystem.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
}
