package com.picpass;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class PasswordPickerActivity extends AppCompatActivity {
    private static final String TAG = "PasswordPickerActivity";
    private static final int GENERATED_PASSWORD_LENGTH = 30; /* /!\ WARNING /!\: CHANGING THIS BREAKS EXISTING PASSWORDS!!!! */
    private static final int MINIMUM_LENGTH = 3;

    private String pin;
    private ImageView[] images;
    private ArrayList<String> sequence;
    private Button backspaceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_picker);

        sequence = new ArrayList<>();
        pin = getIntent().getStringExtra("pin");
        backspaceButton = findViewById(R.id.backspace_btn);

        images = new ImageView[9];
        images[0] = findViewById(R.id.image0);
        images[1] = findViewById(R.id.image1);
        images[2] = findViewById(R.id.image2);
        images[3] = findViewById(R.id.image3);
        images[4] = findViewById(R.id.image4);
        images[5] = findViewById(R.id.image5);
        images[6] = findViewById(R.id.image6);
        images[7] = findViewById(R.id.image7);
        images[8] = findViewById(R.id.image8);

        String[] imageNames = {"river", "castle", "cape",
                                "bridge", "fields", "mill",
                                "beach", "sea", "iceberg"};

        initializeImages(imageNames);
    }

    private void initializeImages(String[] imageNames) {
        if (imageNames.length != 9) {
            throw new IllegalArgumentException("imageNames array must be of size 9!");
        }

        try {
            for (int i = 0; i < 9; i++) {
                images[i].setImageResource(getDrawableIdFromString(imageNames[i]));
                images[i].setTag(imageNames[i]);
                //TODO: change onImageClick to be a touch listener. add programmatically here (setOnTouchListener)
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void onImageClick(View v) {
        v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.image_click));
        sequence.add(String.valueOf(v.getTag()));
        backspaceButton.setVisibility(View.VISIBLE);

    }

    public void onBackspace(View v) {
        if (sequence.size() > 0) {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.image_click));
            sequence.remove(sequence.size() - 1);
        } else {
            v.setVisibility(View.INVISIBLE);
        }
    }

    private int getDrawableIdFromString(String resName) throws NoSuchFieldException, IllegalAccessException {
        Field field = R.drawable.class.getDeclaredField(resName);
        return field.getInt(field);
    }

    public void onGeneratePassword(View v) {
        if (sequence.size() < MINIMUM_LENGTH) {
            Toast.makeText(this, getResources().getString(R.string.sequence_too_short), Toast.LENGTH_SHORT).show();
            return;
        }

        String generatedPassword = generatePassword(pin, TextUtils.join("", sequence));
        if (generatedPassword != null) {
            Toast.makeText(this, generatedPassword, Toast.LENGTH_SHORT).show();
            Log.d(TAG, generatedPassword);
        } else {
            Toast.makeText(this, "An unexpected error occurred. Please try again.", Toast.LENGTH_SHORT).show();
        }
        sequence.clear();
        backspaceButton.setVisibility(View.INVISIBLE);
    }

    private String generatePassword(String toEncode, String key) {
        try {
            String algorithm = "HmacSHA256";
            SecretKeySpec sKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
            Mac sha256_HMAC = Mac.getInstance(algorithm);
            sha256_HMAC.init(sKey);

            // "aA1!" guarantees the passwords have: lowercase, uppercase, number, symbol
            String encodedString = Base64.encodeToString(sha256_HMAC.doFinal(toEncode.getBytes(StandardCharsets.UTF_8)), Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
            return encodedString.substring(0, GENERATED_PASSWORD_LENGTH - 4).concat("aA1!");
        } catch (NoSuchAlgorithmException | InvalidKeyException e){
            Log.wtf(TAG, Log.getStackTraceString(e));
            return null;
        }
    }
}
