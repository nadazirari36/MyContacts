package com.nada.mycontact.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.nada.mycontact.R;
import com.nada.mycontact.databinding.ActivityAddEditContactBinding;
import com.nada.mycontact.models.Contact;
import com.nada.mycontact.utils.FirebaseHelper;
import com.nada.mycontact.utils.ImageUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEditContactActivity extends AppCompatActivity {

    private ActivityAddEditContactBinding binding;
    private Contact currentContact;
    private boolean isEditMode = false;
    private Bitmap selectedPhotoBitmap;
    private Calendar selectedBirthday;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupImagePicker();
        checkEditMode();
        setupListeners();
        setupTextWatchers();
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            selectedPhotoBitmap = ImageUtils.resizeBitmap(bitmap, 400);
                            binding.photoImageView.setImageBitmap(selectedPhotoBitmap);
                            binding.photoInitialsText.setVisibility(View.GONE);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void checkEditMode() {
        Intent intent = getIntent();

        // Vérifier si on reçoit un ID
        String contactId = intent.getStringExtra("contactId");

        if (contactId != null && !contactId.isEmpty()) {
            isEditMode = true;
            binding.titleText.setText(R.string.edit_contact);
            loadContactFromFirestore(contactId);
        } else {
            isEditMode = false;
            currentContact = new Contact();
            binding.titleText.setText(R.string.new_contact);
        }
    }

    private void loadContactFromFirestore(String contactId) {
        FirebaseHelper.getInstance()
                .getDb()
                .collection(FirebaseHelper.getInstance().getCollectionName())
                .document(contactId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentContact = documentSnapshot.toObject(Contact.class);
                        if (currentContact != null) {
                            currentContact.setId(documentSnapshot.getId());
                            populateFields();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void populateFields() {
        if (currentContact == null) return;

        binding.firstNameEditText.setText(currentContact.getFirstName());
        binding.lastNameEditText.setText(currentContact.getLastName());
        binding.phoneEditText.setText(currentContact.getPhoneNumber());
        binding.emailEditText.setText(currentContact.getEmail());
        binding.urlEditText.setText(currentContact.getUrl());
        binding.addressEditText.setText(currentContact.getAddress());
        binding.notesEditText.setText(currentContact.getNotes());
        binding.favoriteSwitch.setChecked(currentContact.isFavorite());

        // Photo
        if (currentContact.getPhotoBase64() != null && !currentContact.getPhotoBase64().isEmpty()) {
            selectedPhotoBitmap = ImageUtils.base64ToBitmap(currentContact.getPhotoBase64());
            binding.photoImageView.setImageBitmap(selectedPhotoBitmap);
            binding.photoInitialsText.setVisibility(View.GONE);
        } else {
            updateInitialsDisplay();
        }

        // Birthday
        if (currentContact.getBirthday() != null) {
            selectedBirthday = Calendar.getInstance();
            selectedBirthday.setTime(currentContact.getBirthday().toDate());
            updateBirthdayDisplay();
        }
    }

    private void setupListeners() {
        // Bouton Annuler
        binding.btnCancel.setOnClickListener(v -> finish());

        // Bouton Enregistrer
        binding.btnSave.setOnClickListener(v -> saveContact());

        // Bouton Ajouter Photo
        binding.btnAddPhoto.setOnClickListener(v -> openImagePicker());

        // Date de naissance
        binding.birthdayEditText.setOnClickListener(v -> showDatePicker());
    }

    private void setupTextWatchers() {
        TextWatcher initialsWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (selectedPhotoBitmap == null) {
                    updateInitialsDisplay();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.firstNameEditText.addTextChangedListener(initialsWatcher);
        binding.lastNameEditText.addTextChangedListener(initialsWatcher);
    }

    private void updateInitialsDisplay() {
        String firstName = binding.firstNameEditText.getText().toString().trim();
        String lastName = binding.lastNameEditText.getText().toString().trim();

        StringBuilder initials = new StringBuilder();
        if (!firstName.isEmpty()) {
            initials.append(firstName.charAt(0));
        }
        if (!lastName.isEmpty()) {
            initials.append(lastName.charAt(0));
        }

        if (initials.length() > 0) {
            binding.photoInitialsText.setText(initials.toString().toUpperCase());
            binding.photoInitialsText.setVisibility(View.VISIBLE);
        } else {
            binding.photoInitialsText.setText("");
            binding.photoInitialsText.setVisibility(View.GONE);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void showDatePicker() {
        Calendar calendar = selectedBirthday != null ? selectedBirthday : Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedBirthday = Calendar.getInstance();
                    selectedBirthday.set(year, month, dayOfMonth);
                    updateBirthdayDisplay();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void updateBirthdayDisplay() {
        if (selectedBirthday != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            binding.birthdayEditText.setText(sdf.format(selectedBirthday.getTime()));
        }
    }

    private void saveContact() {
        // Validation
        String firstName = binding.firstNameEditText.getText().toString().trim();
        String lastName = binding.lastNameEditText.getText().toString().trim();
        String phone = binding.phoneEditText.getText().toString().trim();

        if (firstName.isEmpty() && lastName.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer au moins un prénom ou un nom", Toast.LENGTH_SHORT).show();
            return;
        }

        // Remplir l'objet Contact
        currentContact.setFirstName(firstName);
        currentContact.setLastName(lastName);
        currentContact.setPhoneNumber(phone);
        currentContact.setEmail(binding.emailEditText.getText().toString().trim());
        currentContact.setUrl(binding.urlEditText.getText().toString().trim());
        currentContact.setAddress(binding.addressEditText.getText().toString().trim());
        currentContact.setNotes(binding.notesEditText.getText().toString().trim());
        currentContact.setFavorite(binding.favoriteSwitch.isChecked());
        currentContact.setUpdatedAt(Timestamp.now());

        // Birthday
        if (selectedBirthday != null) {
            currentContact.setBirthday(new Timestamp(selectedBirthday.getTime()));
        }

        // Photo
        if (selectedPhotoBitmap != null) {
            String photoBase64 = ImageUtils.bitmapToBase64(selectedPhotoBitmap);
            currentContact.setPhotoBase64(photoBase64);
        }

        // Sauvegarder dans Firestore
        if (isEditMode) {
            updateContactInFirestore();
        } else {
            currentContact.setCreatedAt(Timestamp.now());
            addContactToFirestore();
        }
    }

    private void addContactToFirestore() {
        FirebaseHelper.getInstance()
                .getDb()
                .collection(FirebaseHelper.getInstance().getCollectionName())
                .add(currentContact)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Contact ajouté", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateContactInFirestore() {
        FirebaseHelper.getInstance()
                .getDb()
                .collection(FirebaseHelper.getInstance().getCollectionName())
                .document(currentContact.getId())
                .set(currentContact)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Contact mis à jour", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}