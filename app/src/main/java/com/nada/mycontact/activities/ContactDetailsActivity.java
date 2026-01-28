package com.nada.mycontact.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.nada.mycontact.R;
import com.nada.mycontact.databinding.ActivityContactDetailsBinding;
import com.nada.mycontact.models.Contact;
import com.nada.mycontact.utils.FirebaseHelper;
import com.nada.mycontact.utils.ImageUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ContactDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ContactDetails";
    private ActivityContactDetailsBinding binding;
    private Contact contact;
    private String contactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.d(TAG, "=== ContactDetailsActivity Created ===");

        // Récupérer l'ID
        Intent intent = getIntent();
        contactId = intent.getStringExtra("contactId");

        Log.d(TAG, "Received contactId: " + contactId);

        if (contactId != null && !contactId.isEmpty()) {
            setupListeners();
            loadContactFromFirestore();
        } else {
            Log.e(TAG, "ERROR: No contactId provided!");
            Toast.makeText(this, "Erreur: ID manquant", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadContactFromFirestore() {
        Log.d(TAG, "Loading contact from Firestore with ID: " + contactId);

        FirebaseHelper.getInstance()
                .getDb()
                .collection(FirebaseHelper.getInstance().getCollectionName())
                .document(contactId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d(TAG, "Firestore query successful");

                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Document exists");

                        contact = documentSnapshot.toObject(Contact.class);

                        if (contact != null) {
                            contact.setId(documentSnapshot.getId());
                            Log.d(TAG, "Contact loaded: " + contact.getFullName());
                            displayContactInfo();
                        } else {
                            Log.e(TAG, "ERROR: Failed to parse contact");
                            showErrorAndFinish();
                        }
                    } else {
                        Log.e(TAG, "ERROR: Document does not exist");
                        showErrorAndFinish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "ERROR loading contact", e);
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void setupListeners() {
        Log.d(TAG, "Setting up listeners");

        // Bouton Retour
        binding.btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back clicked");
            finish();
        });

        // Bouton Modifier
        binding.btnEdit.setOnClickListener(v -> {
            Log.d(TAG, "Edit clicked");
            if (contactId != null) {
                Intent intent = new Intent(this, AddEditContactActivity.class);
                intent.putExtra("contactId", contactId);
                startActivity(intent);
            }
        });

        // Bouton Appeler
        binding.btnCall.setOnClickListener(v -> makePhoneCall());

        // Bouton Message
        binding.btnMessage.setOnClickListener(v -> sendSMS());

        // Bouton Email
        binding.btnEmail.setOnClickListener(v -> sendEmail());

        // ✨ NOUVEAU : Bouton Share
        binding.btnShare.setOnClickListener(v -> shareContact());

        // ✨ NOUVEAU : Bouton Block/Unblock
        binding.btnBlock.setOnClickListener(v -> toggleBlockContact());

        // Bouton Supprimer
        binding.btnDelete.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void displayContactInfo() {
        if (contact == null) {
            Log.e(TAG, "ERROR: Contact is null in displayContactInfo");
            return;
        }

        Log.d(TAG, "Displaying contact: " + contact.getFullName());

        // Nom
        binding.nameText.setText(contact.getFullName());

        // Photo ou initiales
        if (contact.getPhotoBase64() != null && !contact.getPhotoBase64().isEmpty()) {
            try {
                binding.photoImageView.setImageBitmap(
                        ImageUtils.base64ToBitmap(contact.getPhotoBase64())
                );
                binding.initialsText.setVisibility(View.GONE);
            } catch (Exception e) {
                Log.e(TAG, "Error loading photo", e);
                showInitials();
            }
        } else {
            showInitials();
        }

        // Favori
        binding.favoriteIcon.setVisibility(
                contact.isFavorite() ? View.VISIBLE : View.GONE
        );

        // Téléphone
        if (contact.getPhoneNumber() != null && !contact.getPhoneNumber().isEmpty()) {
            binding.phoneLayout.setVisibility(View.VISIBLE);
            binding.phoneDivider.setVisibility(View.VISIBLE);
            binding.phoneText.setText(contact.getPhoneNumber());
        } else {
            binding.phoneLayout.setVisibility(View.GONE);
            binding.phoneDivider.setVisibility(View.GONE);
        }

        // Email
        if (contact.getEmail() != null && !contact.getEmail().isEmpty()) {
            binding.emailLayout.setVisibility(View.VISIBLE);
            binding.emailDivider.setVisibility(View.VISIBLE);
            binding.emailText.setText(contact.getEmail());
        } else {
            binding.emailLayout.setVisibility(View.GONE);
            binding.emailDivider.setVisibility(View.GONE);
        }

        // URL
        if (contact.getUrl() != null && !contact.getUrl().isEmpty()) {
            binding.urlLayout.setVisibility(View.VISIBLE);
            binding.urlDivider.setVisibility(View.VISIBLE);
            binding.urlText.setText(contact.getUrl());
            binding.urlText.setOnClickListener(v -> openUrl());
        } else {
            binding.urlLayout.setVisibility(View.GONE);
            binding.urlDivider.setVisibility(View.GONE);
        }

        // Adresse
        if (contact.getAddress() != null && !contact.getAddress().isEmpty()) {
            binding.addressLayout.setVisibility(View.VISIBLE);
            binding.addressDivider.setVisibility(View.VISIBLE);
            binding.addressText.setText(contact.getAddress());
        } else {
            binding.addressLayout.setVisibility(View.GONE);
            binding.addressDivider.setVisibility(View.GONE);
        }

        // Date de naissance
        if (contact.getBirthday() != null) {
            binding.birthdayLayout.setVisibility(View.VISIBLE);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            binding.birthdayText.setText(sdf.format(contact.getBirthday().toDate()));
        } else {
            binding.birthdayLayout.setVisibility(View.GONE);
        }

        // Notes
        if (contact.getNotes() != null && !contact.getNotes().isEmpty()) {
            binding.notesCard.setVisibility(View.VISIBLE);
            binding.notesText.setText(contact.getNotes());
        } else {
            binding.notesCard.setVisibility(View.GONE);
        }

        // ✨ NOUVEAU : Mettre à jour le bouton Block/Unblock
        updateBlockButton();

        Log.d(TAG, "Contact info displayed successfully");
    }

    private void showInitials() {
        binding.initialsText.setText(contact.getInitials());
        binding.initialsText.setVisibility(View.VISIBLE);
    }

    private void makePhoneCall() {
        if (contact.getPhoneNumber() != null && !contact.getPhoneNumber().isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + contact.getPhoneNumber()));
            startActivity(intent);
        } else {
            Toast.makeText(this, "Aucun numéro de téléphone", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSMS() {
        if (contact.getPhoneNumber() != null && !contact.getPhoneNumber().isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + contact.getPhoneNumber()));
            startActivity(intent);
        } else {
            Toast.makeText(this, "Aucun numéro de téléphone", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail() {
        if (contact.getEmail() != null && !contact.getEmail().isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + contact.getEmail()));
            startActivity(intent);
        } else {
            Toast.makeText(this, "Aucune adresse email", Toast.LENGTH_SHORT).show();
        }
    }

    private void openUrl() {
        if (contact.getUrl() != null && !contact.getUrl().isEmpty()) {
            String url = contact.getUrl();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        }
    }

    // ✨ NOUVEAU : Partager le contact
    private void shareContact() {
        if (contact == null) return;

        Log.d(TAG, "Sharing contact: " + contact.getFullName());

        // Créer le texte à partager
        StringBuilder shareText = new StringBuilder();
        shareText.append("📇 Contact Information\n\n");
        shareText.append("Name: ").append(contact.getFullName()).append("\n");

        if (contact.getPhoneNumber() != null && !contact.getPhoneNumber().isEmpty()) {
            shareText.append("Phone: ").append(contact.getPhoneNumber()).append("\n");
        }

        if (contact.getEmail() != null && !contact.getEmail().isEmpty()) {
            shareText.append("Email: ").append(contact.getEmail()).append("\n");
        }

        if (contact.getAddress() != null && !contact.getAddress().isEmpty()) {
            shareText.append("Address: ").append(contact.getAddress()).append("\n");
        }

        if (contact.getUrl() != null && !contact.getUrl().isEmpty()) {
            shareText.append("Website: ").append(contact.getUrl()).append("\n");
        }

        // Créer l'intent de partage
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Contact: " + contact.getFullName());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());

        // Afficher le sélecteur de partage
        startActivity(Intent.createChooser(shareIntent, "Partager le contact via"));
    }

    // ✨ NOUVEAU : Bloquer/Débloquer le contact
    private void toggleBlockContact() {
        if (contact == null) return;

        boolean newBlockedState = !contact.isBlocked();
        contact.setBlocked(newBlockedState);

        Log.d(TAG, "Toggling block status to: " + newBlockedState);

        // Mettre à jour dans Firestore
        FirebaseHelper.getInstance()
                .getDb()
                .collection(FirebaseHelper.getInstance().getCollectionName())
                .document(contactId)
                .update("blocked", newBlockedState)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Block status updated successfully");
                    updateBlockButton();

                    String message = newBlockedState ?
                            contact.getFullName() + " a été bloqué" :
                            contact.getFullName() + " a été débloqué";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating block status", e);
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Annuler le changement local
                    contact.setBlocked(!newBlockedState);
                });
    }

    // ✨ NOUVEAU : Mettre à jour l'apparence du bouton Block
    private void updateBlockButton() {
        if (contact == null) return;

        if (contact.isBlocked()) {
            binding.blockText.setText("Unblock contact");
            binding.blockIcon.setImageResource(R.drawable.ic_unblock);
        } else {
            binding.blockText.setText("Block contact");
            binding.blockIcon.setImageResource(R.drawable.ic_block);
        }
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer le contact")
                .setMessage("Êtes-vous sûr de vouloir supprimer " + contact.getFullName() + " ?")
                .setPositiveButton("Supprimer", (dialog, which) -> deleteContact())
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void deleteContact() {
        Log.d(TAG, "Deleting contact: " + contactId);

        FirebaseHelper.getInstance()
                .getDb()
                .collection(FirebaseHelper.getInstance().getCollectionName())
                .document(contactId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Contact deleted successfully");
                    Toast.makeText(this, "Contact supprimé", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting contact", e);
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showErrorAndFinish() {
        Toast.makeText(this, "Contact introuvable", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (contactId != null) {
            loadContactFromFirestore();
        }
    }
}