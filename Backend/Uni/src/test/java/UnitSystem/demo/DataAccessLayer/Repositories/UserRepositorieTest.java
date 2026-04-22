package UnitSystem.demo.DataAccessLayer.Repositories;

import UnitSystem.demo.DataAccessLayer.Entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositorieTest {

    @Autowired
    private UserRepository userRepositorie;


    private User testUser;
    private User testUser2;
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserName("testuser");
        testUser.setEmail("test@gmail.com");
        testUser.setPassword("password");
        testUser.setActive(true);
        testUser2 = new User();
        testUser2.setUserName("testuser2");
        testUser2.setEmail("test2@gami.com");
        testUser2.setPassword("password");
        testUser2.setActive(true);
    }

    @Test
    void itShouldSaveUser() {
        User savedUser = userRepositorie.save(testUser);
        assertNotNull(savedUser.getId());
        assertEquals(testUser.getUserName(), savedUser.getUserName());
        assertEquals(testUser.getEmail(), savedUser.getEmail());
    }

    @Test
    void itShouldSaveMultipleUsers() {
        User savedUser1 = userRepositorie.save(testUser);
        User savedUser2 = userRepositorie.save(testUser2);
        assertNotNull(savedUser1.getId());
        assertNotNull(savedUser2.getId());
        assertEquals(testUser.getUserName(), savedUser1.getUserName());
        assertEquals(testUser2.getUserName(), savedUser2.getUserName());
        assertEquals(2, userRepositorie.findAll().size());
    }

   @Test
    void itShouldDeleteUser() {
        User savedUser = userRepositorie.save(testUser);
        userRepositorie.delete(savedUser);
        assertFalse(userRepositorie.existsById(savedUser.getId()));
    }

    @Test
    void isShouldFindUserByEmail() {
        User savedUser = userRepositorie.save(testUser);
        User foundUser = userRepositorie.findByEmail(testUser.getEmail()).orElse(null);
        assertNotNull(foundUser);
        assertEquals(savedUser.getId(), foundUser.getId());
    }

    @Test
    void itShouldFindUserByUserName() {
        User savedUser = userRepositorie.save(testUser);
        User foundUser = userRepositorie.findByUserName(testUser.getUserName()).orElse(null);
        assertNotNull(foundUser);
        assertEquals(savedUser.getId(), foundUser.getId());
    }

    @Test
    void itShouldDeleteUserByEmail() {
        User savedUser = userRepositorie.save(testUser);
        userRepositorie.delete(savedUser);
        assertFalse(userRepositorie.existsByEmail(savedUser.getEmail()));
    }
    @Test
    void itShouldExistByUserName(){
        userRepositorie.save(testUser);
        assertTrue(userRepositorie.existsByUserName(testUser.getUserName()));
    }
    @Test
    void itShouldExistByEmail(){
        userRepositorie.save(testUser);
        assertTrue(userRepositorie.existsByEmail(testUser.getEmail()));
    }
    @Test
    void itShouldFindAllUsers(){
        userRepositorie.save(testUser);
        userRepositorie.save(testUser2);
        assertEquals(2, userRepositorie.findAll().size());
    }

    @Test
    void itShouldFindById() {
        User savedUser = userRepositorie.save(testUser);
        User foundUser = userRepositorie.findById(savedUser.getId()).orElse(null);
        assertNotNull(foundUser);
        assertEquals(savedUser.getId(), foundUser.getId());
    }


}
