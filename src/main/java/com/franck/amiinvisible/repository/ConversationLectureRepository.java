package com.franck.amiinvisible.repository;

import com.franck.amiinvisible.entity.ConversationLecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversationLectureRepository extends JpaRepository<ConversationLecture, Long> {
    Optional<ConversationLecture> findByConversationIdAndParticipantId(Long conversationId, Long participantId);
}
