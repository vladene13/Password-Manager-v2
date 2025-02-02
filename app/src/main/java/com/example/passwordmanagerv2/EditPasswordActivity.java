package com.example.passwordmanagerv2;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.passwordmanagerv2.data.entity.SavedPassword;
import com.example.passwordmanagerv2.view.PasswordStrengthIndicator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class EditPasswordActivity extends AppCompatActivity {
    private TextInputEditText siteInput, usernameInput, passwordInput;
    private DatabaseHelper dbHelper;
    private PasswordStrengthIndicator strengthIndicator;
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
        setupAnimations();
    }
    private void setupAnimations() {
        // Animație fundal circuit
        View circuitBackground = findViewById(R.id.circuitBackground);
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(circuitBackground, "rotation", 0f, 360f);
        rotationAnimator.setDuration(5000);
        rotationAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotationAnimator.setInterpolator(new LinearInterpolator());
        rotationAnimator.start();

        // Efect glitch overlay
        View glitchOverlay = findViewById(R.id.glitchOverlay);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(glitchOverlay, "alpha", 0.1f, 0.3f, 0.1f);
        alphaAnimator.setDuration(2000);
        alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
        alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        alphaAnimator.start();
    }

    private void initializeViews() {
        siteInput = findViewById(R.id.siteInput);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        strengthIndicator = findViewById(R.id.passwordStrengthIndicator);
        dbHelper = new DatabaseHelper(this);

        MaterialButton saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveChanges());

        // Monitor pentru schimbările în parola editată
        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null) {
                    strengthIndicator.updateStrength(s.toString());
                }
            }
        });
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

        if (!validateInputs(site, username, password)) {
            return;
        }

        if (currentPassword != null) {
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
                String decryptedPassword = dbHelper.decryptPassword(password.encryptedPassword);
                passwordInput.setText(decryptedPassword);
                strengthIndicator.updateStrength(decryptedPassword);
            }
        }
    }

    private class SaveChangesTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            return dbHelper.updatePassword(currentPassword, params[0]);
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