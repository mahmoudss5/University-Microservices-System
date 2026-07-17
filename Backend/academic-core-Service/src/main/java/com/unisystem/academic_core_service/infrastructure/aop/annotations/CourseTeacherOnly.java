package com.unisystem.academic_core_service.infrastructure.aop.annotations;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CourseTeacherOnly {


    boolean requireCourseOwnership() default true;


    String param() default "courseId";

    String bodyParam() default "";


    String bodyField() default "courseId";
}
