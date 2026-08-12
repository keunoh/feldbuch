package io.github.kaltz.feldbuch.batch.tasklet;

import io.github.kaltz.feldbuch.ai.service.AiKnowledgeSummaryService;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.entity.ConversationMessage;
import io.github.kaltz.feldbuch.conversation.entity.ConversationRole;
import io.github.kaltz.feldbuch.conversation.entity.KnowledgeExtractStatus;
import io.github.kaltz.feldbuch.conversation.reader.KnowledgeConversationReader;
import io.github.kaltz.feldbuch.conversation.repository.ConversationMessageRepository;
import io.github.kaltz.feldbuch.conversation.repository.ConversationRepository;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBatchTest
@SpringBootTest(
        properties = {
                "spring.batch.job.enabled=false"
        }
)
@ActiveProfiles("test")
class KnowledgeExtractionTaskletIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("knowledgeExtractionJob")
    private Job knowledgeExtractionJob;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationMessageRepository conversationMessageRepository;

    /*
     * 추출 대상 조회 자체는 이 테스트의 관심사가 아니다.
     *
     * 우리가 만든 conversation 하나만 Tasklet에 전달해서
     * 트랜잭션 실패 격리 여부만 정확하게 검증한다.
     */
    @MockitoBean
    private KnowledgeConversationReader conversationReader;

    /*
     * 실제 OpenAI API는 호출하지 않는다.
     *
     * AI 요약 단계에서 의도적으로 예외를 발생시켜
     * KnowledgeExtractionService의 REQUIRES_NEW
     * 트랜잭션을 rollback시킨다.
     */
    @MockitoBean
    private AiKnowledgeSummaryService aiKnowledgeSummaryService;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(
                knowledgeExtractionJob
        );
    }

    @Test
    void 지식_추출이_실패해도_Step_트랜잭션은_정상적으로_완료된다()
            throws Exception {

        // given
        User user = User.builder()
                .email(
                        "batch-integration-"
                                + System.nanoTime()
                                + "@feldbuch.com"
                )
                .password("test-password")
                .nickname("batch-test")
                .build();

        userRepository.saveAndFlush(user);

        Conversation conversation =
                Conversation.create(
                        user,
                        "트랜잭션 테스트 대화"
                );

        conversation.complete();

        conversationRepository.saveAndFlush(
                conversation
        );

        ConversationMessage message =
                ConversationMessage.create(
                        conversation,
                        1,
                        ConversationRole.USER,
                        "HTTP에 대해서 알려줘"
                );

        conversationMessageRepository.saveAndFlush(
                message
        );

        /*
         * 실제 Repository의 추출 대상 조회 여부와 관계없이
         * 이번 테스트에서는 우리가 만든 대화 하나만 처리한다.
         */
        when(
                conversationReader
                        .findExtractionTargets()
        )
                .thenReturn(
                        List.of(conversation)
                );

        /*
         * KnowledgeExtractionService 내부에서
         * AI 요약을 호출하는 순간 예외를 발생시킨다.
         */
        when(
                aiKnowledgeSummaryService
                        .summarize(anyString())
        )
                .thenThrow(
                        new RuntimeException(
                                "AI 호출 실패"
                        )
                );

        JobParameters jobParameters =
                new JobParametersBuilder()
                        .addLong(
                                "executionTime",
                                System.currentTimeMillis()
                        )
                        .toJobParameters();

        // when
        JobExecution jobExecution =
                jobLauncherTestUtils.launchJob(
                        jobParameters
                );

        // then
        /*
         * 핵심 검증 1
         *
         * 내부 지식 추출은 실패했지만
         * REQUIRES_NEW로 분리되어 있기 때문에
         * Step 트랜잭션까지 rollback-only가 되면 안 된다.
         */
        assertThat(
                jobExecution.getStatus()
        )
                .isEqualTo(
                        BatchStatus.COMPLETED
                );

        assertThat(
                jobExecution
                        .getExitStatus()
                        .getExitCode()
        )
                .isEqualTo("COMPLETED");

        /*
         * 핵심 검증 2
         *
         * Step도 UnexpectedRollbackException 없이
         * 정상 완료되어야 한다.
         */
        assertThat(
                jobExecution.getStepExecutions()
        )
                .hasSize(1);

        StepExecution stepExecution =
                jobExecution
                        .getStepExecutions()
                        .iterator()
                        .next();

        assertThat(
                stepExecution.getStatus()
        )
                .isEqualTo(
                        BatchStatus.COMPLETED
                );

        /*
         * 핵심 검증 3
         *
         * Tasklet catch 블록에서 호출한
         * statusService.fail()의 REQUIRES_NEW 트랜잭션은
         * 정상 commit되어야 한다.
         */
        Conversation failedConversation =
                conversationRepository
                        .findById(
                                conversation.getId()
                        )
                        .orElseThrow();

        assertThat(
                failedConversation
                        .getKnowledgeExtractStatus()
        )
                .isEqualTo(
                        KnowledgeExtractStatus.FAILED
                );

        assertThat(
                failedConversation
                        .getKnowledgeExtractRetryCount()
        )
                .isEqualTo(1);

        assertThat(
                failedConversation
                        .getKnowledgeExtractErrorMessage()
        )
                .isEqualTo(
                        "AI 호출 실패"
                );

        assertThat(
                failedConversation
                        .getKnowledgeExtractFailedAt()
        )
                .isNotNull();
    }
}