package com.franck.amiinvisible.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * Genere des codes aleatoires cryptographiquement surs (S-03) :
 * - code d'acces d'une activite (partage aux participants)
 * - identifiant anonyme d'un participant (jamais sequentiel, jamais devinable)
 * - code secret de reconnexion d'un participant
 */
@Service
public class CodeGeneratorService {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // sans 0/O/1/I ambigus
    private final SecureRandom random = new SecureRandom();

    public String genererCode(int longueur) {
        StringBuilder sb = new StringBuilder(longueur);
        for (int i = 0; i < longueur; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    public String genererCodeAcces() {
        return genererCode(8);
    }

    public String genererIdentifiantAnonyme() {
        return "AMI-" + genererCode(6);
    }

    public String genererCodeSecret() {
        return genererCode(16);
    }
}
