## Messagerie type WhatsApp en JavaFX

Application de messagerie instantanée avec interface JavaFX et serveur TCP multi‑clients, inspirée de WhatsApp (thème sombre), avec persistance des utilisateurs et des messages via Hibernate / PostgreSQL.

---

## 🎯 Objectifs du projet

- **Messagerie temps réel** entre plusieurs utilisateurs connectés à un **serveur TCP**.
- **Interface graphique moderne** en JavaFX (écrans de login et de chat).
- **Persistance des données** (utilisateurs, messages, statuts) avec **Hibernate** et **PostgreSQL**.
- **Respect de règles fonctionnelles** classiques de messagerie :
  - unicité des usernames,
  - authentification obligatoire avant l’envoi de messages,
  - gestion ONLINE/OFFLINE,
  - livraison des messages aux utilisateurs offline à leur reconnexion,
  - historique des conversations.

---

## 🏗️ Architecture générale

- **Client JavaFX (`com.example.whatsapp_java.client`)**
  - `HelloApplication` / `Launcher` : point d’entrée JavaFX, charge les vues FXML (`login-view.fxml`, `chat-view.fxml`) et la feuille de style `styles.css`.
  - `LoginController` : logique de l’écran de connexion / inscription, interaction avec le serveur pour `REGISTER` et `LOGIN`.
  - `ChatController` : gestion de l’écran de chat (liste des contacts, messages, envoi / réception, gestion de la déconnexion).
  - `ChatClient` : client TCP générique, encapsule la connexion au serveur, la lecture en continu et l’envoi de commandes texte.
  - `AppSession` : stocke la session courante côté client (client TCP, username).
  - `MessageItem`, `MessageCell` : représentation et rendu personnalisé des messages dans la `ListView` (bulles de chat).

- **Serveur TCP (`com.example.whatsapp_java.server`)**
  - `ChatServer` : serveur TCP qui écoute sur un port (par défaut 5000), accepte les connexions et crée un `ClientHandler` par client.
  - `ClientHandler` : thread dédié à un client, parse les commandes (LOGIN, REGISTER, SEND, HISTORY, ALL_USERS, LOGOUT, …), applique les règles métier et envoie les réponses.

- **Couche d’accès aux données (`com.example.whatsapp_java.dao`)**
  - `UserDAO` : opérations CRUD sur les utilisateurs (création, recherche par username, mise à jour du statut, etc.).
  - `MessageDAO` : gestion des messages (sauvegarde, récupération de l’historique par paire d’utilisateurs, livraison des messages offline).

- **Couche métier / entités (`com.example.whatsapp_java.entities`)**
  - `User`, `UserStatus` : entité utilisateur, avec username unique, mot de passe haché et statut ONLINE/OFFLINE.
  - `Message`, `MessageStatus` : entité message avec horodatage, auteur, destinataire et statut (ENVOYE, RECU, LU).

- **Utilitaires (`com.example.whatsapp_java.utils`)**
  - `HibernateUtil` : configuration et fourniture de la `SessionFactory` Hibernate.
  - `PasswordUtil` : hachage des mots de passe (SHA‑256).

- **Ressources JavaFX (`src/main/resources/com/example/whatsapp_java`)**
  - `login-view.fxml` : écran de connexion / inscription stylé (dark theme).
  - `chat-view.fxml` : écran principal de chat avec sidebar contacts + zone de conversation.
  - `styles.css` : thème sombre inspiré de WhatsApp (couleurs, bulles, boutons…).

---

## 🔌 Protocole de communication client / serveur

La communication entre le client JavaFX et le serveur TCP se fait en **texte brut**, une ligne par message, avec des commandes de la forme :

- `REGISTER|username|passwordHash`  
  → demande de création d’un nouvel utilisateur.

- `LOGIN|username|passwordHash`  
  → authentification d’un utilisateur existant.

- `SEND|destinataire|message`  
  → envoi d’un message privé au destinataire.

- `HISTORY|autreUtilisateur`  
  → récupération de l’historique des messages avec l’utilisateur donné.

- `ALL_USERS|`  
  → récupération de la liste de tous les utilisateurs (ONLINE et OFFLINE) avec leur statut.

- `LOGOUT|`  
  → déconnexion propre du client.

Les réponses du serveur sont également des lignes texte, typiquement :

- `OK|REGISTER` / `OK|LOGIN` en cas de succès,
- `ERROR|<message>` en cas d’erreur fonctionnelle (username déjà pris, mauvais mot de passe, etc.),
- des notifications de message et d’historique, parsées côté client et affichées dans l’interface (via `ChatController` et `MessageItem`).

---

## 💾 Persistances & configuration Hibernate

La base de données utilisée est **PostgreSQL**.  
La configuration Hibernate est définie dans `src/main/resources/hibernate.cfg.xml` :

- URL JDBC, utilisateur, mot de passe,
- dialecte PostgreSQL,
- stratégie de génération / mise à jour du schéma (`hbm2ddl.auto=update`),
- mapping des entités `User` et `Message`.

> ⚠️ Pensez à adapter `hibernate.cfg.xml` à votre environnement (nom de base, identifiants, mot de passe).  
> Pour des raisons de sécurité, ce fichier peut être exclu du dépôt Git dans un contexte réel.

---

## ▶️ Lancer le projet

### 1. Prérequis

- **Java 17** ou supérieur.
- **Maven** (ou utilisation du wrapper `mvnw`).
- **PostgreSQL** installé et accessible.

### 2. Préparer la base de données

1. Créer une base de données, par exemple `messagerie_db`.
2. Créer un utilisateur PostgreSQL avec les droits nécessaires.
3. Adapter `hibernate.cfg.xml` (URL, username, password).

Au premier lancement, Hibernate crée / met à jour automatiquement les tables nécessaires (`User`, `Message`).

### 3. Lancer le serveur

- Via une classe `main` (par ex. `ServerMain`) ou directement `ChatServer` avec le port souhaité (par défaut 5000 suivant l’implémentation).
- Le serveur écoute les connexions des clients et loggue les évènements avec horodatage.

### 4. Lancer le client JavaFX

Depuis la racine du projet :

```bash
mvn clean javafx:run
```

Le plugin `javafx-maven-plugin` est configuré dans le `pom.xml` pour démarrer la classe `com.example.whatsapp_java.Launcher` (qui lance `HelloApplication`).

Vous pouvez alors :

1. Ouvrir plusieurs instances du client.
2. Créer des comptes (REGISTER) puis se connecter (LOGIN).
3. Envoyer des messages entre utilisateurs, même si le destinataire est offline (les messages seront envoyés à la reconnexion).

---

## 🎨 Interface utilisateur (JavaFX)

- **Écran de login (`login-view.fxml`)**
  - Fond sombre (`#0B141A`) avec conteneur centré.
  - Champs pour **nom d’utilisateur** et **mot de passe**.
  - Boutons **S’inscrire** et **Se connecter**.
  - Gestion de la touche **Entrée** (navigation + validation).
  - Zone de statut pour afficher les messages d’erreur / succès.

- **Écran de chat (`chat-view.fxml`)**
  - **Sidebar** à gauche : liste des contacts avec statut (`ONLINE` / `OFFLINE`).
  - **Zone centrale** : `ListView` de messages stylés en bulles (bulle envoyée / reçue).
  - **Barre de saisie** en bas : champ de texte + bouton Envoyer (ou touche Entrée).
  - **Barre de statut** : messages d’état (erreurs, informations).

---

## 🔐 Règles fonctionnelles principales

Certaines des règles implémentées côté serveur / client sont :

- **Username unique** : impossibilité de créer deux comptes avec le même identifiant.
- **Authentification obligatoire** : seul un utilisateur authentifié peut envoyer des messages.
- **Connexion unique** : un même utilisateur ne peut pas être connecté plusieurs fois en parallèle.
- **Statuts ONLINE/OFFLINE** : la présence est gérée côté serveur et stockée en base.
- **Messages offline** : les messages adressés à un utilisateur offline sont enregistrés et délivrés à sa prochaine connexion.
- **Messages validés** : messages non vides, taille maximale (par ex. 1000 caractères), contrôlée côté client et côté serveur.
- **Historique** : l’historique des conversations est récupérable et affiché dans l’ordre chronologique.

---

## 🧪 Tests

Le projet utilise **JUnit 5** pour les tests unitaires (dépendances `junit-jupiter-api` et `junit-jupiter-engine` dans le `pom.xml`).  
Vous pouvez lancer les tests avec :

```bash
mvn test
```

---

## 🛠️ Dépendances principales

Les principales dépendances utilisées (voir `pom.xml`) sont :

- **JavaFX** (`javafx-controls`, `javafx-fxml`) pour l’interface graphique.
- **Hibernate Core** + **Jakarta Persistence API** pour l’ORM.
- **Hibernate Validator** + **Jakarta Validation** pour la validation.
- **PostgreSQL JDBC Driver** pour l’accès à la base de données.
- **Lombok** (scope `provided`) pour réduire le boilerplate dans les entités.
- **JUnit 5** pour les tests.

---

## 👥 Auteurs

- **Moussa Dione**
- **Mouhamadou Makhtar Diop**

La répartition des tâches (qui s’occupe de la BDD, qui s’occupe du client/serveur/UI) est décrite dans [REPARTITION.md](REPARTITION.md).

## 🔍 Documentation interactive du projet

Ce projet dispose d’un site web généré automatiquement qui permet d’explorer le code :
- Voir quelles **classes** sont utilisées
- Identifier où les **méthodes** sont implémentées
- Comprendre où elles sont appelées dans le projet

👉 [Accéder à la documentation en ligne](https://dionecena.github.io/Java-Semestre5/)


