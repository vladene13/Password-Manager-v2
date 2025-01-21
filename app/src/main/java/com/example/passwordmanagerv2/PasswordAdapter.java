package com.example.passwordmanagerv2;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.passwordmanagerv2.data.entity.SavedPassword;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PasswordAdapter extends RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder> {
    private List<SavedPassword> passwords = new ArrayList<>();
    private final Context context;
    private final DatabaseHelper dbHelper;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public PasswordAdapter(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public PasswordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.password_item, parent, false);
        return new PasswordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PasswordViewHolder holder, int position) {
        SavedPassword password = passwords.get(position);
        holder.bind(password);
    }

    @Override
    public int getItemCount() {
        return passwords.size();
    }

    public void setPasswords(List<SavedPassword> passwords) {
        this.passwords = passwords;
        notifyDataSetChanged();
    }

    class PasswordViewHolder extends RecyclerView.ViewHolder {
        private final TextView siteNameText;
        private final TextView usernameText;
        private final TextView passwordText;
        private final TextView lastUpdateText;
        private final ImageButton copyButton;
        private final ImageButton editButton;
        private final ImageButton deleteButton;
        private boolean isPasswordVisible = false;

        PasswordViewHolder(View itemView) {
            super(itemView);
            siteNameText = itemView.findViewById(R.id.siteNameText);
            usernameText = itemView.findViewById(R.id.usernameText);
            passwordText = itemView.findViewById(R.id.passwordText);
            lastUpdateText = itemView.findViewById(R.id.lastUpdateText);
            copyButton = itemView.findViewById(R.id.copyButton);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            setupButtonListeners();
        }

        private void setupButtonListeners() {
            passwordText.setOnClickListener(v -> togglePasswordVisibility());

            copyButton.setOnClickListener(v -> copyPassword());

            editButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    SavedPassword password = passwords.get(position);
                    Intent intent = new Intent(context, EditPasswordActivity.class);
                    intent.putExtra("PASSWORD_ID", password.id);
                    context.startActivity(intent);
                }
            });

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    showDeleteConfirmationDialog(passwords.get(position));
                }
            });
        }

        void bind(SavedPassword password) {
            siteNameText.setText(password.siteName);
            usernameText.setText(password.username);
            setPasswordVisibility(false);

            // Formatare și afișare dată
            String lastUpdate = dateFormat.format(new Date(password.updatedAt));
            lastUpdateText.setText("Ultima actualizare: " + lastUpdate);

            // Verifică dacă parola este mai veche de 6 luni
            long sixMonthsInMillis = 180L * 24 * 60 * 60 * 1000;
            if (System.currentTimeMillis() - password.updatedAt >= sixMonthsInMillis) {
                lastUpdateText.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            } else {
                lastUpdateText.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            }
        }

        private void togglePasswordVisibility() {
            setPasswordVisibility(!isPasswordVisible);
        }

        private void setPasswordVisibility(boolean visible) {
            isPasswordVisible = visible;
            SavedPassword password = passwords.get(getAdapterPosition());
            passwordText.setText(isPasswordVisible ?
                    dbHelper.decryptPassword(password.encryptedPassword) :
                    "••••••••");
        }

        private void copyPassword() {
            SavedPassword password = passwords.get(getAdapterPosition());
            String decryptedPassword = dbHelper.decryptPassword(password.encryptedPassword);

            ClipboardManager clipboard = (ClipboardManager)
                    context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("password", decryptedPassword);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(context, "Parola a fost copiată", Toast.LENGTH_SHORT).show();
        }

        private void showDeleteConfirmationDialog(SavedPassword password) {
            new AlertDialog.Builder(context)
                    .setTitle("Confirmare ștergere")
                    .setMessage("Sigur doriți să ștergeți această parolă?")
                    .setPositiveButton("Da", (dialog, which) -> new DeletePasswordTask(password).execute())
                    .setNegativeButton("Nu", null)
                    .show();
        }

        private class DeletePasswordTask extends AsyncTask<Void, Void, Boolean> {
            private final SavedPassword password;
            private final int position;

            DeletePasswordTask(SavedPassword password) {
                this.password = password;
                this.position = getAdapterPosition();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                return dbHelper.deletePassword(password.id);
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    passwords.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Parola a fost ștearsă", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Eroare la ștergerea parolei", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}