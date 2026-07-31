package com.franck.amiinvisible.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * F-19 : chaque paire (offrant -> destinataire) issue du tirage possede exactement
 * une conversation. Chaque participant y accede sous deux angles differents :
 *  - en tant qu'offrant (fil "je dois offrir a ...")
 *  - en tant que destinataire (fil "on m'offre par ...")
 * mais ne voit jamais le nom reel de son interlocuteur (F-21, S-02).
 */
@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activite_id", nullable = false)
    private Activite activite;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tirage_id", nullable = false, unique = true)
    private Tirage tirage;
}
