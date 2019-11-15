package com.picpass;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.picpass.Managers.ResourceManager;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static com.picpass.Managers.ResourceManager.getDrawableIdFromString;

/**
 * PasswordPickerActivity is responsible for generating passwords based on image selection input.
 * @author John Shelnutt, Jackson Gregory
 */
public class PasswordPickerActivity extends AppCompatActivity {
    private static final String TAG = "PasswordPickerActivity";
    private static final String CLIPBOARD_LABEL = "picpass_password";
    private static final int MODIFY_IMAGE_SET_REQUEST_CODE = 0;

    private static final int GENERATED_PASSWORD_LENGTH = 30; // WARNING: CHANGING THIS BREAKS EXISTING PASSWORDS!!!!
    private static final int MINIMUM_LENGTH = 5;    // minimum number of images in the sequence (repeats allowed) before password generation is allowed
    private static final int SESSION_DURATION = 60; // number of seconds being out of this activity that requires PIN re-entry
    private static final int COOLDOWN_DURATION = 4; // number of seconds after password generation before another password generation is allowed

    private String pin;
    private ImageView[] images;
    private List<String> imageNames;
    private ArrayList<String> sequence;
    private Button backspaceButton;
    private Animation animation;

    private Calendar inactivityStartTime;
    private Calendar cooldownStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_picker);

        sequence = new ArrayList<>();
        pin = getIntent().getStringExtra("pin");
        backspaceButton = findViewById(R.id.backspace_btn);
        animation = AnimationUtils.loadAnimation(this, R.anim.image_click);

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

        if (getIntent().getBooleanExtra("tutorialMode", false)) {
            new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.Theme_AppCompat_Light_Dialog))
                    .setTitle("Welcome to PicPass!")
                    .setMessage(("Tap at least 5 images (repeats allowed) and click \"Copy Password\" to get a password!\n\n" +
                            "To customize your images, click the gallery icon in the top-left corner."))
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) { }
                    })
                    .setIcon(R.drawable.cape)
                    .show();
        }

        imageNames = ResourceManager.loadImageSet(this);
        initializeImages(imageNames);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        long secondsInactive = (Calendar.getInstance().getTimeInMillis() - inactivityStartTime.getTimeInMillis()) / 1000;
        if (secondsInactive >= SESSION_DURATION) {
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        inactivityStartTime = Calendar.getInstance();
    }

    /**
     * This method binds the image name and resource to the 9 image views.
     * @param imageNames the names of all the images to display
     */
    private void initializeImages(List<String> imageNames) {
        if (imageNames == null || imageNames.size() != 9) {
            throw new IllegalArgumentException("imageNames array must non-null and of size 9!");
        }

        try {
            for (int i = 0; i < 9; i++) {
                images[i].setImageResource(getDrawableIdFromString(imageNames.get(i)));
                images[i].setTag(imageNames.get(i));
            }
        } catch (ReflectiveOperationException e) {
            Log.wtf(TAG, Log.getStackTraceString(e));
        }
    }

    public void onImageClick(View v) {
        v.startAnimation(animation);
        sequence.add(String.valueOf(v.getTag()));
        backspaceButton.setVisibility(View.VISIBLE);
    }

    public void onBackspace(View v) {
        if (sequence.size() > 0) {
            backspaceButton.startAnimation(animation);
            sequence.remove(sequence.size() - 1);
        }

        if (sequence.size() == 0) {
            backspaceButton.setVisibility(View.INVISIBLE);
        }
    }

    public void onLaunchGallery(View v) {
        sequence.clear();
        backspaceButton.setVisibility(View.INVISIBLE);

        Intent intent = new Intent(this, ImageGalleryActivity.class);
        intent.putExtra("currentImages", imageNames.toArray(new String[0]));
        startActivityForResult(intent, MODIFY_IMAGE_SET_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MODIFY_IMAGE_SET_REQUEST_CODE && resultCode == RESULT_OK) {
            imageNames = Arrays.asList(data.getStringArrayExtra("newImages"));
            initializeImages(imageNames);
        }
    }

    /**
     * Creates a secure password and copies it to the clipboard.
     * @param v View passed from onClick (unused)
     */
    public void onGeneratePassword(View v) {
        // Don't generate a password if the user is on cooldown
        if (cooldownStartTime != null) {
            long remainingCooldownSeconds = COOLDOWN_DURATION - ((Calendar.getInstance().getTimeInMillis() - cooldownStartTime.getTimeInMillis()) / 1000);
            if (remainingCooldownSeconds > 0) {
                Toast.makeText(this, String.format(Locale.getDefault(), "You must wait another %d %s", remainingCooldownSeconds, remainingCooldownSeconds == 1 ? "second" : "seconds"), Toast.LENGTH_SHORT).show();
                sequence.clear();
                backspaceButton.setVisibility(View.INVISIBLE);
                return;
            }
        }

        // Don't generate a password if the image sequence is too short
        if (sequence.size() < MINIMUM_LENGTH) {
            int imagesLeft = MINIMUM_LENGTH - sequence.size();
            Toast.makeText(this,
                    String.format(Locale.getDefault(), "You must select at least %d more %s.", imagesLeft, (imagesLeft == 1 ? "image" : "images")),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String generatedPassword = generatePassword(pin, TextUtils.join("", sequence));
        if (generatedPassword != null) {
//            Toast.makeText(this, generatedPassword, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, getResources().getString(R.string.password_generation_success), Toast.LENGTH_SHORT).show();

            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText(CLIPBOARD_LABEL, generatedPassword));

            sequence.clear();
            backspaceButton.setVisibility(View.INVISIBLE);
            cooldownStartTime = Calendar.getInstance(); // reset the cooldown start time

        } else {
            Toast.makeText(this, "An unexpected error occurred. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles the actual password generation using sha-256, the sequence of images, and the PIN.
     * @param toEncode the string to encode. toEncode will be the concatenation of tapped image's names smushed into one string.
     * @param key The key for sha-256. key will be the PIN.
     * @return a 30 character long password containing at least one of each: lowercase and uppercase letter, a number, and a symbol
     * or null, if the encryption process fails.
     */
    private String generatePassword(String toEncode, String key) {
        try {
            // initiate sha-256
            String algorithm = "HmacSHA256";
            SecretKeySpec sKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
            Mac sha256_HMAC = Mac.getInstance(algorithm);
            sha256_HMAC.init(sKey);

            // generate password and add guarantee
            String encodedString = Base64.encodeToString(sha256_HMAC.doFinal(toEncode.getBytes(StandardCharsets.UTF_8)), Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
            String passwordBeforeGuarantee = encodedString.substring(0, GENERATED_PASSWORD_LENGTH - 4).replace('-', '!').replace('_', '@');
            return addGuarantee(passwordBeforeGuarantee);

        } catch (NoSuchAlgorithmException | InvalidKeyException e){
            Log.wtf(TAG, Log.getStackTraceString(e));
            return null;
        }
    }

    /**
     * addGuarantee returns a string guaranteed to contain a lowercase letter, an uppercase letter, a number, and a symbol
     * @param orig Original string representing an almost complete password, just needs the guarantee described above.
     * @return A modified string of the original that meets the guarantee. This is the completed password.
     */
    private String addGuarantee(String orig) {
        StringBuilder sb = new StringBuilder(orig);

        int hash;
        char hashChar;
        int pos;

        // add a lowercase letter
        hash = Math.abs(sb.toString().hashCode());
        hashChar = (char) ((hash % 26) + (int) 'a');
        pos = hash % (sb.length() + 1);
        sb.insert(pos, hashChar);

        // add an uppercase letter
        hash = Math.abs(sb.toString().hashCode());
        hashChar = (char) ((hash % 26) + (int) 'A');
        pos = hash % (sb.length() + 1);
        sb.insert(pos, hashChar);

        // add a number
        hash = Math.abs(sb.toString().hashCode());
        hashChar = (char) ((hash % 10) + (int) '0');
        pos = hash % (sb.length() + 1);
        sb.insert(pos, hashChar);

        // add a symbol
        hash = Math.abs(sb.toString().hashCode());
        hashChar = (hash % 2 == 0) ? '!' : '@';
        pos = hash % (sb.length() + 1);
        sb.insert(pos, hashChar);

        return sb.toString();
    }
}
