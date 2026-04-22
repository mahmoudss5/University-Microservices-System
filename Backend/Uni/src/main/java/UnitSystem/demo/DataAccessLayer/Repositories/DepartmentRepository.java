package UnitSystem.demo.DataAccessLayer.Repositories;

import UnitSystem.demo.DataAccessLayer.Dto.Department.DepartmentsDetails;
import UnitSystem.demo.DataAccessLayer.Entities.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department,Long> {

    Department findByName(String name);
   Boolean existsByName(String name);

   @Query("SELECT COUNT(c) FROM Course c WHERE c.department.id = :departmentId")
   long CountNumberOfCoursesInDepartment(Long departmentId);


    @Query("SELECT new UnitSystem.demo.DataAccessLayer.Dto.Department.DepartmentsDetails(d.id, d.name, COUNT(c)) " +
            "FROM Department d " +
            "LEFT JOIN Course c ON c.department.id = d.id " +
            "GROUP BY d.id, d.name")
    List<DepartmentsDetails> findAllDepartmentsWithCourseCount();
}
