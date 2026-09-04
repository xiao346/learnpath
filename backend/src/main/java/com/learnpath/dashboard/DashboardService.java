package com.learnpath.dashboard;

import com.learnpath.course.Chapter;
import com.learnpath.course.Course;
import com.learnpath.course.CourseRepository;
import com.learnpath.course.LearningProgress;
import com.learnpath.course.LearningProgressRepository;
import com.learnpath.dashboard.DashboardDtos.DashboardView;
import com.learnpath.dashboard.DashboardDtos.DayView;
import com.learnpath.dashboard.DashboardDtos.FocusView;
import com.learnpath.dashboard.DashboardDtos.RecommendationView;
import com.learnpath.dashboard.DashboardDtos.TaskView;
import com.learnpath.dashboard.DashboardDtos.TrendView;
import com.learnpath.practice.PracticeAttempt;
import com.learnpath.practice.PracticeAttemptRepository;
import com.learnpath.practice.PracticeQuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final int WEEKLY_GOAL_MINUTES = 600;
    private static final List<String> DAY_LABELS = List.of("一", "二", "三", "四", "五", "六", "日");

    private final CourseRepository courseRepository;
    private final LearningProgressRepository progressRepository;
    private final StudyTaskRepository taskRepository;
    private final StudySessionRepository sessionRepository;
    private final PracticeAttemptRepository attemptRepository;
    private final PracticeQuestionRepository questionRepository;

    public DashboardService(
            CourseRepository courseRepository,
            LearningProgressRepository progressRepository,
            StudyTaskRepository taskRepository,
            StudySessionRepository sessionRepository,
            PracticeAttemptRepository attemptRepository,
            PracticeQuestionRepository questionRepository
    ) {
        this.courseRepository = courseRepository;
        this.progressRepository = progressRepository;
        this.taskRepository = taskRepository;
        this.sessionRepository = sessionRepository;
        this.attemptRepository = attemptRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional(readOnly = true)
    public DashboardView dashboard(Long userId) {
        LocalDate today = LocalDate.now();
        List<StudyTask> tasks = taskRepository.findByUserIdAndTaskDateOrderByIdAsc(userId, today);
        List<TaskView> taskViews = tasks.stream().map(this::toTaskView).toList();
        int tasksCompleted = (int) tasks.stream().filter(StudyTask::isCompleted).count();
        FocusView focus = focus(userId);
        TrendView trend = trend(userId, today);
        int weeklyGoalPercent = Math.min(100, Math.round(trend.totalMinutes() * 100f / WEEKLY_GOAL_MINUTES));
        int weeklyRemainingMinutes = Math.max(0, WEEKLY_GOAL_MINUTES - trend.totalMinutes());

        return new DashboardView(
                focus,
                streak(userId, today),
                tasksCompleted,
                tasks.size(),
                taskViews,
                trend,
                weeklyGoalPercent,
                weeklyRemainingMinutes,
                recommendation(userId, focus));
    }

    @Transactional
    public DashboardView toggleTask(Long userId, Long taskId) {
        StudyTask task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("学习任务不存在"));
        task.toggle();
        taskRepository.save(task);
        return dashboard(userId);
    }

    private FocusView focus(Long userId) {
        List<Course> courses = courseRepository.findAllByPublishedTrueOrderByIdAsc();
        if (courses.isEmpty()) {
            return new FocusView(null, "暂无课程", "课程资源正在准备中", 0, 0, 0, 0);
        }
        Map<Long, Course> courseById = courses.stream().collect(Collectors.toMap(Course::getId, Function.identity()));
        List<LearningProgress> progresses = progressRepository.findByUserIdOrderByLastStudiedAtDesc(userId);
        LearningProgress selectedProgress = progresses.stream()
                .filter(progress -> courseById.containsKey(progress.getCourseId()))
                .filter(progress -> progress.getCompletedLessons() < courseById.get(progress.getCourseId()).getChapters().size())
                .findFirst()
                .orElse(null);
        Course course = selectedProgress == null ? courses.getFirst() : courseById.get(selectedProgress.getCourseId());
        int completed = selectedProgress == null ? 0 : selectedProgress.getCompletedLessons();
        if (course.getChapters().isEmpty()) {
            return new FocusView(course.getId(), course.getTitle(), "课程章节正在准备中", 0, 0, 0, 0);
        }
        Chapter nextChapter = course.getChapters().stream()
                .filter(chapter -> chapter.getOrderIndex() > completed)
                .findFirst()
                .orElse(course.getChapters().getLast());
        int totalLessons = course.getChapters().size();
        int progressPercent = totalLessons == 0 ? 0 : Math.round(completed * 100f / totalLessons);
        return new FocusView(
                course.getId(), course.getTitle(), nextChapter.getTitle(), nextChapter.getDurationMinutes(),
                completed, totalLessons, progressPercent);
    }

    private TrendView trend(Long userId, LocalDate today) {
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        List<StudySession> current = sessionRepository
                .findByUserIdAndStudyDateBetweenOrderByStudyDateAsc(userId, weekStart, weekEnd);
        List<StudySession> previous = sessionRepository
                .findByUserIdAndStudyDateBetweenOrderByStudyDateAsc(userId, weekStart.minusWeeks(1), weekEnd.minusWeeks(1));
        Map<LocalDate, Integer> minutesByDate = current.stream().collect(Collectors.groupingBy(
                StudySession::getStudyDate, Collectors.summingInt(StudySession::getMinutes)));
        List<DayView> days = new ArrayList<>();
        for (int index = 0; index < 7; index++) {
            LocalDate date = weekStart.plusDays(index);
            int minutes = minutesByDate.getOrDefault(date, 0);
            days.add(new DayView(date, DAY_LABELS.get(index), minutes, minutes > 0, date.equals(today)));
        }
        int totalMinutes = current.stream().mapToInt(StudySession::getMinutes).sum();
        int previousMinutes = previous.stream().mapToInt(StudySession::getMinutes).sum();
        int changePercent = previousMinutes == 0
                ? (totalMinutes == 0 ? 0 : 100)
                : Math.round((totalMinutes - previousMinutes) * 100f / previousMinutes);
        return new TrendView(totalMinutes, previousMinutes, changePercent, days);
    }

    private int streak(Long userId, LocalDate today) {
        List<StudySession> sessions = sessionRepository.findByUserIdAndStudyDateBetweenOrderByStudyDateAsc(
                userId, today.minusDays(365), today);
        Set<LocalDate> activeDates = sessions.stream().map(StudySession::getStudyDate).collect(Collectors.toSet());
        int streak = 0;
        LocalDate cursor = activeDates.contains(today) ? today : today.minusDays(1);
        while (activeDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private RecommendationView recommendation(Long userId, FocusView focus) {
        PracticeAttempt latestWrong = attemptRepository.findByUserIdOrderByAnsweredAtDesc(userId).stream()
                .filter(attempt -> !attempt.isCorrect())
                .max(Comparator.comparing(PracticeAttempt::getAnsweredAt))
                .orElse(null);
        if (latestWrong != null) {
            String subject = questionRepository.findById(latestWrong.getQuestionId())
                    .map(question -> question.getSubject())
                    .orElse("核心知识");
            Course matched = courseRepository.findAllByPublishedTrueOrderByIdAsc().stream()
                    .filter(course -> course.getTitle().contains(subject) || subject.contains(course.getTitle()))
                    .findFirst()
                    .orElse(null);
            if (matched != null) {
                return new RecommendationView(
                        subject + "错题巩固",
                        "根据最近一次错题记录，建议回到课程资源复习相关概念后再次练习。",
                        "/courses/" + matched.getId());
            }
        }
        String route = focus.courseId() == null ? "/courses" : "/courses/" + focus.courseId();
        return new RecommendationView(
                focus.courseTitle() + "继续学习",
                "根据当前学习进度，为你推荐下一未完成章节及配套官方资料。",
                route);
    }

    private TaskView toTaskView(StudyTask task) {
        return new TaskView(
                task.getId(), task.getTitle(), task.getSubject(), task.getEstimatedMinutes(),
                task.getXpReward(), task.isCompleted());
    }
}
