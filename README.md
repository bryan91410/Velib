# Projet Velib - BTS SIO SLAM

Application JavaFX permettant de consulter les disponibilites des stations Velib en temps reel a partir de l'API Open Data Paris.

Ce projet a ete realise dans le cadre de l'epreuve E6 du BTS SIO option SLAM. Il met en pratique la programmation orientee objet, la consommation d'une API REST, le traitement de donnees JSON et la creation d'une interface graphique avec JavaFX.

## Objectif du projet

L'objectif de l'application est de permettre a un utilisateur de visualiser les stations Velib et leurs disponibilites.

L'utilisateur peut :

- consulter la liste des stations Velib ;
- filtrer les stations par commune ;
- filtrer les stations par departement ;
- filtrer les stations mobiles ou fixes ;
- selectionner une station ;
- afficher les informations detaillees de la station selectionnee.

## Technologies utilisees

- Java 17
- JavaFX 21
- Maven
- FXML
- CSS
- API Open Data Paris
- Bibliotheque `org.json`

## Fonctionnalites principales

### Recuperation des donnees en temps reel

L'application interroge l'API publique Open Data Paris afin de recuperer les donnees de disponibilite Velib.

Les informations recuperees comprennent notamment :

- le code de la station ;
- le nom de la station ;
- la commune ou l'arrondissement ;
- le code INSEE ;
- la capacite totale ;
- le nombre de velos disponibles ;
- le nombre de velos mecaniques ;
- le nombre de velos electriques ;
- le nombre de places libres ;
- l'etat de la station.

### Interface graphique JavaFX

L'interface est construite avec JavaFX et FXML.

Elle est composee :

- d'un en-tete avec le titre de l'application ;
- de menus deroulants pour filtrer les stations ;
- d'une liste affichant les stations ;
- d'un panneau de details ;
- d'une zone d'information en bas de l'application.

### Filtrage des stations

L'utilisateur peut filtrer les stations selon plusieurs criteres :

- commune ;
- departement ;
- stations mobiles uniquement ;
- stations fixes uniquement.

Les valeurs de filtre sont generees automatiquement a partir des donnees recuperees depuis l'API.

### Affichage des details

Lorsqu'une station est selectionnee, l'application affiche :

- le numero de la station ;
- son statut ;
- la presence ou non d'une borne de paiement ;
- sa capacite totale ;
- le nombre total de velos disponibles ;
- le nombre de velos mecaniques ;
- le nombre de velos electriques ;
- le nombre de places libres.

## Architecture du projet

Le projet est organise en plusieurs packages.

```text
src/
├── app/
│   ├── Main.java
│   └── Launcher.java
├── model/
│   ├── Bike.java
│   ├── BikeStatus.java
│   ├── Rental.java
│   ├── Station.java
│   └── User.java
├── service/
│   ├── ConnectAPI.java
│   └── VelibService.java
└── ui/
    ├── MainController.java
    ├── MainView.fxml
    └── style.css
```

## Role des principaux fichiers

### `Main.java`

Classe principale de l'application JavaFX.

Elle charge le fichier FXML, cree la scene principale et affiche la fenetre de l'application.

### `Launcher.java`

Point d'entree secondaire permettant de lancer l'application plus facilement dans certains environnements de developpement.

### `MainView.fxml`

Fichier qui decrit la structure de l'interface graphique.

Il contient les composants JavaFX :

- `BorderPane`
- `VBox`
- `HBox`
- `Label`
- `ComboBox`
- `ListView`
- `TextArea`

### `MainController.java`

Controleur principal de l'interface.

Il gere :

- l'initialisation de l'ecran ;
- le chargement des stations ;
- les actions sur les filtres ;
- l'affichage de la liste ;
- l'affichage des details d'une station.

### `ConnectAPI.java`

Classe responsable de la connexion a l'API Open Data Paris.

Elle effectue les requetes HTTP, recupere les reponses JSON, gere la pagination et transforme les donnees en objets `Station`.

### `Station.java`

Classe modele representant une station Velib.

Elle contient les informations principales d'une station et des methodes utiles comme :

- `getDepartement()`
- `getNumBikesAvailable()`
- `getMechanical()`
- `getEbike()`
- `toString()`

### `VelibService.java`

Classe de service metier permettant de simuler la gestion de locations de velos.

Elle permet notamment :

- d'ajouter des stations ;
- d'ajouter des velos ;
- de louer un velo ;
- de rendre un velo ;
- de verifier si un utilisateur a deja une location active.

## Fonctionnement technique

Au lancement de l'application :

1. `Main.java` demarre l'application JavaFX.
2. Le fichier `MainView.fxml` est charge.
3. JavaFX instancie automatiquement `MainController`.
4. La methode `initialize()` du controleur est appelee.
5. Le controleur lance le chargement des stations via `ConnectAPI`.
6. `ConnectAPI` appelle l'API Open Data Paris.
7. Les reponses JSON sont analysees.
8. Chaque station JSON est transformee en objet `Station`.
9. Les stations sont stockees dans une liste.
10. L'interface affiche les stations et active les filtres.

Le chargement de l'API est realise dans un thread secondaire afin de ne pas bloquer l'interface graphique.

Les mises a jour de l'interface sont effectuees avec `Platform.runLater()`, car JavaFX impose que les modifications graphiques soient faites depuis le thread JavaFX.

## Installation et lancement

### Prerequis

- Java 17 ou superieur
- Maven
- Connexion Internet pour acceder a l'API Velib

### Lancer l'application

Depuis la racine du projet :

```bash
mvn javafx:run
```

## API utilisee

Le projet utilise le jeu de donnees public suivant :

```text
https://opendata.paris.fr/api/explore/v2.1/catalog/datasets/velib-disponibilite-en-temps-reel/records
```

Cette API fournit les disponibilites des stations Velib en temps reel.

## Points importants pour l'epreuve E6

Pour presenter ce projet, il faut etre capable d'expliquer :

- pourquoi l'application utilise JavaFX ;
- le role du fichier FXML ;
- le role du controleur ;
- comment les donnees sont recuperees depuis l'API ;
- comment le JSON est transforme en objets Java ;
- pourquoi le chargement API est fait dans un thread secondaire ;
- comment fonctionnent les filtres ;
- comment les details d'une station sont affiches ;
- comment les classes du modele representent les donnees metier.

Phrase de synthese possible :

```text
Mon application JavaFX consomme l'API Open Data Paris pour recuperer les disponibilites Velib en temps reel. Les donnees JSON sont transformees en objets Java, puis affichees dans une interface graphique permettant de filtrer les stations et de consulter leurs details.
```

## Ameliorations possibles

Plusieurs evolutions peuvent etre envisagees :

- ajouter une carte interactive ;
- ajouter une barre de recherche par nom de station ;
- ajouter un bouton de rafraichissement manuel ;
- enregistrer un historique des disponibilites en base de donnees ;
- ajouter des tests unitaires ;
- ameliorer la gestion des erreurs reseau ;
- separer davantage le modele API du modele metier ;
- ajouter une authentification utilisateur.

## Auteur

Projet realise dans le cadre du BTS SIO option SLAM.
