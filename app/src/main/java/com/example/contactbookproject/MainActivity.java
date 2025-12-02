package com.example.contactbookproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ImageButton btnProfile;
    private FloatingActionButton fabAddContact;
    private RecyclerView rvContacts;
    private TextView tvEmptyTitle, tvEmptySubtitle;
    private TextInputLayout tilSearch;
    private TextInputEditText etSearch;
    private ContactAdapter contactAdapter;
    private List<Contact> contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        btnProfile = findViewById(R.id.btnProfile);
        fabAddContact = findViewById(R.id.fabAddContact);
        rvContacts = findViewById(R.id.rvContacts);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptySubtitle = findViewById(R.id.tvEmptySubtitle);
        tilSearch = findViewById(R.id.tilSearch);
        etSearch = findViewById(R.id.etSearch);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setupRecyclerView();
        loadContacts(currentUser.getUid());
        setupSearch();

        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });

        fabAddContact.setOnClickListener(v -> {
            startActivity(new Intent(this, AddContactActivity.class));
        });
    }

    private void setupRecyclerView() {
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        contactList = new ArrayList<>();
        contactAdapter = new ContactAdapter();
        rvContacts.setAdapter(contactAdapter);

        contactAdapter.setOnItemClickListener(contact -> {
            Intent intent = new Intent(MainActivity.this, ViewContactActivity.class);
            intent.putExtra("contact", contact);
            startActivity(intent);
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (contactAdapter != null && contactAdapter.getFilter() != null) {
                    contactAdapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadContacts(String userId) {
        mDatabase.child("contacts").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Contact contact = dataSnapshot.getValue(Contact.class);
                    if (contact != null) {
                        contactList.add(contact);
                    }
                }
                
                Collections.sort(contactList, new Comparator<Contact>() {
                    @Override
                    public int compare(Contact c1, Contact c2) {
                        return c1.getName().compareToIgnoreCase(c2.getName());
                    }
                });

                contactAdapter.setContacts(contactList);

                if (contactList.isEmpty()) {
                    tvEmptyTitle.setVisibility(View.VISIBLE);
                    tvEmptySubtitle.setVisibility(View.VISIBLE);
                    rvContacts.setVisibility(View.GONE);
                    tilSearch.setVisibility(View.GONE); // Hide search bar if list is empty
                } else {
                    tvEmptyTitle.setVisibility(View.GONE);
                    tvEmptySubtitle.setVisibility(View.GONE);
                    rvContacts.setVisibility(View.VISIBLE);
                    tilSearch.setVisibility(View.VISIBLE); // Show search bar if contacts exist
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load contacts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Global touch handler to clear focus and hide keyboard
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}