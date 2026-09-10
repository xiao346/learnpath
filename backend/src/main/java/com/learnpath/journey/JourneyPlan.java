package com.learnpath.journey;

import java.util.ArrayList;
import java.util.List;

public final class JourneyPlan {

    private JourneyPlan() {
    }

    public static List<String> requiredStages(String frontend, String backend, String database) {
        List<String> stages = new ArrayList<>(List.of("intro", "style", "interaction"));
        if ("vue".equals(frontend)) stages.add("framework");
        if (!"later".equals(backend)) {
            stages.add("backend");
            if (!"later".equals(database)) stages.add("database");
        }
        stages.add("publish");
        stages.add("launch");
        return List.copyOf(stages);
    }

    public static String courseTitle(String frontend, String backend, String stageId) {
        return switch (stageId) {
            case "framework" -> "vue".equals(frontend) ? "Vue 3 前端开发" : null;
            case "backend" -> "python".equals(backend) ? "FastAPI 后端开发"
                    : "java".equals(backend) ? "Java Web 应用开发" : null;
            case "database" -> "数据库原理";
            default -> null;
        };
    }
}
