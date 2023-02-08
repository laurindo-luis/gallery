package br.com.luislaurindo.gallery.adapter;

import android.graphics.Bitmap;

import java.io.File;


public class Image {

    private Long id;
    private File file;
    private Bitmap bitmap;
    private boolean selected;

    public Image() {
        this.selected = false;
    }

    public Image(File file) {
        this.file = file;
        this.selected = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
