package com.example.pa.ui.photo;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.pa.R;

public class PhotoEditActivity extends AppCompatActivity {
    private ImageView editImageView;
    private Button btnRotate, btnBrightness, btnContrast, btnCancel, btnSave;
    private Bitmap currentBitmap;
    private float currentRotation = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_edit);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        initViews();
        loadImage();
        setupListeners();
    }

    private void initViews() {
        editImageView = findViewById(R.id.editImageView);
        btnRotate = findViewById(R.id.btnRotate);
        btnBrightness = findViewById(R.id.btnBrightness);
        btnContrast = findViewById(R.id.btnContrast);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
    }

    private void loadImage() {
        String imageUrl = getIntent().getStringExtra("image_url");
        Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                        currentBitmap = bitmap;
                        editImageView.setImageBitmap(bitmap);
                    }
                });
    }

    private void setupListeners() {
        btnRotate.setOnClickListener(v -> rotateImage());
        btnBrightness.setOnClickListener(v -> adjustBrightness());
        btnContrast.setOnClickListener(v -> adjustContrast());
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveImage());
    }

    private void rotateImage() {
        if (currentBitmap == null) return;
        currentRotation = (currentRotation + 90) % 360;
        Matrix matrix = new Matrix();
        matrix.postRotate(currentRotation);
        Bitmap rotatedBitmap = Bitmap.createBitmap(
                currentBitmap,
                0,
                0,
                currentBitmap.getWidth(),
                currentBitmap.getHeight(),
                matrix,
                true
        );
        editImageView.setImageBitmap(rotatedBitmap);
    }

    private void adjustBrightness() {
        Toast.makeText(this, "Brightness adjustment coming soon", Toast.LENGTH_SHORT).show();
    }

    private void adjustContrast() {
        Toast.makeText(this, "Contrast adjustment coming soon", Toast.LENGTH_SHORT).show();
    }

    private void saveImage() {
        Toast.makeText(this, "Saving edited image...", Toast.LENGTH_SHORT).show();
        finish();
    }
}
