# AppF1

**AppF1** est une application Android dédiée à la Formule 1. Elle permet de consulter les derniers résultats, les classements pilotes et constructeurs, ainsi qu'un widget affichant la position de Pierre Gasly.

## Fonctionnalités

- **Dernier résultat** : affiche le classement final de la dernière course démarrée.
- **Classement pilotes** : consulte les classements officiels par saison.
- **Classement constructeurs** : consulte les classements des écuries par saison.
- **Widget Android** : affiche le circuit, la session et la position de Pierre Gasly.
- **Mode hors ligne limité** : conserve le dernier résultat affiché sur l'écran principal.
- Interface française avec les couleurs et polices Formula 1.

## Technologies utilisées

- Java
- Android SDK (API minimum 26)
- AndroidX / Material Components
- Retrofit 2 et Gson
- OpenF1 API
- Gradle Kotlin DSL
- RecyclerView
- Widgets Android

## Architecture du projet

```text
app/src/main/java/com/example/f1widget2/
├── api/                 # Services Retrofit et configuration HTTP
├── models/              # Modèles des données OpenF1
├── adapters             # Adaptateurs des listes et classements
├── MainActivity.java    # Dernier résultat de course
├── DriverActivity.java  # Classement pilotes
├── ConstructorActivity.java # Classement constructeurs
├── MenuActivity.java    # Menu principal
└── GaslyWidgetProvider.java # Widget Android
```

## Prérequis

- Android Studio récent
- JDK 17 ou compatible avec le projet
- Android SDK 34
- Un émulateur Android ou un téléphone Android sous Android 8.0 (API 26) ou plus récent
- Une connexion Internet pour récupérer les données F1

## Installation

1. Cloner le dépôt :

```bash
git clone https://github.com/AyScorp/AppF1.git
cd AppF1
```

2. Ouvrir le dossier du projet dans Android Studio.

3. Laisser Gradle télécharger les dépendances.

4. Sélectionner un appareil ou émulateur, puis lancer l'application avec **Run**.

## Utilisation

Depuis le menu principal, choisissez une des options :

- **Dernier Résultat** : résultat de la course la plus récente disponible.
- **Pilotes** : sélectionnez une saison puis consultez son classement.
- **Constructeurs** : sélectionnez une saison puis consultez son classement.

### Ajouter le widget

1. Depuis l'écran d'accueil Android, appuyez longtemps sur un emplacement vide.
2. Ouvrez la liste des widgets.
3. Recherchez le widget associé à **F1Widget2**.
4. Faites glisser le widget sur l'écran d'accueil.

## API

Les données sont récupérées auprès de [OpenF1](https://openf1.org/), une API publique fournissant des informations sur les sessions, pilotes, positions et classements.

Aucune clé API n'est nécessaire pour utiliser cette version.

## Construction

Depuis Windows :

```bat
gradlew.bat assembleDebug
```

L'APK de développement est généré dans :

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Auteur

Projet développé par AyScorp.

## Licence

Ce projet est actuellement distribué sans fichier de licence. En l’absence de licence explicite, les droits par défaut s’appliquent.
