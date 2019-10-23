package com.picpass;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class PasswordGeneratorActivity extends AppCompatActivity {
    private static final String TAG = "PasswordGenActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_generator);

        Intent intent = getIntent();
        String pin = intent.getStringExtra("pin");

        if (pin != null) {
            Toast.makeText(this, String.format("PIN: %s", pin), Toast.LENGTH_SHORT).show();
        }
    }
}
