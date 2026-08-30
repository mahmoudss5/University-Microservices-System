package com.unisystem.academic_core_service.domain.exceptions;

import java.util.List;

public class PrerequisiteNotMetException extends RuntimeException {
    private final Long studentId;
    private final Long requestedCourseId;
    private final List<MissingPrerequisite> missingPrerequisites;

    public PrerequisiteNotMetException(
            Long studentId, Long requestedCourseId, List<MissingPrerequisite> missingPrerequisites) {
        super("Complete and pass all prerequisite courses before enrolling");
        this.studentId = studentId;
        this.requestedCourseId = requestedCourseId;
        this.missingPrerequisites = List.copyOf(missingPrerequisites);
    }

    public Long getStudentId() { return studentId; }
    public Long getRequestedCourseId() { return requestedCourseId; }
    public List<MissingPrerequisite> getMissingPrerequisites() { return missingPrerequisites; }

    public record MissingPrerequisite(Long courseId, String courseCode, String courseName) {}
}
