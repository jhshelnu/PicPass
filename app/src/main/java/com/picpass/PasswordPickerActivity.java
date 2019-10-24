package com.picpass;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.lang.reflect.Field;

public class PasswordPickerActivity extends AppCompatActivity {
    private static final String TAG = "PasswordPickerActivity";

    ImageView[] images;
    String pin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_picker);

        pin = getIntent().getStringExtra("pin");

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
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void onImageClick(View v) {
        Toast.makeText(this, String.valueOf(v.getTag()), Toast.LENGTH_SHORT).show();
    }

    private int getDrawableIdFromString(String resName) throws NoSuchFieldException, IllegalAccessException {
        Field field = R.drawable.class.getDeclaredField(resName);
        return field.getInt(field);
    }
}
