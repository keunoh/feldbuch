package io.github.kaltz.feldbuch.conversation.service;

import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.repository.ConversationRepository;
import io.github.kaltz.feldbuch.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ConversationService {

    private final ConversationRepository repository;

    public Long create(User user, String title) {
        Conversation conversation = Conversation.create(user, title);

        repository.save(conversation);

        return conversation.getId();
    }
}
