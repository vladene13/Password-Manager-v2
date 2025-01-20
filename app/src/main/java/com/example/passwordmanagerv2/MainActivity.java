package com.example.passwordmanagerv2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.passwordmanagerv2.data.AppDatabase;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new DatabaseHelper(this);

        // Verifică dacă utilizatorul este deja autentificat
        if (dbHelper.isLoggedIn()) {
            startActivity(new Intent(this, PinActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        setupUI();
    }

    private void setupUI() {
        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);

        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Verifică din nou starea de autentificare în cazul în care utilizatorul
        // se întoarce din alte activități
        if (dbHelper != null && dbHelper.isLoggedIn()) {
            startActivity(new Intent(this, PinActivity.class));
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Curăță resursele când activitatea este distrusă
        if (dbHelper != null) {
            AppDatabase.destroyInstance();
        }
    }
}