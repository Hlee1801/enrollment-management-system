package com.gcash.enrollmentmanagementsystem.repository;

import com.gcash.enrollmentmanagementsystem.entity.Enrollment;
import com.gcash.enrollmentmanagementsystem.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudentId(Long studentId);

    List<Enrollment> findBySectionId(Long sectionId);

    boolean existsByStudentIdAndSectionId(Long studentId, Long sectionId);

    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.section.term.id = :termId")
    List<Enrollment> findByStudentIdAndTermId(@Param("studentId") Long studentId, @Param("termId") Long termId);

    @Query("SELECT e FROM Enrollment e WHERE e.section.id = :sectionId AND e.status = :status")
    List<Enrollment> findBySectionIdAndStatus(@Param("sectionId") Long sectionId, @Param("status") EnrollmentStatus status);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.section.id = :sectionId AND e.status IN ('PENDING', 'ENROLLED')")
    Long countActiveEnrollmentsBySectionId(@Param("sectionId") Long sectionId);

    @Query("SELECT e FROM Enrollment e " +
           "JOIN e.section s " +
           "JOIN s.schedule sch " +
           "WHERE e.student.id = :studentId " +
           "AND s.term.id = :termId " +
           "AND e.status IN ('PENDING', 'ENROLLED')")
    List<Enrollment> findActiveEnrollmentsForScheduleConflictCheck(@Param("studentId") Long studentId,
                                                                    @Param("termId") Long termId);

    @Query("SELECT e FROM Enrollment e " +
           "JOIN e.section s " +
           "WHERE e.student.id = :studentId " +
           "AND s.course.degree.id = :degreeId " +
           "AND e.status IN ('ENROLLED', 'COMPLETED')")
    List<Enrollment> findByStudentIdAndDegreeId(@Param("studentId") Long studentId,
                                                 @Param("degreeId") Long degreeId);
}
