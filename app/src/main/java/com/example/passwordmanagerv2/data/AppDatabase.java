package com.example.passwordmanagerv2.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.passwordmanagerv2.data.dao.PasswordDao;
import com.example.passwordmanagerv2.data.dao.UserDao;
import com.example.passwordmanagerv2.data.dao.WiFiPasswordDao;
import com.example.passwordmanagerv2.data.entity.SavedPassword;
import com.example.passwordmanagerv2.data.entity.User;
import com.example.passwordmanagerv2.data.entity.wifi.WiFiPassword;

@Database(
        entities = {User.class, SavedPassword.class, WiFiPassword.class},
        version = 3
)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "password_manager_db";
    private static volatile AppDatabase instance;
    private static final Object LOCK = new Object();

    public abstract UserDao userDao();
    public abstract PasswordDao passwordDao();
    public abstract WiFiPasswordDao wifiPasswordDao();

    public static AppDatabase getDatabase(Context context) {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME)
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE saved_passwords ADD COLUMN is_archived INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP TABLE IF EXISTS wifi_passwords");
            database.execSQL("CREATE TABLE wifi_passwords (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "ssid TEXT, " +
                    "encryptedPassword TEXT, " +
                    "securityType TEXT, " +
                    "createdAt INTEGER NOT NULL)");
        }
    };

    public void close() {
        if (instance != null) {
            synchronized (LOCK) {
                if (instance.isOpen()) {
                    instance.close();
                }
                instance = null;
            }
        }
    }

    public static void destroyInstance() {
        instance = null;
    }

    public boolean isOpen() {
        return instance != null && instance.getOpenHelper().getWritableDatabase().isOpen();
    }
}