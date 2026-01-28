package com.nada.mycontact.adapters;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nada.mycontact.R;
import com.nada.mycontact.databinding.ItemContactBinding;
import com.nada.mycontact.models.Contact;
import com.nada.mycontact.models.ContactListItem;
import com.nada.mycontact.utils.ImageUtils;

import java.util.List;
import java.util.Random;

public class ContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ContactListItem> items;
    private final OnContactClickListener listener;

    public interface OnContactClickListener {
        void onContactClick(Contact contact);
    }

    public ContactsAdapter(List<ContactListItem> items, OnContactClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ContactListItem.TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_contact_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            ItemContactBinding binding = ItemContactBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ContactViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ContactListItem item = items.get(position);

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(item.getHeader());
        } else if (holder instanceof ContactViewHolder) {
            ((ContactViewHolder) holder).bind(item.getContact());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder pour les headers
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView headerText;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.headerText);
        }

        public void bind(String header) {
            headerText.setText(header);
        }
    }

    // ViewHolder pour les contacts
    class ContactViewHolder extends RecyclerView.ViewHolder {
        private final ItemContactBinding binding;

        public ContactViewHolder(ItemContactBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Contact contact) {
            // Afficher le nom
            binding.nameText.setText(contact.getFullName());

            // Gérer la photo ou les initiales
            if (contact.getPhotoBase64() != null && !contact.getPhotoBase64().isEmpty()) {
                Bitmap photo = ImageUtils.base64ToBitmap(contact.getPhotoBase64());
                binding.avatarImage.setImageBitmap(photo);
                binding.initialsText.setVisibility(View.GONE);
            } else {
                // Afficher les initiales avec couleur aléatoire
                binding.initialsText.setText(contact.getInitials());
                binding.initialsText.setVisibility(View.VISIBLE);
                binding.avatarImage.setColorFilter(getRandomColor());
            }

            // Afficher l'icône favori
            if (contact.isFavorite()) {
                binding.favoriteIcon.setVisibility(View.VISIBLE);
            } else {
                binding.favoriteIcon.setVisibility(View.GONE);
            }

            // ✨ NOUVEAU : Afficher l'icône bloqué
            // (Commentez ces lignes si vous n'avez pas ajouté blockedIcon dans le XML)
            /*
            if (contact.isBlocked()) {
                binding.blockedIcon.setVisibility(View.VISIBLE);
            } else {
                binding.blockedIcon.setVisibility(View.GONE);
            }
            */

            // Gérer le clic
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContactClick(contact);
                }
            });
        }

        private int getRandomColor() {
            Random random = new Random();
            int[] colors = {
                    Color.parseColor("#FF6B6B"),
                    Color.parseColor("#4ECDC4"),
                    Color.parseColor("#45B7D1"),
                    Color.parseColor("#FFA07A"),
                    Color.parseColor("#98D8C8"),
                    Color.parseColor("#F7DC6F"),
                    Color.parseColor("#BB8FCE"),
                    Color.parseColor("#85C1E2")
            };
            return colors[random.nextInt(colors.length)];
        }
    }
}