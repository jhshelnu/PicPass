package com.picpass;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.picpass.Managers.ResourceManager;

public class PINActivity extends AppCompatActivity {
    private static final String TAG = "PINActivity";

    ImageView bubble1, bubble2, bubble3, bubble4; // The 4 PIN bubbles that fill-in as the user types
    EditText pinTextField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        bubble1 = findViewById(R.id.bubble1);
        bubble2 = findViewById(R.id.bubble2);
        bubble3 = findViewById(R.id.bubble3);
        bubble4 = findViewById(R.id.bubble4);
        pinTextField = findViewById(R.id.pin_text);

        // Define the actions to take when the pin text field changes
        pinTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable currentPINText) {
                switch (currentPINText.length()) {
                    case 0:
                        bubble1.setImageResource(R.drawable.circle);
                        bubble2.setImageResource(R.drawable.circle);
                        bubble3.setImageResource(R.drawable.circle);
                        bubble4.setImageResource(R.drawable.circle);
                        break;
                    case 1:
                        bubble1.setImageResource(R.drawable.circle_filled);
                        bubble2.setImageResource(R.drawable.circle);
                        bubble3.setImageResource(R.drawable.circle);
                        bubble4.setImageResource(R.drawable.circle);
                        break;
                    case 2:
                        bubble1.setImageResource(R.drawable.circle_filled);
                        bubble2.setImageResource(R.drawable.circle_filled);
                        bubble3.setImageResource(R.drawable.circle);
                        bubble4.setImageResource(R.drawable.circle);
                        break;
                    case 3:
                        bubble1.setImageResource(R.drawable.circle_filled);
                        bubble2.setImageResource(R.drawable.circle_filled);
                        bubble3.setImageResource(R.drawable.circle_filled);
                        bubble4.setImageResource(R.drawable.circle);
                        break;
                    case 4:
                        bubble1.setImageResource(R.drawable.circle_filled);
                        bubble2.setImageResource(R.drawable.circle_filled);
                        bubble3.setImageResource(R.drawable.circle_filled);
                        bubble4.setImageResource(R.drawable.circle_filled);

                        onPINEntered();
                        break;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
        });

        onShowKeyboard(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        pinTextField.setText("");
    }

    public void onShowKeyboard(View v) {
        pinTextField.requestFocus();
        InputMethodManager keyboardManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboardManager.showSoftInput(pinTextField, InputMethodManager.SHOW_IMPLICIT);
    }

    private void onPINEntered() {
        String[] imageSet = ResourceManager.loadImageSet(this);
        Intent intent;

        // First time using the app, no config file exists
        if (imageSet == null) {
            intent = new Intent(this, ImageGalleryActivity.class);
        } else {
            // Not the first time using the app - config file exists and we go straight to the password picker activity
            intent = new Intent(this, PasswordPickerActivity.class);
            intent.putExtra("imageSet", imageSet);
        }

        // PIN needs to be passed either way, as the end result will be to get to the PasswordPickerActivity eventually
        intent.putExtra("pin", pinTextField.getText().toString());
        startActivity(intent);
    }

}
