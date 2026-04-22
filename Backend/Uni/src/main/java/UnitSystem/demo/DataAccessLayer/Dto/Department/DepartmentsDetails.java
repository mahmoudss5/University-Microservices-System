package UnitSystem.demo.DataAccessLayer.Dto.Department;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentsDetails {
Long id;
String name;
Long numberOfCourses;
}
