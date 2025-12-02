package com.example.contactbookproject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class AddContactActivity extends AppCompatActivity {

    private TextInputEditText etName, etPhone, etEmail, etBirthday;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private Button btnSaveContact, btnDeleteContact, btnCancel;
    private ImageButton btnBack;
    private TextView tvAddContactTitle;
    private ImageView ivBackground;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private Contact existingContact;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etBirthday = findViewById(R.id.etBirthday);
        rgGender = findViewById(R.id.rgGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        btnSaveContact = findViewById(R.id.btnSaveContact);
        btnDeleteContact = findViewById(R.id.btnDeleteContact);
        btnCancel = findViewById(R.id.btnCancel);
        btnBack = findViewById(R.id.btnBack);
        tvAddContactTitle = findViewById(R.id.tvAddContactTitle);
        ivBackground = findViewById(R.id.ivBackground);

        // Limit phone number to 11 characters
        etPhone.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});

        // Setup birthday field text watcher for automatic formatting
        setupBirthdayFormatting();

        // Default title
        tvAddContactTitle.setText("Add New Contact");
        
        // Ensure background is visible for Add Mode
        if (ivBackground != null) {
            ivBackground.setVisibility(View.VISIBLE);
        }

        // Check if we are in Edit Mode
        if (getIntent().hasExtra("contact")) {
            existingContact = (Contact) getIntent().getSerializableExtra("contact");
            isEditMode = true;
            populateFields();
        }

        btnSaveContact.setOnClickListener(v -> saveContact());
        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());
        btnDeleteContact.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void setupBirthdayFormatting() {
        etBirthday.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private String ddmmyyyy = "DDMMYYYY";
            private Calendar cal = Calendar.getInstance();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d.]|\\.", "");
                    String cleanC = current.replaceAll("[^\\d.]|\\.", "");

                    int cl = clean.length();
                    int sel = cl;
                    for (int i = 2; i <= cl && i < 6; i += 2) {
                        sel++;
                    }
                    //Fix for pressing delete next to a forward slash
                    if (clean.equals(cleanC)) sel--;

                    if (clean.length() < 8){
                        String ddmmyyyy = "DDMMYYYY";
                        clean = clean + ddmmyyyy.substring(clean.length());
                    } else {
                        //This part makes sure that when we finish entering numbers
                        //the date is correct, fixing it otherwise
                        int day  = Integer.parseInt(clean.substring(0,2));
                        int mon  = Integer.parseInt(clean.substring(2,4));
                        int year = Integer.parseInt(clean.substring(4,8));

                        mon = mon < 1 ? 1 : mon > 12 ? 12 : mon;
                        cal.set(Calendar.MONTH, mon - 1);
                        year = (year<1900)?1900:(year>2100)?2100:year;
                        cal.set(Calendar.YEAR, year);
                        // ^ first set year for the line below to work correctly
                        //with leap years - otherwise, date e.g. 29/02/2012
                        //would be automatically corrected to 28/02/2012

                        day = (day > cal.getActualMaximum(Calendar.DATE))? cal.getActualMaximum(Calendar.DATE):day;
                        clean = String.format("%02d%02d%02d",day, mon, year);
                    }

                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8));

                    sel = sel < 0 ? 0 : sel;
                    current = clean;
                    etBirthday.setText(current);
                    etBirthday.setSelection(sel < current.length() ? sel : current.length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void populateFields() {
        tvAddContactTitle.setText("Edit Contact");
        
        // Hide background for Edit Mode
        if (ivBackground != null) {
            ivBackground.setVisibility(View.GONE);
        }
        
        etName.setText(existingContact.getName());
        etPhone.setText(existingContact.getPhone());
        etEmail.setText(existingContact.getEmail());
        etBirthday.setText(existingContact.getBirthday());

        if ("Male".equals(existingContact.getGender())) {
            rbMale.setChecked(true);
        } else if ("Female".equals(existingContact.getGender())) {
            rbFemale.setChecked(true);
        }

        btnSaveContact.setText("Update");
        btnDeleteContact.setVisibility(View.VISIBLE);
    }

    private void saveContact() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String birthday = etBirthday.getText().toString().trim();
        String gender = "";

        int selectedId = rgGender.getCheckedRadioButtonId();
        if (selectedId == R.id.rbMale) {
            gender = "Male";
        } else if (selectedId == R.id.rbFemale) {
            gender = "Female";
        }

        // Validation: Name is required
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }

        // Validation: Phone is required
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone number is required");
            return;
        }
        
        if (phone.length() != 11) {
            etPhone.setError("Phone number must be 11 digits");
            return;
        }
        
        // Email Validation
        if (!TextUtils.isEmpty(email) && !email.endsWith("@gmail.com")) {
            etEmail.setError("Email must end with @gmail.com");
            return;
        }

        // Validation: Gender is required
        if (TextUtils.isEmpty(gender)) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return;
        }

        // Email and Birthday are optional, so no checks needed for them

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String contactId = isEditMode ? existingContact.getId() : mDatabase.child("contacts").child(userId).push().getKey();

        if (contactId == null) {
            Toast.makeText(this, "Could not create or update contact.", Toast.LENGTH_SHORT).show();
            return;
        }

        Contact contact = new Contact(contactId, name, phone, email, gender, birthday);

        mDatabase.child("contacts").child(userId).child(contactId).setValue(contact)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String message = isEditMode ? "Contact updated successfully!" : "Contact saved successfully!";
                        Toast.makeText(AddContactActivity.this, message, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddContactActivity.this, "Failed to save: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete this contact?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteContact();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteContact() {
        if (!isEditMode || existingContact == null) return;

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        String contactId = existingContact.getId();

        mDatabase.child("contacts").child(userId).child(contactId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AddContactActivity.this, "Contact deleted successfully!", Toast.LENGTH_SHORT).show();
                        // Go back to MainActivity, clearing the back stack so we don't return to ViewContactActivity
                        Intent intent = new Intent(AddContactActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(AddContactActivity.this, "Failed to delete contact: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
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