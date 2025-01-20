package com.example.passwordmanagerv2.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.passwordmanagerv2.data.dao.PasswordDao;
import com.example.passwordmanagerv2.data.dao.UserDao;
import com.example.passwordmanagerv2.data.entity.SavedPassword;
import com.example.passwordmanagerv2.data.entity.User;

@Database(
        entities = {User.class, SavedPassword.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "password_manager_db";
    private static volatile AppDatabase instance;
    private static final Object LOCK = new Object();

    public abstract UserDao userDao();
    public abstract PasswordDao passwordDao();

    public static AppDatabase getDatabase(Context context) {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }

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
}
