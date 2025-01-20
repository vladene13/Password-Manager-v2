package com.example.passwordmanagerv2.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "first_name")
    public String firstName;

    @ColumnInfo(name = "last_name")
    public String lastName;

    @ColumnInfo(name = "password_hash")
    public String passwordHash;

    @ColumnInfo(name = "pin_hash")
    public String pinHash;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    public User(String email, String firstName, String lastName, String passwordHash, String pinHash) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.passwordHash = passwordHash;
        this.pinHash = pinHash;
        this.createdAt = System.currentTimeMillis();
    }
}