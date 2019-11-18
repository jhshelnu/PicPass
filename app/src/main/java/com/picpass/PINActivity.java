package com.picpass;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.service.autofill.Dataset;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillManager;
import android.view.autofill.AutofillValue;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

import com.picpass.managers.ResourceManager;

import java.util.List;

import static android.provider.Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE;
import static android.view.autofill.AutofillManager.EXTRA_AUTHENTICATION_RESULT;
import static com.picpass.PasswordPickerActivity.EXTRA_GENERATED_PASSWORD;
import static com.picpass.managers.PicPassAutofillService.EXTRA_AUTOFILL_FIELD_IDS;
import static com.picpass.managers.PicPassAutofillService.EXTRA_AUTOFILL_MODE;

public class PINActivity extends AppCompatActivity {
    private static final String TAG = "PINActivity";
    private static final int AUTOFILL_PASSWORD_REQUEST_CODE = 0;
    private static final int AUTOFILL_ENABLE_REQUEST_CODE = 1;

    private ImageView bubble1, bubble2, bubble3, bubble4; // The 4 PIN bubbles that fill-in as the user types
    private EditText pinTextField;
    private boolean tutorialMode;
    private LinearLayout autofillLink;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        bubble1 = findViewById(R.id.bubble1);
        bubble2 = findViewById(R.id.bubble2);
        bubble3 = findViewById(R.id.bubble3);
        bubble4 = findViewById(R.id.bubble4);
        pinTextField = findViewById(R.id.pin_text);
        autofillLink = findViewById(R.id.autofill_enable_link);

        // Present the user with an easy way to enable autofill functionality,
        // but only if the phone supports that feature and it is not already set to PicPass
        if (isAutofillCapable()) {
            AutofillManager autofillManager = getSystemService(AutofillManager.class);
            if (!autofillManager.hasEnabledAutofillServices()) {
                autofillLink.setVisibility(View.VISIBLE);
            }
        }

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
        return isAutofillCapable() && getIntent().getBooleanExtra(EXTRA_AUTOFILL_MODE, false);
    }

    private boolean isAutofillCapable() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
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
            startActivityForResult(intent, AUTOFILL_PASSWORD_REQUEST_CODE);
        } else {
            startActivity(intent);
        }
    }

    public void onEnableAutofill(View v) {
        if (isAutofillCapable()) {
            new AlertDialog.Builder(this, R.style.PicPassDialog)
                    .setIcon(R.drawable.cape)
                    .setTitle("Enable PicPass Autofill")
                    .setMessage("PicPass can be used directlywhen signing into any other app!\n\n" +
                            "You can do this by tapping \"Login with PicPass\" below any password field.\n\n" +
                            "Select \"Enable\" below to be taken into your phone's autofill settings, then simply select PicPass!")
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Uri uri = Uri.parse("package:" + getPackageName());
                            startActivityForResult(new Intent(ACTION_REQUEST_SET_AUTOFILL_SERVICE, uri), AUTOFILL_ENABLE_REQUEST_CODE);
                        }
                    }).setNegativeButton("Later", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == AUTOFILL_PASSWORD_REQUEST_CODE && isAutofillMode()) {
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
            } else if (requestCode == AUTOFILL_ENABLE_REQUEST_CODE) {
                autofillLink.setVisibility(View.INVISIBLE);
            }
        }
    }

}
