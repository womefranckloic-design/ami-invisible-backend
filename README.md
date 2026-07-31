# Ami Invisible — Backend Spring Boot

Backend uniquement (pas de frontend), implémentant le cahier des charges "Ami Invisible" :
plateforme multi-activités permettant à un administrateur de gérer un ou plusieurs tirages
au sort ("ami invisible"), avec anonymat strict entre participants et messagerie restreinte.

## Stack

- Java 21
- Spring Boot 3.3.5 (Web, Data JPA, Security, Validation)
- PostgreSQL
- JWT (io.jsonwebtoken / jjwt 0.12.6)
- Lombok

## Installation

```bash
# 1. Créer la base PostgreSQL
createdb ami_invisible

# 2. Variables d'environnement (ou modifier application.yml directement)
export DB_URL=jdbc:postgresql://localhost:5432/ami_invisible
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET="change-moi-en-production-256-bits-minimum"

# 3. Lancer
./mvnw spring-boot:run
```

L'API démarre sur `http://localhost:8080`.

## Architecture fonctionnelle

### Comptes

Il y a **deux types de comptes distincts**, chacun avec son propre JWT :

- **Administrateur** : email + mot de passe classique (`/api/auth/admin/register`, `/api/auth/admin/login`).
- **Participant** : pas d'email/mot de passe. À l'inscription à une activité, il reçoit un
  `identifiantAnonyme`, un `codeSecret` (à conserver précieusement, affiché **une seule fois**)
  et un token JWT. Il peut se reconnecter plus tard avec `codeAcces` + `codeSecret`
  (`/api/inscription/login`).

### Cycle de vie d'une activité

`INSCRIPTION` → (`EN_ATTENTE_DECISION` si chronomètre expiré et quota non atteint) →
tirage → `EN_COURS` → `CLOTUREE` (ou `ANNULEE` à tout moment avant clôture).

Une tâche planifiée (`ActiviteSchedulerService`) bascule automatiquement une activité en
`EN_ATTENTE_DECISION` quand le chronomètre d'inscription expire sans que le quota soit
atteint (F-13) — sans jamais prendre de décision à la place de l'administrateur.

### Anonymat (cœur du cahier des charges)

- Le **nom réel** d'un participant n'est jamais exposé sur les routes `/api/participants/me/**`
  ni `/api/conversations/**` (endpoints participant) : uniquement `identifiantAnonyme` + `sexe`.
- Les routes admin (`/api/activites/{id}/participants`, `/api/activites/{id}/tirage`,
  `/api/activites/{id}/conversations/**`) sont les seules à exposer les noms réels, et
  uniquement à l'administrateur propriétaire de l'activité.
- Les identifiants anonymes et codes secrets sont générés avec `SecureRandom` (non séquentiels,
  non devinables — S-03).

### Algorithme de tirage

Les participants sont mélangés aléatoirement puis chaînés en un seul cycle
(`participant[i]` offre à `participant[i+1 mod n]`). Cela garantit sans boucle de réessai :
pas d'auto-attribution, pas d'orphelin, chacun offre et reçoit exactement une fois (F-15).

### Messagerie

Chaque ligne de tirage génère **une** conversation. Le participant "offrant" et le participant
"destinataire" y accèdent chacun avec un rôle différent, mais ni l'un ni l'autre ne voit jamais
le nom réel de son interlocuteur (F-21, S-02). L'administrateur seul peut tout superviser avec
les noms réels (F-22).

## Arbitrages du 2ᵉ tour (relecture cahier des charges)

| # | Point soulevé | Décision | Implémentation |
|---|---|---|---|
| 1 | Perte du code secret | Récupération assistée par l'admin | `POST /api/activites/{id}/participants/{pid}/regenerer-code` |
| 2 | Ré-inscription à chaque activité | Compte participant léger, optionnel | `CompteParticipant` (voir ci-dessous) |
| 3 | Anonymat fragile en petit groupe | Min. 5 participants, sexe optionnel | `CreerActiviteRequest` + `Participant.sexe` nullable |
| 4 | Pas de notification | Compteur non-lu + polling | `ConversationLecture` + `nbNonLus` dans `ConversationResponse` |
| 5 | Admin voit tout | 3 modes configurables, défaut = supervision totale | `ModeConfidentialite` sur `Activite` |

### Compte participant léger (Point 2)

Un participant peut, **optionnellement**, créer un compte transverse (email + mot de passe)
via `/api/auth/participant/register` puis `/login`. Ce compte ne stocke **aucune donnée
d'activité** — il ne fait que pointer vers ses différentes inscriptions (`Participant`),
chacune restant cloisonnée comme avant (S-01 intact).

- S'il présente son token de compte (`Authorization: Bearer ...`) au moment de
  `POST /api/inscription/{codeAcces}`, sa nouvelle inscription est automatiquement liée.
- `GET /api/comptes/me/participations` : liste ses inscriptions (nom d'activité, statut,
  identifiant anonyme — jamais les données d'une autre activité).
- `POST /api/comptes/me/participations/{participantId}/token` : échange contre un token
  `PARTICIPANT` scopé à cette activité précise (vérifie l'appartenance avant émission).

L'inscription 100% anonyme sans compte reste possible et inchangée.

### Modes de confidentialité admin (Point 5)

Choisi à la création de l'activité (`modeConfidentialite`, optionnel, défaut
`SUPERVISION_TOTALE`) :

- `SUPERVISION_TOTALE` : comportement historique, l'admin voit tout.
- `SUPERVISION_TECHNIQUE` : l'admin voit qui parle à qui (noms réels, nb de messages,
  dates) mais le contenu des messages est masqué.
- `AUCUNE_SUPERVISION` : les endpoints de supervision (`/conversations`, `/conversations/{id}/messages`)
  renvoient une erreur 403 pour l'admin.

*Non traité dans ce tour* : la question "l'admin peut-il participer à sa propre activité"
reste ouverte — elle suppose de lier un compte `Admin` à un `Participant`, ce qui n'est pas
encore modélisé. À trancher avant implémentation si besoin.

### Backlog non implémenté (mentionné mais non demandé ce tour)

- Exclusions (couples/famille) dans l'algorithme de tirage — nécessite un tirage sous
  contrainte (backtracking) plutôt qu'une simple rotation.
- Budget indicatif par activité (champ simple à ajouter).
- Liste de souhaits par participant (nouvelle entité, visible uniquement du destinataire).



### Auth admin
| Méthode | URL | Description |
|---|---|---|
| POST | `/api/auth/admin/register` | Créer un compte admin |
| POST | `/api/auth/admin/login` | Connexion admin |

### Compte participant léger (Point 2, optionnel)
| Méthode | URL | Description |
|---|---|---|
| POST | `/api/auth/participant/register` | Créer un compte participant transverse |
| POST | `/api/auth/participant/login` | Connexion compte participant |
| GET | `/api/comptes/me/participations` | Mes activités (toutes participations liées) |
| POST | `/api/comptes/me/participations/{pid}/token` | Obtenir un token scopé pour une activité |

### Inscription participant (public)
| Méthode | URL | Description |
|---|---|---|
| GET | `/api/inscription/{codeAcces}` | Infos publiques d'une activité |
| POST | `/api/inscription/{codeAcces}` | S'inscrire (nomReel, sexe) |
| POST | `/api/inscription/login` | Reconnexion (codeAcces, codeSecret) |

### Activités (admin, JWT admin requis)
| Méthode | URL | Description |
|---|---|---|
| POST | `/api/activites` | Créer une activité |
| GET | `/api/activites` | Lister mes activités |
| GET | `/api/activites/{id}` | Détail d'une activité |
| PATCH | `/api/activites/{id}/prolonger` | Prolonger l'inscription |
| POST | `/api/activites/{id}/demarrer-maintenant` | Démarrer avec les inscrits actuels |
| POST | `/api/activites/{id}/annuler` | Annuler l'activité |
| POST | `/api/activites/{id}/cloturer?lectureSeule=true` | Clôturer |
| DELETE | `/api/activites/{id}` | Supprimer (si clôturée/annulée) |
| GET | `/api/activites/{id}/participants` | Liste avec noms réels |
| POST | `/api/activites/{id}/participants/{pid}/regenerer-code` | Régénérer un code secret perdu (Point 1) |
| POST | `/api/activites/{id}/tirage` | Lancer le tirage |
| GET | `/api/activites/{id}/tirage` | Voir la correspondance complète |
| POST | `/api/activites/{id}/tirage/reinitialiser` | Réinitialiser (destructif) |
| GET | `/api/activites/{id}/conversations` | Superviser toutes les conversations |
| GET | `/api/activites/{id}/conversations/{cid}/messages` | Lire les messages (avec noms réels) |

### Participant connecté (JWT participant requis)
| Méthode | URL | Description |
|---|---|---|
| GET | `/api/participants/me` | Mon profil (identifiant anonyme) |
| GET | `/api/participants/me/tirage` | À qui j'offre / qui m'offre |
| GET | `/api/conversations` | Mes 2 conversations |
| GET | `/api/conversations/{id}/messages` | Messages d'une conversation |
| POST | `/api/conversations/{id}/messages` | Envoyer un message |

## Sécurité

- JWT stateless, deux types de principal (`ADMIN` / `PARTICIPANT`), filtre unique
  (`JwtAuthFilter`) qui peuple le `SecurityContext`.
- Chaque service vérifie explicitement la propriété/l'appartenance (activité de l'admin,
  conversation du participant) en plus du contrôle de rôle Spring Security — cloisonnement
  strict entre activités (S-01).

## Hors périmètre (V1, conforme au cahier des charges)

- Envoi d'e-mails/SMS/push (section 1.3).
- Paiement en ligne.
- Modération automatisée du contenu des messages.

## Pistes d'évolution

- Frontend (non demandé ici, backend uniquement).
- Notifications e-mail lors du tirage / de l'échéance.
- Tests d'intégration (JUnit 5 + Testcontainers PostgreSQL) — la structure du projet
  (services découplés des controllers) s'y prête directement.
