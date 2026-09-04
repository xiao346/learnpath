package com.learnpath.course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "course_resource")
public class CourseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 50)
    private String provider;

    @Column(nullable = false, length = 20)
    private String resourceType;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(nullable = false)
    private int sortOrder;

    protected CourseResource() {
    }

    public CourseResource(Long courseId, String title, String provider, String resourceType,
                          String description, String url, int sortOrder) {
        this.courseId = courseId;
        this.title = title;
        this.provider = provider;
        this.resourceType = resourceType;
        this.description = description;
        this.url = url;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public Long getCourseId() { return courseId; }
    public String getTitle() { return title; }
    public String getProvider() { return provider; }
    public String getResourceType() { return resourceType; }
    public String getDescription() { return description; }
    public String getUrl() { return url; }
}
