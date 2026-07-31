package com.franck.amiinvisible.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Une ligne = un participant "offrant" attribue a un participant "destinataire".
 * F-15/F-16/F-17 : chaque participant offre exactement une fois et recoit exactement une fois.
 * Seul l'administrateur peut lire cette table avec les noms reels (jointure Participant).
 */
@Entity
@Table(name = "tirages", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"activite_id", "offrant_id"}),
        @UniqueConstraint(columnNames = {"activite_id", "destinataire_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tirage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activite_id", nullable = false)
    private Activite activite;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offrant_id", nullable = false)
    private Participant offrant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinataire_id", nullable = false)
    private Participant destinataire;

    @Column(name = "date_tirage", nullable = false)
    private Instant dateTirage;
}
