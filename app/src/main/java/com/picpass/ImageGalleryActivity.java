package com.picpass;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.picpass.managers.ImageGalleryAdapter;
import com.picpass.managers.ResourceManager;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * ImageGalleryActivity allows the user to customize their image set for generating passwords.
 *
 * @author John Shelnutt, Jackson Gregory
 */
public class ImageGalleryActivity extends AppCompatActivity {
    public static final int REQUIRED_NUM_IMAGES = 9;
    private static final int SESSION_DURATION = 60; // number of seconds being out of this activity that requires PIN re-entry

    private ImageGalleryAdapter adapter;
    private ObservableArrayList<String> selectedImages;
    private TextView numSelected;
    private Button submitButton;
    private Button clearButton;

    private Calendar inactivityStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);

        numSelected = findViewById(R.id.num_selected);
        submitButton = findViewById(R.id.btn_submit);
        clearButton = findViewById(R.id.btn_clear);

        // When the adapter modifies the arraylist of selectedImages, this activity gets notified via the following callbacks (used to update UI)
        selectedImages = new ObservableArrayList<>();
        selectedImages.addOnListChangedCallback(new ObservableList.OnListChangedCallback<ObservableArrayList<String>>() {
            @Override
            public void onItemRangeInserted(ObservableArrayList<String> sender, int positionStart, int itemCount) {
                updateUI();
            }

            @Override
            public void onItemRangeRemoved(ObservableArrayList<String> sender, int positionStart, int itemCount) {
                updateUI();
            }

            @Override
            public void onChanged(ObservableArrayList<String> sender) { }
            @Override
            public void onItemRangeChanged(ObservableArrayList<String> sender, int positionStart, int itemCount) { }
            @Override
            public void onItemRangeMoved(ObservableArrayList<String> sender, int fromPosition, int toPosition, int itemCount) { }
        });

        // Pre-populate with current images
        List<String> currentImages = Arrays.asList(getIntent().getStringArrayExtra("currentImages"));
        selectedImages.addAll(currentImages);

        // Initialize the gallery recycler view
        RecyclerView galleryView = findViewById(R.id.gallery_recycler_view);
        galleryView.setLayoutManager(new GridLayoutManager(this, 3)); // number of columns

        // Set the adapter for the gallery recycler view (passes an empty observable arraylist for the adapter to populate with selected images)
        adapter = new ImageGalleryAdapter(selectedImages);
        galleryView.setAdapter(adapter);

        updateUI();
    }

    /**
     * Updates the display to show the current number of images selected and the submit button if appropriate
     */
    public void updateUI() {
        numSelected.setText(String.format(Locale.getDefault(), "%d/%d", selectedImages.size(), REQUIRED_NUM_IMAGES));

        if (selectedImages.size() > 0) {
            clearButton.setVisibility(View.VISIBLE);
        } else {
            clearButton.setVisibility(View.GONE);
        }

        if (selectedImages.size() == 9) {
            submitButton.setVisibility(View.VISIBLE);
        } else {
            submitButton.setVisibility(View.GONE);
        }
    }

    /**
     * Stores the current selected images to the PicPass configuration file
     * Passes the selected images along with the PIN to the PasswordPickerActivity
     * @param v The view (unused, but required for the callback)
     */
    public void onSubmit(View v) {
        if (selectedImages.size() == 9) {
            ResourceManager.saveImageSet(this, selectedImages);
            setResult(RESULT_OK, (new Intent()).putExtra("newImages", selectedImages.toArray(new String[0])));
            finish();
        }
    }

    public void onClear(View v) {
        selectedImages.clear();
        adapter.unCheckAll();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (inactivityStartTime != null) { // will be null when returning from the gallery
            long secondsInactive = (Calendar.getInstance().getTimeInMillis() - inactivityStartTime.getTimeInMillis()) / 1000;
            if (secondsInactive >= SESSION_DURATION) {
                Intent timeoutIntent = new Intent(this, PINActivity.class);
                timeoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(timeoutIntent);
                finish();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        inactivityStartTime = Calendar.getInstance();
    }
}
