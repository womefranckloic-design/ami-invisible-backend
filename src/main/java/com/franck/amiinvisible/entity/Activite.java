package com.franck.amiinvisible.entity;

import com.franck.amiinvisible.entity.enums.ModeConfidentialite;
import com.franck.amiinvisible.entity.enums.StatutActivite;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "activites", uniqueConstraints = @UniqueConstraint(columnNames = "code_acces"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @Column(nullable = false)
    private String nom;

    @Column(length = 1000)
    private String description;

    @Column(name = "nb_participants_attendu", nullable = false)
    private Integer nbParticipantsAttendu;

    @Column(name = "code_acces", nullable = false, length = 12)
    private String codeAcces;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutActivite statut;

    @Column(nullable = false)
    private Instant dateCreation;

    // F-06 : chronometre d'inscription
    @Column(name = "date_debut_inscription")
    private Instant dateDebutInscription;

    @Column(name = "date_fin_inscription")
    private Instant dateFinInscription;

    // F-07 : duree globale a partir de la fin du tirage
    @Column(name = "duree_globale_jours")
    private Integer dureeGlobaleJours;

    @Column(name = "date_debut_activite")
    private Instant dateDebutActivite; // = date du tirage effectif

    @Column(name = "date_fin_activite")
    private Instant dateFinActivite;

    @Column(name = "date_cloture")
    private Instant dateCloture;

    // F-26 : figer les echanges en lecture seule apres cloture
    @Builder.Default
    @Column(name = "lecture_seule")
    private boolean lectureSeule = false;

    @Builder.Default
    @Column(name = "tirage_effectue")
    private boolean tirageEffectue = false;

    // Point 5 : mode de supervision admin, defaut = comportement historique
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "mode_confidentialite", nullable = false)
    private ModeConfidentialite modeConfidentialite = ModeConfidentialite.SUPERVISION_TOTALE;
}
