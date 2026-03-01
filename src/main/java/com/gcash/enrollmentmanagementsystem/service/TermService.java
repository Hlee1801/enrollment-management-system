package com.gcash.enrollmentmanagementsystem.service;

import com.gcash.enrollmentmanagementsystem.dto.TermCreateRequest;
import com.gcash.enrollmentmanagementsystem.dto.TermDto;
import com.gcash.enrollmentmanagementsystem.entity.Term;
import com.gcash.enrollmentmanagementsystem.exception.BadRequestException;
import com.gcash.enrollmentmanagementsystem.exception.ResourceNotFoundException;
import com.gcash.enrollmentmanagementsystem.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TermService {

    private final TermRepository termRepository;

    @Transactional(readOnly = true)
    public List<TermDto> getAllTerms() {
        List<Term> terms = termRepository.findAll();
        return terms.stream().map(TermService::toDto).toList();
    }

    @Transactional(readOnly = true)
    public TermDto getTermById(Long id) {
        Term term = termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Term", "id", id));
        return toDto(term);
    }

    @Transactional(readOnly = true)
    public TermDto getActiveTerm() {
        Term term = termRepository.findFirstByIsActiveTrueOrderByStartDateDesc()
                .orElseThrow(() -> new ResourceNotFoundException("No active term found"));
        return toDto(term);
    }

    @Transactional
    public TermDto createTerm(TermCreateRequest request) {
        if (termRepository.existsByTermName(request.getTermName())) {
            throw new BadRequestException("Term with name '" + request.getTermName() + "' already exists");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        Term term = Term.builder()
                .termName(request.getTermName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : false)
                .build();

        term = termRepository.save(term);
        log.info("Created new term: {}", term.getTermName());

        return toDto(term);
    }

    @Transactional(readOnly = true)
    public Term getTermEntityById(Long id) {
        return termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Term", "id", id));
    }

    public static TermDto toDto(Term term) {
        if (term == null) return null;
        return TermDto.builder()
                .id(term.getId())
                .termName(term.getTermName())
                .startDate(term.getStartDate())
                .endDate(term.getEndDate())
                .isActive(term.getIsActive())
                .build();
    }
}
