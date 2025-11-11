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
<img width="945" height="129" alt="image" src="https://github.com/user-attachments/assets/f657f23f-f748-4902-9e51-36216cba6bdf" />

### Test des APIs
<img width="945" height="757" alt="image" src="https://github.com/user-attachments/assets/c9d6e323-5508-43f2-b4d0-53b143394ee4" />
<img width="945" height="727" alt="image" src="https://github.com/user-attachments/assets/d962e77a-1e70-439d-b44e-b305b531ed35" />

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

## 📡 6. Configuration de Prometheus

La collecte des métriques applicatives pour le benchmark est entièrement gérée par **Prometheus**.  
Le fichier `prometheus.yml` définit les endpoints exposés par chaque service REST et les paramètres de scraping.

---

### ⚙️ Rôle du fichier `prometheus.yml`
- Spécifie **les targets** à surveiller (chaque variante REST : Jersey, Spring MVC, Spring Data REST).  
- Configure **la fréquence de scraping** (intervalle entre chaque collecte de métriques).  
- Définit les labels et jobs pour organiser les données dans Grafana.
<img width="945" height="468" alt="image" src="https://github.com/user-attachments/assets/558aa7b2-ecf3-42ee-ac06-20595cb7a2ab" />
<img width="945" height="619" alt="image" src="https://github.com/user-attachments/assets/4705820f-34cf-403e-8194-dc254fcdcbfd" />

<img width="945" height="409" alt="image" src="https://github.com/user-attachments/assets/72c15041-c922-4927-ab3a-d93a936d04f0" />

<img width="902" height="498" alt="image" src="https://github.com/user-attachments/assets/57504479-345d-4a61-a0ce-e68a351cb484" />
<img width="895" height="502" alt="image" src="https://github.com/user-attachments/assets/30bab91a-ceca-4367-aa09-6e4ee231d15e" />

<img width="886" height="488" alt="image" src="https://github.com/user-attachments/assets/f0fc9a54-8440-402a-be95-7c54d26f58e9" />


## 🧪 7. Scénarios de tests JMeter

Les benchmarks de performance ont été réalisés avec **Apache JMeter (v5.6.3)** afin de simuler différents types de charge sur les endpoints REST des trois implémentations :  
- JAX-RS (Jersey)  
- Spring MVC  
- Spring Data REST  

Trois types de scénarios ont été définis pour représenter des profils d’utilisation distincts : **lecture intensive** et **requêtes volumineuses**.

---

### 📘 Scénario 1 — Lecture intensive (ReadHeavy)

Ce scénario vise à évaluer la capacité du serveur à répondre à un **grand nombre de requêtes GET simultanées**, en simulant un trafic fortement orienté lecture.

#### 🔹 Paramètres du Thread Group
- **Nombre d’utilisateurs (threads) :** 100  
- **Ramp-up period :** 60 secondes  
- **Durée totale du test :** 600 secondes  
- **Type de requêtes :** GET sur plusieurs endpoints  
- **Répétition :** Continue jusqu’à la fin de la durée définie  
- **Backend Listener :** Envoi des métriques vers InfluxDB

#### 🔹 Endpoints testés
- `GET /items?page=&size=`  
- `GET /items?categoryId=&page=&size=`  
- `GET /categories/{id}/items?page=&size=`  
- `GET /categories?page=&size=`

#### 🔹 Objectifs du test
- Mesurer **le throughput** (nombre de requêtes traitées par seconde)  
- Observer la **latence moyenne et maximale**  
- Suivre **l’utilisation CPU et mémoire** via Prometheus et Grafana  
- Identifier les **goulots d’étranglement liés aux lectures simultanées**

<img width="945" height="512" alt="image" src="https://github.com/user-attachments/assets/46773384-c605-43b9-a19b-f33c3e9d9e7d" />

---

### 📘 Scénario 2 — HeavyBody (Requêtes volumineuses)

Ce scénario simule des **POST/PUT avec des corps JSON lourds** (~5 Ko par item) afin de tester la performance du serveur lors d’opérations d’écriture intensives.

#### 🔹 Paramètres du Thread Group
- **Nombre d’utilisateurs (threads) :** 50  
- **Ramp-up period :** 120 secondes  
- **Durée totale du test :** 600 secondes  
- **Type de requêtes :** POST / PUT sur les endpoints items  
- **Backend Listener :** InfluxDB pour collecte des métriques

#### 🔹 Objectifs du test
- Évaluer la **gestion des gros payloads** par le serveur  
- Mesurer l’impact sur **CPU, mémoire et threads actifs**  
- Vérifier la **stabilité de l’application** sous écriture lourde

---

💡 **Astuce :**  
Pour chaque scénario, les résultats sont envoyés automatiquement à **InfluxDB**, puis visualisés dans **Grafana** pour un suivi temps réel et une comparaison des trois implémentations REST.

<img width="945" height="494" alt="image" src="https://github.com/user-attachments/assets/a18fbd05-6da3-48a1-9f05-9e6874a42022" />


### 📘 Scénario 3 — Join & Filter

Ce scénario simule des requêtes GET complexes combinant **jointures et filtres** sur les entités `Category` et `Item`.  
L’objectif est de mesurer les performances du serveur lors de requêtes SQL plus lourdes et de vérifier la latence côté API.

#### 🔹 Paramètres du Thread Group
- **Nombre d’utilisateurs (threads) :** 60  
- **Ramp-up period :** 90 secondes  
- **Durée totale du test :** 600 secondes  
- **Type de requêtes :** GET avec paramètres de filtrage et pagination  
- **Backend Listener :** Envoi des métriques vers InfluxDB

#### 🔹 Endpoints testés
- `GET /items?categoryId=&page=&size=&filter=price>100`  
- `GET /categories/{id}/items?page=&size=&filter=name~"Widget"`  
- `GET /items?page=&size=&filter=stock<50`  

#### 🔹 Objectifs du test
- Mesurer la **latence et le throughput** sur des requêtes filtrées et avec jointures  
- Observer l’impact des **requêtes complexes sur CPU et mémoire**  
- Comparer les performances des trois variantes REST lors d’opérations de lecture filtrées  

💡 **Astuce :**  
- Les filtres simulés peuvent être adaptés pour tester différents cas d’usage (prix, stock, texte)  
- Les métriques collectées via InfluxDB et visualisées sur Grafana permettent de détecter rapidement les goulots d’étranglement liés aux JOIN et aux filtres complexes.

![WhatsApp Image 2025-11-11 à 22 40 16_99493623](https://github.com/user-attachments/assets/b4a1fd7b-e0ba-406c-9031-4e0a6d65dfcc)
### 📘 Scénario 4 — Mixed (Lecture + Écriture + Filtrage)

Ce scénario simule un **trafic mixte**, combinant des requêtes GET, POST et PUT afin de reproduire un profil utilisateur réaliste.  
Il permet de tester la **capacité globale du serveur** sous une charge variée.

#### 🔹 Paramètres du Thread Group
- **Nombre d’utilisateurs (threads) :** 80  
- **Ramp-up period :** 120 secondes  
- **Durée totale du test :** 600 secondes  
- **Type de requêtes :** GET (lecture), POST/PUT (écriture), GET avec filtres (jointures)  
- **Backend Listener :** InfluxDB pour collecte des métriques en temps réel

#### 🔹 Endpoints testés
- `GET /items?page=&size=` → lecture simple  
- `GET /items?categoryId=&page=&size=&filter=price>100` → lecture filtrée  
- `POST /items` → création d’items avec corps JSON (~5 Ko)  
- `PUT /items/{id}` → mise à jour d’items existants  
- `GET /categories/{id}/items?page=&size=&filter=stock<50` → jointures + filtrage  

#### 🔹 Objectifs du test
- Mesurer la **latence moyenne et maximale** sous un mix de requêtes concurrentes  
- Suivre l’impact des écritures et des lectures filtrées sur **CPU, mémoire et threads actifs**  
- Comparer les performances des trois implémentations REST sur un **profil utilisateur réaliste**  

💡 **Astuce :**  
- Ajuster le ratio GET/POST/PUT pour simuler différents profils de charge  
- Les métriques remontées dans Grafana permettent d’identifier rapidement les **points faibles et goulets d’étranglement**

![WhatsApp Image 2025-11-11 à 22 59 18_f5ffeda0](https://github.com/user-attachments/assets/59dcb90f-8976-448f-9230-7ba5a4621036)

####   Conclusion 

Ce projet compare les performances de trois implémentations REST Java : **JAX-RS (Jersey)**, **Spring MVC**, et **Spring Data REST**, à l’aide de **JMeter, Prometheus, Grafana, InfluxDB et Docker**.  
L’objectif est d’évaluer la capacité des serveurs à gérer des charges lourdes et des volumes importants de données.

#### Réalisées par : 

EL OUAHI Najat ET
HAILALA Yassmin
