package com.example.passwordmanagerv2;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class PinActivity extends AppCompatActivity implements BiometricHelper.BiometricAuthCallback {
    private TextInputEditText pinInput;
    private TextInputLayout pinInputLayout;
    private DatabaseHelper dbHelper;
    private BiometricHelper biometricHelper;
    private LinearLayout biometricPromptContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        // Adaugă animațiile cyberpunk
        setupAnimations();

        initializeViews();
        initializeBiometric();

        // Adaugă listener pentru schimbarea metodei de autentificare
        MaterialButton switchAuthMethodButton = findViewById(R.id.switchAuthMethodButton);
        switchAuthMethodButton.setOnClickListener(v -> switchAuthenticationMethod());
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

        // Animații pentru butoane
        setupButtonAnimations();
    }

    private void setupButtonAnimations() {
        MaterialButton submitButton = findViewById(R.id.submitButton);
        MaterialButton switchAuthMethodButton = findViewById(R.id.switchAuthMethodButton);

        // Animație pulsatorie pentru butoane
        ObjectAnimator scaleXSubmit = ObjectAnimator.ofFloat(submitButton, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator scaleYSubmit = ObjectAnimator.ofFloat(submitButton, "scaleY", 1f, 1.05f, 1f);

        scaleXSubmit.setRepeatCount(ValueAnimator.INFINITE);
        scaleYSubmit.setRepeatCount(ValueAnimator.INFINITE);

        scaleXSubmit.setDuration(1000);
        scaleYSubmit.setDuration(1000);

        scaleXSubmit.start();
        scaleYSubmit.start();
    }

    private void switchAuthenticationMethod() {
        if (biometricHelper.isBiometricAvailable()) {
            showAuthenticationDialog();
        } else {
            Toast.makeText(this,
                    biometricHelper.getBiometricStatusMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void showAuthenticationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Metodă de autentificare")
                .setMessage("Cum doriți să vă autentificați?")
                .setPositiveButton("Amprentă", (dialog, which) -> {
                    if (biometricHelper.isBiometricEnabled()) {
                        biometricHelper.authenticate();
                    } else {
                        Toast.makeText(this,
                                "Autentificarea biometrică nu este activată",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("PIN", (dialog, which) -> {
                    // Ascunde containerul biometric și arată inputul PIN
                    biometricPromptContainer.setVisibility(View.GONE);
                    pinInputLayout.setVisibility(View.VISIBLE);
                })
                .setCancelable(false)
                .show();
    }

    private void initializeViews() {
        dbHelper = new DatabaseHelper(this);
        pinInput = findViewById(R.id.pinInput);
        pinInputLayout = findViewById(R.id.pinInputLayout);
        biometricPromptContainer = findViewById(R.id.biometricPromptContainer);

        MaterialButton submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> attemptVerifyPin());
    }

    private void initializeBiometric() {
        biometricHelper = new BiometricHelper((FragmentActivity) this, this);

        // Verifică disponibilitatea biometrică
        if (biometricHelper.isBiometricAvailable()) {
            // Verifică dacă biometria este activată de utilizator
            if (biometricHelper.isBiometricEnabled()) {
                // Arată containerul biometric
                biometricPromptContainer.setVisibility(View.VISIBLE);
                pinInputLayout.setVisibility(View.GONE);

                // Încearcă autentificarea biometrică
                biometricHelper.authenticate();
            } else {
                // Biometria nu este activată de utilizator
                Toast.makeText(this,
                        "Autentificarea biometrică nu este activată",
                        Toast.LENGTH_LONG).show();

                // Arată inputul PIN
                biometricPromptContainer.setVisibility(View.GONE);
                pinInputLayout.setVisibility(View.VISIBLE);
            }
        } else {
            // Afișează mesaj dacă biometria nu este disponibilă
            Toast.makeText(this,
                    biometricHelper.getBiometricStatusMessage(),
                    Toast.LENGTH_LONG).show();

            // Arată inputul PIN
            biometricPromptContainer.setVisibility(View.GONE);
            pinInputLayout.setVisibility(View.VISIBLE);
        }
    }

    private void attemptVerifyPin() {
        String pin = pinInput.getText().toString().trim();

        if (pin.isEmpty()) {
            pinInput.setError(getString(R.string.error_field_required));
            return;
        }

        new VerifyPinTask().execute(pin);
    }

    @Override
    public void onAuthenticationSuccess() {
        startActivity(new Intent(PinActivity.this, HomeActivity.class));
        finish();
    }

    @Override
    public void onAuthenticationError(int errorCode, String errorMessage) {
        Toast.makeText(this,
                "Autentificare biometrică eșuată: " + errorMessage,
                Toast.LENGTH_LONG).show();

        // Comută înapoi la inputul PIN
        biometricPromptContainer.setVisibility(View.GONE);
        pinInputLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAuthenticationFailed() {
        Toast.makeText(this,
                "Autentificare biometrică eșuată. Încercați din nou sau folosiți PIN-ul.",
                Toast.LENGTH_SHORT).show();

        // Comută înapoi la inputul PIN
        biometricPromptContainer.setVisibility(View.GONE);
        pinInputLayout.setVisibility(View.VISIBLE);
    }

    private class VerifyPinTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String pin = params[0];
            return dbHelper.verifyPin(pin);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                startActivity(new Intent(PinActivity.this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(PinActivity.this,
                        getString(R.string.error_wrong_pin),
                        Toast.LENGTH_SHORT).show();
                pinInput.setText("");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!dbHelper.isLoggedIn()) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}