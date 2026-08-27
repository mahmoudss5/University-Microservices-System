package UnitSystem.demo.DataAccessLayer.Entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
        name = "enrollment_snapshots",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_enrollment_snapshots_student_course",
                columnNames = {"student_id", "course_id"}),
        indexes = @Index(
                name = "idx_enrollment_snapshots_course_id",
                columnList = "course_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @CreationTimestamp
    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private LocalDateTime enrolledAt;
}
