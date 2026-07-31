package com.franck.amiinvisible.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Point 2 (arbitrage Franck) : compte participant LEGER, optionnel, transverse aux activites.
 * Il ne sert qu'a lister "mes activites" et a produire un token d'acces a une activite
 * donnee ; il ne stocke et n'agrege AUCUNE donnee d'activite (nom reel, sexe, messages
 * restent portes par Participant, isole par activite - S-01 intact).
 */
@Entity
@Table(name = "comptes_participants", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompteParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String motDePasseHash;

    @Builder.Default
    @Column(nullable = false)
    private Instant dateCreation = Instant.now();
}
