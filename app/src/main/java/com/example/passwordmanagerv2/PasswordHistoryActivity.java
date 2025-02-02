package com.example.passwordmanagerv2;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.passwordmanagerv2.data.entity.SavedPassword;
import com.example.passwordmanagerv2.wifi.WiFiPasswordsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class PasswordHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PasswordAdapter adapter;
    private TextView emptyStateText;
    private DatabaseHelper dbHelper;
    private FloatingActionButton addPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_history);

        setupToolbar();
        initializeViews();
        setupAnimations();
        loadArchivedPasswords();
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

        // Animație buton adăugare
        if (addPasswordButton != null) {
            ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(addPasswordButton, "scaleX", 1f, 1.1f, 1f);
            ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(addPasswordButton, "scaleY", 1f, 1.1f, 1f);

            scaleXAnimator.setDuration(1000);
            scaleYAnimator.setDuration(1000);

            scaleXAnimator.setRepeatCount(ValueAnimator.INFINITE);
            scaleYAnimator.setRepeatCount(ValueAnimator.INFINITE);

            scaleXAnimator.start();
            scaleYAnimator.start();
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Istoric parole");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void initializeViews() {
        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.archivedPasswordsRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);
        addPasswordButton = findViewById(R.id.addPasswordButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PasswordAdapter(this, true); // true pentru modul arhivă
        recyclerView.setAdapter(adapter);

        // Listener pentru butonul de adăugare
        if (addPasswordButton != null) {
            addPasswordButton.setOnClickListener(v -> {
                // Deschide activitatea de adăugare parolă
                startActivity(new Intent(this, AddPasswordActivity.class));
            });
        }
    }

    private void loadArchivedPasswords() {
        new LoadArchivedPasswordsTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadArchivedPasswords();
    }

    private class LoadArchivedPasswordsTask extends AsyncTask<Void, Void, List<SavedPassword>> {
        @Override
        protected List<SavedPassword> doInBackground(Void... voids) {
            return dbHelper.getArchivedPasswords();
        }

        @Override
        protected void onPostExecute(List<SavedPassword> passwords) {
            if (passwords != null && !passwords.isEmpty()) {
                adapter.setPasswords(passwords);
                emptyStateText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                emptyStateText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}