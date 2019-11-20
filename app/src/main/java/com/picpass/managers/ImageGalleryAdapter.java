package com.picpass.managers;

import android.content.Context;
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

    private final Context ctx;
    private ArrayList<String> imageSet;
    private ArrayList<ImageViewHolder> viewHolders;

    public ImageGalleryAdapter(Context ctx, ArrayList<String> imageSet) {
        this.ctx = ctx;
        this.imageSet = imageSet;
        viewHolders = new ArrayList<>();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.selectable_image, parent, false);
        ImageViewHolder holder = new ImageViewHolder(imageSet, view);

        viewHolders.add(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageName = ResourceManager.galleryImages.get(position);
        int resID = ctx.getResources().getIdentifier(imageName, "drawable", ctx.getPackageName());

        holder.image.setTag(imageName);
        holder.image.setImageResource(resID);

        if (imageSet.contains(imageName)) {
            holder.checked.setVisibility(View.VISIBLE);
        } else {
            holder.checked.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return ResourceManager.galleryImages.size();
    }

    public void unCheckAll() {
        for (ImageViewHolder holder : viewHolders) {
            holder.unCheck();
        }
    }
}
