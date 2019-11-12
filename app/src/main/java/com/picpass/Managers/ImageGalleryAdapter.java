package com.picpass.Managers;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.picpass.R;

import java.util.ArrayList;

public class ImageGalleryAdapter extends RecyclerView.Adapter<ImageViewHolder> {
    private static final String TAG = "ImageGalleryAdapter";

    // Complete set of PicPass Images. To add more, drag .png files to res/drawable and add the filename to this array.
    private static final String[] gallery = {"beach", "bridge", "cape", "castle", "cityscape", "desert",
                                             "desert2", "fields", "fields2", "forest", "hills", "home",
                                             "home2", "iceberg", "island", "mill", "mountains", "mountains2",
                                             "nuclearplant", "river", "ruins", "sea", "spruce", "trees",
                                             "village", "waterfall", "waterfall2", "windmills"};
    private ArrayList<String> imageSet;

    public ImageGalleryAdapter(ArrayList<String> imageSet) {
        this.imageSet = imageSet;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.selectable_image, parent, false);
        return new ImageViewHolder(imageSet, view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageName = gallery[position];
        holder.image.setTag(imageName);

        if (imageSet.contains(imageName)) {
            holder.checked.setVisibility(View.VISIBLE);
        } else {
            holder.checked.setVisibility(View.INVISIBLE);
        }

        try {
            holder.image.setImageResource(ResourceManager.getDrawableIdFromString(imageName));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.wtf(TAG, Log.getStackTraceString(e));
        }
    }

    @Override
    public int getItemCount() {
        return gallery.length;
    }
}
