package io.github.kaltz.feldbuch.knowledge.service;

import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeRootCategory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class KnowledgeRootCategoryResolver {

    private static final KnowledgeRootCategory DEFAULT_CATEGORY =
            KnowledgeRootCategory.COMPUTER_SCIENCE;

    private static final Map<KnowledgeRootCategory, Set<String>>
            CATEGORY_KEYWORDS =
            Map.ofEntries(
                    Map.entry(
                            KnowledgeRootCategory.WEB_DEVELOPMENT,
                            Set.of(
                                    "spring",
                                    "spring framework",
                                    "spring boot",
                                    "spring mvc",
                                    "spring webflux",
                                    "webflux",
                                    "react",
                                    "vue",
                                    "angular",
                                    "javascript",
                                    "typescript",
                                    "html",
                                    "css",
                                    "thymeleaf",
                                    "servlet",
                                    "jsp",
                                    "rest api",
                                    "oauth",
                                    "oidc"
                            )
                    ),
                    Map.entry(
                            KnowledgeRootCategory.PROGRAMMING_LANGUAGE,
                            Set.of(
                                    "java",
                                    "kotlin",
                                    "python",
                                    "javascript language",
                                    "typescript language",
                                    "c",
                                    "c++",
                                    "c#",
                                    "go",
                                    "rust",
                                    "stream api",
                                    "lambda",
                                    "generic"
                            )
                    ),
                    Map.entry(
                            KnowledgeRootCategory.DATABASE,
                            Set.of(
                                    "database",
                                    "db",
                                    "mysql",
                                    "postgresql",
                                    "oracle",
                                    "mssql",
                                    "redis",
                                    "jpa",
                                    "hibernate",
                                    "querydsl",
                                    "mybatis",
                                    "sql",
                                    "index",
                                    "transaction"
                            )
                    ),
                    Map.entry(
                            KnowledgeRootCategory.NETWORK,
                            Set.of(
                                    "network",
                                    "tcp",
                                    "udp",
                                    "http",
                                    "https",
                                    "dns",
                                    "ip",
                                    "socket",
                                    "websocket"
                            )
                    ),
                    Map.entry(
                            KnowledgeRootCategory.OPERATING_SYSTEM,
                            Set.of(
                                    "operating system",
                                    "os",
                                    "linux",
                                    "macos",
                                    "windows",
                                    "process",
                                    "thread",
                                    "memory",
                                    "filesystem"
                            )
                    ),
                    Map.entry(
                            KnowledgeRootCategory.CLOUD,
                            Set.of(
                                    "aws",
                                    "azure",
                                    "gcp",
                                    "cloud",
                                    "ec2",
                                    "s3",
                                    "rds",
                                    "lambda function"
                            )
                    ),
                    Map.entry(
                            KnowledgeRootCategory.DEVOPS,
                            Set.of(
                                    "docker",
                                    "kubernetes",
                                    "ci/cd",
                                    "github actions",
                                    "jenkins",
                                    "deployment",
                                    "monitoring",
                                    "terraform"
                            )
                    ),
                    Map.entry(
                            KnowledgeRootCategory.ARTIFICIAL_INTELLIGENCE,
                            Set.of(
                                    "ai",
                                    "artificial intelligence",
                                    "machine learning",
                                    "deep learning",
                                    "llm",
                                    "openai",
                                    "prompt",
                                    "embedding",
                                    "rag"
                            )
                    ),
                    Map.entry(
                            KnowledgeRootCategory.SECURITY,
                            Set.of(
                                    "security",
                                    "authentication",
                                    "authorization",
                                    "jwt",
                                    "oauth security",
                                    "encryption",
                                    "csrf",
                                    "xss",
                                    "csp"
                            )
                    ),
                    Map.entry(
                            KnowledgeRootCategory.COMPUTER_USAGE,
                            Set.of(
                                    "intellij",
                                    "vscode",
                                    "postman",
                                    "firefox",
                                    "mac",
                                    "shortcut",
                                    "terminal",
                                    "git command",
                                    "document editing"
                            )
                    ),
                    Map.entry(
                            KnowledgeRootCategory.COMMUNICATION,
                            Set.of(
                                    "communication",
                                    "question",
                                    "answer",
                                    "interview",
                                    "email",
                                    "message",
                                    "writing",
                                    "presentation"
                            )
                    )
            );

    public KnowledgeRootCategory resolve(
            List<String> knowledgePath
    ) {
        List<String> normalizedPath =
                normalizePath(
                        knowledgePath
                );

        String joinedPath =
                String.join(
                        " ",
                        normalizedPath
                );

        return CATEGORY_KEYWORDS.entrySet()
                .stream()
                .filter(entry ->
                        containsAnyKeyword(
                                joinedPath,
                                entry.getValue()
                        )
                )
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(
                        DEFAULT_CATEGORY
                );
    }

    private boolean containsAnyKeyword(
            String path,
            Set<String> keywords
    ) {
        return keywords.stream()
                .anyMatch(path::contains);
    }

    private List<String> normalizePath(
            List<String> knowledgePath
    ) {
        if (
                knowledgePath == null
                        || knowledgePath.isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "Knowledge 경로는 필수입니다."
            );
        }

        List<String> normalized =
                knowledgePath.stream()
                        .filter(name ->
                                name != null
                                        && !name.isBlank()
                        )
                        .map(name ->
                                name.trim()
                                        .toLowerCase(
                                                Locale.ROOT
                                        )
                        )
                        .toList();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    "Knowledge 경로에 유효한 이름이 없습니다."
            );
        }

        return normalized;
    }
}