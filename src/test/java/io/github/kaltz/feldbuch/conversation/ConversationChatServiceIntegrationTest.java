package io.github.kaltz.feldbuch.conversation;

import io.github.kaltz.feldbuch.ai.dto.ChatRequest;
import io.github.kaltz.feldbuch.ai.model.ChatResponse;
import io.github.kaltz.feldbuch.ai.model.TitleResponse;
import io.github.kaltz.feldbuch.ai.service.ChatService;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.entity.ConversationMessage;
import io.github.kaltz.feldbuch.conversation.entity.ConversationRole;
import io.github.kaltz.feldbuch.conversation.repository.ConversationMessageRepository;
import io.github.kaltz.feldbuch.conversation.repository.ConversationRepository;
import io.github.kaltz.feldbuch.conversation.service.ConversationChatService;
import io.github.kaltz.feldbuch.support.IntegrationTestSupport;
import io.github.kaltz.feldbuch.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

class ConversationChatServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private ConversationChatService conversationChatService;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationMessageRepository conversationMessageRepository;

    @MockitoBean
    private ChatService chatService;

    @Test
    @DisplayName("기본 제목이면 AI가 제목을 생성한다.")
    void generateTitle() {

        // given
        User user = testDataFactory.createUser();

        Conversation conversation = testDataFactory.createConversation(user);

        given(chatService.chat(any()))
                .willReturn(new ChatResponse("AI 응답"));

        given(chatService.generateTitle(any()))
                .willReturn(new TitleResponse("Spring Security"));

        // when
        ChatResponse response = conversationChatService.chat(
                user.getId(),
                conversation.getId(),
                new ChatRequest("Spring Security가 뭐야?")
        );

        // then
        assertThat(response.content())
                .isEqualTo("AI 응답");

        Conversation updated = conversationRepository.findById(conversation.getId())
                .orElseThrow();

        assertThat(updated.getTitle())
                .isEqualTo("Spring Security");

        List<ConversationMessage> messages =
                conversationMessageRepository.findAllByConversationIdOrderBySequenceAsc(
                        conversation.getId()
                );

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getRole())
                .isEqualTo(ConversationRole.USER);
        assertThat(messages.get(0).getContent())
                .isEqualTo("Spring Security가 뭐야?");
        assertThat(messages.get(1).getRole())
                .isEqualTo(ConversationRole.ASSISTANT);
        assertThat(messages.get(1).getContent())
                .isEqualTo("AI 응답");

        then(chatService)
                .should()
                .generateTitle(any());
    }

    @Test
    @DisplayName("제목 생성에 실패해도 채팅은 정상 동작한다.")
    void titleGenerationFailureShouldNotFailChat() {

        // given
        User user = testDataFactory.createUser();

        Conversation conversation = testDataFactory.createConversation(user);

        given(chatService.chat(any()))
                .willReturn(new ChatResponse("AI 응답"));

        given(chatService.generateTitle(any()))
                .willThrow(new RuntimeException("OpenAI Error"));

        // when
        ChatResponse response =
                conversationChatService.chat(
                        user.getId(),
                        conversation.getId(),
                        new ChatRequest("Docker")
                );

        // then
        assertThat(response.content())
                .isEqualTo("AI 응답");

        Conversation updated =
                conversationRepository.findById(conversation.getId())
                        .orElseThrow();

        assertThat(updated.getTitle())
                .isEqualTo(Conversation.DEFAULT_TITLE);

        List<ConversationMessage> messages =
                conversationMessageRepository.findAllByConversationIdOrderBySequenceAsc(
                        conversation.getId()
                );

        assertThat(messages)
                .hasSize(2);

        then(chatService)
                .should()
                .generateTitle(any());
    }

    @Test
    @DisplayName("이미 제목이 존재하면 제목을 생성하지 않는다.")
    void doNotGenerateTitleWhenAlreadyExists() {

        // given
        User user = testDataFactory.createUser();

        Conversation conversation = testDataFactory.createConversation(user, "JWT");

        given(chatService.chat(any()))
                .willReturn(new ChatResponse("AI 응답"));

        // when
        conversationChatService.chat(
                user.getId(),
                conversation.getId(),
                new ChatRequest("질문")
        );

        // then
        Conversation updated =
                conversationRepository.findById(conversation.getId())
                        .orElseThrow();

        assertThat(updated.getTitle())
                .isEqualTo("JWT");

        then(chatService)
                .should(never())
                .generateTitle(any());

    }
}
