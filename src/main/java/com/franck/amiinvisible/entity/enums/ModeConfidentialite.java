package com.franck.amiinvisible.entity.enums;

/**
 * Point 5 (arbitrage Franck) : niveau de supervision de l'administrateur sur une activite.
 * Choisi a la creation, defaut = SUPERVISION_TOTALE (comportement historique).
 */
public enum ModeConfidentialite {
    // L'admin voit tout : noms reels, tirage, contenu des messages (comportement actuel)
    SUPERVISION_TOTALE,
    // L'admin voit qui parle a qui (noms reels, nb de messages, dates) mais jamais le contenu
    SUPERVISION_TECHNIQUE,
    // L'admin ne supervise rien : ni le contenu, ni les metadonnees des conversations
    AUCUNE_SUPERVISION
}
