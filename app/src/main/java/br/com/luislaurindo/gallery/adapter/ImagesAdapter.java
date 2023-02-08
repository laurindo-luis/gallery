package br.com.luislaurindo.gallery.adapter;

import static java.util.Objects.nonNull;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.luislaurindo.R;


public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {

    private List<Image> images;

    private final ImageAdapterClickListener onClickListener;

    private final ImageAdapterLongClickListener imageAdapterLongClickListener;

    private boolean longClick = false;

    public interface ImageAdapterClickListener {
        void onClick(View view, int index);
    }

    public interface ImageAdapterLongClickListener {
        void onLongClickListener(View view, int index);
    }

    public ImagesAdapter(List<Image> images, ImageAdapterClickListener onClickListener,
                         ImageAdapterLongClickListener imageAdapterLongClickListener){
        this.images = images;
        this.onClickListener = onClickListener;
        this.imageAdapterLongClickListener = imageAdapterLongClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_image, viewGroup, false);
        return new ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView image;
        private final ImageView flagSelected;
        private final ImageView flagToSelect;

        public ViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.image);
            flagSelected = view.findViewById(R.id.flagSelected);
            flagToSelect = view.findViewById(R.id.flagToSelect);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Image image = images.get(position);

        if(longClick) {
            viewHolder.flagToSelect.setVisibility(View.VISIBLE);
        } else {
            viewHolder.flagToSelect.setVisibility(View.GONE);
        }

        if (image.isSelected()) {
            viewHolder.flagSelected.setVisibility(View.VISIBLE);
            viewHolder.flagToSelect.setVisibility(View.GONE);
        } else {
            viewHolder.flagSelected.setVisibility(View.GONE);
        }

        viewHolder.image.setImageBitmap(image.getBitmap());

        if(nonNull(onClickListener)) {
            viewHolder.itemView.setOnClickListener(v -> onClickListener.onClick(viewHolder.itemView, position));
        }

        if(nonNull(imageAdapterLongClickListener)) {
            viewHolder.itemView.setOnLongClickListener(view -> {
                imageAdapterLongClickListener.onLongClickListener(viewHolder.itemView, position);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void addImage(Image image) {
        this.images.add(image);
        notifyItemInserted(getItemCount());
    }

    public void removeImage(int index) {
        this.images.remove(index);
        notifyItemRemoved(index);
        notifyItemRangeChanged(index, getItemCount());
    }

    public List<Image> getImageList() {
        return this.images;
    }

    public void enableLongClick() {
        this.longClick = true;
    }

    public void disableLongClick() {
        this.longClick = false;
    }

    public boolean isLongClick() {
        return this.longClick;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }
}