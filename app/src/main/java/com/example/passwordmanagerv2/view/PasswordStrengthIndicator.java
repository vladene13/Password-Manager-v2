package com.example.passwordmanagerv2.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.passwordmanagerv2.R;
import com.example.passwordmanagerv2.util.PasswordStrengthChecker;
import com.example.passwordmanagerv2.util.PasswordStrengthChecker.PasswordStrength;

public class PasswordStrengthIndicator extends LinearLayout {
    private View strengthIndicator;
    private TextView strengthText;

    public PasswordStrengthIndicator(Context context) {
        super(context);
        init(context);
    }

    public PasswordStrengthIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.password_strength_indicator, this, true);
        strengthIndicator = findViewById(R.id.strengthIndicator);
        strengthText = findViewById(R.id.strengthText);
        setOrientation(VERTICAL);
    }

    public void updateStrength(String password) {
        PasswordStrength strength = PasswordStrengthChecker.calculateStrength(password);
        strengthIndicator.setBackgroundColor(ContextCompat.getColor(getContext(), strength.getColorId()));
        strengthText.setText(strength.getMessage());
        strengthText.setTextColor(ContextCompat.getColor(getContext(), strength.getColorId()));
    }
}
