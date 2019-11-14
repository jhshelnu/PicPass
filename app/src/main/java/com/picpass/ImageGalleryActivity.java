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

import com.picpass.Managers.ImageGalleryAdapter;
import com.picpass.Managers.ResourceManager;

import java.util.Arrays;
import java.util.Locale;

/**
 * ImageGalleryActivity allows the user to customize their image set for generating passwords.
 *
 * @author John Shelnutt, Jackson Gregory
 */
public class ImageGalleryActivity extends AppCompatActivity {
    public static final int REQUIRED_NUM_IMAGES = 9;

    private ObservableArrayList<String> selectedImages;
    private TextView numSelected;
    private Button submitButton;

    private boolean editMode; // indicates if this activity should return the images to the PasswordPicker (editing)
                              // or launch the PasswordPicker afterwards (not editing, only happens once on the first launch)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);

        submitButton = findViewById(R.id.btn_submit);
        numSelected = findViewById(R.id.num_selected);

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

        // Pre-populate with current images if applicable
        String[] currentImages = getIntent().getStringArrayExtra("currentImages");
        if (currentImages != null) {
            editMode = true;
            selectedImages.addAll(Arrays.asList(currentImages));
        }

        // Initialize the gallery recycler view
        RecyclerView galleryView = findViewById(R.id.gallery_recycler_view);
        galleryView.setLayoutManager(new GridLayoutManager(this, 3)); // number of columns

        // Set the adapter for the gallery recycler view (passes an empty observable arraylist for the adapter to populate with selected images)
        final ImageGalleryAdapter adapter = new ImageGalleryAdapter(selectedImages);
        galleryView.setAdapter(adapter);

        updateUI();
    }

    /**
     * Updates the display to show the current number of images selected and the submit button if appropriate
     */
    public void updateUI() {
        numSelected.setText(String.format(Locale.getDefault(), "%d/%d", selectedImages.size(), REQUIRED_NUM_IMAGES));

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
        final String[] imageSet = selectedImages.toArray(new String[0]); // convert ArrayList<String> to String[]
        ResourceManager.saveImageSet(this, imageSet);

        if (editMode) {
            setResult(RESULT_OK, (new Intent()).putExtra("newImages", imageSet));
            finish();
        } else {
            final Intent intent = new Intent(this, PasswordPickerActivity.class);
            intent.putExtra("imageSet", imageSet);
            intent.putExtra("pin", getIntent().getStringExtra("pin"));
            finish();
            startActivity(intent);
        }
    }
}
