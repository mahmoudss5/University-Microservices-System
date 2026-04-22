package UnitSystem.demo.BusinessLogic.Mappers;
import UnitSystem.demo.DataAccessLayer.Dto.Department.DepartmentRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Department.DepartmentResponse;
import UnitSystem.demo.DataAccessLayer.Entities.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public DepartmentResponse mapToDepartmentResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .build();
    }

    public Department mapToDepartment(DepartmentRequest departmentRequest) {
        Department department = new Department();
        department.setName(departmentRequest.getName());
        return department;
    }
}
