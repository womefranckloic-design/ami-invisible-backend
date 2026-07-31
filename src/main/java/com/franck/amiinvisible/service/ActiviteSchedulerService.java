package com.franck.amiinvisible.service;

import com.franck.amiinvisible.entity.Activite;
import com.franck.amiinvisible.entity.enums.StatutActivite;
import com.franck.amiinvisible.repository.ActiviteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * F-13 : a l'expiration du chronometre d'inscription, si le quota n'est pas atteint,
 * aucune action automatique n'est prise sur les donnees : l'activite passe simplement
 * en EN_ATTENTE_DECISION pour que l'administrateur choisisse (prolonger / demarrer / annuler).
 * F-23 : alerte (journalisee) a l'approche et a l'echeance de la duree globale.
 * Aucun envoi d'e-mail/SMS en V1 (1.3 Hors perimetre).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActiviteSchedulerService {

    private final ActiviteRepository activiteRepository;

    @Scheduled(fixedRate = 5 * 60 * 1000) // toutes les 5 minutes
    @Transactional
    public void verifierChronometresInscription() {
        List<Activite> expirees = activiteRepository.findByStatutAndDateFinInscriptionBefore(
                StatutActivite.INSCRIPTION, Instant.now());

        for (Activite a : expirees) {
            log.warn("Activite '{}' (id={}) : chronometre d'inscription expire, en attente de decision admin", a.getNom(), a.getId());
            a.setStatut(StatutActivite.EN_ATTENTE_DECISION);
            activiteRepository.save(a);
        }
    }

    @Scheduled(fixedRate = 60 * 60 * 1000) // toutes les heures
    public void alerterEcheanceActivite() {
        Instant dansUnJour = Instant.now().plus(1, ChronoUnit.DAYS);
        List<Activite> proches = activiteRepository.findByStatutAndDateFinActiviteBefore(
                StatutActivite.EN_COURS, dansUnJour);

        for (Activite a : proches) {
            log.warn("Activite '{}' (id={}) : echeance de la duree globale proche ou atteinte ({})", a.getNom(), a.getId(), a.getDateFinActivite());
        }
    }
}
