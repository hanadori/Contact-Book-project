package com.example.contactbookproject;

import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> implements Filterable {

    private List<Contact> contactList = new ArrayList<>();
    private List<Contact> contactListFull = new ArrayList<>(); // Copy of the full list
    private OnItemClickListener listener;
    private String currentSearchText = ""; // To hold the search text for highlighting

    // Interface for click events
    public interface OnItemClickListener {
        void onItemClick(Contact contact);
    }

    // Set the click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Method to update the list of contacts in the adapter
    public void setContacts(List<Contact> contacts) {
        this.contactList = contacts;
        this.contactListFull = new ArrayList<>(contacts); // Create a copy
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view, listener, contactList);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact currentContact = contactList.get(position);
        
        // Highlight search text in the name
        String name = currentContact.getName();
        if (currentSearchText != null && !currentSearchText.isEmpty()) {
            int startPos = name.toLowerCase().indexOf(currentSearchText.toLowerCase());
            if (startPos != -1) {
                int endPos = startPos + currentSearchText.length();
                String highlightedName = name.substring(0, startPos) +
                        "<font color='#3F51B5'><b>" + name.substring(startPos, endPos) + "</b></font>" +
                        name.substring(endPos);
                holder.tvContactName.setText(Html.fromHtml(highlightedName, Html.FROM_HTML_MODE_LEGACY));
            } else {
                holder.tvContactName.setText(name);
            }
        } else {
            holder.tvContactName.setText(name);
        }

        holder.tvContactPhone.setText(currentContact.getPhone());
        
        holder.ivContactPhoto.clearColorFilter();

        if ("Male".equals(currentContact.getGender())) {
            holder.ivContactPhoto.setImageResource(R.drawable.maledefaultpicture);
        } else if ("Female".equals(currentContact.getGender())) {
            holder.ivContactPhoto.setImageResource(R.drawable.femaledefaultpicture);
        } else {
            holder.ivContactPhoto.setImageResource(R.drawable.sparkledesign);
        }
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    @Override
    public Filter getFilter() {
        return contactFilter;
    }

    private Filter contactFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Contact> filteredList = new ArrayList<>();
            currentSearchText = constraint == null ? "" : constraint.toString();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(contactListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Contact item : contactListFull) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            contactList.clear();
            contactList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivContactPhoto;
        private final TextView tvContactName;
        private final TextView tvContactPhone;

        public ContactViewHolder(@NonNull View itemView, final OnItemClickListener listener, final List<Contact> contactList) {
            super(itemView);
            ivContactPhoto = itemView.findViewById(R.id.ivContactPhoto);
            tvContactName = itemView.findViewById(R.id.tvContactName);
            tvContactPhone = itemView.findViewById(R.id.tvContactPhone);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            // Use the filtered list
                            listener.onItemClick(contactList.get(position));
                        }
                    }
                }
            });
        }
    }
}