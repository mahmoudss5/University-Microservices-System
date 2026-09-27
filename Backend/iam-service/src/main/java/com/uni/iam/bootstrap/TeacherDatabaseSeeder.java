package com.uni.iam.bootstrap;

import com.uni.iam.entity.Role;
import com.uni.iam.entity.Teacher;
import com.uni.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/** Seeds real teacher accounts used by the Academic Core demo courses. */
@Component
@Slf4j
@RequiredArgsConstructor
public class TeacherDatabaseSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${iam.seed.teachers.enabled:true}")
    private boolean enabled;

    @Value("${iam.seed.teachers.password:test1234}")
    private String password;

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            log.info("IAM teacher seed disabled");
            return;
        }

        List<SeedTeacher> teachers = List.of(
                new SeedTeacher("sara.teacher", "sara.teacher@university.test", "Computer Science", "1000.00"),
                new SeedTeacher("omar.teacher", "omar.teacher@university.test", "Information Systems", "1100.00"),
                new SeedTeacher("lina.teacher", "lina.teacher@university.test", "Software Engineering", "1050.00"),
                new SeedTeacher("youssef.teacher", "youssef.teacher@university.test", "Artificial Intelligence", "1200.00"),
                new SeedTeacher("mariam.teacher", "mariam.teacher@university.test", "Data Science", "1150.00")
        );

        for (SeedTeacher seed : teachers) {
            if (userRepository.existsByEmail(seed.email()) || userRepository.existsByUsername(seed.username())) {
                continue;
            }

            userRepository.save(Teacher.builder()
                    .username(seed.username())
                    .email(seed.email())
                    .password(passwordEncoder.encode(password))
                    .role(Role.TEACHER)
                    .active(true)
                    .officeLocation(seed.department())
                    .salary(new BigDecimal(seed.salary()))
                    .build());
        }

        log.info("IAM teacher seed ready: {} teacher accounts", teachers.size());
    }

    private record SeedTeacher(String username, String email, String department, String salary) {
    }
}
