package UnitSystem.demo.BusinessLogic.InterfaceServiceLayer;

import UnitSystem.demo.DataAccessLayer.Dto.Student.StudentRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Student.StudentResponse;
import UnitSystem.demo.DataAccessLayer.Dto.UserDetails.StudentDetailsResponse;
import UnitSystem.demo.DataAccessLayer.Dto.UserDetails.UserDetailsRequest;
import UnitSystem.demo.DataAccessLayer.Entities.User;

import java.util.List;

public interface StudentService {
    List<StudentResponse> getAllStudents();

    StudentResponse getStudentById(Long studentId);

    StudentResponse getStudentByUserName(String userName);

    StudentResponse createStudent(StudentRequest studentRequest);

    StudentResponse updateStudent(Long studentId, StudentRequest studentRequest);

    void saveUserAsStudent(User user);
    void deleteStudent(Long studentId);

    StudentDetailsResponse getStudentDetails(UserDetailsRequest userDetailsRequest);
}
