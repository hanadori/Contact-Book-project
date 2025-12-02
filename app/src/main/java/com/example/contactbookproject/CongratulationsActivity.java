package com.example.contactbookproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class CongratulationsActivity extends AppCompatActivity {

    private Button btnGoToSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congratulations);

        btnGoToSignIn = findViewById(R.id.btnGoToSignIn);

        // Sign out the user immediately so they have to log in again
        // This prevents the auto-login in MainActivity from skipping the login screen
        FirebaseAuth.getInstance().signOut();

        btnGoToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CongratulationsActivity.this, LoginActivity.class);
                // Clear the activity stack so user can't go back to congratulations screen
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
}