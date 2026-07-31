package com.franck.amiinvisible.repository;

import com.franck.amiinvisible.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderByDateEnvoiAsc(Long conversationId);

    List<Message> findByConversationActiviteIdOrderByDateEnvoiAsc(Long activiteId);

    long countByConversationIdAndExpediteurIdNotAndDateEnvoiAfter(Long conversationId, Long expediteurId, Instant date);

    long countByConversationIdAndExpediteurIdNot(Long conversationId, Long expediteurId);
}
