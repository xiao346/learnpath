package com.learnpath;

import com.learnpath.auth.LoginResponse;
import com.learnpath.community.CommunityDtos.CommunityPostView;
import com.learnpath.community.CommunityDtos.CreateCommunityCommentRequest;
import com.learnpath.community.CommunityDtos.CreateCommunityPostRequest;
import com.learnpath.community.CommunityService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserStatePersistenceTests {

    @Autowired private UserRepository userRepository;
    @Autowired private JourneyService journeyService;
    @Autowired private GameService gameService;
    @Autowired private CommunityService communityService;

    @Test
    void journeyConfigurationArtifactsAndStagesPersist() {
        Long userId = student().getId();
        journeyService.saveConfiguration(userId, new SaveJourneyRequest("blog", "vue", "python", "sqlite"));
        journeyService.saveFirstPage(userId, new SaveFirstPageRequest("小林", "我在学习 Web。", "喜欢摄影和校园生活。", "green"));
        journeyService.saveStyle(userId, new SaveStyleRequest("#32ad83", 24, 30, true));
        journeyService.completeStage(userId, "intro");
        JourneyView view = journeyService.completeStage(userId, "intro");
        JourneyView skipped = journeyService.skipStage(userId, "style");
        JourneyView resumed = journeyService.completeStage(userId, "style");

        assertThat(view.configured()).isTrue();
        assertThat(view.project()).isEqualTo("blog");
        assertThat(view.backend()).isEqualTo("python");
        assertThat(view.firstPage().name()).isEqualTo("小林");
        assertThat(view.style().radius()).isEqualTo(24);
        assertThat(view.completedStages()).containsExactly("intro");
        assertThat(skipped.skippedStages()).containsExactly("style");
        assertThat(resumed.completedStages()).containsExactly("intro", "style");
        assertThat(resumed.skippedStages()).isEmpty();
    }

    @Test
    void gameChallengeAwardsPointsOnlyOnce() {
        Long userId = student().getId();
        gameService.complete(userId, "layout-0");
        GameProgressView view = gameService.complete(userId, "layout-0");

        assertThat(view.totalScore()).isEqualTo(100);
        assertThat(view.completedChallenges()).containsExactly("layout-0");
        assertThat(view.totalChallenges()).isEqualTo(21);
    }

    @Test
    void communityPostKeepsAuthorJourneyStackAndWebsite() {
        User student = student();
        journeyService.saveConfiguration(student.getId(), new SaveJourneyRequest("portfolio", "vue", "java", "mysql"));
        LoginResponse.UserView author = new LoginResponse.UserView(
                student.getId(), student.getAccount(), student.getDisplayName(), student.getRole());

        MockMultipartFile screenshot = new MockMultipartFile(
                "images", "home.png", "image/png", new byte[]{1, 2, 3, 4});
        CommunityPostView published = communityService.publish(author, new CreateCommunityPostRequest(
                "WEBSITE", "我的第一个课程网站", "我完成了首页和后端接口，准备继续优化手机端体验。", "https://example.com/my-site"),
                java.util.List.of(screenshot));

        assertThat(published.authorName()).isEqualTo("林知夏");
        assertThat(published.stackSummary()).contains("Vue 3", "Spring Boot", "MySQL");
        assertThat(published.imageUrls()).hasSize(1);
        String[] imagePath = published.imageUrls().getFirst().split("/");
        var storedImage = communityService.image(published.id(), Long.valueOf(imagePath[imagePath.length - 1]));
        assertThat(storedImage.contentType()).isEqualTo("image/png");
        assertThat(storedImage.data()).containsExactly(1, 2, 3, 4);
        var like = communityService.toggleLike(student.getId(), published.id());
        var comment = communityService.comment(author, published.id(),
                new CreateCommunityCommentRequest("配色很舒服，可以再补一张手机端截图。"));

        assertThat(like.liked()).isTrue();
        assertThat(like.likeCount()).isEqualTo(1);
        assertThat(comment.authorName()).isEqualTo("林知夏");
        assertThat(communityService.list("WEBSITE", student.getId()).posts())
                .extracting(CommunityPostView::title)
                .contains("我的第一个课程网站");
        CommunityPostView feedPost = communityService.list("WEBSITE", student.getId()).posts().stream()
                .filter(post -> post.id().equals(published.id()))
                .findFirst().orElseThrow();
        assertThat(feedPost.imageUrls()).containsExactly(published.imageUrls().getFirst());
        assertThat(feedPost.likedByCurrentUser()).isTrue();
        assertThat(feedPost.likeCount()).isEqualTo(1);
        assertThat(feedPost.comments()).extracting(item -> item.content())
                .containsExactly("配色很舒服，可以再补一张手机端截图。");
    }

    private User student() {
        return userRepository.findByAccountAndRole("20240001", UserRole.STUDENT).orElseThrow();
    }
}
