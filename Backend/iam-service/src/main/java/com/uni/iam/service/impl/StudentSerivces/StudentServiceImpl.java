package com.uni.iam.service.impl.StudentSerivces;

import com.uni.iam.aop.ExecutionTime;
import com.uni.iam.aop.GeneralLog;
import com.uni.iam.dto.response.StudentResponse;
import com.uni.iam.entity.Student;
import com.uni.iam.exception.UserNotFoundException;
import com.uni.iam.repository.StudentRepository;
import com.uni.iam.service.Mappers.StudentMapper;
import com.uni.iam.service.interfaces.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

        private final StudentRepository studentRepository;
        private final StudentMapper studentMapper;

        @Override
        @ExecutionTime
        @GeneralLog
        @Transactional(readOnly = true)
        public List<StudentResponse> getAllStudents() {
                return studentRepository.findAll().stream()
                        .map(studentMapper::toStudentResponse)
                        .toList();
        }

        @Override
        @ExecutionTime
        @GeneralLog
        public Student getById(Long id) {
                return studentRepository.findById(id)
                        .orElseThrow(() -> new UserNotFoundException(id));
        }

        @Override
        @ExecutionTime
        @GeneralLog
        public String getStudneName(Long id) {
                Student student = studentRepository.findById(id)
                        .orElseThrow(() -> new UserNotFoundException(id));
                return student.getUsername();
        }

}
