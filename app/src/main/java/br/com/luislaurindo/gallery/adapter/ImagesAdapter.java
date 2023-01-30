package br.com.luislaurindo.gallery.adapter;

import static java.util.Objects.nonNull;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
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

        Uri imageUri = Uri.fromFile(image.getFile());
        Bitmap bitmap = decodeSampledBitmapFromFile(imageUri.getPath(), 200, 200);
        try {
            bitmap = rotateImage(bitmap, imageUri.getPath());
            viewHolder.image.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    private Bitmap rotateImage(Bitmap bitmap, String path) throws IOException {
        int rotate = 0;
        ExifInterface exif;
        exif = new ExifInterface(path);
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                matrix, true);
    }

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
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