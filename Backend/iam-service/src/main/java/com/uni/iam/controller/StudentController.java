package com.uni.iam.controller;

import com.uni.iam.dto.response.StudentBasicResponse;
import com.uni.iam.dto.response.StudentResponse;
import com.uni.iam.dto.response.StudentProfileResponse;
import com.uni.iam.service.impl.StudentSerivces.StudentDashboardFacade;
import com.uni.iam.service.interfaces.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final StudentDashboardFacade studentDashboardFacade;
    private final StudentService studentService;
    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<StudentProfileResponse> getStudentDetails(@PathVariable Long id) {
        log.info("Fetching details for student with ID: {}", id);
        StudentProfileResponse studentProfile =studentDashboardFacade.getFullStudentDashboard(id);
         log.info("Student details fetched successfully: {}", studentProfile);
        return ResponseEntity.ok(studentProfile);
    }
    @GetMapping("/basic/{id}")
    public ResponseEntity<StudentBasicResponse> getStudentBasic(@PathVariable Long id) {
        log.info("Fetching basic info for student with ID: {}", id);
        String name=studentService.getStudneName(id);
        StudentBasicResponse response=new StudentBasicResponse(name);
        log.info("Basic info fetched successfully: {}", response);
        return ResponseEntity.ok(response);
    }
}
