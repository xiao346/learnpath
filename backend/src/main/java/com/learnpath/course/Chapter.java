package com.learnpath.course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "course_chapter")
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private int orderIndex;

    @Column(nullable = false)
    private int durationMinutes;

    protected Chapter() {
    }

    Chapter(Course course, String title, int orderIndex, int durationMinutes) {
        this.course = course;
        this.title = title;
        this.orderIndex = orderIndex;
        this.durationMinutes = durationMinutes;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public int getOrderIndex() { return orderIndex; }
    public int getDurationMinutes() { return durationMinutes; }
}
