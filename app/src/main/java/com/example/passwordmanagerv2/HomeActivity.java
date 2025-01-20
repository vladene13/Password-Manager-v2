package com.example.passwordmanagerv2;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.passwordmanagerv2.data.entity.User;
import com.google.android.material.button.MaterialButton;

public class HomeActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private TextView userNameText;

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
        toolbar.setPopupTheme(R.style.PopupMenu);
    }

    private void initializeViews() {
        dbHelper = new DatabaseHelper(this);
        userNameText = findViewById(R.id.userNameText);
    }

    private void setupListeners() {
        MaterialButton viewPasswordsButton = findViewById(R.id.viewPasswordsButton);
        viewPasswordsButton.setOnClickListener(v -> {
            startActivity(new Intent(this, PasswordListActivity.class));
        });
    }

    private void loadUserData() {
        new AsyncTask<Void, Void, User>() {
            @Override
            protected User doInBackground(Void... voids) {
                return dbHelper.getCurrentUser();
            }

            @Override
            protected void onPostExecute(User user) {
                if (user != null) {
                    userNameText.setText(user.lastName); // Folosim lastName în loc de firstName
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
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    dbHelper.logout();
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }.execute();
        } catch (Exception e) {
            e.printStackTrace();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}