package com.learnpath.journey;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public final class JourneyDtos {

    private JourneyDtos() {
    }

    public record JourneyView(
            boolean configured,
            String project,
            String frontend,
            String backend,
            String database,
            FirstPageView firstPage,
            StyleView style,
            String deploymentUrl,
            String apiUrl,
            List<String> completedStages,
            List<String> skippedStages,
            List<StageEvidenceView> stageEvidence,
            Instant graduatedAt,
            Instant updatedAt
    ) {
    }

    public record FirstPageView(String name, String introduction, String interest, String theme) {
    }

    public record StyleView(String accent, int radius, int spacing, boolean shadow) {
    }

    public record StageEvidenceView(String stageId, String evidence) {
    }

    public record SaveJourneyRequest(
            @NotBlank String project,
            @NotBlank String frontend,
            @NotBlank String backend,
            @NotBlank String database
    ) {
    }

    public record SaveFirstPageRequest(
            @NotBlank @Size(max = 32) String name,
            @NotBlank @Size(max = 100) String introduction,
            @NotBlank @Size(max = 180) String interest,
            @NotBlank @Pattern(regexp = "blue|orange|green") String theme
    ) {
    }

    public record SaveStyleRequest(
            @NotBlank @Pattern(regexp = "#[0-9a-fA-F]{6}") String accent,
            @Min(0) @Max(34) int radius,
            @Min(12) @Max(42) int spacing,
            boolean shadow
    ) {
    }

    public record SaveDeploymentRequest(
            @NotBlank @Size(max = 500) @Pattern(regexp = "https?://.+", message = "请输入以 http:// 或 https:// 开头的网址") String deploymentUrl,
            @Size(max = 500) @Pattern(regexp = "https?://.+", message = "接口地址需要以 http:// 或 https:// 开头") String apiUrl
    ) {
    }

    public record SaveStageEvidenceRequest(
            @NotBlank @Size(min = 20, max = 600) String evidence
    ) {
    }
}
