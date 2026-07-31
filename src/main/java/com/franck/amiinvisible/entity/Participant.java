package com.franck.amiinvisible.entity;

import com.franck.amiinvisible.entity.enums.Sexe;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "participants", uniqueConstraints = {
        @UniqueConstraint(columnNames = "identifiant_anonyme"),
        @UniqueConstraint(columnNames = {"activite_id", "code_secret"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activite_id", nullable = false)
    private Activite activite;

    // Point 2 (arbitrage Franck) : lien optionnel vers un compte participant
    // permettant une vue "mes activites" multi-activites, sans fusion de donnees :
    // ce champ ne sert qu'a filtrer une liste, chaque activite reste cloisonnee (S-01).
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "compte_participant_id", nullable = true)
    private CompteParticipant compteParticipant;

    // F-11 : visible uniquement par l'admin de l'activite
    @Column(name = "nom_reel", nullable = false)
    private String nomReel;

    // Point 3 : sexe desormais optionnel (reduit la devinabilite en petit groupe)
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Sexe sexe;

    // F-10 : identifiant anonyme, aleatoire, non sequentiel, non devinable (S-03)
    @Column(name = "identifiant_anonyme", nullable = false, length = 20)
    private String identifiantAnonyme;

    // code secret remis une seule fois au participant pour se reconnecter (aucun email/mdp requis)
    @Column(name = "code_secret", nullable = false, length = 24)
    private String codeSecret;

    @Column(name = "date_inscription", nullable = false)
    private Instant dateInscription;
}

