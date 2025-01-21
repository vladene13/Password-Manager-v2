package com.example.passwordmanagerv2.service;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.passwordmanagerv2.R;
import com.example.passwordmanagerv2.data.entity.SavedPassword;
import com.example.passwordmanagerv2.DatabaseHelper;

import java.util.Calendar;
import java.util.List;

public class PasswordNotificationService extends BroadcastReceiver {
    private static final String CHANNEL_ID = "password_updates";
    private static final long SIX_MONTHS_IN_MILLIS = 180L * 24 * 60 * 60 * 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                scheduleNotifications(context);
            } else {
                DatabaseHelper dbHelper = new DatabaseHelper(context);
                checkPasswordsAge(context, dbHelper);
            }
        }
    }

    public static void scheduleNotifications(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, PasswordNotificationService.class);
        intent.setAction("CHECK_PASSWORDS");

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        } else {
            pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // Dacă timpul setat a trecut deja azi, programează pentru mâine
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } else {
            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
    }

    private void checkPasswordsAge(Context context, DatabaseHelper dbHelper) {
        List<SavedPassword> passwords = dbHelper.getSavedPasswords();
        if (passwords == null) return;

        long currentTime = System.currentTimeMillis();
        for (SavedPassword password : passwords) {
            if (currentTime - password.updatedAt >= SIX_MONTHS_IN_MILLIS) {
                showNotification(context, password);
            }
        }

        // Reprogramează pentru următoarea verificare
        scheduleNotifications(context);
    }

    private void showNotification(Context context, SavedPassword password) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Actualizări parole",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notificări pentru parole care necesită actualizare");
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Parolă învechită")
                .setContentText("Parola pentru " + password.siteName + " nu a fost actualizată de 6 luni")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Verifică permisiunea de notificare pentru Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(password.id, builder.build());
            }
        } else {
            notificationManager.notify(password.id, builder.build());
        }
    }
}
