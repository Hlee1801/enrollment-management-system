package com.gcash.enrollmentmanagementsystem.repository;

import com.gcash.enrollmentmanagementsystem.entity.Degree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DegreeRepository extends JpaRepository<Degree, Long> {
}
