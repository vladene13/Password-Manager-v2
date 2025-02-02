package com.example.passwordmanagerv2;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class SettingsActivity extends AppCompatActivity {
    private TextInputEditText currentPinInput, newPinInput, confirmPinInput;
    private DatabaseHelper dbHelper;
    private BiometricHelper biometricHelper;
    private SwitchMaterial biometricSwitch, darkModeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dbHelper = new DatabaseHelper(this);
        biometricHelper = new BiometricHelper(this, null);

        initializeViews();
        setupToolbar();
        setupAnimations();
        setupSwitches();

        MaterialButton changePinButton = findViewById(R.id.changePinButton);
        changePinButton.setOnClickListener(v -> attemptChangePin());
    }

    private void setupSwitches() {
        biometricSwitch = findViewById(R.id.biometricSwitch);
        //darkModeSwitch = findViewById(R.id.darkModeSwitch);

        // Configurare switch biometrică
        biometricSwitch.setChecked(biometricHelper.isBiometricEnabled());
        biometricSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            biometricHelper.setBiometricEnabled(isChecked);
            Toast.makeText(this,
                    isChecked ? "Autentificare biometrică activată" : "Autentificare biometrică dezactivată",
                    Toast.LENGTH_SHORT).show();
        });


    }

//    private boolean isDarkModeEnabled() {
//        return getSharedPreferences("AppSettings", MODE_PRIVATE)
//                .getBoolean("dark_mode_enabled", false);
//    }
//
//    private void setDarkModeEnabled(boolean enabled) {
//        getSharedPreferences("AppSettings", MODE_PRIVATE)
//                .edit()
//                .putBoolean("dark_mode_enabled", enabled)
//                .apply();
//    }

    private void setupAnimations() {
        View circuitBackground = findViewById(R.id.circuitBackground);
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(circuitBackground, "rotation", 0f, 360f);
        rotationAnimator.setDuration(5000);
        rotationAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotationAnimator.setInterpolator(new LinearInterpolator());
        rotationAnimator.start();

        View glitchOverlay = findViewById(R.id.glitchOverlay);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(glitchOverlay, "alpha", 0.1f, 0.3f, 0.1f);
        alphaAnimator.setDuration(2000);
        alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
        alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        alphaAnimator.start();
    }

    private void initializeViews() {
        currentPinInput = findViewById(R.id.currentPinInput);
        newPinInput = findViewById(R.id.newPinInput);
        confirmPinInput = findViewById(R.id.confirmPinInput);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.settings);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void attemptChangePin() {
        String currentPin = currentPinInput.getText().toString().trim();
        String newPin = newPinInput.getText().toString().trim();
        String confirmPin = confirmPinInput.getText().toString().trim();

        if (!validateInputs(currentPin, newPin, confirmPin)) {
            return;
        }

        new ChangePinTask().execute(currentPin, newPin);
    }

    private boolean validateInputs(String currentPin, String newPin, String confirmPin) {
        if (currentPin.isEmpty()) {
            currentPinInput.setError(getString(R.string.error_field_required));
            return false;
        }

        if (newPin.isEmpty()) {
            newPinInput.setError(getString(R.string.error_field_required));
            return false;
        }

        if (newPin.length() != 4) {
            newPinInput.setError(getString(R.string.error_invalid_pin));
            return false;
        }

        if (!newPin.equals(confirmPin)) {
            confirmPinInput.setError(getString(R.string.error_pins_dont_match));
            return false;
        }

        return true;
    }

    private class ChangePinTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String currentPin = params[0];
            String newPin = params[1];
            return dbHelper.changePin(currentPin, newPin);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(SettingsActivity.this,
                        R.string.pin_changed, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(SettingsActivity.this,
                        R.string.error_wrong_pin, Toast.LENGTH_SHORT).show();
                currentPinInput.setText("");
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}