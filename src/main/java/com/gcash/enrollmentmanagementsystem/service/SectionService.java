package com.gcash.enrollmentmanagementsystem.service;

import com.gcash.enrollmentmanagementsystem.dto.*;
import com.gcash.enrollmentmanagementsystem.entity.*;
import com.gcash.enrollmentmanagementsystem.enums.EnrollmentStatus;
import com.gcash.enrollmentmanagementsystem.exception.BadRequestException;
import com.gcash.enrollmentmanagementsystem.exception.ResourceNotFoundException;
import com.gcash.enrollmentmanagementsystem.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final RoomRepository roomRepository;
    private final TermRepository termRepository;
    private final ScheduleRepository scheduleRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentService studentService;

    @Transactional(readOnly = true)
    public Page<SectionDto> getAllSections(Pageable pageable) {
        Page<Section> sections = sectionRepository.findAll(pageable);
        List<SectionDto> dtoList = sections.getContent().stream()
                .map(SectionService::toSectionDto)
                .toList();
        return new PageImpl<>(dtoList, pageable, sections.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<SectionDto> getAvailableSections(Long termId, String courseCode) {
        List<Section> sections;

        if (termId != null && courseCode != null) {
            Course course = courseRepository.findByCourseCode(courseCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Course", "courseCode", courseCode));
            sections = sectionRepository.findByCourseIdAndTermId(course.getId(), termId);
        } else if (termId != null) {
            sections = sectionRepository.findAvailableSectionsByTerm(termId);
        } else if (courseCode != null) {
            Course course = courseRepository.findByCourseCode(courseCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Course", "courseCode", courseCode));
            sections = sectionRepository.findByCourseId(course.getId());
        } else {
            Term activeTerm = termRepository.findFirstByIsActiveTrueOrderByStartDateDesc()
                    .orElseThrow(() -> new ResourceNotFoundException("No active term found"));
            sections = sectionRepository.findAvailableSectionsByTerm(activeTerm.getId());
        }

        return sections.stream().map(SectionService::toSectionDto).toList();
    }

    @Transactional(readOnly = true)
    public SectionDto getSectionById(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", id));
        return toSectionDto(section);
    }

    @Transactional(readOnly = true)
    public Page<StudentDto> getStudentsInSection(Long sectionId, Pageable pageable) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", sectionId));

        List<Enrollment> enrollments = enrollmentRepository.findBySectionIdAndStatus(
                sectionId, EnrollmentStatus.ENROLLED);

        List<StudentDto> dtoList = enrollments.stream()
                .map(e -> studentService.toDto(e.getStudent()))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), dtoList.size());

        List<StudentDto> pageContent = start >= dtoList.size()
                ? List.of()
                : dtoList.subList(start, end);

        return new PageImpl<>(pageContent, pageable, dtoList.size());
    }

    @Transactional
    public SectionDto createSection(SectionCreateRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", request.getCourseId()));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", request.getRoomId()));

        Term term = termRepository.findById(request.getTermId())
                .orElseThrow(() -> new ResourceNotFoundException("Term", "id", request.getTermId()));

        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", "id", request.getScheduleId()));

        List<Section> conflictingSections = sectionRepository.findConflictingSections(
                room.getId(), term.getId(), schedule.getId());
        if (!conflictingSections.isEmpty()) {
            throw new BadRequestException("Room is already occupied at this schedule for the term");
        }

        if (request.getMaxSeats() > room.getCapacity()) {
            throw new BadRequestException(
                    String.format("Max seats (%d) cannot exceed room capacity (%d)",
                            request.getMaxSeats(), room.getCapacity()));
        }

        Section section = Section.builder()
                .sectionCode(request.getSectionCode())
                .course(course)
                .room(room)
                .term(term)
                .schedule(schedule)
                .maxSeats(request.getMaxSeats())
                .currentEnrollment(0)
                .build();

        section = sectionRepository.save(section);
        log.info("Created new section: {} for course: {}", section.getSectionCode(), course.getCourseCode());

        return toSectionDto(section);
    }

    @Transactional
    public SectionDto updateSection(Long id, SectionUpdateRequest request) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", id));

        if (request.getSectionCode() != null) {
            section.setSectionCode(request.getSectionCode());
        }

        if (request.getRoomId() != null) {
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room", "id", request.getRoomId()));

            List<Section> conflictingSections = sectionRepository.findConflictingSections(
                    room.getId(), section.getTerm().getId(), section.getSchedule().getId());
            conflictingSections.removeIf(s -> s.getId().equals(id));
            if (!conflictingSections.isEmpty()) {
                throw new BadRequestException("Room is already occupied at this schedule for the term");
            }

            section.setRoom(room);
        }

        if (request.getScheduleId() != null) {
            Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Schedule", "id", request.getScheduleId()));

            List<Section> conflictingSections = sectionRepository.findConflictingSections(
                    section.getRoom().getId(), section.getTerm().getId(), schedule.getId());
            conflictingSections.removeIf(s -> s.getId().equals(id));
            if (!conflictingSections.isEmpty()) {
                throw new BadRequestException("Room is already occupied at this schedule for the term");
            }

            section.setSchedule(schedule);
        }

        if (request.getMaxSeats() != null) {
            if (request.getMaxSeats() < section.getCurrentEnrollment()) {
                throw new BadRequestException(
                        String.format("Max seats (%d) cannot be less than current enrollment (%d)",
                                request.getMaxSeats(), section.getCurrentEnrollment()));
            }
            section.setMaxSeats(request.getMaxSeats());
        }

        section = sectionRepository.save(section);
        log.info("Updated section: {}", section.getSectionCode());

        return toSectionDto(section);
    }

    @Transactional
    public void deleteSection(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", id));

        Long activeEnrollments = enrollmentRepository.countActiveEnrollmentsBySectionId(id);
        if (activeEnrollments > 0) {
            throw new BadRequestException(
                    String.format("Cannot delete section with %d active enrollment(s)", activeEnrollments));
        }

        sectionRepository.delete(section);
        log.info("Deleted section: {}", section.getSectionCode());
    }

    @Transactional(readOnly = true)
    public Section getSectionEntityById(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section", "id", id));
    }

    public static SectionDto toSectionDto(Section section) {
        if (section == null) return null;
        return SectionDto.builder()
                .id(section.getId())
                .sectionCode(section.getSectionCode())
                .course(toCourseDto(section.getCourse()))
                .room(toRoomDto(section.getRoom()))
                .term(TermService.toDto(section.getTerm()))
                .schedule(toScheduleDto(section.getSchedule()))
                .maxSeats(section.getMaxSeats())
                .currentEnrollment(section.getCurrentEnrollment())
                .availableSeats(section.getMaxSeats() - section.getCurrentEnrollment())
                .build();
    }

    private static CourseDto toCourseDto(Course course) {
        if (course == null) return null;
        return CourseDto.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .units(course.getUnits())
                .degreeName(course.getDegree() != null ? course.getDegree().getName() : null)
                .build();
    }

    private static RoomDto toRoomDto(Room room) {
        if (room == null) return null;
        return RoomDto.builder()
                .id(room.getId())
                .roomCode(room.getRoomCode())
                .capacity(room.getCapacity())
                .building(room.getBuilding())
                .build();
    }

    private static ScheduleDto toScheduleDto(Schedule schedule) {
        if (schedule == null) return null;
        return ScheduleDto.builder()
                .id(schedule.getId())
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .build();
    }
}
