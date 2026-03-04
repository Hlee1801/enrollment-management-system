package com.gcash.enrollmentmanagementsystem.repository;

import com.gcash.enrollmentmanagementsystem.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    List<Section> findByCourseId(Long courseId);

    @Query("SELECT s FROM Section s WHERE s.term.id = :termId")
    List<Section> findAllSectionsByTerm(@Param("termId") Long termId);

    @Query("SELECT s FROM Section s WHERE s.course.id = :courseId AND s.term.id = :termId")
    List<Section> findByCourseIdAndTermId(@Param("courseId") Long courseId, @Param("termId") Long termId);

    @Query("SELECT s FROM Section s JOIN s.schedule sch WHERE s.room.id = :roomId AND s.term.id = :termId AND sch.id = :scheduleId")
    List<Section> findConflictingSections(@Param("roomId") Long roomId,
                                          @Param("termId") Long termId,
                                          @Param("scheduleId") Long scheduleId);
}
