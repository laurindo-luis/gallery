package br.com.luislaurindo.gallery;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import br.com.luislaurindo.DialogComponent;
import br.com.luislaurindo.MainActivity;
import br.com.luislaurindo.R;

public class ImageViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_image_view);

        Uri imageUri = (Uri) getIntent().getExtras().get("uri");

        FloatingActionButton btDelete = findViewById(R.id.btDelete);
        btDelete.setOnClickListener(v -> {
            DialogComponent.createDeleteAlertDialog(
                    this,
                    "Excluir imagem",
                    "Deseja realmente excluir a imagem?",
                    (dialogInterface, i) -> {
                        setResult(MainActivity.IMAGE_DELETED);
                        finish();
                    }
            ).show();
        });

        ImageView imageView = findViewById(R.id.image);
        imageView.setImageURI(imageUri);
        imageView.setOnClickListener(v -> {
            if(btDelete.getVisibility() == View.GONE) {
                btDelete.setVisibility(View.VISIBLE);
            } else {
                btDelete.setVisibility(View.GONE);
            }
        });
    }
}
