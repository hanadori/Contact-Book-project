package com.example.contactbookproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewContactActivity extends AppCompatActivity {

    private ImageView ivGenderPicture;
    private TextView tvContactNameView, tvPhoneNumberView, tvEmailView, tvBirthdayView, tvGenderView;
    private Button btnUpdateContactView;
    private ImageButton btnBackFromView;

    private Contact contact;
    private DatabaseReference mContactRef;
    private ValueEventListener mContactListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_contact);

        ivGenderPicture = findViewById(R.id.ivGenderPicture);
        tvContactNameView = findViewById(R.id.tvContactNameView);
        tvPhoneNumberView = findViewById(R.id.tvPhoneNumberView);
        tvEmailView = findViewById(R.id.tvEmailView);
        tvBirthdayView = findViewById(R.id.tvBirthdayView);
        tvGenderView = findViewById(R.id.tvGenderView);
        btnUpdateContactView = findViewById(R.id.btnUpdateContactView);
        btnBackFromView = findViewById(R.id.btnBackFromView);

        // Get the contact from the intent initially
        contact = (Contact) getIntent().getSerializableExtra("contact");

        if (contact != null) {
            populateContactDetails();
            attachDatabaseListener();
        }

        btnBackFromView.setOnClickListener(v -> finish());

        btnUpdateContactView.setOnClickListener(v -> {
            Intent intent = new Intent(ViewContactActivity.this, AddContactActivity.class);
            intent.putExtra("contact", contact);
            startActivity(intent);
        });
    }

    private void attachDatabaseListener() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        mContactRef = FirebaseDatabase.getInstance().getReference()
                .child("contacts")
                .child(currentUser.getUid())
                .child(contact.getId());

        mContactListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Contact updatedContact = snapshot.getValue(Contact.class);
                    if (updatedContact != null) {
                        contact = updatedContact; // Update the local contact object
                        populateContactDetails(); // Refresh UI
                    }
                } else {
                    // Contact was deleted
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewContactActivity.this, "Failed to refresh data", Toast.LENGTH_SHORT).show();
            }
        };
        mContactRef.addValueEventListener(mContactListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mContactRef != null && mContactListener != null) {
            mContactRef.removeEventListener(mContactListener);
        }
    }

    private void populateContactDetails() {
        tvContactNameView.setText(contact.getName());
        tvPhoneNumberView.setText(contact.getPhone());
        
        // Handle optional Email
        if (TextUtils.isEmpty(contact.getEmail())) {
            tvEmailView.setText("No email provided");
        } else {
            tvEmailView.setText(contact.getEmail());
        }

        // Handle optional Birthday
        if (TextUtils.isEmpty(contact.getBirthday())) {
            tvBirthdayView.setText("No birthday provided");
        } else {
            tvBirthdayView.setText(contact.getBirthday());
        }
        
        tvGenderView.setText(contact.getGender());

        if ("Male".equals(contact.getGender())) {
            ivGenderPicture.setImageResource(R.drawable.maledefaultpicture);
        } else if ("Female".equals(contact.getGender())) {
            ivGenderPicture.setImageResource(R.drawable.femaledefaultpicture);
        } else {
            ivGenderPicture.setImageResource(R.drawable.sparkledesign); // Fallback
        }
    }
}