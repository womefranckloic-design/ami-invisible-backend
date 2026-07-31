package com.franck.amiinvisible.repository;

import com.franck.amiinvisible.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByActiviteId(Long activiteId);

    Optional<Conversation> findByTirageId(Long tirageId);

    @org.springframework.data.jpa.repository.Query("""
            select c from Conversation c
            where c.activite.id = :activiteId
            and (c.tirage.offrant.id = :participantId or c.tirage.destinataire.id = :participantId)
            """)
    List<Conversation> findAllForParticipant(Long activiteId, Long participantId);
}
