package com.example.passwordmanagerv2;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.passwordmanagerv2.data.entity.SavedPassword;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class PasswordListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PasswordAdapter adapter;
    private TextView emptyStateTextView;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_list);

        setupToolbar();
        initializeViews();
        loadPasswords();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.saved_passwords);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initializeViews() {
        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.savedPasswordsRecyclerView);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);
        FloatingActionButton fabAddPassword = findViewById(R.id.addPasswordButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PasswordAdapter(this);
        recyclerView.setAdapter(adapter);

        fabAddPassword.setOnClickListener(v ->
                startActivity(new Intent(this, AddPasswordActivity.class)));
    }

    private void loadPasswords() {
        new LoadPasswordsTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPasswords();
    }

    private class LoadPasswordsTask extends AsyncTask<Void, Void, List<SavedPassword>> {
        @Override
        protected List<SavedPassword> doInBackground(Void... voids) {
            return dbHelper.getSavedPasswords();
        }

        @Override
        protected void onPostExecute(List<SavedPassword> passwords) {
            if (passwords != null && !passwords.isEmpty()) {
                adapter.setPasswords(passwords);
                emptyStateTextView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                emptyStateTextView.setVisibility(View.VISIBLE);
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
