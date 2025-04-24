package com.example.pa.ui.photo;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;

import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.WindowManager;
import android.view.View;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pa.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PhotoEditActivity extends AppCompatActivity {
    private ImageView editImageView;
    private LinearLayout adjustmentLayout;
    private TextView adjustmentLabel;
    private SeekBar adjustmentSeekBar;
    private Button btnRotate, btnBrightness, btnContrast, btnCancel, btnSave;
    private Bitmap currentBitmap;//current photo after edit
    private Bitmap originalBitmap;//origin photo
    private float brightness = 0f;
    private float contrast = 1f;
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

        adjustmentLayout = findViewById(R.id.adjustmentLayout);
        adjustmentLabel = findViewById(R.id.adjustmentLabel);
        adjustmentSeekBar = findViewById(R.id.adjustmentSeekBar);
        btnBrightness = findViewById(R.id.btnBrightness);
        btnContrast = findViewById(R.id.btnContrast);
    }

    private void loadImage() {
        String imagePath = getIntent().getStringExtra("image_path");

        // TEST
//        String imagePath = "/storage/emulated/0/DCIM/example.png";
        if (imagePath == null) {
            Toast.makeText(this, "Image path is null", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        try {
            // Load the image from the file path
            originalBitmap = BitmapFactory.decodeFile(imagePath);
            if (originalBitmap == null) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            currentBitmap = originalBitmap;
            editImageView.setImageBitmap(currentBitmap);
        } catch (Exception e) {
            Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
//   /storage/emulated/0/DCIM/
    private void setupListeners() {
        btnRotate.setOnClickListener(v -> rotateImage());

        btnBrightness.setOnClickListener(v -> setupAdjustment("Brightness"));
        btnContrast.setOnClickListener(v -> setupAdjustment("Contrast"));

        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveImage());

        adjustmentSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (adjustmentLabel.getText().toString().contains("Brightness")) {
                    brightness = (progress - 100) / 100f * 255;
                }
                else if(adjustmentLabel.getText().toString().contains("Contrast")){
                    contrast = progress / 100f;
                }
                applyImageAdjustments();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void render() {
        if (originalBitmap == null) return;

        /* ① 先做旋转 */
        Matrix matrix = new Matrix();
        matrix.postRotate(currentRotation);
        Bitmap rotated = Bitmap.createBitmap(
                originalBitmap, 0, 0,
                originalBitmap.getWidth(), originalBitmap.getHeight(),
                matrix, true);

        /* ② 再做亮度/对比度 */
        ColorMatrix cm = new ColorMatrix();
        // 对比度
        float scale = contrast;
        float translate = (-.5f * scale + .5f) * 255f;
        cm.set(new float[]{
                scale, 0, 0, 0, translate,
                0, scale, 0, 0, translate,
                0, 0, scale, 0, translate,
                0, 0, 0, 1, 0
        });
        // 亮度
        cm.postConcat(new ColorMatrix(new float[]{
                1, 0, 0, 0, brightness,
                0, 1, 0, 0, brightness,
                0, 0, 1, 0, brightness,
                0, 0, 0, 1, 0
        }));

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));

        Bitmap out = Bitmap.createBitmap(
                rotated.getWidth(), rotated.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        canvas.drawBitmap(rotated, 0, 0, paint);

        currentBitmap = out;
        editImageView.setImageBitmap(currentBitmap);
    }


    private void rotateImage() {
        currentRotation = (currentRotation + 90) % 360;
        render();
    }

    private void applyImageAdjustments() {
        render();
    }


    private void setupAdjustment(String type) {
        adjustmentLabel.setText(type + " Adjustment");
        adjustmentLayout.setVisibility(View.VISIBLE);
        // 不重置进度条，保持当前值
        if (type.equals("Brightness")) {
            adjustmentSeekBar.setProgress((int)(brightness / 255f * 100f + 100));
        } else if (type.equals("Contrast")) {
            adjustmentSeekBar.setProgress((int)(contrast * 100f));
        }
    }



    private void saveImage() {
        if (currentBitmap == null) {
            Toast.makeText(this, "没有可保存的图像", Toast.LENGTH_SHORT).show();
            return;
        }

        String imagePath = getIntent().getStringExtra("image_path");   // 原文件路径
        String displayName = new File(imagePath).getName();            // 保留原文件名
        String mimeType     = displayName.endsWith(".png") ? "image/png" : "image/jpeg";

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // === 29+ 统一走 MediaStore ===
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, displayName);
                values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
                values.put(MediaStore.Images.Media.IS_PENDING, 1);   // 先标记“写入中”
                values.put(MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_DCIM + "/Edit"); // 存到 DCIM/Edit

                Uri uri = getContentResolver()
                        .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri == null) throw new IOException("insert return null uri");

                try (OutputStream os = getContentResolver().openOutputStream(uri, "w")) {
                    Bitmap.CompressFormat fmt = mimeType.equals("image/png") ?
                            Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;
                    if (!currentBitmap.compress(fmt, 95, os))
                        throw new IOException("compress() return false");
                }

                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);   // 写完，解除占用
                getContentResolver().update(uri, values, null, null);
            } else {
                // === 28- 仍可直接写路径，但要确保权限 ===
                try (FileOutputStream fos = new FileOutputStream(imagePath)) {
                    Bitmap.CompressFormat fmt = mimeType.equals("image/png") ?
                            Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;
                    currentBitmap.compress(fmt, 95, fos);
                    fos.flush();
                }
                // 通知媒体库刷新
                MediaScannerConnection.scanFile(this,
                        new String[]{imagePath}, new String[]{mimeType}, null);
            }

            Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PhotoEditActivity", "save error", e);
        }
    }

}
