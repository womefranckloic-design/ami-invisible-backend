package com.franck.amiinvisible.dto.response;

import com.franck.amiinvisible.entity.enums.ModeConfidentialite;
import com.franck.amiinvisible.entity.enums.StatutActivite;

import java.time.Instant;

public record ActiviteResponse(
        Long id,
        String nom,
        String description,
        Integer nbParticipantsAttendu,
        long nbParticipantsInscrits,
        String codeAcces,
        StatutActivite statut,
        Instant dateCreation,
        Instant dateDebutInscription,
        Instant dateFinInscription,
        Integer dureeGlobaleJours,
        Instant dateDebutActivite,
        Instant dateFinActivite,
        Instant dateCloture,
        boolean lectureSeule,
        ModeConfidentialite modeConfidentialite
) {
}
