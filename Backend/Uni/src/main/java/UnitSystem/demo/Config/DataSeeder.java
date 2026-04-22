package UnitSystem.demo.Config;
import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.DepartmentService;
import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.UserService;
import UnitSystem.demo.DataAccessLayer.Entities.DepartmentsType;
import UnitSystem.demo.DataAccessLayer.Entities.Role;
import UnitSystem.demo.DataAccessLayer.Entities.RoleType;
import UnitSystem.demo.DataAccessLayer.Repositories.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@AllArgsConstructor
public class DataSeeder {

    private final UserService userService;
    private final DepartmentService departmentService;
    @Bean
    CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            Arrays.stream(RoleType.values()).forEach(roleType -> {
                if (!roleRepository.existsByName(roleType.name())) {
                    roleRepository.save(Role.builder().name(roleType.name()).build());
                    System.out.println("Seeded Role: " + roleType.name());
                }
            });
          Arrays.stream(DepartmentsType.values()).forEach(departmentType -> {
                if (departmentService.getDepartmentByName(departmentType.name()) == null) {
                    departmentService.createDepartment(
                            UnitSystem.demo.DataAccessLayer.Dto.Department.DepartmentRequest.builder()
                                    .name(departmentType.name())
                                    .build()
                    );
                    System.out.println("Seeded Department: " + departmentType.name());
                }
          });
        };
    }

}