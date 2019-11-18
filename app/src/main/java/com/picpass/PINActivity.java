package com.picpass;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.assist.AssistStructure;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.autofill.Dataset;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.picpass.managers.ResourceManager;

import java.util.List;

import static android.view.autofill.AutofillManager.EXTRA_AUTHENTICATION_RESULT;
import static com.picpass.PasswordPickerActivity.EXTRA_GENERATED_PASSWORD;
import static com.picpass.managers.PicPassAutofillService.EXTRA_AUTOFILL_FIELD_IDS;
import static com.picpass.managers.PicPassAutofillService.EXTRA_AUTOFILL_MODE;

public class PINActivity extends AppCompatActivity {
    private static final String TAG = "PINActivity";
    private static final int AUTOFILL_REQUEST_CODE = 0;

    private ImageView bubble1, bubble2, bubble3, bubble4; // The 4 PIN bubbles that fill-in as the user types
    private EditText pinTextField;
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

    private boolean isAutofillMode() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getIntent().getBooleanExtra(EXTRA_AUTOFILL_MODE, false);
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

        if (isAutofillMode()) {
            intent.putExtra(EXTRA_AUTOFILL_MODE, true);
            startActivityForResult(intent, AUTOFILL_REQUEST_CODE);
        } else {
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOFILL_REQUEST_CODE && resultCode == RESULT_OK) {
            if (isAutofillMode()) {
                String password = data.getStringExtra(EXTRA_GENERATED_PASSWORD);

                Dataset.Builder builder = new Dataset.Builder(new RemoteViews(getPackageName(), R.layout.autofill_message));

                List<AutofillId> passwordFieldAutofillIDs = getIntent().getParcelableArrayListExtra(EXTRA_AUTOFILL_FIELD_IDS);
                for (AutofillId id : passwordFieldAutofillIDs) {
                    builder.setValue(id, AutofillValue.forText(password));
                }

                Intent intent = new Intent();
                intent.putExtra(EXTRA_AUTHENTICATION_RESULT, builder.build());
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

}
