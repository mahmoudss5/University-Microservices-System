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
@Table(name = "teachers")
@PrimaryKeyJoinColumn(name = "user_id")
@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Teacher extends User{

    @Column(name = "office_location", nullable = false)
    private String officeLocation;

    @Column(name = "salary", nullable = false)
    private BigDecimal salary;


    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Course> courses = new HashSet<>();



}
