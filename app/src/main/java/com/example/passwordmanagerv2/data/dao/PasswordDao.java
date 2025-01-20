package com.example.passwordmanagerv2.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.passwordmanagerv2.data.entity.SavedPassword;

import java.util.List;

@Dao
public interface PasswordDao {
    @Query("SELECT * FROM saved_passwords WHERE userId = :userId ORDER BY createdAt DESC")
    List<SavedPassword> getAllForUser(int userId);

    @Query("SELECT * FROM saved_passwords WHERE id = :passwordId")
    SavedPassword findById(int passwordId);

    @Insert
    long insert(SavedPassword password);

    @Update
    void update(SavedPassword password);

    @Delete
    void delete(SavedPassword password);

    @Query("DELETE FROM saved_passwords WHERE id = :passwordId")
    void deleteById(int passwordId);

    @Query("DELETE FROM saved_passwords WHERE userId = :userId")
    void deleteAllForUser(int userId);

    @Query("SELECT COUNT(*) FROM saved_passwords WHERE userId = :userId")
    int getPasswordCount(int userId);

    @Query("SELECT * FROM saved_passwords WHERE userId = :userId AND siteName LIKE :search")
    List<SavedPassword> searchPasswords(int userId, String search);

    @Query("SELECT * FROM saved_passwords WHERE userId = :userId AND createdAt >= :timestamp")
    List<SavedPassword> getPasswordsCreatedAfter(int userId, long timestamp);

    @Query("SELECT * FROM saved_passwords WHERE userId = :userId AND updatedAt >= :timestamp")
    List<SavedPassword> getPasswordsUpdatedAfter(int userId, long timestamp);
}