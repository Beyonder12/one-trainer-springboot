package com.beyonder.bookservice.service.lazyandeager;

import com.beyonder.bookservice.entity.lazyandeager.Course;
import com.beyonder.bookservice.entity.lazyandeager.Lesson;
import com.beyonder.bookservice.repository.lazyandeager.CourseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    public Course getCourseOnly(Long id) {
        // lessons BELUM di load
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
    }

    @Transactional
    public List<Lesson> getCourseLessons(Long id) {
        Course course = this.getCourseOnly(id);

        // lessons baru akan di-load DI SINI
        return course.getLessons();
    }
}
