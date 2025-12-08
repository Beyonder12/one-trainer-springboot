package com.beyonder.bookservice.repository.lazyandeager;

import com.beyonder.bookservice.entity.lazyandeager.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
}
