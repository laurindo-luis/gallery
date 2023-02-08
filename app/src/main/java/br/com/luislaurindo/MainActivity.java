package br.com.luislaurindo;

import static java.util.Objects.nonNull;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import br.com.luislaurindo.azure.AzureStorage;
import br.com.luislaurindo.gallery.ImageViewActivity;
import br.com.luislaurindo.gallery.adapter.Image;
import br.com.luislaurindo.gallery.adapter.ImagesAdapter;
import br.com.luislaurindo.utils.BitmapUtils;

public class MainActivity extends AppCompatActivity {

    public final int PERMISSION_CODE = 1000;
    public static final int IMAGE_DELETED = 2000;

    private final String TAG = MainActivity.class.getSimpleName();

    private File imageOutput;

    private ImagesAdapter imagesAdapter;

    private int positionSelected;

    private FloatingActionButton btDelete;

    private List<Image> imageSelectedList;

    private CloudBlobContainer cloudBlobContainer;

    private TextView currentStateTextView;
    private TextView targetTextView;

    private ProgressBar progressBar;

    private Handler handler;

    private AlertDialog loadingAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler(Looper.getMainLooper());

        View dialogProgressView = getLayoutInflater().inflate(R.layout.dialog_progress, null);
        loadingAlertDialog = DialogComponent.createProgressAlertDialog(
                this,
                dialogProgressView,
                "Por favor, aguarde..."
        );

        currentStateTextView = dialogProgressView.findViewById(R.id.currentState);
        targetTextView = dialogProgressView.findViewById(R.id.target);
        progressBar = dialogProgressView.findViewById(R.id.determinateBar);

        imageSelectedList = new ArrayList<>();

        imagesAdapter = new ImagesAdapter(new ArrayList<>(), onClickListener, imageAdapterLongClickListener);

        RecyclerView recyclerView = findViewById(R.id.listImages);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(imagesAdapter);

        FloatingActionButton btCamera = findViewById(R.id.btCamera);
        btCamera.setOnClickListener(v -> {
            if(!isPermissions()) {
                String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions, PERMISSION_CODE);
            } else {
                openCamera();
            }
        });

        btDelete = findViewById(R.id.btDelete);
        btDelete.setOnClickListener(v -> {
            DialogComponent.createDeleteAlertDialog(
                    this,
                    "Excluir imagens",
                    "Deseja realmente excluir as imagens selecionadas?",
                    (dialogInterface, i) -> {
                        deleteImages(imageSelectedList);
                        imagesAdapter.disableLongClick();
                        btDelete.setVisibility(View.GONE);
                        imageSelectedList = new ArrayList<>();
                    }
            ).show();
        });

//        String key = "";
//        String containerName = "";
//
//        ExecutorService executorService = Executors.newSingleThreadExecutor();
//        executorService.execute(() -> {
//            try {
//                CloudBlobClient cloudBlobClient = AzureStorage.connect(key);
//                cloudBlobContainer = AzureStorage.getCloudBlobContainer(cloudBlobClient, containerName);
//            } catch (URISyntaxException | InvalidKeyException | StorageException e) {
//                e.printStackTrace();
//            }
//        });
    }

    @Override
    public void onBackPressed() {
        if(!imageSelectedList.isEmpty()) {
            imageSelectedList = new ArrayList<>();
            imagesAdapter.disableLongClick();
            btDelete.setVisibility(View.GONE);
            imagesAdapter.getImageList().forEach(image -> image.setSelected(false));
            imagesAdapter.notifyItemRangeChanged(0, imagesAdapter.getItemCount());
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_upload) {

            List<File> files = imagesAdapter.getImageList().stream()
                    .map(Image::getFile)
                    .collect(Collectors.toList());

            if(!files.isEmpty()) {
                DialogComponent.createConfirmAlertDialog(
                        this,
                        "Upload imagens",
                        "Deseja enviar as imagens?",
                        (dialogInterface, i) -> {
                            loadingAlertDialog.show();
                            uploadFiles(cloudBlobContainer, files);
                        }
                ).show();
            } else {
                DialogComponent.createInformationAlertDialog(
                        this,
                        "Aviso",
                        "Não há imagens para serem enviadas!"
                ).show();
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isPermissions() {
        return checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Permissão não concedida", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        imageOutput = getCaptureImageOutputFile();
        if(nonNull(imageOutput)) {
            Uri outputFileUri = FileProvider.getUriForFile(this, "br.com.luislaurindo.provider", imageOutput);
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraActivityResultLauncher.launch(cameraIntent);
        }
    }

    private File getCaptureImageOutputFile() {
        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + Environment.DIRECTORY_PICTURES + File.separator + "BRforce";
        File directory = new File(path);
        if(!directory.exists() && !directory.mkdir()) {
            Log.i(TAG, "Não foi possível criar o diretório "+directory.getPath());
            return null;
        }

        return new File(directory.getPath(), System.currentTimeMillis() + ".jpg");
    }

    ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == Activity.RESULT_OK) {
                    Image image = new Image(imageOutput);
                    try {
                        Bitmap bitmap = BitmapUtils.decodeSampledBitmapFromFile(imageOutput.getAbsolutePath(), 200, 200);
                        image.setBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    imagesAdapter.addImage(image);
                    openCamera();
                }
            }
    );

    ActivityResultLauncher<Intent> imageViewActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == IMAGE_DELETED) {
                    imagesAdapter.removeImage(positionSelected);
                }
            }
    );

    @SuppressLint("NotifyDataSetChanged")
    private void deleteImages(List<Image> imageSelectedList) {
        List<Image> images = imagesAdapter.getImageList()
                .stream()
                .filter(image -> !imageSelectedList.contains(image))
                .collect(Collectors.toList());
        imagesAdapter.setImages(images);
        imagesAdapter.notifyDataSetChanged();
    }

    private final ImagesAdapter.ImageAdapterClickListener onClickListener = (view, index) -> {
        Image imageSelected = imagesAdapter.getImageList().get(index);

        if(imagesAdapter.isLongClick()) {
            if(!imageSelected.isSelected()) {
                imageSelected.setSelected(true);
                imageSelectedList.add(imageSelected);
            } else {
                imageSelected.setSelected(false);
                imageSelectedList.remove(imageSelected);
            }

            if(imageSelectedList.isEmpty()) {
                imagesAdapter.disableLongClick();
                btDelete.setVisibility(View.GONE);
                imagesAdapter.notifyItemRangeChanged(0, imagesAdapter.getItemCount());
            }
        } else {
            positionSelected = index;
            Intent intent = new Intent(this, ImageViewActivity.class);
            intent.putExtra("uri", Uri.fromFile(imageSelected.getFile()));
            imageViewActivityResultLauncher.launch(intent);
        }

        imagesAdapter.notifyItemChanged(index);
    };

    private final ImagesAdapter.ImageAdapterLongClickListener imageAdapterLongClickListener = (view, index) -> {
        imagesAdapter.enableLongClick();
        if(btDelete.getVisibility() == View.GONE) {
            btDelete.setVisibility(View.VISIBLE);
        }
        Image imageSelected = imagesAdapter.getImageList().get(index);
        imageSelected.setSelected(true);
        imageSelectedList.add(imageSelected);
        imagesAdapter.notifyItemRangeChanged(0, imagesAdapter.getItemCount());
    };

    private void uploadFiles(CloudBlobContainer cloudBlobContainer, List<File> files) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            int countSucessUpload = 0;

            for (File file : files) {
                int finalCountSucessUpload = countSucessUpload + 1;
                handler.post(() -> uploadProgress(finalCountSucessUpload, files.size()));
                try {
                    String fileName =  file.getName().replaceAll("\\.jpg|\\.jpeg|\\.bmp|\\.png", ".webp");
                    Bitmap bitmap = BitmapUtils.decodeSampledBitmapFromFile(file.getAbsolutePath(), 1280, 720);
                    AzureStorage.upload(cloudBlobContainer, BitmapUtils.getInputStream(bitmap, 50), fileName);
                    handler.post(() -> uploadProgressBar(finalCountSucessUpload, files.size()));
                    countSucessUpload++;
                } catch (URISyntaxException | StorageException | IOException e) {
                    e.printStackTrace();
                    handler.post(() -> {
                        loadingAlertDialog.dismiss();
                        DialogComponent.createInformationAlertDialog(
                                this,
                                "Aviso",
                                "Erro de comunicação com o servidor! Falha ao realizar o upload das imagens!"
                        ).show();
                    });
                }
            }

            int finalcountSucessUpload = countSucessUpload;
            handler.postDelayed(() -> {
                loadingAlertDialog.dismiss();
                if(finalcountSucessUpload == files.size()) {
                    DialogComponent.createInformationAlertDialog(
                            this,
                            "Aviso",
                            "Upload realizado com sucesso!"
                    ).show();
                }
                cleanProgressUpload();
            }, 1000);
        });
    }

    private void uploadProgress(int countUpload, int target) {
        currentStateTextView.setText(String.valueOf(countUpload));
        targetTextView.setText(String.valueOf(target));
    }

    private void uploadProgressBar(int countUpload, int target) {
        double progress = (countUpload * 100) / (double) target;
        progressBar.setProgress((int) progress, true);
    }

    private void cleanProgressUpload() {
        currentStateTextView.setText("0");
        targetTextView.setText("0");
        progressBar.setProgress(0);
    }
}