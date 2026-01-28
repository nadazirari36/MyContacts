📱 MyContact — Application Android de Gestion de Contacts
📖 Description

MyContact est une application Android moderne de gestion de contacts développée en Java avec Firebase Cloud Firestore.
Elle propose une interface élégante en Dark Mode, inspirée de l'interface d'applications natives iOS , offrant une expérience utilisateur fluide, intuitive et professionnelle.

🎥 Démonstration vidéo

👉 (Ajouter votre vidéo ici)

[▶️ Voir la démonstration vidéo](media/demo.webm)

✨ Fonctionnalités
🔹 Gestion des contacts (CRUD)

➕ Ajouter un nouveau contact

📄 Consulter les détails d’un contact

✏️ Modifier les informations

🗑️ Supprimer un contact

📸 Photo de profil personnalisée

⭐ Marquer un contact comme favori (HIGHLIGHTS)

🔤 Tri alphabétique automatique (A–Z)

🔍 Recherche instantanée (nom / numéro)

🔹 Actions rapides

📞 Appel téléphonique direct

💬 Envoi de SMS

✉️ Envoi d’email

📤 Partage de contact (WhatsApp, SMS, Email…)

🚫 Blocage / déblocage de contacts

🌐 Ouverture de liens URL

🔹 Interface utilisateur

🌙 Dark Mode activé par défaut

🎨 Material Design 3

⚡ Animations fluides

📱 Compatible avec toutes tailles d’écran

🛠️ Technologies utilisées

Langage : Java

SDK Android : API 24 → 34

Architecture : MVC

Base de données : Firebase Cloud Firestore

UI : Material Design 3, RecyclerView, ViewBinding

📚 Bibliothèques principales

// Material Design
implementation 'com.google.android.material:material:1.11.0'

// Firebase
implementation platform('com.google.firebase:firebase-bom:32.7.0')
implementation 'com.google.firebase:firebase-firestore'

// Image circulaire
implementation 'de.hdodenhof:circleimageview:3.1.0'

🚀 Installation et exécution
✅ Prérequis

Android Studio (Arctic Fox ou supérieur)

JDK 8+

Compte Google (pour Firebase)

Émulateur Android ou appareil physique (API 24+)

📥 Étapes

# Cloner le projet
git clone https://github.com/nadazirari36/MyContacts.git
cd MyContact

1-Ouvrir le projet dans Android Studio

2-Configurer Firebase (voir section suivante)

3-Synchroniser Gradle

4-Lancer l’application

🔥 Configuration Firebase

⚠️ Important
Le fichier google-services.json n’est pas inclus dans ce dépôt pour des raisons de sécurité.

🔧 Étapes

1-Créer un projet sur Firebase Console

2-Ajouter une application Android

Package : com.nada.mycontact

3-Télécharger google-services.json

4-Placer le fichier dans :

app/google-services.json

5-Activer Cloud Firestore

📁 Structure du projet

MyContact/
├── app/
│   ├── src/main/java/com/nada/mycontact/
│   │   ├── activities/
│   │   ├── adapters/
│   │   ├── models/
│   │   └── utils/
│   ├── res/
│   │   ├── layout/
│   │   ├── drawable/
│   │   └── values/
│   └── google-services.json (non inclus)
├── media/
│   └── demo.mp4
└── README.md

🗄️ Structure Firestore

contacts (collection)
└── {contactId}
├── firstName : String
├── lastName : String
├── phoneNumber : String
├── email : String
├── isFavorite : Boolean
├── isBlocked : Boolean
├── photoBase64 : String
├── createdAt : Timestamp
└── updatedAt : Timestamp

👩‍💻 Auteur

<div align="center"> ⭐ Projet réalisé avec passion par Nada ⭐ </div>

