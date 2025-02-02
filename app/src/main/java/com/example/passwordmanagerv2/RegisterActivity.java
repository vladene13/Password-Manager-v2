package com.example.passwordmanagerv2;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText emailInput, firstNameInput, lastNameInput, passwordInput, pinInput;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews();
        setupAnimations();
        dbHelper = new DatabaseHelper(this);

        MaterialButton registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> attemptRegister());
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
        emailInput = findViewById(R.id.emailInput);
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        passwordInput = findViewById(R.id.passwordInput);
        pinInput = findViewById(R.id.pinInput);
    }

    private void attemptRegister() {
        String email = emailInput.getText().toString().trim();
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String pin = pinInput.getText().toString().trim();

        if (!validateInputs(email, firstName, lastName, password, pin)) {
            return;
        }

        new RegisterTask().execute(email, firstName, lastName, password, pin);
    }

    private boolean validateInputs(String email, String firstName,
                                   String lastName, String password, String pin) {
        if (email.isEmpty()) {
            emailInput.setError(getString(R.string.error_field_required));
            return false;
        }
        if (firstName.isEmpty()) {
            firstNameInput.setError(getString(R.string.error_field_required));
            return false;
        }
        if (lastName.isEmpty()) {
            lastNameInput.setError(getString(R.string.error_field_required));
            return false;
        }
        if (password.isEmpty()) {
            passwordInput.setError(getString(R.string.error_field_required));
            return false;
        }
        if (pin.isEmpty() || pin.length() != 4) {
            pinInput.setError(getString(R.string.error_invalid_pin));
            return false;
        }
        return true;
    }

    private class RegisterTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            return dbHelper.registerUser(params[0], params[1],
                    params[2], params[3], params[4]);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(RegisterActivity.this,
                        "Înregistrare reușită", Toast.LENGTH_SHORT).show();
                // După înregistrare, ne întoarcem la MainActivity
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(RegisterActivity.this,
                        "Eroare la înregistrare", Toast.LENGTH_SHORT).show();
            }
        }
    }
}