package com.learnpath;

import com.learnpath.game.GameDtos.GameProgressView;
import com.learnpath.game.GameService;
import com.learnpath.journey.JourneyDtos.JourneyView;
import com.learnpath.journey.JourneyDtos.SaveFirstPageRequest;
import com.learnpath.journey.JourneyDtos.SaveJourneyRequest;
import com.learnpath.journey.JourneyDtos.SaveStyleRequest;
import com.learnpath.journey.JourneyService;
import com.learnpath.user.User;
import com.learnpath.user.UserRepository;
import com.learnpath.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserStatePersistenceTests {

    @Autowired private UserRepository userRepository;
    @Autowired private JourneyService journeyService;
    @Autowired private GameService gameService;

    @Test
    void journeyConfigurationArtifactsAndStagesPersist() {
        Long userId = student().getId();
        journeyService.saveConfiguration(userId, new SaveJourneyRequest("blog", "vue", "python", "sqlite"));
        journeyService.saveFirstPage(userId, new SaveFirstPageRequest("小林", "我在学习 Web。", "喜欢摄影和校园生活。", "green"));
        journeyService.saveStyle(userId, new SaveStyleRequest("#32ad83", 24, 30, true));
        journeyService.completeStage(userId, "intro");
        JourneyView view = journeyService.completeStage(userId, "intro");

        assertThat(view.configured()).isTrue();
        assertThat(view.project()).isEqualTo("blog");
        assertThat(view.backend()).isEqualTo("python");
        assertThat(view.firstPage().name()).isEqualTo("小林");
        assertThat(view.style().radius()).isEqualTo(24);
        assertThat(view.completedStages()).containsExactly("intro");
    }

    @Test
    void gameChallengeAwardsPointsOnlyOnce() {
        Long userId = student().getId();
        gameService.complete(userId, "layout-0");
        GameProgressView view = gameService.complete(userId, "layout-0");

        assertThat(view.totalScore()).isEqualTo(100);
        assertThat(view.completedChallenges()).containsExactly("layout-0");
        assertThat(view.totalChallenges()).isEqualTo(9);
    }

    private User student() {
        return userRepository.findByAccountAndRole("20240001", UserRole.STUDENT).orElseThrow();
    }
}
