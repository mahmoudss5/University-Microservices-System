package com.unisystem.academic_core_service.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "course_prerequisites")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursePrerequisiteEntity {

    @EmbeddedId
    private CoursePrerequisiteId id;
}
