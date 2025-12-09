package com.beyonder.bookservice.controller.lazyandeager;

import com.beyonder.bookservice.entity.lazyandeager.Course;
import com.beyonder.bookservice.entity.lazyandeager.Lesson;
import com.beyonder.bookservice.service.lazyandeager.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // Endpoint 1 — tidak load lessons
    @GetMapping("/{id}")
    public Course getCourse(@PathVariable Long id) {
        return courseService.getCourseOnly(id);
    }

    // Endpoint 2 — TRIGGER lazy load
    @GetMapping("/{id}/lessons")
    public List<Lesson> getCourseLessons(@PathVariable Long id) {
        return courseService.getCourseLessons(id);
    }

    @PostMapping
    public Course addCourse(@RequestBody Course course) {
        courseService.addCourse(course);
        return course;
    }

    @GetMapping
    public List<Course> findAll() {
        return courseService.findAll();
    }
}
