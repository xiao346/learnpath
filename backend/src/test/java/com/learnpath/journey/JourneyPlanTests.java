package com.learnpath.journey;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JourneyPlanTests {

    @Test
    void allFortyTwoProjectAndStackRoutesKeepDependenciesBeforePublishing() {
        List<String> projects = List.of("portfolio", "blog", "campus");
        List<String> frontends = List.of("vanilla", "vue");
        List<String> backends = List.of("later", "java", "python");
        List<String> databases = List.of("later", "mysql", "sqlite");
        int routes = 0;

        for (String project : projects) {
            for (String frontend : frontends) {
                for (String backend : backends) {
                    for (String database : databases) {
                        if (backend.equals("later") && !database.equals("later")) continue;
                        List<String> stages = JourneyPlan.requiredStages(frontend, backend, database);
                        routes++;

                        assertThat(project).isNotBlank();
                        assertThat(stages).startsWith("intro", "style", "interaction");
                        assertThat(stages).endsWith("publish", "launch");
                        assertThat(stages.contains("framework")).isEqualTo(frontend.equals("vue"));
                        assertThat(stages.contains("backend")).isEqualTo(!backend.equals("later"));
                        assertThat(stages.contains("database"))
                                .isEqualTo(!backend.equals("later") && !database.equals("later"));
                        if (stages.contains("backend")) {
                            assertThat(stages.indexOf("backend")).isLessThan(stages.indexOf("publish"));
                        }
                        if (stages.contains("database")) {
                            assertThat(stages.indexOf("database")).isLessThan(stages.indexOf("publish"));
                        }
                    }
                }
            }
        }

        assertThat(routes).isEqualTo(42);
    }
}
