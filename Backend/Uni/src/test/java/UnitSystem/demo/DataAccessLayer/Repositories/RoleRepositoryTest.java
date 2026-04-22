package UnitSystem.demo.DataAccessLayer.Repositories;

import UnitSystem.demo.DataAccessLayer.Entities.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    private Role role;
    private Role role2;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setName("ADMIN");

        role2 = new Role();
        role2.setName("USER");
    }

    @Test
    void itShouldSaveRole() {
        Role savedRole = roleRepository.save(role);

        assertNotNull(savedRole.getId());
        assertEquals(role.getName(), savedRole.getName());
    }

    @Test
    void itShouldSaveMultipleRoles() {
        Role savedRole1 = roleRepository.save(role);
        Role savedRole2 = roleRepository.save(role2);

        assertNotNull(savedRole1.getId());
        assertNotNull(savedRole2.getId());
        assertEquals(2, roleRepository.findAll().size());
    }

    @Test
    void itShouldFindRoleById() {
        Role savedRole = roleRepository.save(role);

        Optional<Role> foundRole = roleRepository.findById(savedRole.getId());

        assertTrue(foundRole.isPresent());
        assertEquals(savedRole.getId(), foundRole.get().getId());
        assertEquals(savedRole.getName(), foundRole.get().getName());
    }

    @Test
    void itShouldFindAllRoles() {
        roleRepository.save(role);
        roleRepository.save(role2);

        List<Role> allRoles = roleRepository.findAll();

        assertEquals(2, allRoles.size());
    }

    @Test
    void itShouldDeleteRole() {
        Role savedRole = roleRepository.save(role);
        Long roleId = savedRole.getId();

        roleRepository.delete(savedRole);

        assertFalse(roleRepository.existsById(roleId));
    }

    @Test
    void itShouldDeleteRoleById() {
        Role savedRole = roleRepository.save(role);
        Long roleId = savedRole.getId();

        roleRepository.deleteById(roleId);

        assertFalse(roleRepository.existsById(roleId));
    }

    @Test
    void itShouldUpdateRole() {
        Role savedRole = roleRepository.save(role);

        savedRole.setName("SUPER_ADMIN");
        Role updatedRole = roleRepository.save(savedRole);

        assertEquals("SUPER_ADMIN", updatedRole.getName());
        assertEquals(savedRole.getId(), updatedRole.getId());
    }

    @Test
    void itShouldCheckIfRoleExistsById() {
        Role savedRole = roleRepository.save(role);

        assertTrue(roleRepository.existsById(savedRole.getId()));
        assertFalse(roleRepository.existsById(999L));
    }

    @Test
    void itShouldCountRoles() {
        assertEquals(0, roleRepository.count());

        roleRepository.save(role);
        assertEquals(1, roleRepository.count());

        roleRepository.save(role2);
        assertEquals(2, roleRepository.count());
    }

    @Test
    void itShouldDeleteAllRoles() {
        roleRepository.save(role);
        roleRepository.save(role2);
        assertEquals(2, roleRepository.count());

        roleRepository.deleteAll();

        assertEquals(0, roleRepository.count());
    }

    @Test
    void itShouldReturnEmptyWhenRoleNotFound() {
        Optional<Role> foundRole = roleRepository.findById(999L);

        assertFalse(foundRole.isPresent());
    }
}
