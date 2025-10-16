package service;

import org.example.model.Student;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StudentService {
    private final Map<String, Student> students = new HashMap<>();

    public Student getStudent(String name) {
        return students.get(name);
    }

}
