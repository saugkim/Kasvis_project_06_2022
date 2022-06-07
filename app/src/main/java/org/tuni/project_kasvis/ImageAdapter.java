package org.tuni.project_kasvis;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

public class ImageAdapter extends ListAdapter<Image, ImageViewHolder> {

    public ImageAdapter(@NonNull DiffUtil.ItemCallback<Image> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ImageViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        Image currentImage = getItem(position);
        holder.bind(currentImage);
    }

    static class ItemDiff extends DiffUtil.ItemCallback<Image> {
        @Override
        public boolean areItemsTheSame(@NonNull Image oldItem, @NonNull Image newItem) {
            return oldItem == newItem;
        }
        @Override
        public boolean areContentsTheSame(@NonNull Image oldItem, @NonNull Image newItem) {
            return oldItem.getId() == newItem.getId();
        }
    }
}
