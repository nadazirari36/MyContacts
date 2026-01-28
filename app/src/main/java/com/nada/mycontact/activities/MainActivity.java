package com.nada.mycontact.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.nada.mycontact.R;
import com.nada.mycontact.adapters.ContactsAdapter;
import com.nada.mycontact.databinding.ActivityMainBinding;
import com.nada.mycontact.models.Contact;
import com.nada.mycontact.models.ContactListItem;
import com.nada.mycontact.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private ContactsAdapter adapter;
    private List<Contact> contactsList = new ArrayList<>();
    private List<Contact> filteredContactsList = new ArrayList<>();
    private List<ContactListItem> displayItems = new ArrayList<>();
    private ListenerRegistration contactsListener;
    private boolean showFavoritesOnly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.d(TAG, "MainActivity created");

        setupRecyclerView();
        setupSearchView();
        setupFab();
        loadContacts();
    }

    private void setupRecyclerView() {
        adapter = new ContactsAdapter(displayItems, contact -> {
            Log.d(TAG, "Contact clicked: " + contact.getFullName());
            Log.d(TAG, "Contact ID: " + contact.getId());

            if (contact.getId() == null || contact.getId().isEmpty()) {
                Toast.makeText(this, "Erreur: ID du contact manquant", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(MainActivity.this, ContactDetailsActivity.class);
            intent.putExtra("contactId", contact.getId());
            startActivity(intent);
        });

        binding.recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewContacts.setAdapter(adapter);

        Log.d(TAG, "RecyclerView configured");
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterContacts(newText);
                return true;
            }
        });
    }

    private void setupFab() {
        binding.fabAddContact.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditContactActivity.class);
            startActivity(intent);
        });
    }

    private void loadContacts() {
        Log.d(TAG, "Loading contacts from Firestore...");

        contactsListener = FirebaseHelper.getInstance()
                .getDb()
                .collection(FirebaseHelper.getInstance().getCollectionName())
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading contacts", error);
                        Toast.makeText(this, "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        Log.d(TAG, "Received " + value.size() + " documents");

                        contactsList.clear();

                        for (DocumentSnapshot doc : value.getDocuments()) {
                            try {
                                Contact contact = doc.toObject(Contact.class);
                                if (contact != null) {
                                    contact.setId(doc.getId());
                                    contactsList.add(contact);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing contact", e);
                            }
                        }

                        // Trier par ordre alphabétique
                        sortContacts();

                        // Mettre à jour l'affichage
                        filteredContactsList.clear();
                        filteredContactsList.addAll(contactsList);
                        updateDisplayList();
                    }
                });
    }

    private void sortContacts() {
        Collections.sort(contactsList, new Comparator<Contact>() {
            @Override
            public int compare(Contact c1, Contact c2) {
                // Favoris en premier
                if (c1.isFavorite() && !c2.isFavorite()) return -1;
                if (!c1.isFavorite() && c2.isFavorite()) return 1;

                // Puis ordre alphabétique
                String name1 = c1.getFullName().toLowerCase();
                String name2 = c2.getFullName().toLowerCase();
                return name1.compareTo(name2);
            }
        });
    }

    private void filterContacts(String query) {
        filteredContactsList.clear();

        if (query == null || query.isEmpty()) {
            filteredContactsList.addAll(contactsList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Contact contact : contactsList) {
                String fullName = contact.getFullName().toLowerCase();
                String phone = contact.getPhoneNumber() != null ? contact.getPhoneNumber() : "";

                if (fullName.contains(lowerCaseQuery) || phone.contains(query)) {
                    filteredContactsList.add(contact);
                }
            }
        }

        updateDisplayList();
    }

    private void updateDisplayList() {
        displayItems.clear();

        if (filteredContactsList.isEmpty()) {
            binding.emptyView.setVisibility(View.VISIBLE);
            binding.recyclerViewContacts.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
            return;
        }

        binding.emptyView.setVisibility(View.GONE);
        binding.recyclerViewContacts.setVisibility(View.VISIBLE);

        // Séparer favoris et autres
        List<Contact> favorites = new ArrayList<>();
        List<Contact> others = new ArrayList<>();

        for (Contact contact : filteredContactsList) {
            if (contact.isFavorite()) {
                favorites.add(contact);
            } else {
                others.add(contact);
            }
        }

        // Ajouter la section HIGHLIGHTS (Favoris)
        if (!favorites.isEmpty()) {
            displayItems.add(new ContactListItem("HIGHLIGHTS"));
            for (Contact contact : favorites) {
                displayItems.add(new ContactListItem(contact));
            }
        }

        // Ajouter les autres contacts avec headers alphabétiques
        if (!others.isEmpty()) {
            char currentLetter = '\0';

            for (Contact contact : others) {
                String name = contact.getFullName();
                if (!name.isEmpty()) {
                    char firstLetter = Character.toUpperCase(name.charAt(0));

                    if (firstLetter != currentLetter) {
                        currentLetter = firstLetter;
                        displayItems.add(new ContactListItem(String.valueOf(currentLetter)));
                    }

                    displayItems.add(new ContactListItem(contact));
                }
            }
        }

        adapter.notifyDataSetChanged();
        Log.d(TAG, "Display list updated with " + displayItems.size() + " items");
    }


    protected void onDestroy() {
        super.onDestroy();
        if (contactsListener != null) {
            contactsListener.remove();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
    }
}
