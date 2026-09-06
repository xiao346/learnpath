package com.learnpath.journey;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "web_journey", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class WebJourney {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String projectType = "portfolio";

    @Column(nullable = false, length = 20)
    private String frontendStack = "vue";

    @Column(nullable = false, length = 20)
    private String backendStack = "java";

    @Column(nullable = false, length = 20)
    private String databaseType = "mysql";

    @Column(nullable = false, length = 32)
    private String pageName = "小途";

    @Column(nullable = false, length = 100)
    private String pageIntroduction = "一名正在探索 Web 世界的大一学生。";

    @Column(nullable = false, length = 180)
    private String pageInterest = "我喜欢摄影、音乐，也喜欢把新点子做出来。";

    @Column(nullable = false, length = 16)
    private String pageTheme = "blue";

    @Column(nullable = false, length = 16)
    private String styleAccent = "#5b72f2";

    @Column(nullable = false)
    private int styleRadius = 18;

    @Column(nullable = false)
    private int styleSpacing = 24;

    @Column(nullable = false)
    private boolean styleShadow = true;

    private Instant graduatedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    protected WebJourney() {
    }

    public WebJourney(Long userId) {
        this.userId = userId;
    }

    public void configure(String projectType, String frontendStack, String backendStack, String databaseType) {
        this.projectType = projectType;
        this.frontendStack = frontendStack;
        this.backendStack = backendStack;
        this.databaseType = databaseType;
        touch();
    }

    public void updateFirstPage(String name, String introduction, String interest, String theme) {
        this.pageName = name;
        this.pageIntroduction = introduction;
        this.pageInterest = interest;
        this.pageTheme = theme;
        touch();
    }

    public void updateStyle(String accent, int radius, int spacing, boolean shadow) {
        this.styleAccent = accent;
        this.styleRadius = radius;
        this.styleSpacing = spacing;
        this.styleShadow = shadow;
        touch();
    }

    public void graduate() {
        if (graduatedAt == null) graduatedAt = Instant.now();
        touch();
    }

    private void touch() {
        updatedAt = Instant.now();
    }

    public Long getUserId() { return userId; }
    public String getProjectType() { return projectType; }
    public String getFrontendStack() { return frontendStack; }
    public String getBackendStack() { return backendStack; }
    public String getDatabaseType() { return databaseType; }
    public String getPageName() { return pageName; }
    public String getPageIntroduction() { return pageIntroduction; }
    public String getPageInterest() { return pageInterest; }
    public String getPageTheme() { return pageTheme; }
    public String getStyleAccent() { return styleAccent; }
    public int getStyleRadius() { return styleRadius; }
    public int getStyleSpacing() { return styleSpacing; }
    public boolean isStyleShadow() { return styleShadow; }
    public Instant getGraduatedAt() { return graduatedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
