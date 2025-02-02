package com.example.passwordmanagerv2.wifi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.passwordmanagerv2.DatabaseHelper;
import com.example.passwordmanagerv2.R;
import com.example.passwordmanagerv2.data.entity.wifi.WiFiPassword;
import java.util.ArrayList;
import java.util.List;

public class WiFiPasswordAdapter extends RecyclerView.Adapter<WiFiPasswordAdapter.WiFiPasswordViewHolder> {
    private List<WiFiPassword> passwords = new ArrayList<>();
    private final Context context;
    private final DatabaseHelper dbHelper;

    public WiFiPasswordAdapter(Context context, DatabaseHelper dbHelper) {
        this.context = context;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public WiFiPasswordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wifi_password_item, parent, false);
        return new WiFiPasswordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WiFiPasswordViewHolder holder, int position) {
        WiFiPassword password = passwords.get(position);
        holder.bind(password);
    }

    @Override
    public int getItemCount() {
        return passwords.size();
    }

    public void setPasswords(List<WiFiPassword> passwords) {
        this.passwords = passwords;
        notifyDataSetChanged();
    }

    class WiFiPasswordViewHolder extends RecyclerView.ViewHolder {
        private final TextView ssidText;
        private final TextView securityTypeText;

        WiFiPasswordViewHolder(View itemView) {
            super(itemView);
            ssidText = itemView.findViewById(R.id.ssidText);
            securityTypeText = itemView.findViewById(R.id.securityTypeText);
        }

        void bind(WiFiPassword password) {
            ssidText.setText(password.getSsid());
            securityTypeText.setText(password.getSecurityType());
        }
    }
}