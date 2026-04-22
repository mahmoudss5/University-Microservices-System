package UnitSystem.demo.DataAccessLayer.Entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
@SuperBuilder
@Table(name = "students")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)

public class Student extends User{

    @Column(name = "gpa")
    private BigDecimal gpa;

    @Column(name = "enrollment_year")
    private int enrollmentYear;

    @Column(name = "total_credits")
    private int totalCredits;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<EnrolledCourse> enrolledCourses = new HashSet<>();

}
