package io.github.kaltz.feldbuch.conversation.service;

import io.github.kaltz.feldbuch.conversation.dto.request.CreateConversationRequest;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.repository.ConversationRepository;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.reader.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ConversationCommandService {

    private final ConversationRepository repository;
    private final UserReader userReader;

    public Long create(Long userId, CreateConversationRequest request) {

        User user = userReader.get(userId);

        Conversation conversation = Conversation.create(user, request.title());

        repository.save(conversation);

        return conversation.getId();
    }
}
