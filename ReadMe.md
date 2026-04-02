# Vocabulaire technique d'archéologie

**Monorepo pour un dictionnaire technique multilingue spécialisé en archéologie.**  
Ce projet propose une interface web Angular et une API REST sécurisée avec Spring Boot. Il permet la gestion de termes spécialisés, leur traduction, leur organisation par thèmes, et la création de relations (synonymes, traductions, thèmes).

🔗 **Site en ligne** : [https://patrick.le-cadre.net](https://patrick.le-cadre.net)

Le site est hébergé sur un Raspberry-pi v4 serveur, avec un tunnel cloudlflare pour le rendre accessible.
La base de données relationnelle utilise mySQL.

## 📦 Structure du projet

Ce dépôt est organisé en monorepo :

```
/vocabulaire-technique-archeologie/
├── frontend/       → Application Angular v20
└── backend/        → API Java v21 avec Spring Boot 3.3
```

---

## 🚀 Fonctionnalités principales

- Authentification JWT (inscription, login)
- Recherche plein texte insensible à la casse et aux accents

### pour les profils admin :

- Gestion des mots techniques (ajout, édition, suppression)
- Système de relations (synonymes, traductions)
- Thématisation des mots (organisation par domaine archéologique)
- API REST documentée avec OpenAPI

---

## 🧩 Technologies

| Frontend    | Backend                   | BDD     |
| ----------- | ------------------------- | ------- |
| Angular v20 | Java 21 / Spring Boot 3.3 | mySQL 8 |
| TypeScript  | Spring Security (JWT)     |         |
| RxJS        | OpenAPI 3.0               |         |

---

## 🔐 Authentification

- `POST /api/auth/login` : Connexion et récupération d’un JWT

---

## 🧠 Routes API Principales

### 🌐 Voctech (vocabulaire)

- `GET /api/voctech/words` : Liste complète des mots
- `GET /api/voctech/words/last?n=10` : N derniers mots ajoutés à la BDD
- `GET /api/voctech/search?word=xxx` : Recherche libre dans les mots
- `PATCH /api/voctech/` : Mise à jour d’un mot (sécurisé)

### 🎨 Thèmes

- `GET /api/voctech/themes` : Liste des thèmes existants

### 🧱 Relations et base de données (sécurisé Admin)

- `POST /api/database/addpair` : Ajout de deux mots nouveaux liés ensemble (traduction ou synonyme)
- `PATCH /api/database/addword` : Ajout d’un mot nouveau avec lien vers un existant
- `PATCH /api/database/addrelation` : Ajout de lien entre deux mots existants

---

## 📚 Documentation API

La documentation OpenAPI (Swagger) est générée automatiquement et accessible localement via :

```
http://localhost:8082/swagger-ui.html
```

Toutes les routes sont sécurisées avec JWT (authentification obligatoire sauf connexion).

---

## 🛠️ Installation et développement

### Prérequis

- Node.js >= 20
- Angular CLI >= 19
- Java 21
- Maven

### Backend

```bash
cd backend/
./mvnw spring-boot:run
```

### Frontend

```bash
cd frontend/VocTech/
npm install
ng serve
```

---

## ✅ À faire / Roadmap

- [ ] Gestion des profils utilisateurs
- [ ] RGPD, Accessibilité Aria,...
- [ ] Gestion des abonnements
- [ ] Peuplement de la BDD
- [ ] Amélioration version mobile

---

## 📄 Licence

Ce projet est publié sous licence MIT — voir le fichier `LICENSE` pour plus d’informations.

---

## 🙏 Remerciements

Projet personnel réalisé par [Tangi Le Cadre](https://le-cadre.net), à partir du travail de collecte de données de Patrick LE CADRE.
