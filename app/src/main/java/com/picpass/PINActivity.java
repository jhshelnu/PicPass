package com.picpass;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

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
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable currentPINText) {
                switch (currentPINText.length()) {
                    case 0:
                        bubble1.setImageResource(R.drawable.circle);
                        break;
                    case 1:
                        bubble1.setImageResource(R.drawable.circle_filled);
                        bubble2.setImageResource(R.drawable.circle);
                        break;
                    case 2:
                        bubble2.setImageResource(R.drawable.circle_filled);
                        bubble3.setImageResource(R.drawable.circle);
                        break;
                    case 3:
                        bubble3.setImageResource(R.drawable.circle_filled);
                        bubble4.setImageResource(R.drawable.circle);
                        break;
                    case 4:
                        bubble4.setImageResource(R.drawable.circle_filled);
                        Intent intent = new Intent(getApplicationContext(), PasswordPickerActivity.class);
                        intent.putExtra("pin", currentPINText.toString());
                        startActivity(intent);
                        break;
                }
            }
        });

        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(pinTextField, InputMethodManager.SHOW_FORCED);
        pinTextField.requestFocus();
        pinTextField.callOnClick();
    }

}
