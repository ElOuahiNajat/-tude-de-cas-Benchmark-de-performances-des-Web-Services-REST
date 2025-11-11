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

<img width="1014" height="560" alt="image" src="https://github.com/user-attachments/assets/9fb4b2f4-9b19-4e3d-86c0-6ee665725f20" />
