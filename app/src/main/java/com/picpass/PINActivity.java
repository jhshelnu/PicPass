package com.picpass;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.picpass.Managers.ResourceManager;

public class PINActivity extends AppCompatActivity {
    private static final String TAG = "PINActivity";

    ImageView bubble1, bubble2, bubble3, bubble4; // The 4 PIN bubbles that fill-in as the user types
    EditText pinTextField;
    boolean tutorialMode;

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

        tutorialMode = ResourceManager.shouldDoTutorial(this);
        if (tutorialMode) {
            new AlertDialog.Builder(this, R.style.PicPassDialog)
                    .setTitle("Welcome to PicPass!")
                    .setMessage("PicPass lets you create complex, secure passwords by tapping images!\n\n" +
                            "To get started, enter a PIN.\n" +
                            "(You will need to remember this PIN in order to get the same passwords later!)")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            onShowKeyboard(null);
                        }
                    })
                    .setIcon(R.drawable.cape)
                    .show();
        } else {
            onShowKeyboard(null);
        }
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
        Intent intent = new Intent(this, PasswordPickerActivity.class);
        intent.putExtra("pin", pinTextField.getText().toString());
        if (tutorialMode) {
            intent.putExtra("tutorialMode", true);

            // Hide the keyboard manually on tutorial mode
            // Otherwise it won't close til after the user dismisses the dialog on the next screen
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            View view = getCurrentFocus();
            if (view == null) {
                view = new View(this);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        startActivity(intent);
    }
}
