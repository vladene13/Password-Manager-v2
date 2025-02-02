package com.example.passwordmanagerv2.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.passwordmanagerv2.data.entity.wifi.WiFiPassword;

import java.util.List;

@Dao
public interface WiFiPasswordDao {
    @Query("SELECT * FROM wifi_passwords ORDER BY createdAt DESC")
    List<WiFiPassword> getAll();

    @Query("SELECT * FROM wifi_passwords WHERE id = :wifiPasswordId")
    WiFiPassword findById(int wifiPasswordId);

    @Query("SELECT * FROM wifi_passwords WHERE ssid = :ssid LIMIT 1")
    WiFiPassword findBySSID(String ssid);

    @Insert
    long insert(WiFiPassword wifiPassword);

    @Update
    void update(WiFiPassword wifiPassword);

    @Delete
    void delete(WiFiPassword wifiPassword);

    @Query("DELETE FROM wifi_passwords WHERE id = :wifiPasswordId")
    void deleteById(int wifiPasswordId);

    @Query("SELECT COUNT(*) FROM wifi_passwords")
    int getWiFiPasswordCount();

    @Query("SELECT * FROM wifi_passwords WHERE createdAt >= :timestamp")
    List<WiFiPassword> getWiFiPasswordsCreatedAfter(long timestamp);
}