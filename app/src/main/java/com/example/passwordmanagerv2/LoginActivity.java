package com.example.passwordmanagerv2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText emailInput, passwordInput;
    private DatabaseHelper dbHelper;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        setupListeners();

        dbHelper = new DatabaseHelper(this);
        preferences = getSharedPreferences("PasswordManager", MODE_PRIVATE);
    }

    private void initializeViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
    }

    private void setupListeners() {
        MaterialButton loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty()) {
            emailInput.setError(getString(R.string.error_field_required));
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError(getString(R.string.error_field_required));
            return;
        }

        new LoginTask().execute(email, password);
    }

    private class LoginTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String email = params[0];
            String password = params[1];
            return dbHelper.loginUser(email, password);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                // După autentificare reușită, mergem la PinActivity
                startActivity(new Intent(LoginActivity.this, PinActivity.class));
                finish();
            } else {
                Toast.makeText(LoginActivity.this,
                        "Email sau parolă incorectă", Toast.LENGTH_SHORT).show();
            }
        }
    }
}