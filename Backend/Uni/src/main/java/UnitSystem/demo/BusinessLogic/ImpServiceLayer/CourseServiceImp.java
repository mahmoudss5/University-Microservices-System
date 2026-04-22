package UnitSystem.demo.BusinessLogic.ImpServiceLayer;

import UnitSystem.demo.Aspect.Security.CourseTeacherOnly;
import UnitSystem.demo.Aspect.Security.TeachersOnly;
import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.CourseService;
import UnitSystem.demo.BusinessLogic.Mappers.CourseMapper;
import UnitSystem.demo.DataAccessLayer.Dto.Course.CourseRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Course.CourseResponse;
import UnitSystem.demo.DataAccessLayer.Entities.Course;
import UnitSystem.demo.DataAccessLayer.Entities.Teacher;
import UnitSystem.demo.DataAccessLayer.Repositories.CourseRepository;
import UnitSystem.demo.ExcHandler.Entites.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImp implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    @Override
    @Cacheable(value = "coursesCache", key = "'allCourses'")
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(courseMapper::mapToCourseResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "coursesCache", key = "'popularCourses_' + #topN")
    public List<CourseResponse> getMostPopularCourses(int topN) {
        return courseRepository.findTopPopularCourses().stream()
                .limit(topN)
                .map(courseMapper::mapToCourseResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "coursesCache", key = "'courseById:' + #courseId")
    public CourseResponse getCourseById(Long courseId) {
        log.info("Fetching course with ID: {}", courseId);
        return courseRepository.findById(courseId)
                .map(courseMapper::mapToCourseResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
    }

    @Override
    @TeachersOnly
    @CacheEvict(value = "coursesCache", allEntries = true)
    public CourseResponse createCourse(CourseRequest courseRequest) {
        log.info("Creating course: {}", courseRequest);
        Course course = courseMapper.mapToCourse(courseRequest);
        courseRepository.save(course);
        return courseMapper.mapToCourseResponse(course);
    }

    @Override
    @CourseTeacherOnly
    @CacheEvict(value = "coursesCache", allEntries = true)
    public CourseResponse updateCourse(CourseRequest courseRequest, Long courseId) {
        log.info("Updating course: {}", courseRequest);
        Course course = courseMapper.mapToCourse(courseRequest);
        course.setId(courseId);
        courseRepository.save(course);
        return courseMapper.mapToCourseResponse(course);
    }

    @Override
    @CourseTeacherOnly
    @CacheEvict(value = "coursesCache", allEntries = true)
    public void deleteCourse(Long courseId) {
        courseRepository.deleteById(courseId);

    }

    @Override
    @Cacheable(value = "coursesCache", key = "'coursesByDepartment:' + #departmentName")
    public List<CourseResponse> getCoursesByDepartment(String departmentName) {
        return courseRepository.findByDepartmentName(departmentName).stream()
                .map(courseMapper::mapToCourseResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "coursesCache", key = "'coursesByTeacherId:' + #teacherId")
    public List<CourseResponse> getCoursesByTeacherId(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId).stream()
                .map(courseMapper::mapToCourseResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Teacher findCourseTeacher(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId))
                .getTeacher();
    }

    @Override
    public Course getCourseEntityById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
    }

    @Override
    public List<String> findStudentEmailsByCourseId(Long courseId) {
        return courseRepository.findStudentEmailsByCourseId(courseId);
    }
}
