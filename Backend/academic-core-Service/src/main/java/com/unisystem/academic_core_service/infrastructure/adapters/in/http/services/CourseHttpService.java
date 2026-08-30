package com.unisystem.academic_core_service.infrastructure.adapters.in.http.services;

import com.unisystem.academic_core_service.application.port.in.CreateCourseUseCase;
import com.unisystem.academic_core_service.application.port.in.GetCoursesQuery;
import com.unisystem.academic_core_service.application.port.out.CourseRepositoryPort;
import com.unisystem.academic_core_service.application.port.out.CoursePrerequisiteRepositoryPort;
import com.unisystem.academic_core_service.application.port.out.EventPublisherPort;
import com.unisystem.academic_core_service.domain.events.CourseDeletedEvent;
import com.unisystem.academic_core_service.domain.exceptions.InvalidPrerequisiteException;
import com.unisystem.academic_core_service.domain.exceptions.CourseNotFoundException;
import com.unisystem.academic_core_service.domain.model.Course;
import com.unisystem.academic_core_service.infrastructure.adapters.in.http.Dto.Request.CreateCourseRequest;
import com.unisystem.academic_core_service.infrastructure.adapters.in.http.Dto.Request.UpdateCourseRequest;
import com.unisystem.academic_core_service.infrastructure.adapters.in.http.Dto.Response.CourseCardResponse;
import com.unisystem.academic_core_service.infrastructure.adapters.in.http.Dto.Response.CoureseDetailsResponse;
import com.unisystem.academic_core_service.infrastructure.adapters.in.http.Mappers.CourseMapper;
import com.unisystem.academic_core_service.infrastructure.adapters.out.iam.IamUserClient;
import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.entity.DepartmentEntity;
import com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.repository.DepartmentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseHttpService {

    private final CreateCourseUseCase createCourseUseCase;
    private final GetCoursesQuery getCoursesQuery;
    private final CourseRepositoryPort courseRepositoryPort;
    private final DepartmentJpaRepository departmentJpaRepository;
    private final IamUserClient iamUserClient;
    private final CourseMapper courseMapper;
    private final CoursePrerequisiteRepositoryPort prerequisiteRepository;
    private final EventPublisherPort events;

    @Transactional
    public Course createCourse(CreateCourseRequest request, String userIdHeader) {
        Long teacherId = resolveTeacherId(userIdHeader, request.userId());
        Long departmentId = resolveDepartmentId(request.departmentName());
        CreateCourseUseCase.CreateCourseCommand command =
                courseMapper.courseRequestToCreateCourseCommand(request, teacherId, departmentId);
        return createCourseUseCase.create(command);
    }

    @Transactional
    public void deleteCourse(Long id) {
        getExistingCourse(id);
        if (prerequisiteRepository.isRequiredByAnyCourse(id)) {
            throw new InvalidPrerequisiteException("Course cannot be deleted while another course requires it");
        }
        courseRepositoryPort.deleteById(id);
        events.publishCourseDeleted(new CourseDeletedEvent(id.toString()));
    }

    public Course updateCourse(Long id, UpdateCourseRequest request, String userIdHeader) {
        Course existingCourse = getExistingCourse(id);
        Long teacherId = resolveTeacherId(userIdHeader, request.userId());
        Long departmentId = resolveDepartmentId(request.departmentName());
        Course course = courseMapper.UpdateCourseRequestToCourse(request, existingCourse, teacherId, departmentId);
        return courseRepositoryPort.save(course);
    }

    public CoureseDetailsResponse getCourseById(Long id, String authHeader) {
        Course course = getExistingCourse(id);
        String teacherName = course.getTeacherId() == null
                ? null
                : iamUserClient.getTeacherName(course.getTeacherId(), authHeader);
        return courseMapper.courseToCoureseDetailsResponse(course, teacherName);
    }

    public List<CourseCardResponse> getAllCourses(String authHeader) {
        return getCoursesQuery.findAll().stream()
                .map(course -> courseMapper.courseToCourseCardResponse(
                        course,
                        course.getTeacherId() == null ? null : iamUserClient.getTeacherName(course.getTeacherId(), authHeader)))
                .toList();
    }

    public List<Course> getCoursesByIds(List<Long> ids) {
        return getCoursesQuery.findByIds(ids == null ? List.of() : ids);
    }

    public List<Course> getPopularCourses(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        return getCoursesQuery.findPopular(safeLimit);
    }

    public List<Course> getCoursesByTeacherName(String teacherName) {
        return getCoursesQuery.findByTeacherName(teacherName);
    }

    public List<Course> getCoursesByTeacherId(Long teacherId) {
        return getCoursesQuery.findByTeacherId(teacherId);
    }

    public List<Course> getCoursesByDepartmentName(String departmentName) {
        return getCoursesQuery.findByDepartmentName(departmentName);
    }

    private Course getExistingCourse(Long id) {
        return getCoursesQuery.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));
    }

    private Long resolveDepartmentId(String departmentName) {
        if (departmentName == null || departmentName.isBlank()) {
            throw new IllegalArgumentException("Department name is required");
        }
        List<DepartmentEntity> departments = departmentJpaRepository.findByNameIgnoreCase(departmentName.trim());
        if (departments.isEmpty()) {
            throw new IllegalArgumentException("Department not found: " + departmentName);
        }
        return departments.get(0).getId();
    }

    private Long resolveTeacherId(String userIdHeader, Long userId) {
        Long headerId = parseLong(userIdHeader);
        if (headerId != null) {
            return headerId;
        }
        if (userId != null && userId > 0) {
            return userId;
        }
        throw new IllegalArgumentException("Teacher id is required");
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
