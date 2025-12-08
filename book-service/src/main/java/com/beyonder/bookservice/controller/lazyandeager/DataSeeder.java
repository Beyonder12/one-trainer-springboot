package com.beyonder.bookservice.controller.lazyandeager;

import com.beyonder.bookservice.entity.lazyandeager.Course;
import com.beyonder.bookservice.entity.lazyandeager.Lesson;
import com.beyonder.bookservice.repository.lazyandeager.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CourseRepository courseRepository;

    @Override
    public void run(String... args) {
        Course c = Course.builder()
                .name("Spring Boot Advanced")
                .instructor("Fajri Illahi")
                .build();

        Lesson l1 = Lesson.builder().topic("Lazy Loading").durationMinutes(20).course(c).build();
        Lesson l2 = Lesson.builder().topic("Eager Loading").durationMinutes(20).course(c).build();

        c.setLessons(List.of(l1, l2));

        courseRepository.save(c);
    }
}
