package com.example.passwordmanagerv2;

import java.security.SecureRandom;
import java.util.Random;

public class PasswordGenerator {
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    public static String generateStrongPassword() {
        StringBuilder password = new StringBuilder();
        Random random = new SecureRandom();

        // Asigură cel puțin un caracter din fiecare categorie
        password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        password.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        password.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        // Completează până la 16 caractere
        String allChars = UPPERCASE + LOWERCASE + NUMBERS + SPECIAL;
        for (int i = 4; i < 16; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // Amestecă caracterele
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }
}