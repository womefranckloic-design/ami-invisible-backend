package com.franck.amiinvisible.entity.enums;

public enum StatutActivite {
    INSCRIPTION,        // periode d'inscription en cours (F-06)
    EN_ATTENTE_DECISION,// chronometre expire, quota non atteint (F-13)
    TIRAGE_EFFECTUE,    // tirage realise, avant demarrage effectif
    EN_COURS,           // activite demarree, messagerie ouverte
    CLOTUREE,           // cloturee par l'administrateur (F-25/F-26)
    ANNULEE             // annulee par l'administrateur (F-13)
}
