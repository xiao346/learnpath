package com.learnpath.course;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false, length = 120)
    private String subtitle;

    @Column(nullable = false, length = 30)
    private String category;

    @Column(nullable = false, length = 40)
    private String teacherName;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false, length = 20)
    private String difficulty;

    @Column(nullable = false)
    private int durationMinutes;

    @Column(nullable = false, length = 20)
    private String accent;

    @Column(nullable = false, length = 8)
    private String icon;

    @Column(nullable = false)
    private boolean published = true;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private List<Chapter> chapters = new ArrayList<>();

    protected Course() {
    }

    public Course(String title, String subtitle, String category, String teacherName, String description,
                  String difficulty, int durationMinutes, String accent, String icon) {
        this.title = title;
        this.subtitle = subtitle;
        this.category = category;
        this.teacherName = teacherName;
        this.description = description;
        this.difficulty = difficulty;
        this.durationMinutes = durationMinutes;
        this.accent = accent;
        this.icon = icon;
    }

    public Course addChapter(String title, int orderIndex, int durationMinutes) {
        chapters.add(new Chapter(this, title, orderIndex, durationMinutes));
        return this;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getCategory() { return category; }
    public String getTeacherName() { return teacherName; }
    public String getDescription() { return description; }
    public String getDifficulty() { return difficulty; }
    public int getDurationMinutes() { return durationMinutes; }
    public String getAccent() { return accent; }
    public String getIcon() { return icon; }
    public boolean isPublished() { return published; }
    public List<Chapter> getChapters() { return chapters; }
}
