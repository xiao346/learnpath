package com.learnpath.course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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

    @Column(length = 1500)
    private String overview;

    @Column(length = 2500)
    private String objectives;

    @Column(length = 5000)
    private String keyPoints;

    @Column(length = 2500)
    private String practiceTask;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String beginnerIntro;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String beginnerAnalogy;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String beginnerWalkthrough;

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
    public String getOverview() { return overview; }
    public String getObjectives() { return objectives; }
    public String getKeyPoints() { return keyPoints; }
    public String getPracticeTask() { return practiceTask; }
    public String getBeginnerIntro() { return beginnerIntro; }
    public String getBeginnerAnalogy() { return beginnerAnalogy; }
    public String getBeginnerWalkthrough() { return beginnerWalkthrough; }

    public void updateLesson(String overview, String objectives, String keyPoints, String practiceTask) {
        this.overview = overview;
        this.objectives = objectives;
        this.keyPoints = keyPoints;
        this.practiceTask = practiceTask;
    }

    public void updateBeginnerGuide(String intro, String analogy, String walkthrough) {
        this.beginnerIntro = intro;
        this.beginnerAnalogy = analogy;
        this.beginnerWalkthrough = walkthrough;
    }
}
