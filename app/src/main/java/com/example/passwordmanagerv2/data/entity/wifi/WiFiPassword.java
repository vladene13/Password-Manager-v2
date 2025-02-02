package com.example.passwordmanagerv2.data.entity.wifi;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "wifi_passwords")
public class WiFiPassword {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "ssid", defaultValue = "")
    public String ssid;

    @ColumnInfo(name = "encryptedPassword", defaultValue = "")
    public String encryptedPassword;

    @ColumnInfo(name = "securityType", defaultValue = "")
    public String securityType;

    @ColumnInfo(name = "createdAt")
    public long createdAt;

    public WiFiPassword(String ssid, String encryptedPassword, String securityType) {
        this.ssid = ssid;
        this.encryptedPassword = encryptedPassword;
        this.securityType = securityType;
        this.createdAt = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getSecurityType() {
        return securityType;
    }

    public void setSecurityType(String securityType) {
        this.securityType = securityType;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "WiFiPassword{" +
                "id=" + id +
                ", ssid='" + ssid + '\'' +
                ", securityType='" + securityType + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}