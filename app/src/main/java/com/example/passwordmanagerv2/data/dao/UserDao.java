package com.example.passwordmanagerv2.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.passwordmanagerv2.data.entity.User;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User findByEmail(String email);

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User findById(int userId);

    @Insert
    long insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("UPDATE users SET pin_hash = :newPinHash WHERE id = :userId")
    void updatePin(int userId, String newPinHash);

    @Query("UPDATE users SET password_hash = :newPasswordHash WHERE id = :userId")
    void updatePassword(int userId, String newPasswordHash);

    @Query("UPDATE users SET email = :newEmail WHERE id = :userId")
    void updateEmail(int userId, String newEmail);

    @Query("UPDATE users SET first_name = :firstName, last_name = :lastName WHERE id = :userId")
    void updateNames(int userId, String firstName, String lastName);

    @Query("SELECT * FROM users ORDER BY created_at DESC")
    List<User> getAllUsers();

    @Query("DELETE FROM users WHERE id = :userId")
    void deleteById(int userId);

    @Query("DELETE FROM users")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    boolean doesEmailExist(String email);

    @Query("SELECT * FROM users WHERE created_at >= :timestamp")
    List<User> getUsersCreatedAfter(long timestamp);

    @Query("SELECT * FROM users WHERE created_at BETWEEN :startTime AND :endTime")
    List<User> getUsersCreatedBetween(long startTime, long endTime);
}