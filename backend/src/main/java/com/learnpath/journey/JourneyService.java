package com.learnpath.journey;

import com.learnpath.cache.JsonCache;
import com.learnpath.journey.JourneyDtos.FirstPageView;
import com.learnpath.journey.JourneyDtos.JourneyView;
import com.learnpath.journey.JourneyDtos.SaveFirstPageRequest;
import com.learnpath.journey.JourneyDtos.SaveJourneyRequest;
import com.learnpath.journey.JourneyDtos.SaveStyleRequest;
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

    public JourneyService(WebJourneyRepository journeyRepository,
                          JourneyStageProgressRepository stageRepository,
                          JsonCache cache) {
        this.journeyRepository = journeyRepository;
        this.stageRepository = stageRepository;
        this.cache = cache;
    }

    @Transactional(readOnly = true)
    public JourneyView get(Long userId) {
        String key = cacheKey(userId);
        var cached = cache.get(key, JourneyView.class);
        if (cached.isPresent() && cached.get().skippedStages() != null) return cached.get();
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
    public JourneyView completeStage(Long userId, String stageId) {
        validateChoice(STAGES, stageId, "建站阶段");
        WebJourney journey = findOrCreate(userId);
        journeyRepository.save(journey);
        JourneyStageProgress progress = stageRepository.findByUserIdAndStageId(userId, stageId)
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
        journeyRepository.save(journey);
        JourneyStageProgress progress = stageRepository.findByUserIdAndStageId(userId, stageId).orElse(null);
        if (progress == null) stageRepository.save(new JourneyStageProgress(userId, stageId, "SKIPPED"));
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
        return new JourneyView(true, journey.getProjectType(), journey.getFrontendStack(), journey.getBackendStack(),
                journey.getDatabaseType(),
                new FirstPageView(journey.getPageName(), journey.getPageIntroduction(), journey.getPageInterest(), journey.getPageTheme()),
                new StyleView(journey.getStyleAccent(), journey.getStyleRadius(), journey.getStyleSpacing(), journey.isStyleShadow()),
                completedStages, skippedStages, journey.getGraduatedAt(), journey.getUpdatedAt());
    }

    private JourneyView emptyView(List<String> completedStages, List<String> skippedStages) {
        return new JourneyView(false, "portfolio", "vue", "java", "mysql",
                new FirstPageView("小途", "一名正在探索 Web 世界的大一学生。", "我喜欢摄影、音乐，也喜欢把新点子做出来。", "blue"),
                new StyleView("#5b72f2", 18, 24, true), completedStages, skippedStages, null, null);
    }

    private void validateChoice(Set<String> allowed, String value, String label) {
        if (value == null || !allowed.contains(value)) throw new IllegalArgumentException(label + "选择无效");
    }

    private String cacheKey(Long userId) {
        return "journey:" + userId;
    }
}
