package com.example.passwordmanagerv2;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.passwordmanagerv2.data.entity.SavedPassword;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class EditPasswordActivity extends AppCompatActivity {
    private TextInputEditText siteInput, usernameInput, passwordInput;
    private DatabaseHelper dbHelper;
    private int passwordId;
    private SavedPassword currentPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);

        passwordId = getIntent().getIntExtra("PASSWORD_ID", -1);
        if (passwordId == -1) {
            finish();
            return;
        }

        initializeViews();
        setupToolbar();
        loadPassword();
    }

    private void initializeViews() {
        siteInput = findViewById(R.id.siteInput);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        dbHelper = new DatabaseHelper(this);

        MaterialButton saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveChanges());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Editare parolă");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadPassword() {
        new LoadPasswordTask().execute(passwordId);
    }

    private void saveChanges() {
        String site = siteInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (validateInputs(site, username, password)) {
            currentPassword.siteName = site;
            currentPassword.username = username;
            new SaveChangesTask().execute(password);
        }
    }

    private boolean validateInputs(String site, String username, String password) {
        if (site.isEmpty()) {
            siteInput.setError(getString(R.string.error_field_required));
            return false;
        }
        if (username.isEmpty()) {
            usernameInput.setError(getString(R.string.error_field_required));
            return false;
        }
        if (password.isEmpty()) {
            passwordInput.setError(getString(R.string.error_field_required));
            return false;
        }
        return true;
    }

    private class LoadPasswordTask extends AsyncTask<Integer, Void, SavedPassword> {
        @Override
        protected SavedPassword doInBackground(Integer... ids) {
            return dbHelper.getPasswordById(ids[0]);
        }

        @Override
        protected void onPostExecute(SavedPassword password) {
            if (password != null) {
                currentPassword = password;
                siteInput.setText(password.siteName);
                usernameInput.setText(password.username);
                passwordInput.setText(dbHelper.decryptPassword(password.encryptedPassword));
            }
        }
    }

    private class SaveChangesTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String newPassword = params[0];
            return dbHelper.updatePassword(currentPassword, newPassword);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(EditPasswordActivity.this,
                        "Modificările au fost salvate", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditPasswordActivity.this,
                        "Eroare la salvarea modificărilor", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}