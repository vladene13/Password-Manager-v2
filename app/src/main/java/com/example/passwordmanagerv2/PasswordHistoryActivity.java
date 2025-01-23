package com.example.passwordmanagerv2;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.passwordmanagerv2.data.entity.SavedPassword;
import java.util.List;

public class PasswordHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PasswordAdapter adapter;
    private TextView emptyStateText;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_history);
        setupToolbar();
        initializeViews();
        loadArchivedPasswords();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Istoric parole");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initializeViews() {
        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.archivedPasswordsRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PasswordAdapter(this, true); // true pentru modul arhivă
        recyclerView.setAdapter(adapter);
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