package com.example.passwordmanagerv2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.passwordmanagerv2.data.AppDatabase;
import com.example.passwordmanagerv2.service.PasswordNotificationService;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private static final int NOTIFICATION_PERMISSION_CODE = 123;
    private ImageView logoImage;
    private MaterialButton loginButton, registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inițializare DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Verifică dacă utilizatorul este deja autentificat
        if (dbHelper.isLoggedIn()) {
            startActivity(new Intent(this, PinActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        initializeViews();
        setupListeners();
        startLogoAnimation();
        requestNotificationPermission();
        setupNotificationService();
    }

    private void initializeViews() {
        logoImage = findViewById(R.id.logoImage);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void startLogoAnimation() {
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1500);
        fadeIn.setFillAfter(true);

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                // Activează butoanele după ce animația s-a terminat
                loginButton.setEnabled(true);
                registerButton.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        // Dezactivează butoanele în timpul animației
        loginButton.setEnabled(false);
        registerButton.setEnabled(false);

        logoImage.startAnimation(fadeIn);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    private void setupNotificationService() {
        PasswordNotificationService.scheduleNotifications(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Verifică din nou starea de autentificare în cazul în care utilizatorul
        // se întoarce din alte activități
        if (dbHelper != null && dbHelper.isLoggedIn()) {
            startActivity(new Intent(this, PinActivity.class));
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupNotificationService();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            AppDatabase.destroyInstance();
        }
    }
}