package UnitSystem.demo.BusinessLogic.ImpServiceLayer;

import UnitSystem.demo.BusinessLogic.InterfaceServiceLayer.DepartmentService;
import UnitSystem.demo.BusinessLogic.Mappers.DepartmentMapper;
import UnitSystem.demo.DataAccessLayer.Dto.Department.DepartmentRequest;
import UnitSystem.demo.DataAccessLayer.Dto.Department.DepartmentResponse;
import UnitSystem.demo.DataAccessLayer.Dto.Department.DepartmentsDetails;
import UnitSystem.demo.DataAccessLayer.Entities.Department;
import UnitSystem.demo.DataAccessLayer.Repositories.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImp implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    @Cacheable(value = "departmentsCache", key = "'allDepartments'")
    public List<DepartmentsDetails> getAllDepartments() {
        return departmentRepository.findAllDepartmentsWithCourseCount();
    }

    @Override
    @Cacheable(value = "departmentsCache", key = "'departmentById:' + #departmentId")
    public DepartmentResponse getDepartmentById(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .map(departmentMapper::mapToDepartmentResponse)
                .orElse(null);
    }

    @Override
    @Cacheable(value = "departmentsCache", key = "'departmentByName:' + #name")
    public DepartmentResponse getDepartmentByName(String name) {
        Department department = departmentRepository.findByName(name);
        return department != null ? departmentMapper.mapToDepartmentResponse(department) : null;
    }

    @Override
    @CacheEvict(value = "departmentsCache", allEntries = true)
    public DepartmentResponse createDepartment(DepartmentRequest departmentRequest) {
        Department department = departmentMapper.mapToDepartment(departmentRequest);
        departmentRepository.save(department);
        return departmentMapper.mapToDepartmentResponse(department);
    }

    @Override
    @CacheEvict(value = "departmentsCache", allEntries = true)
    public DepartmentResponse updateDepartment(Long departmentId, DepartmentRequest departmentRequest) {
        Department existingDepartment = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        existingDepartment.setName(departmentRequest.getName());
        departmentRepository.save(existingDepartment);
        return departmentMapper.mapToDepartmentResponse(existingDepartment);
    }

    @Override
    @CacheEvict(value = "departmentsCache", allEntries = true)
    public void deleteDepartment(Long departmentId) {
        departmentRepository.deleteById(departmentId);
    }

    @Override
    @Cacheable(value = "departmentsCache", key = "'departmentExistsByName:' + #name")
    public Boolean existsByName(String name) {
        return departmentRepository.existsByName(name);
    }
}
