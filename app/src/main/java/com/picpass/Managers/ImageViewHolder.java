package com.picpass.Managers;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.picpass.R;

import java.util.ArrayList;

class ImageViewHolder extends RecyclerView.ViewHolder {
    private static final int REQUIRED_NUM_IMAGES = 9;
    private static Animation animation;

    ImageView image;
    CheckBox checked;

    ImageViewHolder(final ArrayList<String> imageSet, @NonNull View itemView) {
        super(itemView);
        image = itemView.findViewById(R.id.gallery_image);
        checked = itemView.findViewById(R.id.check_box);

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
                } else {
                    // Too many images already selected
                    Toast.makeText(image.getContext(), "You may not choose more than " + REQUIRED_NUM_IMAGES + " images", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
