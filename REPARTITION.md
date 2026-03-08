# Répartition des tâches – Messagerie JavaFX

Ce document décrit la répartition du travail entre les deux membres du binôme. Chacun est responsable de sa partie et effectue les commits correspondants sur le dépôt GitHub.

---

## 👤 Makhoumetzo – Partie **Base de données & persistance**

Responsable de tout ce qui concerne le modèle de données, Hibernate et PostgreSQL.

### Fichiers à committer / maintenir

| Fichier | Rôle |
|--------|------|
| `src/main/java/com/example/whatsapp_java/entities/User.java` | Entité utilisateur (username, mot de passe haché, statut) |
| `src/main/java/com/example/whatsapp_java/entities/UserStatus.java` | Énumération des statuts utilisateur (ONLINE, OFFLINE) |
| `src/main/java/com/example/whatsapp_java/entities/Message.java` | Entité message (auteur, destinataire, contenu, date, statut) |
| `src/main/java/com/example/whatsapp_java/entities/MessageStatus.java` | Énumération des statuts de message (ENVOYE, RECU, LU) |
| `src/main/java/com/example/whatsapp_java/dao/UserDAO.java` | Accès base : création, recherche, mise à jour des utilisateurs |
| `src/main/java/com/example/whatsapp_java/dao/MessageDAO.java` | Accès base : sauvegarde messages, historique, messages offline |
| `src/main/java/com/example/whatsapp_java/utils/HibernateUtil.java` | Configuration SessionFactory Hibernate |
| `src/main/resources/hibernate.cfg.xml` | Configuration connexion PostgreSQL + mapping entités |
| `src/main/resources/hibernate.cfg.xml.example` | Exemple de config (sans mots de passe réels) |

### Tâches typiques

- Créer / modifier les entités JPA si le modèle évolue.
- Adapter les DAO (nouvelles requêtes, optimisations).
- Gérer le schéma DB (migrations, `hbm2ddl.auto`, etc.).
- Mettre à jour `hibernate.cfg.xml` et la doc liée à la BDD dans le README.



---

## 👤 Dionecena – Partie **Client, serveur & interface**

Responsable du réseau (client TCP, serveur), de la logique métier côté serveur, et de l’interface JavaFX.

### Fichiers à committer / maintenir

| Fichier | Rôle |
|--------|------|
| **Serveur** | |
| `src/main/java/com/example/whatsapp_java/server/ChatServer.java` | Serveur TCP, gestion des clients connectés |
| `src/main/java/com/example/whatsapp_java/server/ClientHandler.java` | Thread par client, parsing des commandes (LOGIN, SEND, etc.) |
| `src/main/java/com/example/whatsapp_java/server/ServerMain.java` | Point d’entrée du serveur |
| **Client & session** | |
| `src/main/java/com/example/whatsapp_java/client/ChatClient.java` | Client TCP (connexion, envoi, réception des lignes) |
| `src/main/java/com/example/whatsapp_java/client/AppSession.java` | Session courante (client, username) |
| **Controllers & UI** | |
| `src/main/java/com/example/whatsapp_java/HelloApplication.java` | Application JavaFX, chargement des vues |
| `src/main/java/com/example/whatsapp_java/Launcher.java` | Launcher pour JavaFX + Maven |
| `src/main/java/com/example/whatsapp_java/client/LoginController.java` | Logique écran de connexion / inscription |
| `src/main/java/com/example/whatsapp_java/client/ChatController.java` | Logique écran de chat (messages, liste contacts, déconnexion) |
| `src/main/java/com/example/whatsapp_java/client/MessageItem.java` | DTO pour l’affichage d’un message dans la ListView |
| `src/main/java/com/example/whatsapp_java/client/MessageCell.java` | Cellule personnalisée (bulles de chat) |
| **Utilitaires (hors BDD)** | |
| `src/main/java/com/example/whatsapp_java/utils/PasswordUtil.java` | Hachage SHA-256 des mots de passe |
| **Ressources FXML / CSS** | |
| `src/main/resources/com/example/whatsapp_java/login-view.fxml` | Vue écran de login |
| `src/main/resources/com/example/whatsapp_java/chat-view.fxml` | Vue écran de chat (sidebar + messages) |
| `src/main/resources/com/example/whatsapp_java/styles.css` | Feuille de style (thème sombre) |

### Tâches typiques

- Évolution du protocole (nouvelles commandes, format des réponses).
- Amélioration de l’UI (nouveaux écrans, comportements, accessibilité).
- Gestion de la déconnexion, reconnexion, messages en temps réel côté client.
- Documentation protocole et lancement dans le README.


---

## Fichiers partagés (à valider ensemble si modifié)

| Fichier | Raison |
|--------|--------|
| `pom.xml` | Dépendances (Hibernate, JavaFX, PostgreSQL, etc.) concernent les deux parties |
| `README.md` | Documentation globale du projet |
| `REPARTITION.md` | Ce fichier – à mettre à jour si la répartition change |


---

## Site Web pour les méthodes et classes utilisées

[https://dionecena.github.io/Java-Semestre5/](https://dionecena.github.io/Java-Semestre5/)
