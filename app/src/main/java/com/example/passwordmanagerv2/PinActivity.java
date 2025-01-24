package com.example.passwordmanagerv2;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class PinActivity extends AppCompatActivity implements BiometricHelper.BiometricAuthCallback {
    private TextInputEditText pinInput;
    private DatabaseHelper dbHelper;
    private BiometricHelper biometricHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        initializeViews();
        initializeBiometric();

        // Adaugă listener pentru schimbarea metodei de autentificare
        MaterialButton switchAuthMethodButton = findViewById(R.id.switchAuthMethodButton);
        switchAuthMethodButton.setOnClickListener(v -> switchAuthenticationMethod());
    }

    private void switchAuthenticationMethod() {
        if (biometricHelper.isBiometricAvailable()) {
            try {
                // Forțează deschiderea promptului biometric
                biometricHelper.authenticate();
            } catch (Exception e) {
                Log.e("BiometricTest", "Eroare la autentificare", e);
                Toast.makeText(this,
                        "Eroare la autentificare biometrică: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this,
                    biometricHelper.getBiometricStatusMessage(),
                    Toast.LENGTH_LONG).show();
        }
        if (biometricHelper.isBiometricEnabled()) {
            biometricHelper.authenticate();
        } else {
            Toast.makeText(this,
                    "Autentificarea biometrică nu este activată",
                    Toast.LENGTH_LONG).show();
        }
    }
    private void showAuthenticationDialog() {
        if (biometricHelper.isBiometricAvailable()) {
            new AlertDialog.Builder(this)
                    .setTitle("Metodă de autentificare")
                    .setMessage("Cum doriți să vă autentificați?")
                    .setPositiveButton("Amprentă", (dialog, which) -> biometricHelper.authenticate())
                    .setNegativeButton("PIN", (dialog, which) -> {
                        // Lasă utilizatorul să folosească PIN-ul
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    private void initializeViews() {
        dbHelper = new DatabaseHelper(this);
        pinInput = findViewById(R.id.pinInput);
        MaterialButton submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> attemptVerifyPin());
    }

    private void initializeBiometric() {
        biometricHelper = new BiometricHelper((FragmentActivity) this, this);
        if (biometricHelper.isBiometricEnabled() && biometricHelper.isBiometricAvailable()) {
            biometricHelper.authenticate();
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
    }

    @Override
    public void onAuthenticationFailed() {
        Toast.makeText(this,
                "Autentificare biometrică eșuată. Încercați din nou sau folosiți PIN-ul.",
                Toast.LENGTH_SHORT).show();
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