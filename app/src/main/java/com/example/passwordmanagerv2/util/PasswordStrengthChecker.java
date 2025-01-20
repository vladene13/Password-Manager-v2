package com.example.passwordmanagerv2.util;

import com.example.passwordmanagerv2.R;

public class PasswordStrengthChecker {
    public enum PasswordStrength {
        WEAK(R.color.password_weak, "Parolă slabă"),
        MEDIUM(R.color.password_medium, "Parolă medie"),
        STRONG(R.color.password_strong, "Parolă puternică");

        private final int colorId;
        private final String message;

        PasswordStrength(int colorId, String message) {
            this.colorId = colorId;
            this.message = message;
        }

        public int getColorId() { return colorId; }
        public String getMessage() { return message; }
    }

    public static PasswordStrength calculateStrength(String password) {
        int score = 0;

        if (password == null || password.isEmpty()) {
            return PasswordStrength.WEAK;
        }

        // Lungimea minimă
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;

        // Verifică existența caracterelor speciale
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score++;

        // Verifică existența numerelor
        if (password.matches(".*\\d.*")) score++;

        // Verifică existența literelor mari
        if (password.matches(".*[A-Z].*")) score++;

        // Verifică existența literelor mici
        if (password.matches(".*[a-z].*")) score++;

        // Returnează nivelul de securitate în funcție de scor
        if (score >= 5) return PasswordStrength.STRONG;
        if (score >= 3) return PasswordStrength.MEDIUM;
        return PasswordStrength.WEAK;
    }
}