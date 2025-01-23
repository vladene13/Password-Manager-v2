package com.example.passwordmanagerv2;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.passwordmanagerv2.data.AppDatabase;
import com.example.passwordmanagerv2.data.entity.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {
    private TextView welcomeText;
    private TextView userNameText;
    private MaterialButton viewPasswordsButton;
    private DatabaseHelper dbHelper;
    private FloatingActionButton historyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setupToolbar();
        initializeViews();
        setupListeners();
        loadUserData();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
    }

    private void initializeViews() {
        dbHelper = new DatabaseHelper(this);
        welcomeText = findViewById(R.id.welcomeText);
        userNameText = findViewById(R.id.userNameText);
        viewPasswordsButton = findViewById(R.id.viewPasswordsButton);
        historyButton = findViewById(R.id.historyButton);
    }

    private void setupListeners() {
        if (historyButton != null) {
            historyButton.setOnClickListener(v ->
                    startActivity(new Intent(this, PasswordHistoryActivity.class)));
        }

        if (viewPasswordsButton != null) {
            viewPasswordsButton.setOnClickListener(v ->
                    startActivity(new Intent(this, PasswordListActivity.class)));
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