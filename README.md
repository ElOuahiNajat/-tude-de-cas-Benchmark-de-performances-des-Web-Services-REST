# Benchmark — Web Services REST (Jersey / Spring MVC / Spring Data REST)

**Résumé :**  
Ce projet compare les performances de trois variantes REST Java :
- **A - Jersey** : implémentation JAX-RS (Grizzly).
- **C - Spring MVC** : Spring Boot avec contrôleurs MVC classiques.
- **D - Spring Data REST** : exposition automatique des entités via Spring Data REST.

Les tests de charge sont réalisés avec **Apache JMeter**. Les métriques applicatives (Micrometer) sont exportées vers **Prometheus** et affichées dans **Grafana**. Des collectors supplémentaires peuvent exporter vers **InfluxDB** si nécessaire. Le déploiement et l'orchestration se font via **Docker / docker-compose**.

---

## Fonctionnalités principales
- 3 variantes REST implémentées et packagées.
- Scénarios JMeter fournis (scripts `.jmx`) pour charges légères, mixtes et lourdes.
- Collecte de métriques via Micrometer → Prometheus.
- Dashboards Grafana préconfigurés (CPU, mémoire, latence, GC, threads, RPS).
- Docker Compose pour lancer rapidement l’environnement complet.

---

## Pré-requis
- Git
- Docker & Docker Compose (version récente)
- Java 17+ (pour compiler les variantes si vous ne voulez pas utiliser les images fournies)
- Maven (si compilation locale)
- Apache JMeter (pour exécuter les tests localement)

---

## Installation & lancement (rapide)

```bash
# cloner le dépôt
git clone https://github.com/ElOuahiNajat/-tude-de-cas-Benchmark-de-performances-des-Web-Services-REST.git
cd -tude-de-cas-Benchmark-de-performances-des-Web-Services-REST

# lancer l'environnement avec docker-compose (ex : compose déjà fourni)
docker compose up --build

```
<img width="1033" height="565" alt="image" src="https://github.com/user-attachments/assets/d9784e36-ea4f-496d-8a31-2f9f26b22d45" />
## 🧩 3. Structure du projet

L’architecture du projet est conçue de manière modulaire afin d’isoler chaque implémentation REST tout en mutualisant les éléments communs (scripts, configuration et monitoring).  
Chaque module peut être exécuté et analysé indépendamment pour une comparaison précise des performances.
projet/
├── variant-a-jersey/           # Implémentation JAX-RS (Jersey + Grizzly) - légère et modulaire
├── variant-c-spring-mvc/       # Implémentation REST via Spring MVC (@RestController) - équilibrée
├── variant-d-spring-data-rest/ # Implémentation Spring Data REST - rapide à développer
│
├── src/
│   ├── main/java/com/example/  # Code source principal commun
│   └── test/java/com/example/  # Tests unitaires et d’intégration
│
├── idea/                       # Configuration spécifique à IntelliJ IDEA
├── demo/                       # Fichiers et exemples de démonstration
├── jmeter/                     # Scénarios de tests de charge Apache JMeter (.jmx)
│
├── pom.xml                     # Fichier Maven principal pour la compilation et la gestion des dépendances
├── docker-compose.yml          # Déploiement des containers (Base de données, Prometheus, Grafana, API)
├── prometheus.yml              # Configuration de Prometheus pour l’export des métriques
│
├── .gitignore                  # Liste des fichiers ignorés par Git
├── README.md                   # Documentation principale du projet
├── Compte Rendu Benchmark.pdf  # Rapport détaillé d’analyse comparative des performances
├── category_ids.csv            # Données de test - identifiants des catégories
├── item_ids.csv                # Données de test - identifiants des produits
└── [autres fichiers]           # Scripts ou configurations supplémentaires

<img width="728" height="790" alt="image" src="https://github.com/user-attachments/assets/2e7b7200-0c95-409d-8dc0-59053460e063" />

## 🐳 4. Conteneurs Docker

L’environnement de benchmark est entièrement orchestré à l’aide de **Docker Compose**, garantissant une exécution reproductible et isolée des différents services.  
Chaque conteneur joue un rôle spécifique dans la collecte, le stockage et la visualisation des performances.

📈 Prometheus  →  Collecte les métriques exposées par les applications via Micrometer
📊 Grafana     →  Visualise en temps réel les données issues de Prometheus et InfluxDB
🧮 InfluxDB    →  Stocke les résultats bruts des tests de charge JMeter
🧰 API REST    →  L’une des trois variantes (Jersey, Spring MVC, Spring Data REST) testée
<img width="945" height="524" alt="image" src="https://github.com/user-attachments/assets/65589de0-b851-4f64-b20c-30529568dbc2" />
<img width="945" height="567" alt="image" src="https://github.com/user-attachments/assets/7f229f47-cbf7-45da-beaf-7d311393ba45" />
<img width="945" height="556" alt="image" src="https://github.com/user-attachments/assets/b3213867-5432-4504-8e87-9e52a6deddc0" />

## 📂 5. Jeu de données initial — DataSeeder

Le benchmark s’appuie sur un **jeu de données réaliste** généré automatiquement par la classe `DataSeeder`.  
L’objectif est de simuler un environnement applicatif proche d’une vraie application e-commerce avec un volume important d’enregistrements et des relations complexes.

---

### ⚙️ Génération du dataset

Le script `DataSeeder.java` insère un **volume conséquent de données** dans PostgreSQL pour reproduire un contexte réel :

| Élément                     | Détail                                   |
|------------------------------|-----------------------------------------|
| Nombre de catégories         | 2 000 (Category)                        |
| Nombre d’items par catégorie | 50 (Item)                                |
| Total d’items générés        | 100 000                                  |
| Taille moyenne des descriptions | 5 120 caractères (~5 Ko par item)    |
| Flush batch                  | 5 000 entités (optimisation JPA / mémoire) |

---

### 📜 Description du fonctionnement

Le seeder utilise **JPA (Jakarta Persistence)** via un `EntityManager` configuré avec `FlushModeType.COMMIT` afin de trouver un équilibre entre **performance et cohérence**.

#### 1️⃣ Création des catégories
- Boucle d’insertion de **2 000 entités `Category`**.
- Nettoyage régulier du contexte de persistance avec `em.flush()` et `em.clear()` tous les **500 enregistrements** pour limiter la consommation mémoire.

#### 2️⃣ Création des items
- Boucle imbriquée générant **50 `Item` par catégorie**.
- Référence directe de la catégorie via `em.getReference(Category.class, cid)` pour éviter les rechargements inutiles.
- Flush automatique tous les **5 000 items** pour réduire la consommation mémoire.

#### 3️⃣ Attributs simulés
- Champs : `sku`, `name`, `price`, `stock`, `description`, `category`.
- Les descriptions sont générées via `generateLorem(5120)` pour simuler un **corps JSON d’environ 5 Ko**, utile dans les scénarios POST/PUT lourds (`HeavyBody`).

---

### 📊 Objectifs du dataset
- Reproduire des **volumes comparables à un environnement e-commerce réel**.
- Évaluer les performances sur :
  - Relations **N:1 / 1:N** (Category → Item)
  - Requêtes **JOIN** et filtrées
  - Gestion des **corps JSON volumineux** dans les opérations d’écriture
  - Comportement de l’application sous **charge intensive** (scénarios JMeter)

---

💡 **Astuce :**  
Ce dataset permet de tester efficacement :
- La scalabilité des différentes implémentations REST
- L’impact des flush batch sur la mémoire et le temps de traitement
- Les performances des endpoints CRUD avec de gros volumes de données
  
<img width="745" height="211" alt="image" src="https://github.com/user-attachments/assets/dc091eb8-ccba-4dd3-9dba-6e1003a7eef9" />
<img width="550" height="244" alt="image" src="https://github.com/user-attachments/assets/d45d5f54-8eb8-497b-8814-6ff5e531995d" />


