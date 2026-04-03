# heptathlon-client-server

Projet Java client-serveur pour la gestion simplifiée d'un magasin d'articles de sport. L'application repose sur une architecture RMI avec un client Swing, un serveur Java, une base MySQL et un dossier d'échange `head-office/` simulant le siège pour les mises à jour de prix et les sauvegardes de factures.

## Fonctionnalités

- consultation du catalogue et recherche des produits par référence ou par famille
- gestion du stock
- achat d'un ou plusieurs articles avec création de facture
- paiement de facture avec mode de paiement
- consultation des factures et du chiffre d'affaires journalier
- export local d'une facture depuis l'interface client
- sauvegarde des factures vers le dossier `head-office/backups`
- mise à jour des prix à partir du fichier `head-office/incoming/price-updates.csv`

## Architecture

- `src/main/java/heptathlon/client` : client graphique Swing
- `src/main/java/heptathlon/server` : serveur RMI, services métier, accès aux données
- `src/main/java/heptathlon/common` : contrats distants et modèles partagés
- `database/schema.sql` : schéma SQL et données d'initialisation
- `head-office/` : fichiers d'échange simulant le siège

## Prérequis

- Java 17
- MySQL accessible en local
- Maven

Par défaut, l'application utilise les paramètres suivants pour la base de données :

- URL : `jdbc:mysql://localhost:3306/heptathlon`
- utilisateur : `root`
- mot de passe : `root`

Ces valeurs peuvent être surchargées via des variables d'environnement.

## Variables d'environnement utiles

- `HEPTATHLON_DB_URL`
- `HEPTATHLON_DB_SERVER_URL`
- `HEPTATHLON_DB_USER`
- `HEPTATHLON_DB_PASSWORD`
- `HEPTATHLON_DB_RESET_ON_START`
- `HEPTATHLON_HEAD_OFFICE_PRICE_FILE`
- `HEPTATHLON_HEAD_OFFICE_BACKUP_DIR`
- `HEPTATHLON_PRICE_UPDATE_INTERVAL_SECONDS`
- `HEPTATHLON_INVOICE_BACKUP_INTERVAL_SECONDS`

## Initialisation de la base

Le serveur initialise la base au démarrage.

- si `HEPTATHLON_DB_RESET_ON_START=true`, le script `database/schema.sql` est rejoué
- sinon, la base, les tables et les produits de démonstration sont initialisés automatiquement

## Lancer le projet

Depuis la racine du projet, les scripts suivants permettent d'exécuter les tâches Maven principales :

```bat
scripts\test.cmd
scripts\package.cmd
scripts\site.cmd
```

Pour démarrer l'application :

1. lancer d'abord `heptathlon.server.ServerMain`
2. lancer ensuite `heptathlon.client.ClientMain`

Le serveur publie le service RMI `StoreService` sur le port `1099`.

## Site Maven et rapports

Le projet génère un site Maven avec :

- informations projet
- rapports Surefire
- couverture JaCoCo
- rapport Checkstyle
- navigation dans le code source via JXR

Après génération du site, ouvrir :

```text
target\site\index.html
```

## Fichiers utiles

- [BUILD.md](BUILD.md)
- [pom.xml](pom.xml)
- [schema.sql](database/schema.sql)

## À propos

Ce projet illustre une architecture client-serveur simple pour un contexte de magasin :

- communication distante via Java RMI
- séparation client / contrat commun / serveur
- persistance relationnelle avec MySQL
- automatisation de traitements périodiques côté serveur

Il sert à la fois de support pédagogique et de base fonctionnelle pour manipuler catalogue, stock, achats et facturation.
