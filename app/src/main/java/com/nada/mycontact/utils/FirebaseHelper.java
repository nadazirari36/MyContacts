package com.nada.mycontact.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FirebaseHelper {
    private static FirebaseHelper instance;
    private final FirebaseFirestore db;
    private static final String COLLECTION_CONTACTS = "contacts";

    private FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public String getCollectionName() {
        return COLLECTION_CONTACTS;
    }

    // Query simple sans orderBy (pour éviter les erreurs d'index)
    public Query getContactsQuery() {
        return db.collection(COLLECTION_CONTACTS);
    }

    public Query getFavoritesQuery() {
        return db.collection(COLLECTION_CONTACTS)
                .whereEqualTo("isFavorite", true);
    }
}