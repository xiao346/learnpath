package com.learnpath.journey;

import com.learnpath.cache.JsonCache;
import com.learnpath.course.CourseService;
import com.learnpath.journey.JourneyDtos.FirstPageView;
import com.learnpath.journey.JourneyDtos.JourneyView;
import com.learnpath.journey.JourneyDtos.SaveFirstPageRequest;
import com.learnpath.journey.JourneyDtos.SaveJourneyRequest;
import com.learnpath.journey.JourneyDtos.SaveDeploymentRequest;
import com.learnpath.journey.JourneyDtos.SaveStyleRequest;
import com.learnpath.journey.JourneyDtos.SaveStageEvidenceRequest;
import com.learnpath.journey.JourneyDtos.StageEvidenceView;
import com.learnpath.journey.JourneyDtos.StyleView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Service
public class JourneyService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(15);
    private static final Set<String> PROJECTS = Set.of("portfolio", "blog", "campus");
    private static final Set<String> FRONTENDS = Set.of("vue", "vanilla");
    private static final Set<String> BACKENDS = Set.of("java", "python", "later");
    private static final Set<String> DATABASES = Set.of("mysql", "sqlite", "later");
    private static final Set<String> STAGES = Set.of(
            "intro", "style", "interaction", "framework", "publish", "backend", "database", "launch");
    private static final Set<String> SKIPPABLE_STAGES = Set.of(
            "intro", "style", "interaction", "framework", "publish", "backend", "database");

    private final WebJourneyRepository journeyRepository;
    private final JourneyStageProgressRepository stageRepository;
    private final JsonCache cache;
    private final CourseService courseService;

    public JourneyService(WebJourneyRepository journeyRepository,
                          JourneyStageProgressRepository stageRepository,
                          JsonCache cache,
                          CourseService courseService) {
        this.journeyRepository = journeyRepository;
        this.stageRepository = stageRepository;
        this.cache = cache;
        this.courseService = courseService;
    }

    @Transactional(readOnly = true)
    public JourneyView get(Long userId) {
        String key = cacheKey(userId);
        var cached = cache.get(key, JourneyView.class);
        if (cached.isPresent() && cached.get().skippedStages() != null && cached.get().stageEvidence() != null) return cached.get();
        List<JourneyStageProgress> stageProgress = stageRepository.findByUserIdOrderByCompletedAtAsc(userId);
        JourneyView view = journeyRepository.findByUserId(userId)
                .map(journey -> toView(journey, completedStageIds(stageProgress), skippedStageIds(stageProgress)))
                .orElseGet(() -> emptyView(completedStageIds(stageProgress), skippedStageIds(stageProgress)));
        cache.put(key, view, CACHE_TTL);
        return view;
    }

    @Transactional
    public JourneyView saveConfiguration(Long userId, SaveJourneyRequest request) {
        validateChoice(PROJECTS, request.project(), "网站主题");
        validateChoice(FRONTENDS, request.frontend(), "前端路线");
        validateChoice(BACKENDS, request.backend(), "后端路线");
        validateChoice(DATABASES, request.database(), "数据库路线");
        if (request.backend().equals("later") && !request.database().equals("later")) {
            throw new IllegalArgumentException("暂不使用后端时，数据库也应选择暂不使用");
        }
        WebJourney journey = findOrCreate(userId);
        invalidateChangedRouteStages(userId, journey, request);
        journey.configure(request.project(), request.frontend(), request.backend(), request.database());
        journeyRepository.save(journey);
        return refresh(userId);
    }

    @Transactional
    public JourneyView saveFirstPage(Long userId, SaveFirstPageRequest request) {
        WebJourney journey = findOrCreate(userId);
        journey.updateFirstPage(request.name().trim(), request.introduction().trim(), request.interest().trim(), request.theme());
        journeyRepository.save(journey);
        return refresh(userId);
    }

    @Transactional
    public JourneyView saveStyle(Long userId, SaveStyleRequest request) {
        WebJourney journey = findOrCreate(userId);
        journey.updateStyle(request.accent(), request.radius(), request.spacing(), request.shadow());
        journeyRepository.save(journey);
        return refresh(userId);
    }

    @Transactional
    public JourneyView saveDeployment(Long userId, SaveDeploymentRequest request) {
        WebJourney journey = findOrCreate(userId);
        if (!journey.getBackendStack().equals("later") && (request.apiUrl() == null || request.apiUrl().isBlank())) {
            throw new IllegalArgumentException("全栈路线还需要填写公开的后端接口地址");
        }
        journey.updateDeploymentUrl(request.deploymentUrl().trim(),
                request.apiUrl() == null || request.apiUrl().isBlank() ? null : request.apiUrl().trim());
        journeyRepository.save(journey);
        return refresh(userId);
    }

    @Transactional
    public JourneyView saveStageEvidence(Long userId, String stageId, SaveStageEvidenceRequest request) {
        WebJourney journey = findOrCreate(userId);
        validateRequiredStage(journey, stageId);
        ensurePreviousStagesResolved(userId, journey, stageId);
        journeyRepository.save(journey);
        JourneyStageProgress progress = stageRepository.findByUserIdAndStageId(userId, stageId)
                .orElseGet(() -> new JourneyStageProgress(userId, stageId, "IN_PROGRESS"));
        progress.saveEvidence(request.evidence().trim());
        stageRepository.save(progress);
        return refresh(userId);
    }

    @Transactional
    public JourneyView completeStage(Long userId, String stageId) {
        validateChoice(STAGES, stageId, "建站阶段");
        WebJourney journey = findOrCreate(userId);
        validateRequiredStage(journey, stageId);
        if ((stageId.equals("publish") || stageId.equals("launch"))
                && (journey.getDeploymentUrl() == null || journey.getDeploymentUrl().isBlank())) {
            throw new IllegalArgumentException("请先在发布站保存可以访问的真实网站地址");
        }
        if ((stageId.equals("publish") || stageId.equals("launch"))
                && !journey.getBackendStack().equals("later")
                && (journey.getApiUrl() == null || journey.getApiUrl().isBlank())) {
            throw new IllegalArgumentException("全栈路线还需要保存公开的后端接口地址");
        }
        ensurePreviousStagesResolved(userId, journey, stageId);
        String courseTitle = JourneyPlan.courseTitle(journey.getFrontendStack(), journey.getBackendStack(), stageId);
        JourneyStageProgress existingProgress = stageRepository.findByUserIdAndStageId(userId, stageId).orElse(null);
        if (courseTitle != null && !courseService.isCourseCompleted(userId, courseTitle)) {
            throw new IllegalArgumentException("请先完成“" + courseTitle + "”课程，再把知识应用到项目");
        }
        if (courseTitle != null && (existingProgress == null || existingProgress.getEvidence() == null
                || existingProgress.getEvidence().isBlank())) {
            throw new IllegalArgumentException("请先记录这一站应用到项目的结果");
        }
        if (stageId.equals("launch") && (existingProgress == null || existingProgress.getEvidence() == null
                || existingProgress.getEvidence().isBlank())) {
            throw new IllegalArgumentException("请先记录一条真实访客反馈和完成的修改");
        }
        journeyRepository.save(journey);
        JourneyStageProgress progress = java.util.Optional.ofNullable(existingProgress)
                .orElseGet(() -> new JourneyStageProgress(userId, stageId, "COMPLETED"));
        progress.markCompleted();
        stageRepository.save(progress);
        if (stageId.equals("launch")) {
            journey.graduate();
            journeyRepository.save(journey);
        }
        return refresh(userId);
    }

    @Transactional
    public JourneyView skipStage(Long userId, String stageId) {
        validateChoice(SKIPPABLE_STAGES, stageId, "可跳过的建站阶段");
        WebJourney journey = findOrCreate(userId);
        validateRequiredStage(journey, stageId);
        ensurePreviousStagesResolved(userId, journey, stageId);
        journeyRepository.save(journey);
        JourneyStageProgress progress = stageRepository.findByUserIdAndStageId(userId, stageId).orElse(null);
        if (progress == null) {
            stageRepository.save(new JourneyStageProgress(userId, stageId, "SKIPPED"));
        } else {
            progress.markSkipped();
            stageRepository.save(progress);
        }
        return refresh(userId);
    }

    private WebJourney findOrCreate(Long userId) {
        return journeyRepository.findByUserId(userId).orElseGet(() -> new WebJourney(userId));
    }

    private JourneyView refresh(Long userId) {
        cache.evict(cacheKey(userId));
        WebJourney journey = journeyRepository.findByUserId(userId).orElseThrow();
        List<JourneyStageProgress> stageProgress = stageRepository.findByUserIdOrderByCompletedAtAsc(userId);
        JourneyView view = toView(journey, completedStageIds(stageProgress), skippedStageIds(stageProgress));
        cache.put(cacheKey(userId), view, CACHE_TTL);
        return view;
    }

    private List<String> completedStageIds(List<JourneyStageProgress> stageProgress) {
        return stageProgress.stream()
                .filter(JourneyStageProgress::isCompleted)
                .map(JourneyStageProgress::getStageId)
                .toList();
    }

    private List<String> skippedStageIds(List<JourneyStageProgress> stageProgress) {
        return stageProgress.stream()
                .filter(JourneyStageProgress::isSkipped)
                .map(JourneyStageProgress::getStageId)
                .toList();
    }

    private JourneyView toView(WebJourney journey, List<String> completedStages, List<String> skippedStages) {
        List<StageEvidenceView> evidence = stageRepository.findByUserIdOrderByCompletedAtAsc(journey.getUserId()).stream()
                .filter(progress -> progress.getEvidence() != null && !progress.getEvidence().isBlank())
                .map(progress -> new StageEvidenceView(progress.getStageId(), progress.getEvidence()))
                .toList();
        return new JourneyView(true, journey.getProjectType(), journey.getFrontendStack(), journey.getBackendStack(),
                journey.getDatabaseType(),
                new FirstPageView(journey.getPageName(), journey.getPageIntroduction(), journey.getPageInterest(), journey.getPageTheme()),
                new StyleView(journey.getStyleAccent(), journey.getStyleRadius(), journey.getStyleSpacing(), journey.isStyleShadow()),
                journey.getDeploymentUrl(), journey.getApiUrl(),
                completedStages, skippedStages, evidence, journey.getGraduatedAt(), journey.getUpdatedAt());
    }

    private JourneyView emptyView(List<String> completedStages, List<String> skippedStages) {
        return new JourneyView(false, "portfolio", "vue", "java", "mysql",
                new FirstPageView("小途", "一名正在探索 Web 世界的大一学生。", "我喜欢摄影、音乐，也喜欢把新点子做出来。", "blue"),
                new StyleView("#5b72f2", 18, 24, true), null, null, completedStages, skippedStages, List.of(), null, null);
    }

    private void validateRequiredStage(WebJourney journey, String stageId) {
        if (!JourneyPlan.requiredStages(journey.getFrontendStack(), journey.getBackendStack(), journey.getDatabaseType()).contains(stageId)) {
            throw new IllegalArgumentException("当前技术路线不包含这个建站阶段");
        }
    }

    private void invalidateChangedRouteStages(Long userId, WebJourney journey, SaveJourneyRequest request) {
        Set<String> invalidated;
        if (!journey.getProjectType().equals(request.project())) {
            invalidated = STAGES;
        } else if (!journey.getFrontendStack().equals(request.frontend())) {
            invalidated = Set.of("framework", "backend", "database", "publish", "launch");
        } else if (!journey.getBackendStack().equals(request.backend())) {
            invalidated = Set.of("backend", "database", "publish", "launch");
        } else if (!journey.getDatabaseType().equals(request.database())) {
            invalidated = Set.of("database", "publish", "launch");
        } else {
            return;
        }
        stageRepository.findByUserIdOrderByCompletedAtAsc(userId).stream()
                .filter(progress -> invalidated.contains(progress.getStageId()))
                .forEach(JourneyStageProgress::invalidate);
        journey.invalidateRelease();
    }

    private void ensurePreviousStagesResolved(Long userId, WebJourney journey, String stageId) {
        List<String> required = JourneyPlan.requiredStages(
                journey.getFrontendStack(), journey.getBackendStack(), journey.getDatabaseType());
        int stageIndex = required.indexOf(stageId);
        if (stageIndex < 1) return;
        Set<String> resolved = stageRepository.findByUserIdOrderByCompletedAtAsc(userId).stream()
                .filter(progress -> progress.isCompleted() || progress.isSkipped())
                .map(JourneyStageProgress::getStageId)
                .collect(java.util.stream.Collectors.toSet());
        for (String previous : required.subList(0, stageIndex)) {
            if (!resolved.contains(previous)) {
                throw new IllegalArgumentException("请先完成或跳过上一站，再继续当前阶段");
            }
        }
    }

    private void validateChoice(Set<String> allowed, String value, String label) {
        if (value == null || !allowed.contains(value)) throw new IllegalArgumentException(label + "选择无效");
    }

    private String cacheKey(Long userId) {
        return "journey:" + userId;
    }
}
