package com.example.passwordmanagerv2;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.passwordmanagerv2.data.AppDatabase;
import com.example.passwordmanagerv2.service.PasswordNotificationService;
import com.example.passwordmanagerv2.wifi.WifiConnectionReceiver;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private static final int NOTIFICATION_PERMISSION_CODE = 123;
    private static final int WIFI_PERMISSION_CODE = 124;
    private ImageView logoImage;
    private MaterialButton loginButton, registerButton;
    private WifiConnectionReceiver wifiReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        dbHelper = new DatabaseHelper(this);

        if (dbHelper.isLoggedIn()) {
            startActivity(new Intent(this, PinActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        initializeViews();
        setupListeners();
        startLogoAnimation();
        requestPermissions();
        setupNotificationService();
        setupWifiReceiver();
        setupAnimations();
    }
    private void setupAnimations() {
        // Animație fundal circuit
        View circuitBackground = findViewById(R.id.circuitBackground);
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(circuitBackground, "rotation", 0f, 360f);
        rotationAnimator.setDuration(5000);
        rotationAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotationAnimator.setInterpolator(new LinearInterpolator());
        rotationAnimator.start();

        // Efect glitch overlay
        View glitchOverlay = findViewById(R.id.glitchOverlay);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(glitchOverlay, "alpha", 0.1f, 0.3f, 0.1f);
        alphaAnimator.setDuration(2000);
        alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
        alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        alphaAnimator.start();
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
                loginButton.setEnabled(true);
                registerButton.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        loginButton.setEnabled(false);
        registerButton.setEnabled(false);

        logoImage.startAnimation(fadeIn);
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS,
                                Manifest.permission.ACCESS_WIFI_STATE,
                                Manifest.permission.ACCESS_NETWORK_STATE
                        },
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    private void setupNotificationService() {
        PasswordNotificationService.scheduleNotifications(this);
    }

    private void setupWifiReceiver() {
        wifiReceiver = new WifiConnectionReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(wifiReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                setupWifiReceiver();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wifiReceiver != null) {
            wifiReceiver.stopReceiver(this);
        }
        if (dbHelper != null) {
            AppDatabase.destroyInstance();
        }
    }
}