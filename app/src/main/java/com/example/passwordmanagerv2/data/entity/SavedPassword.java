package com.example.passwordmanagerv2.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "saved_passwords",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ))
public class SavedPassword {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "userId")
    public int userId;

    @ColumnInfo(name = "siteName")
    public String siteName;

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "encryptedPassword")
    public String encryptedPassword;

    @ColumnInfo(name = "createdAt")
    public long createdAt;

    @ColumnInfo(name = "updatedAt")
    public long updatedAt;

    @ColumnInfo(name = "is_archived")
    public boolean isArchived;

    public SavedPassword(int userId, String siteName, String username, String encryptedPassword) {
        this.userId = userId;
        this.siteName = siteName;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isArchived = false;
    }

    public int getId() {
        return id;
    }

    public String getSiteName() {
        return siteName;
    }

    public String getUsername() {
        return username;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
        this.updatedAt = System.currentTimeMillis();
    }

    public void setUsername(String username) {
        this.username = username;
        this.updatedAt = System.currentTimeMillis();
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
        this.updatedAt = System.currentTimeMillis();
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
        this.updatedAt = System.currentTimeMillis();
    }
}