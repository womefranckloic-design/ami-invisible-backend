package com.franck.amiinvisible.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Point 4 (arbitrage Franck) : marque de dernière lecture d'une conversation par un
 * participant, pour calculer un compteur de messages non lus (notification legere,
 * sans envoi d'e-mail/push - conforme au hors-perimetre V1).
 */
@Entity
@Table(name = "conversations_lectures", uniqueConstraints = @UniqueConstraint(columnNames = {"conversation_id", "participant_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationLecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @Column(name = "date_derniere_lecture", nullable = false)
    private Instant dateDerniereLecture;
}
