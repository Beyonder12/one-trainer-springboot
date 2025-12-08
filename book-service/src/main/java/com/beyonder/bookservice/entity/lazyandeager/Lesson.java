package com.beyonder.bookservice.entity.lazyandeager;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topic;
    private Integer durationMinutes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id")
//    @JsonBackReference
    @JsonIgnoreProperties("lessons")
    private Course course;
}
