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
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.passwordmanagerv2.view.PasswordStrengthIndicator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddPasswordActivity extends AppCompatActivity {
    private TextInputEditText siteInput, usernameInput;
    private TextView generatedPasswordText;
    private DatabaseHelper dbHelper;
    private PasswordStrengthIndicator strengthIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_password);

        initializeViews();
        setupToolbar();
        setupListeners();
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
        generatedPasswordText = findViewById(R.id.generatedPasswordText);
        strengthIndicator = findViewById(R.id.passwordStrengthIndicator);
        dbHelper = new DatabaseHelper(this);

        // Adăugăm TextWatcher pentru a monitoriza schimbările în parola generată
        generatedPasswordText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && !s.toString().isEmpty()) {
                    strengthIndicator.updateStrength(s.toString());
                }
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.add_password);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupListeners() {
        MaterialButton generateButton = findViewById(R.id.generateButton);
        MaterialButton saveButton = findViewById(R.id.saveButton);

        generateButton.setOnClickListener(v -> generatePassword());
        saveButton.setOnClickListener(v -> attemptSavePassword());
    }

    private void generatePassword() {
        String password = PasswordGenerator.generateStrongPassword();
        generatedPasswordText.setText(password);
        strengthIndicator.updateStrength(password);
    }

    private void attemptSavePassword() {
        String site = siteInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String password = generatedPasswordText.getText().toString().trim();

        if (!validateInputs(site, username, password)) {
            return;
        }

        new SavePasswordTask().execute(site, username, password);
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
            Toast.makeText(this, R.string.error_generate_password, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private class SavePasswordTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String site = params[0];
            String username = params[1];
            String password = params[2];
            return dbHelper.savePassword(site, username, password);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(AddPasswordActivity.this,
                        R.string.password_saved, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AddPasswordActivity.this,
                        R.string.error_saving_password, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
