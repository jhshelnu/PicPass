package com.picpass;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.picpass.Managers.ImageGalleryAdapter;

public class ImageGalleryActivity extends AppCompatActivity {

    RecyclerView galleryView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);

        galleryView = findViewById(R.id.gallery_recycler_view);
        galleryView.setLayoutManager(new GridLayoutManager(this, 3));
        galleryView.setAdapter(new ImageGalleryAdapter());
    }
}
