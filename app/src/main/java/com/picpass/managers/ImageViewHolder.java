package com.picpass.managers;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.picpass.R;

import java.util.ArrayList;

import static com.picpass.ImageGalleryActivity.REQUIRED_NUM_IMAGES;

class ImageViewHolder extends RecyclerView.ViewHolder {
    private static Animation animation;

    ImageView image;
    ImageView checked;

    ImageViewHolder(final ArrayList<String> imageSet, @NonNull View itemView) {
        super(itemView);
        image = itemView.findViewById(R.id.gallery_image);
        checked = itemView.findViewById(R.id.checked_indicator);

        if (animation == null) {
            animation =  AnimationUtils.loadAnimation(itemView.getContext(), R.anim.image_click);
        }

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image.startAnimation(animation);
                if (imageSet.contains(image.getTag().toString())) {
                    // Deselect image
                    checked.setVisibility(View.INVISIBLE);
                    imageSet.remove(image.getTag().toString());
                } else if (imageSet.size() < REQUIRED_NUM_IMAGES) {
                    // Select image
                    checked.setVisibility(View.VISIBLE);
                    imageSet.add(image.getTag().toString());
                }
            }
        });
    }

    public void unCheck() {
        checked.setVisibility(View.INVISIBLE);
    }
}
