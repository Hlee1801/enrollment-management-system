package com.gcash.enrollmentmanagementsystem.repository;

import com.gcash.enrollmentmanagementsystem.entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TermRepository extends JpaRepository<Term, Long> {

    Optional<Term> findByTermName(String termName);

    boolean existsByTermName(String termName);

    List<Term> findByIsActiveTrue();

    Optional<Term> findFirstByIsActiveTrueOrderByStartDateDesc();
}
