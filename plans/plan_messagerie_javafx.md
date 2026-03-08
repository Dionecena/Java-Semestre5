# Plan : Application de Messagerie JavaFX - Analyse & Design

## 📊 Analyse du repo existant

### ✅ Ce qui est déjà implémenté (conforme au PDF)

| Règle | Description | Fichier |
|-------|-------------|---------|
| **RG1** | Username unique | `ClientHandler.java:82` |
| **RG2** | Auth requise pour envoyer | `ClientHandler.java:147` |
| **RG3** | Un seul login à la fois | `ChatServer.java:36` |
| **RG4** | Statut ONLINE/OFFLINE | `UserDAO.java:41` |
| **RG5** | Destinataire doit exister | `ClientHandler.java:169` |
| **RG6** | Messages offline livrés à la reconnexion | `MessageDAO.java:39` |
| **RG7** | Message non vide, max 1000 chars | Côté serveur ET client |
| **RG8** | Historique chronologique | `MessageDAO.java:31` |
| **RG9** | Mots de passe hachés SHA-256 | `PasswordUtil.java:7` |
| **RG11** | Thread séparé par client | `ClientHandler extends Thread` |

### ❌ Ce qui manque ou est incomplet

| Règle | Problème | Priorité |
|-------|---------|----------|
| **CRITIQUE** | `hibernate.cfg.xml` manquant → le projet ne démarre pas | 🔴 |
| **RG10** | Perte connexion → pas de passage OFFLINE propre côté client | 🟠 |
| **RG12** | Journalisation partielle (System.out.println sans timestamps) | 🟠 |
| **Design** | Interface totalement basique (pas de CSS, pas de bulles) | 🟡 |
| **UX** | Pas de touche Entrée, pas de badges non-lus, liste = seulement online | 🟡 |

---

## 🎨 Design proposé (WhatsApp Dark Theme)

### Palette de couleurs
- **Fond sidebar** : `#111B21` (noir profond)
- **Fond chat** : `#0B141A` (fond sombre)
- **Bulle envoyée** : `#005C4B` (vert WhatsApp)
- **Bulle reçue** : `#1F2C34` (gris foncé)
- **Accent** : `#00A884` (vert clair)
- **Texte principal** : `#E9EDEF`
- **Texte secondaire** : `#8696A0`

### Architecture visuelle

```
┌─────────────────────────────────────────────────────────┐
│  CHAT SCREEN (900x600)                                  │
├──────────────────┬──────────────────────────────────────┤
│  SIDEBAR         │  ZONE DE CONVERSATION                │
│  #111B21         │  #0B141A                             │
│                  │                                      │
│  [Avatar] Alice  │  ┌─────────────────────────────┐    │
│  [Avatar] Bob 🔴 │  │ Conversation avec: Alice    │    │
│  [Avatar] Carol  │  └─────────────────────────────┘    │
│                  │                                      │
│                  │         [Bulle reçue]                │
│                  │  Alice: Bonjour !                    │
│                  │                                      │
│                  │              [Bulle envoyée]         │
│                  │              Salut Alice !           │
│                  │                                      │
│  [Déconnexion]   │  ┌──────────────────┐ [Envoyer]     │
│                  │  │ Écrire...        │               │
└──────────────────┴──────────────────────────────────────┘
```

---

## 📋 Liste des tâches à implémenter

### 🔴 Critique (le projet ne fonctionne pas sans)

1. **Créer `hibernate.cfg.xml`** dans `src/main/resources/`
   - Connexion PostgreSQL (localhost:5432)
   - Mapping des entités User et Message
   - `hbm2ddl.auto = update`

### 🟠 Fonctionnel (règles du prof non respectées)

2. **RG10 - Gestion perte connexion** dans `ChatController.java`
   - À la déconnexion : afficher un Alert JavaFX
   - Retourner à l'écran de login automatiquement

3. **RG12 - Journalisation structurée** dans `ClientHandler.java`
   - Ajouter timestamps sur tous les logs
   - Format : `[2024-01-15 14:32:01] [SERVER] Connexion: alice`

4. **Commande `ALL_USERS`** dans `ClientHandler.java` + `ChatServer.java`
   - Retourner tous les utilisateurs (online ET offline)
   - Indispensable pour écrire à quelqu'un d'offline (RG6)

### 🟡 Design & UX

5. **`styles.css`** dans `src/main/resources/com/example/whatsapp_java/`
   - Dark theme complet
   - Styles pour bulles, sidebar, boutons, champs de saisie

6. **`login-view.fxml`** - Refaire avec :
   - Logo/titre stylé
   - Champs avec icônes
   - Boutons colorés (vert accent)
   - Lien CSS

7. **`chat-view.fxml`** - Refaire avec :
   - `ListView` pour les messages (au lieu de `TextArea`)
   - Sidebar stylée avec `ListView` d'utilisateurs
   - Barre de saisie moderne

8. **`MessageCell.java`** - Custom `ListCell<MessageItem>` :
   - Bulle à droite (messages envoyés) en vert `#005C4B`
   - Bulle à gauche (messages reçus) en gris `#1F2C34`
   - Affichage heure + statut (✓ ENVOYE, ✓✓ RECU, ✓✓ bleu LU)

9. **`UserCell.java`** - Custom `ListCell<String>` :
   - Avatar circulaire avec initiale
   - Indicateur vert/gris (online/offline)
   - Badge rouge pour messages non lus

10. **`ChatController.java`** - Refactoring :
    - Utiliser `ObservableList<MessageItem>` + `ListView`
    - Gérer les badges de messages non lus
    - Touche Entrée pour envoyer

11. **`LoginController.java`** - Amélioration UX :
    - Touche Entrée pour se connecter
    - Validation visuelle des champs

12. **Nettoyage** :
    - Supprimer `HelloController.java`
    - Supprimer `hello-view.fxml`

---

## 🗂️ Nouveaux fichiers à créer

```
src/main/resources/
  ├── hibernate.cfg.xml                          ← MANQUANT (critique)
  └── com/example/whatsapp_java/
      └── styles.css                             ← Design WhatsApp dark

src/main/java/com/example/whatsapp_java/client/
  ├── MessageCell.java                           ← Bulles de messages
  ├── MessageItem.java                           ← DTO pour les messages dans la ListView
  └── UserCell.java                              ← Cellule utilisateur avec badge
```

## 🗂️ Fichiers à modifier

```
src/main/java/com/example/whatsapp_java/
  ├── server/ClientHandler.java                  ← RG12 + commande ALL_USERS
  ├── server/ChatServer.java                     ← méthode allUsersCsv()
  ├── client/ChatController.java                 ← RG10 + design + UX
  └── client/LoginController.java                ← UX améliorée

src/main/resources/com/example/whatsapp_java/
  ├── login-view.fxml                            ← Design moderne
  └── chat-view.fxml                             ← Design moderne
```

## 🗂️ Fichiers à supprimer

```
src/main/java/com/example/whatsapp_java/HelloController.java
src/main/resources/com/example/whatsapp_java/hello-view.fxml
```
