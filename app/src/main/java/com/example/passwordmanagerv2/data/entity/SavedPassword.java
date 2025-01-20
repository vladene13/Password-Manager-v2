package com.example.passwordmanagerv2.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "saved_passwords",
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE))
public class SavedPassword {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;
    public String siteName;
    public String username;
    public String encryptedPassword;
    public long createdAt;
    public long updatedAt;

    public SavedPassword(int userId, String siteName, String username, String encryptedPassword) {
        this.userId = userId;
        this.siteName = siteName;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
}