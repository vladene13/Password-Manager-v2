package com.example.passwordmanagerv2.wifi;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.passwordmanagerv2.DatabaseHelper;
import com.example.passwordmanagerv2.R;
import com.example.passwordmanagerv2.data.entity.wifi.WiFiPassword;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class WiFiPasswordsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DatabaseHelper dbHelper;
    private WiFiPasswordAdapter adapter;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_passwords);

        setupToolbar();
        initializeViews();
        loadWiFiPasswords();
        setupAnimations();
    }



    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.wifi_passwords);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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
        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.wifiPasswordsRecyclerView);
        emptyView = findViewById(R.id.emptyStateText);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WiFiPasswordAdapter(this, dbHelper);
        recyclerView.setAdapter(adapter);

        FloatingActionButton addButton = findViewById(R.id.addWifiPasswordButton);
        addButton.setOnClickListener(v -> {
            startActivity(new Intent(WiFiPasswordsActivity.this, AddWiFiPasswordActivity.class));
        });
    }

    private void loadWiFiPasswords() {
        new LoadWiFiPasswordsTask().execute();
    }

    private class LoadWiFiPasswordsTask extends AsyncTask<Void, Void, List<WiFiPassword>> {
        @Override
        protected List<WiFiPassword> doInBackground(Void... voids) {
            return dbHelper.getWiFiPasswords();
        }

        @Override
        protected void onPostExecute(List<WiFiPassword> passwords) {
            if (passwords != null && !passwords.isEmpty()) {
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
                adapter.setPasswords(passwords);
            } else {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWiFiPasswords();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper = null;
        }
    }
}