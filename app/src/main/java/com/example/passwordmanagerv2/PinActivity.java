package com.example.passwordmanagerv2;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class PinActivity extends AppCompatActivity {
    private TextInputEditText pinInput;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new DatabaseHelper(this);

        // Verifică dacă utilizatorul este autentificat
        if (!dbHelper.isLoggedIn()) {
            // Dacă nu este autentificat, redirectează către MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_pin);
        initializeViews();
    }

    private void initializeViews() {
        pinInput = findViewById(R.id.pinInput);
        MaterialButton submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> attemptVerifyPin());
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
    protected void onResume() {
        super.onResume();
        // Verifică din nou starea de autentificare în onResume
        if (!dbHelper.isLoggedIn()) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private class VerifyPinTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String pin = params[0];
            // Verifică mai întâi dacă utilizatorul este încă autentificat
            if (!dbHelper.isLoggedIn()) {
                return false;
            }
            return dbHelper.verifyPin(pin);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                startActivity(new Intent(PinActivity.this, HomeActivity.class));
                finish();
            } else {
                if (!dbHelper.isLoggedIn()) {
                    // Dacă nu mai este autentificat, redirectează către MainActivity
                    Intent intent = new Intent(PinActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(PinActivity.this,
                            getString(R.string.error_wrong_pin), Toast.LENGTH_SHORT).show();
                    pinInput.setText("");
                }
            }
        }
    }
}