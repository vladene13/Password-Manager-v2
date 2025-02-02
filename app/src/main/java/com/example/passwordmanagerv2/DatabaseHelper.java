package com.example.passwordmanagerv2;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.example.passwordmanagerv2.data.AppDatabase;
import com.example.passwordmanagerv2.data.entity.User;
import com.example.passwordmanagerv2.data.entity.SavedPassword;
import com.example.passwordmanagerv2.data.entity.wifi.WiFiPassword;

public class DatabaseHelper {
    private final AppDatabase db;
    private final Context context;
    private static SecretKey encryptionKey;
    private SharedPreferences preferences;

    private static final String PREFERENCES_FILE = "secure_prefs";
    private static final String KEY_ALIAS = "password_manager_key";
    private static final String ENCRYPTION_KEY_PREF = "encryption_key";
    private static final String CURRENT_USER_ID = "current_user_id";
    private static final String CURRENT_USER_EMAIL = "current_user_email";
    private static final String IS_LOGGED_IN = "isLoggedIn";

    public DatabaseHelper(Context context) {
        this.context = context;
        this.db = AppDatabase.getDatabase(context);
        initializeSharedPreferences();
        initializeEncryptionKey();
    }

    private void initializeSharedPreferences() {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            preferences = EncryptedSharedPreferences.create(
                    context,
                    PREFERENCES_FILE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
            preferences = context.getSharedPreferences("fallback_prefs", Context.MODE_PRIVATE);
        }
    }

    private void initializeEncryptionKey() {
        try {
            if (encryptionKey == null) {
                String savedKeyString = preferences.getString(ENCRYPTION_KEY_PREF, null);
                if (savedKeyString != null) {
                    byte[] encodedKey = Base64.decode(savedKeyString, Base64.NO_WRAP);
                    encryptionKey = new SecretKeySpec(encodedKey, "AES");
                } else {
                    KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                    keyGen.init(256);
                    encryptionKey = keyGen.generateKey();

                    String keyString = Base64.encodeToString(
                            encryptionKey.getEncoded(),
                            Base64.NO_WRAP
                    );
                    preferences.edit()
                            .putString(ENCRYPTION_KEY_PREF, keyString)
                            .apply();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean registerUser(String email, String firstName, String lastName, String password, String pin) {
        try {
            if (db.userDao().findByEmail(email) != null) {
                return false;
            }

            String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
            String pinHash = BCrypt.hashpw(pin, BCrypt.gensalt());

            User user = new User(email, firstName, lastName, passwordHash, pinHash);
            long userId = db.userDao().insert(user);

            if (userId > 0) {
                preferences.edit()
                        .putInt(CURRENT_USER_ID, (int)userId)
                        .putString(CURRENT_USER_EMAIL, email)
                        .apply();
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean loginUser(String email, String password) {
        try {
            User user = db.userDao().findByEmail(email);
            if (user != null && BCrypt.checkpw(password, user.passwordHash)) {
                preferences.edit()
                        .putInt(CURRENT_USER_ID, user.id)
                        .putString(CURRENT_USER_EMAIL, email)
                        .putBoolean(IS_LOGGED_IN, true)
                        .apply();
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verifyPin(String pin) {
        try {
            int userId = getCurrentUserId();
            User user = db.userDao().findById(userId);
            if (user != null) {
                return BCrypt.checkpw(pin, user.pinHash);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean changePin(String currentPin, String newPin) {
        try {
            int userId = getCurrentUserId();
            User user = db.userDao().findById(userId);
            if (user != null && BCrypt.checkpw(currentPin, user.pinHash)) {
                String newPinHash = BCrypt.hashpw(newPin, BCrypt.gensalt());
                db.userDao().updatePin(userId, newPinHash);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean savePassword(String siteName, String username, String password) {
        try {
            String domain = SavedPassword.extractDomain(siteName);
            String encryptedPassword = encryptPassword(password);
            SavedPassword savedPassword = new SavedPassword(
                    getCurrentUserId(),
                    domain,
                    username,
                    encryptedPassword
            );
            long id = db.passwordDao().insert(savedPassword);
            return id > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<SavedPassword> getSavedPasswords() {
        try {
            return db.passwordDao().getActivePasswords(getCurrentUserId());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<SavedPassword> getActivePasswords() {
        try {
            return db.passwordDao().getActivePasswords(getCurrentUserId());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<SavedPassword> getArchivedPasswords() {
        try {
            return db.passwordDao().getArchivedPasswords(getCurrentUserId());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public SavedPassword getPasswordById(int passwordId) {
        try {
            return db.passwordDao().findById(passwordId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updatePassword(SavedPassword password, String newPassword) {
        try {
            password.encryptedPassword = encryptPassword(newPassword);
            password.updatedAt = System.currentTimeMillis();
            db.passwordDao().update(password);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean archivePassword(int passwordId) {
        try {
            SavedPassword password = db.passwordDao().findById(passwordId);
            if (password != null) {
                password.isArchived = true;
                password.updatedAt = System.currentTimeMillis();
                db.passwordDao().update(password);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deletePassword(int passwordId) {
        try {
            db.passwordDao().deleteById(passwordId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String encryptPassword(String password) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
        byte[] iv = cipher.getIV();
        byte[] encrypted = cipher.doFinal(password.getBytes());

        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.encodeToString(combined, Base64.NO_WRAP);
    }

    public String decryptPassword(String encryptedPassword) {
        try {
            byte[] decoded = Base64.decode(encryptedPassword, Base64.NO_WRAP);

            byte[] iv = new byte[12];
            byte[] encrypted = new byte[decoded.length - 12];
            System.arraycopy(decoded, 0, iv, 0, 12);
            System.arraycopy(decoded, 12, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, spec);

            return new String(cipher.doFinal(encrypted));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void logout() {
        preferences.edit()
                .remove(CURRENT_USER_ID)
                .remove(CURRENT_USER_EMAIL)
                .putBoolean(IS_LOGGED_IN, false)
                .apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(IS_LOGGED_IN, false);
    }

    private int getCurrentUserId() {
        return preferences.getInt(CURRENT_USER_ID, -1);
    }

    public String getCurrentUserEmail() {
        return preferences.getString(CURRENT_USER_EMAIL, null);
    }

    public User getCurrentUser() {
        try {
            int userId = getCurrentUserId();
            if (userId != -1) {
                return db.userDao().findById(userId);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public void saveWiFiPassword(WiFiPassword wifiPassword) {
        try {
            long id = db.wifiPasswordDao().insert(wifiPassword);
            Log.d("DatabaseHelper", "WiFi password saved with id: " + id);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error saving WiFi password", e);
        }
    }

    public List<WiFiPassword> getWiFiPasswords() {
        try {
            return db.wifiPasswordDao().getAll();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void clearAllData() {
        try {
            db.clearAllTables();
            preferences.edit().clear().apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}