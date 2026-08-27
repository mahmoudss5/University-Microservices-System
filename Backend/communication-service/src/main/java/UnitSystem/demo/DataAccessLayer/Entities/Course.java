package UnitSystem.demo.DataAccessLayer.Entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Stub — the real Course entity lives in the Academic Core service.
 * Maps to the shared "courses" table for JPA relationship resolution only.
 * Read-only from this service's perspective.
 */
@Entity
@Table(name = "course_snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

   @Id
   @Column(name = "course_id")
   private Long courseId;

   @Column(name = "course_name", nullable = false, length = 255)
   private String courseName;


}
