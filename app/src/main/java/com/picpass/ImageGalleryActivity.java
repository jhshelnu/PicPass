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

import java.util.Locale;

public class ImageGalleryActivity extends AppCompatActivity {
    public static final int REQUIRED_NUM_IMAGES = 9;

    RecyclerView galleryView;
    ObservableArrayList<String> imageSet;
    TextView numSelected;
    Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);

        submitButton = findViewById(R.id.btn_submit);
        numSelected = findViewById(R.id.num_selected);

        imageSet = new ObservableArrayList<>();
        imageSet.addOnListChangedCallback(new ObservableList.OnListChangedCallback<ObservableArrayList<String>>() {
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

        ImageGalleryAdapter adapter = new ImageGalleryAdapter(imageSet);
        galleryView = findViewById(R.id.gallery_recycler_view);
        galleryView.setLayoutManager(new GridLayoutManager(this, 3));
        galleryView.setAdapter(adapter);

        updateUI();
    }


    public void updateUI() {
        numSelected.setText(String.format(Locale.getDefault(), "%d/%d", imageSet.size(), REQUIRED_NUM_IMAGES));

        if (imageSet.size() == 9) {
            submitButton.setVisibility(View.VISIBLE);
        } else {
            submitButton.setVisibility(View.GONE);
        }
    }

    public void onSubmit(View v) {
        String pin = getIntent().getStringExtra("pin");

        Intent intent = new Intent(this, PasswordPickerActivity.class);
        intent.putExtra("images", imageSet.toArray(new String[0])); // convert ArrayList<String> to String[]
        intent.putExtra("pin", pin);
        finish();
        startActivity(intent);
    }
}
