package com.picpass.managers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.picpass.R;

import java.util.ArrayList;

public class PasswordPickerDotAdapter extends RecyclerView.Adapter<PasswordPickerDotAdapter.PasswordPickerDotViewHolder> {

    private final ArrayList<String> sequence;

    public PasswordPickerDotAdapter(ArrayList<String> sequence) {
        this.sequence = sequence;
    }

    @NonNull
    @Override
    public PasswordPickerDotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.password_picker_dot, parent, false);
        return new PasswordPickerDotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PasswordPickerDotViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return sequence.size();
    }

    public class PasswordPickerDotViewHolder extends RecyclerView.ViewHolder {

        public PasswordPickerDotViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
