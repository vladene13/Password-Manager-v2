package com.example.passwordmanagerv2;

import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

public class BiometricHelper {
    private final FragmentActivity activity;
    private final BiometricPrompt biometricPrompt;
    private final BiometricPrompt.PromptInfo promptInfo;
    private final SharedPreferences preferences;
    private static final String BIOMETRIC_ENABLED_KEY = "biometric_enabled";

    public BiometricHelper(FragmentActivity activity, BiometricAuthCallback callback) {
        this.activity = activity;
        this.preferences = activity.getSharedPreferences("biometric_prefs", FragmentActivity.MODE_PRIVATE);

        BiometricPrompt.AuthenticationCallback authCallback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                callback.onAuthenticationSuccess();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                callback.onAuthenticationError(errorCode, errString.toString());
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                callback.onAuthenticationFailed();
            }
        };

        biometricPrompt = new BiometricPrompt(activity,
                ContextCompat.getMainExecutor(activity), authCallback);

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autentificare biometrică")
                .setSubtitle("Folosiți amprenta pentru autentificare")
                .setDescription("Scanați amprenta pentru a vă autentifica în aplicație")
                .setNegativeButtonText("Folosește PIN")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build();
    }

    public void authenticate() {
        BiometricManager biometricManager = BiometricManager.from(activity);
        int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);

        switch (canAuthenticate) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                biometricPrompt.authenticate(promptInfo);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(activity, "Dispozitivul nu suportă autentificare biometrică", Toast.LENGTH_LONG).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(activity, "Autentificarea biometrică este temporar indisponibilă", Toast.LENGTH_LONG).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(activity, "Nu există amprente înregistrate", Toast.LENGTH_LONG).show();
                break;
        }
    }

    public boolean isBiometricAvailable() {
        BiometricManager biometricManager = BiometricManager.from(activity);
        int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public void setBiometricEnabled(boolean enabled) {
        preferences.edit().putBoolean(BIOMETRIC_ENABLED_KEY, enabled).apply();
    }

    public boolean isBiometricEnabled() {
        return preferences.getBoolean(BIOMETRIC_ENABLED_KEY, false);
    }

    public String getBiometricStatusMessage() {
        BiometricManager biometricManager = BiometricManager.from(activity);
        int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);

        switch (canAuthenticate) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return "Autentificarea biometrică este disponibilă";
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                return "Dispozitivul nu suportă autentificare biometrică";
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                return "Autentificarea biometrică este temporar indisponibilă";
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                return "Nu există amprente înregistrate. Vă rugăm să înregistrați cel puțin o amprentă în setările dispozitivului";
            default:
                return "Eroare la verificarea autentificării biometrice";
        }
    }



    public interface BiometricAuthCallback {
        void onAuthenticationSuccess();
        void onAuthenticationError(int errorCode, String errorMessage);
        void onAuthenticationFailed();
    }
}