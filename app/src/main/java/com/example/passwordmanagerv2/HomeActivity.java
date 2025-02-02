package com.example.passwordmanagerv2;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.passwordmanagerv2.data.AppDatabase;
import com.example.passwordmanagerv2.data.entity.User;
import com.example.passwordmanagerv2.wifi.WiFiPasswordsActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {
    // Declararea variabilelor la nivel de clasă
    private TextView welcomeText;
    private TextView userNameText;
    private MaterialButton viewPasswordsButton;
    private MaterialButton viewWifiPasswordsButton;
    private FloatingActionButton historyButton;
    private FloatingActionButton settingsButton;
    private DatabaseHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setupToolbar();
        initializeViews();
        setupListeners();
        loadUserData();

        // Adaugă animațiile
        setupCircuitBackgroundAnimation();
        setupGlitchEffect();
        setupButtonAnimations();
    }

    private void setupCircuitBackgroundAnimation() {
        View circuitBackground = findViewById(R.id.circuitBackground);
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(circuitBackground, "rotation", 0f, 360f);
        rotationAnimator.setDuration(5000);
        rotationAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotationAnimator.setInterpolator(new LinearInterpolator());
        rotationAnimator.start();
    }

    private void setupGlitchEffect() {
        View glitchOverlay = findViewById(R.id.glitchOverlay);

        // Animație alpha pentru efect de glitch
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(glitchOverlay, "alpha", 0.1f, 0.3f, 0.1f);
        alphaAnimator.setDuration(2000);
        alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
        alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        alphaAnimator.start();
    }

    private void setupButtonAnimations() {
        MaterialButton viewPasswordsButton = findViewById(R.id.viewPasswordsButton);
        MaterialButton viewWifiPasswordsButton = findViewById(R.id.viewWifiPasswordsButton);

        // Pulsating effect for first button
        ObjectAnimator scaleX1 = ObjectAnimator.ofFloat(viewPasswordsButton, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator scaleY1 = ObjectAnimator.ofFloat(viewPasswordsButton, "scaleY", 1f, 1.05f, 1f);

        scaleX1.setRepeatCount(ValueAnimator.INFINITE);
        scaleY1.setRepeatCount(ValueAnimator.INFINITE);

        scaleX1.setDuration(1000);
        scaleY1.setDuration(1000);

        scaleX1.start();
        scaleY1.start();

        // Pulsating effect for second button
        ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(viewWifiPasswordsButton, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(viewWifiPasswordsButton, "scaleY", 1f, 1.05f, 1f);

        scaleX2.setRepeatCount(ValueAnimator.INFINITE);
        scaleY2.setRepeatCount(ValueAnimator.INFINITE);

        scaleX2.setDuration(1000);
        scaleY2.setDuration(1000);

        // Adaugă o mică întârziere pentru al doilea buton
        scaleX2.setStartDelay(500);
        scaleY2.setStartDelay(500);

        scaleX2.start();
        scaleY2.start();
    }
    // Dezactivează ActionBar-ul implicit înainte de a seta toolbar-ul

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
    }

    private void initializeViews() {
        // Inițializare DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Găsire și inițializare elemente din interfață
        welcomeText = findViewById(R.id.welcomeText);
        userNameText = findViewById(R.id.userNameText);
        viewPasswordsButton = findViewById(R.id.viewPasswordsButton);
        viewWifiPasswordsButton = findViewById(R.id.viewWifiPasswordsButton);
        historyButton = findViewById(R.id.historyButton);
        settingsButton = findViewById(R.id.settingsButton);
    }

    private void setupListeners() {
        // Listener pentru butonul de istoric
        if (historyButton != null) {
            historyButton.setOnClickListener(v ->
                    startActivity(new Intent(this, PasswordHistoryActivity.class)));
        }

        // Listener pentru butonul de setări
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v ->
                    startActivity(new Intent(this, SettingsActivity.class)));
        }

        // Listener pentru butonul de vizualizare parole
        if (viewPasswordsButton != null) {
            viewPasswordsButton.setOnClickListener(v ->
                    startActivity(new Intent(this, PasswordListActivity.class)));
        }

        // Listener pentru butonul de vizualizare parole WiFi
        if (viewWifiPasswordsButton != null) {
            viewWifiPasswordsButton.setOnClickListener(v ->
                    startActivity(new Intent(this, WiFiPasswordsActivity.class)));
        }
    }

    private void loadUserData() {
        new AsyncTask<Void, Void, User>() {
            @Override
            protected User doInBackground(Void... voids) {
                return dbHelper.getCurrentUser();
            }

            @Override
            protected void onPostExecute(User user) {
                if (user != null && userNameText != null) {
                    // Setează numele utilizatorului
                    userNameText.setText(user.lastName);
                }
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            performLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void performLogout() {
        try {
            dbHelper.logout();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!dbHelper.isLoggedIn()) {
            performLogout();
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