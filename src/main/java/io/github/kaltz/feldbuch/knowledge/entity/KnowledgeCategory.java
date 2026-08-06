package io.github.kaltz.feldbuch.knowledge.entity;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum KnowledgeCategory {

    SPRING(
            KnowledgeRootCategory.WEB_DEVELOPMENT,
            "Spring"
    ),

    SPRING_BOOT(
            KnowledgeRootCategory.WEB_DEVELOPMENT,
            "Spring Boot"
    ),

    SPRING_BATCH(
            KnowledgeRootCategory.WEB_DEVELOPMENT,
            "Spring Batch"
    ),

    SPRING_SECURITY(
            KnowledgeRootCategory.WEB_DEVELOPMENT,
            "Spring Security"
    ),

    SPRING_WEBFLUX(
            KnowledgeRootCategory.WEB_DEVELOPMENT,
            "Spring WebFlux"
    ),

    VUE(
            KnowledgeRootCategory.WEB_DEVELOPMENT,
            "Vue"
    ),

    REACT(
            KnowledgeRootCategory.WEB_DEVELOPMENT,
            "React"
    ),

    JAVASCRIPT(
            KnowledgeRootCategory.PROGRAMMING_LANGUAGE,
            "JavaScript"
    ),

    TYPESCRIPT(
            KnowledgeRootCategory.PROGRAMMING_LANGUAGE,
            "TypeScript"
    ),

    JAVA(
            KnowledgeRootCategory.PROGRAMMING_LANGUAGE,
            "Java"
    ),

    KOTLIN(
            KnowledgeRootCategory.PROGRAMMING_LANGUAGE,
            "Kotlin"
    ),

    PYTHON(
            KnowledgeRootCategory.PROGRAMMING_LANGUAGE,
            "Python"
    ),

    JPA(
            KnowledgeRootCategory.DATABASE,
            "JPA"
    ),

    QUERYDSL(
            KnowledgeRootCategory.DATABASE,
            "QueryDSL"
    ),

    MYSQL(
            KnowledgeRootCategory.DATABASE,
            "MySQL"
    ),

    ORACLE(
            KnowledgeRootCategory.DATABASE,
            "Oracle"
    ),

    MSSQL(
            KnowledgeRootCategory.DATABASE,
            "MSSQL"
    ),

    REDIS(
            KnowledgeRootCategory.DATABASE,
            "Redis"
    ),

    SQL(
            KnowledgeRootCategory.DATABASE,
            "SQL"
    ),

    DOCKER(
            KnowledgeRootCategory.DEVOPS,
            "Docker"
    ),

    KUBERNETES(
            KnowledgeRootCategory.DEVOPS,
            "Kubernetes"
    ),

    CI_CD(
            KnowledgeRootCategory.DEVOPS,
            "CI/CD"
    ),

    GITHUB_ACTIONS(
            KnowledgeRootCategory.DEVOPS,
            "GitHub Actions"
    ),

    AWS(
            KnowledgeRootCategory.CLOUD,
            "AWS"
    ),

    NETWORK_GENERAL(
            KnowledgeRootCategory.NETWORK,
            "Network"
    ),

    OPERATING_SYSTEM_GENERAL(
            KnowledgeRootCategory.OPERATING_SYSTEM,
            "Operating System"
    ),

    SECURITY_GENERAL(
            KnowledgeRootCategory.SECURITY,
            "Security"
    ),

    ARTIFICIAL_INTELLIGENCE_GENERAL(
            KnowledgeRootCategory.ARTIFICIAL_INTELLIGENCE,
            "Artificial Intelligence"
    ),

    COMPUTER_USAGE_GENERAL(
            KnowledgeRootCategory.COMPUTER_USAGE,
            "Computer Usage"
    ),

    COMPUTER_SCIENCE_GENERAL(
            KnowledgeRootCategory.COMPUTER_SCIENCE,
            "Computer Science"
    ),

    COMMUNICATION_GENERAL(
            KnowledgeRootCategory.COMMUNICATION,
            "Communication"
    );

    private final KnowledgeRootCategory rootCategory;
    private final String displayName;

    KnowledgeCategory(
            KnowledgeRootCategory rootCategory,
            String displayName
    ) {
        this.rootCategory = rootCategory;
        this.displayName = displayName;
    }

    public KnowledgeRootCategory getRootCategory() {
        return rootCategory;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static KnowledgeCategory fromName(
            String name
    ) {
        if (
                name == null
                        || name.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "지식 카테고리는 필수입니다."
            );
        }

        String normalized =
                name.trim();

        return Arrays.stream(values())
                .filter(category ->
                        category.name()
                                .equalsIgnoreCase(
                                        normalized
                                )
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "지원하지 않는 지식 카테고리입니다: "
                                        + name
                        )
                );
    }

    public static String toPromptList() {
        return Arrays.stream(values())
                .map(category ->
                        "- %s: %s"
                                .formatted(
                                        category.name(),
                                        category.displayName
                                )
                )
                .collect(
                        Collectors.joining(
                                System.lineSeparator()
                        )
                );
    }
}