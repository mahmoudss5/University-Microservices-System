package UnitSystem.demo.BusinessLogic.InterfaceServiceLayer;

import UnitSystem.demo.DataAccessLayer.Dto.Teacher.TeacherRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Teacher.TeacherResponse;
import UnitSystem.demo.DataAccessLayer.Dto.UserDetails.TeacherDetailsResponse;
import UnitSystem.demo.DataAccessLayer.Dto.UserDetails.UserDetailsRequest;
import UnitSystem.demo.DataAccessLayer.Entities.User;

import java.util.List;

public interface TeacherService {
    List<TeacherResponse> getAllTeachers();

    TeacherResponse getTeacherById(Long teacherId);

    TeacherResponse getTeacherByUserName(String userName);

    TeacherResponse createTeacher(TeacherRequest teacherRequest);

    TeacherResponse updateTeacher(Long teacherId, TeacherRequest teacherRequest);

    void saveUserASTeacher(User user);

    void deleteTeacher(Long teacherId);

    TeacherDetailsResponse getTeacherDetails(UserDetailsRequest userDetailsRequest);
}
