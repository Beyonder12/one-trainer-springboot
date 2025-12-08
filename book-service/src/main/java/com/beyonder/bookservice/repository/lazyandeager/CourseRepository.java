package com.beyonder.bookservice.repository.lazyandeager;

import com.beyonder.bookservice.entity.lazyandeager.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
