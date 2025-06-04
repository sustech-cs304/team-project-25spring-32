package com.example.pa.ui.photo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.RectF;

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
import android.widget.FrameLayout;
import android.app.ProgressDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pa.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public class PhotoEditActivity extends AppCompatActivity {
    private ImageView editImageView;
    private LinearLayout adjustmentLayout;
    private TextView adjustmentLabel;
    private SeekBar adjustmentSeekBar;
    private Button btnRotate, btnBrightness, btnContrast, btnCancel, btnSave;
    private Button btnUndo, btnRedo;

    //裁剪相关
    private Button btnCrop;
    private FrameLayout cropOverlay;
    private CropView cropView;
    private LinearLayout cropControlsLayout;
    private Button btnCropFree, btnCrop11, btnCrop43, btnCrop169;
    private Button btnCropCancel, btnCropApply;

    private Bitmap currentBitmap;//current photo after edit
    private Bitmap originalBitmap;//origin photo
    private float brightness = 0f;
    private float contrast = 1f;
    private float currentRotation = 0f;

    // 用于存储编辑历史的数据结构
    private List<EditState> editHistory = new ArrayList<>();
    private int currentIndex = -1;

    // 内部类用于存储每个编辑状态
    private static class EditState {
        Bitmap bitmap;
        float brightness;
        float contrast;
        float rotation;

        EditState(Bitmap bitmap, float brightness, float contrast, float rotation) {
            this.bitmap = bitmap.copy(bitmap.getConfig(), true);
            this.brightness = brightness;
            this.contrast = contrast;
            this.rotation = rotation;
        }
    }

    private Button btnRemoveBg;
    private static final String REMOVE_BG_API_KEY = "qoEzXEJ75R9se3ReMsenyWWo"; // 请替换为您的API密钥
    private static final String REMOVE_BG_API_URL = "https://api.remove.bg/v1.0/";

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
        
        // 初始设置ImageView预留空间
        adjustImageViewForRotation();
    }

    private void initViews() {
        editImageView = findViewById(R.id.editImageView);
        btnRotate = findViewById(R.id.btnRotate);
        btnBrightness = findViewById(R.id.btnBrightness);
        btnContrast = findViewById(R.id.btnContrast);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);

        btnUndo = findViewById(R.id.btnUndo);
        btnRedo = findViewById(R.id.btnRedo);

        adjustmentLayout = findViewById(R.id.adjustmentLayout);
        adjustmentLabel = findViewById(R.id.adjustmentLabel);
        adjustmentSeekBar = findViewById(R.id.adjustmentSeekBar);

        btnCrop = findViewById(R.id.btnCrop); // 新增的裁剪按钮，需要在布局中添加
        cropOverlay = findViewById(R.id.cropOverlay);
        cropView = findViewById(R.id.cropView);
        cropControlsLayout = findViewById(R.id.cropControlsLayout);

        btnCropFree = findViewById(R.id.btnCropFree);
        btnCrop11 = findViewById(R.id.btnCrop11);
        btnCrop43 = findViewById(R.id.btnCrop43);
        btnCrop169 = findViewById(R.id.btnCrop169);

        btnCropCancel = findViewById(R.id.btnCropCancel);
        btnCropApply = findViewById(R.id.btnCropApply);

        btnRemoveBg = findViewById(R.id.btnRemoveBg);
    }

    private void loadImage() {
        // 从 Intent 获取 Uri 对象（键名统一为 "Uri"）
        Uri imageUri = getIntent().getParcelableExtra("Uri");

        if (imageUri == null) {
            Toast.makeText(this, "图片地址无效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            // 通过 ContentResolver 加载图片
            ContentResolver resolver = getContentResolver();
            InputStream is = resolver.openInputStream(imageUri);

            // 保持原有解码逻辑
            originalBitmap = BitmapFactory.decodeStream(is);
            if (is != null) is.close(); // 确保关闭流

            if (originalBitmap == null) {
                Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            currentBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);  // 使用copy而不是直接赋值
            
            // 确保图像适应视图区域
            adjustImageViewForRotation();

            // 初始化编辑历史，保存初始状态
            addNewState();
            updateUndoRedoButtons();

        } catch (Exception e) {
            Toast.makeText(this, "未知错误: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    //   /storage/emulated/0/DCIM/
    private void setupListeners() {
        btnRotate.setOnClickListener(v -> {
            rotateImage();
            addNewState(); // 添加新状态到历史记录
        });

        btnBrightness.setOnClickListener(v -> setupAdjustment("Brightness"));
        btnContrast.setOnClickListener(v -> setupAdjustment("Contrast"));

        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveImage());

        // 撤销按钮监听器
        btnUndo.setOnClickListener(v -> undo());

        // 重做按钮监听器
        btnRedo.setOnClickListener(v -> redo());

        // 裁剪按钮监听器
        btnCrop.setOnClickListener(v -> showCropMode());

        // 裁剪比例按钮 监听器
        btnCropFree.setOnClickListener(v -> cropView.setAspectRatio(0)); // 自由比例
        btnCrop11.setOnClickListener(v -> cropView.setAspectRatio(1.0f)); // 1:1
        btnCrop43.setOnClickListener(v -> cropView.setAspectRatio(4.0f/3.0f)); // 4:3
        btnCrop169.setOnClickListener(v -> cropView.setAspectRatio(16.0f/9.0f)); // 16:9

        // 裁剪取消和应用按钮
        btnCropCancel.setOnClickListener(v -> hideCropMode());
        btnCropApply.setOnClickListener(v -> {
            applyCrop();
            hideCropMode();
            addNewState(); // 将裁剪操作添加到历史记录
        });

        adjustmentSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (adjustmentLabel.getText().toString().contains("Brightness")) {
                    brightness = (progress - 100) / 100f * 255;
                } else if (adjustmentLabel.getText().toString().contains("Contrast")) {
                    contrast = progress / 100f;
                }
                applyImageAdjustments();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                addNewState();
//              adjustmentLayout.setVisibility(View.GONE); // 隐藏调整布局


            }
        });

        btnRemoveBg.setOnClickListener(v -> removeBackground());
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

        if (currentBitmap != originalBitmap && currentBitmap != null) {
            currentBitmap.recycle();
        }
        currentBitmap = out;
        
        // 确保图像适应视图区域，不遮挡顶部和底部的控件
        adjustImageViewForRotation();
    }
    
    /**
     * 根据图像旋转调整ImageView的显示方式，确保不会遮挡顶部和底部控件
     */
    private void adjustImageViewForRotation() {
        if (currentBitmap == null || editImageView == null) return;
        
        // 设置适当的ScaleType以确保图像完全可见
        editImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        
        // 计算基础的顶部和底部控制区域预留空间
        int topReservedSpace = 180; // 基础顶部控件的预留空间(像素)
        int bottomReservedSpace = 200; // 基础底部控件的高度(像素)
        
        // 检查是否为横向图片被旋转为纵向 (可能需要更多顶部空间)
        boolean isLandscapeRotatedToPortrait = 
            (currentRotation == 90 || currentRotation == 270) && 
            currentBitmap.getWidth() > currentBitmap.getHeight() * 1.5;
            
        // 对于宽图旋转后变高的情况，增加顶部预留空间
        if (isLandscapeRotatedToPortrait) {
            topReservedSpace = Math.max(topReservedSpace, 220);  // 增加更多顶部空间
        }
        
        // 根据图片比例动态调整padding
        float imageRatio = (float) currentBitmap.getWidth() / currentBitmap.getHeight();
        if (currentRotation == 90 || currentRotation == 270) {
            // 旋转后宽高对调
            imageRatio = 1.0f / imageRatio;
        }
        
        // 对于特别宽的图片，可能需要更多的顶部空间
        if (imageRatio > 2.0f) {
            topReservedSpace = Math.max(topReservedSpace, 250);
        }
        
        // 设置ImageView的padding以保留空间
        editImageView.setPadding(0, topReservedSpace, 0, bottomReservedSpace);
        
        // 显示图像
        editImageView.setImageBitmap(currentBitmap);
    }

    private void rotateImage() {
        currentRotation = (currentRotation + 90) % 360;
        render();
        
        // 将旋转后的状态添加到历史记录
        addNewState();
    }

    private void applyImageAdjustments() {
        render();
    }

    private void setupAdjustment(String type) {
        adjustmentLabel.setText(type + " Adjustment");
        adjustmentLayout.setVisibility(View.VISIBLE);
        // 不重置进度条，保持当前值
        if (type.equals("Brightness")) {
            adjustmentSeekBar.setProgress((int) (brightness / 255f * 100f + 100));
        } else if (type.equals("Contrast")) {
            adjustmentSeekBar.setProgress((int) (contrast * 100f));
        }
    }

    /**
     * 撤销与重做，状态管理
     */
    //将当前状态添加到历史记录中
    private void addNewState() {
        // 移除当前索引之后的所有历史记录
        while (editHistory.size() > currentIndex + 1) {
            editHistory.remove(editHistory.size() - 1);
        }

        // 添加新状态
        EditState newState = new EditState(currentBitmap, brightness, contrast, currentRotation);
        editHistory.add(newState);
        currentIndex = editHistory.size() - 1;

        // 更新撤销/重做按钮状态
        updateUndoRedoButtons();
    }

    /**
     * 撤销操作
     */
    private void undo() {
        if (currentIndex > 0) {
            currentIndex--;
            restoreState(editHistory.get(currentIndex));
            updateUndoRedoButtons();
        }
    }

    /**
     * 重做操作
     */
    private void redo() {
        if (currentIndex < editHistory.size() - 1) {
            currentIndex++;
            restoreState(editHistory.get(currentIndex));
            updateUndoRedoButtons();
        }
    }

    /**
     * 恢复到指定的编辑状态
     */
    private void restoreState(EditState state) {
        brightness = state.brightness;
        contrast = state.contrast;
        currentRotation = state.rotation;

        // 直接使用保存的位图，不需要重新渲染
        currentBitmap = state.bitmap.copy(state.bitmap.getConfig(), true);
        editImageView.setImageBitmap(currentBitmap);
    }

    /**
     * 更新撤销/重做按钮的可用状态，调整透明度
     */
    private void updateUndoRedoButtons() {
        btnUndo.setEnabled(currentIndex > 0);
        btnUndo.setAlpha(currentIndex > 0 ? 1.0f : 0.3f);

        btnRedo.setEnabled(currentIndex < editHistory.size() - 1);
        btnRedo.setAlpha(currentIndex < editHistory.size() - 1 ? 1.0f : 0.3f);
    }

    /**
     * 计算ImageView中图像的实际显示区域
     * @param imageView 图像视图
     * @return float[4] 数组，包含 {offsetX, offsetY, displayWidth, displayHeight}
     */
    private float[] getImageDisplayInfo(ImageView imageView) {
        if (imageView == null || imageView.getDrawable() == null) {
            return new float[] {0, 0, 0, 0};
        }
        
        // 获取图像实际尺寸
        int imageWidth = currentBitmap.getWidth();
        int imageHeight = currentBitmap.getHeight();
        
        // 获取ImageView尺寸（需要考虑内边距）
        int paddingLeft = imageView.getPaddingLeft();
        int paddingTop = imageView.getPaddingTop();
        int paddingRight = imageView.getPaddingRight();
        int paddingBottom = imageView.getPaddingBottom();
        
        // 计算内容区域的尺寸（减去padding）
        int contentWidth = imageView.getWidth() - paddingLeft - paddingRight;
        int contentHeight = imageView.getHeight() - paddingTop - paddingBottom;
        
        // 计算图像在视图中的缩放比例
        float scale;
        float scaleX = (float) contentWidth / imageWidth;
        float scaleY = (float) contentHeight / imageHeight;
        
        // 使用FIT_CENTER类似的计算
        scale = Math.min(scaleX, scaleY);
        
        // 计算缩放后图像大小
        float displayWidth = imageWidth * scale;
        float displayHeight = imageHeight * scale;
        
        // 计算居中偏移，需要考虑ImageView的padding和内容居中
        float offsetX = paddingLeft + (contentWidth - displayWidth) / 2f;
        float offsetY = paddingTop + (contentHeight - displayHeight) / 2f;
        
        // 添加日志输出用于调试
        Log.d("PhotoEditActivity", String.format(
            "ImageInfo: content=(%d,%d), image=(%d,%d), scaled=(%f,%f), offset=(%f,%f)", 
            contentWidth, contentHeight, imageWidth, imageHeight, 
            displayWidth, displayHeight, offsetX, offsetY));
        
        return new float[] {offsetX, offsetY, displayWidth, displayHeight};
    }

    /**
     * 显示裁剪界面
     */
    // 显示裁剪模式
    private void showCropMode() {
        if (currentBitmap == null) {
            Toast.makeText(this, "当前图像不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        if (cropView == null) {
            Toast.makeText(this, "裁剪控件未初始化", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 隐藏其他调整界面
        adjustmentLayout.setVisibility(View.GONE);
        
        // 确保cropOverlay与ImageView有相同的padding
        cropOverlay.setPadding(
            editImageView.getPaddingLeft(),
            editImageView.getPaddingTop(),
            editImageView.getPaddingRight(),
            editImageView.getPaddingBottom()
        );

        // 获取ImageView中图像的实际显示区域
        float[] imageDisplayInfo = getImageDisplayInfo(editImageView);
        
        // 设置裁剪视图尺寸和位置
        cropView.setImageDisplay(currentBitmap.getWidth(), currentBitmap.getHeight(), 
                                 imageDisplayInfo[0], imageDisplayInfo[1], 
                                 imageDisplayInfo[2], imageDisplayInfo[3]);

        // 显示裁剪界面
        cropOverlay.setVisibility(View.VISIBLE);
        cropControlsLayout.setVisibility(View.VISIBLE);
    }

    // 隐藏裁剪模式
    private void hideCropMode() {
        cropOverlay.setVisibility(View.GONE);
        cropControlsLayout.setVisibility(View.GONE);
    }

    // 应用裁剪
    private void applyCrop() {
        if (currentBitmap == null) return;

        // 获取裁剪区域
        RectF cropRect = cropView.getCropRect();

        // 创建裁剪后的位图
        int x = (int) cropRect.left;
        int y = (int) cropRect.top;
        int width = (int) cropRect.width();
        int height = (int) cropRect.height();

        // 检查裁剪区域是否有效
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + width > currentBitmap.getWidth()) width = currentBitmap.getWidth() - x;
        if (y + height > currentBitmap.getHeight()) height = currentBitmap.getHeight() - y;

        if (width <= 0 || height <= 0) {
            Toast.makeText(this, "无效的裁剪区域", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // 应用裁剪
            Bitmap croppedBitmap = Bitmap.createBitmap(currentBitmap, x, y, width, height);

            // 更新当前位图和视图
            if (currentBitmap != originalBitmap) {
                currentBitmap.recycle(); // 回收旧位图
            }
            currentBitmap = croppedBitmap;
            
            // 确保图像适应视图区域
            adjustImageViewForRotation();

            // 注意：裁剪会改变图像尺寸，需要更新原始位图以使其他效果基于新尺寸
            originalBitmap = currentBitmap.copy(currentBitmap.getConfig(), true);

            // 重置亮度、对比度等设置，因为这些效果将应用于裁剪后的图像
            brightness = 0f;
            contrast = 1f;
            currentRotation = 0f;

        } catch (Exception e) {
            Toast.makeText(this, "裁剪失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 保存当前图像到本地
     */
    private void saveImage() {
        if (currentBitmap == null) {
            Toast.makeText(this, "没有可保存的图像", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // 获取原始图片Uri（使用Parcelable获取）
            Uri originalUri = getIntent().getParcelableExtra("Uri");
            if (originalUri == null) throw new IOException("原始图片Uri为空");

            // 获取原始文件信息
            String mimeType = getContentResolver().getType(originalUri);
            String fileExtension = getFileExtension(mimeType); // 根据MIME类型获取扩展名
            String originalName = getOriginalFileName(originalUri); // 获取原始文件名（不带扩展名）

            // 生成新文件名（原始文件名_edited）
            String displayName = originalName + "_edited" + fileExtension;
            String relativePath = Environment.DIRECTORY_DCIM + "/Edits";

            // 创建媒体库记录
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, displayName);
            values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
            values.put(MediaStore.Images.Media.RELATIVE_PATH, relativePath);

            // Android 10+ 需要临时文件标记
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
            }

            // 插入新记录并获取Uri
            Uri newUri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
            );
            if (newUri == null) throw new IOException("创建媒体库记录失败");

            // 写入图片数据
            try (OutputStream os = getContentResolver().openOutputStream(newUri)) {
                Bitmap.CompressFormat format = getCompressFormat(mimeType);
                if (!currentBitmap.compress(format, 95, os)) {
                    throw new IOException("图片压缩失败");
                }
            }

            // 更新媒体库状态（Android 10+）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                getContentResolver().update(newUri, values, null, null);
            } else {
                // 触发媒体库扫描（Android 9及以下）
                MediaScannerConnection.scanFile(
                        this,
                        new String[]{getPathFromUri(newUri)},
                        new String[]{mimeType},
                        null
                );
            }

            Toast.makeText(this, "已保存至相册/Edits目录", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "保存失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PhotoEdit", "保存错误", e);
        }
    }

    private void removeBackground() {
        if (currentBitmap == null) {
            Toast.makeText(this, "没有可处理的图片", Toast.LENGTH_SHORT).show();
            return;
        }

        // 显示加载对话框
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在移除背景...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // 将Bitmap转换为字节数组
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        currentBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageBytes = stream.toByteArray();

        // 创建Retrofit实例
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(REMOVE_BG_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RemoveBgService service = retrofit.create(RemoveBgService.class);

        // 创建请求体
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image_file", "image.jpg", requestFile);

        // 发送请求
        Call<ResponseBody> call = service.removeBackground(REMOVE_BG_API_KEY, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // 将响应转换为Bitmap
                        byte[] responseBytes = response.body().bytes();
                        Bitmap resultBitmap = BitmapFactory.decodeByteArray(responseBytes, 0, responseBytes.length);
                        
                        // 更新UI
                        currentBitmap = resultBitmap;
                        editImageView.setImageBitmap(currentBitmap);
                        
                        // 添加到历史记录
                        addNewState();
                        
                        Toast.makeText(PhotoEditActivity.this, "背景移除成功", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(PhotoEditActivity.this, "处理图片失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PhotoEditActivity.this, "移除背景失败: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(PhotoEditActivity.this, "网络请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理位图资源
        clearBitmaps();
    }
    /**
     * 释放所有位图资源
     */
    private void clearBitmaps() {
        for (EditState state : editHistory) {
            if (state.bitmap != null && !state.bitmap.isRecycled()) {
                state.bitmap.recycle();
            }
        }
        editHistory.clear();

        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            originalBitmap.recycle();
        }

        if (currentBitmap != null && !currentBitmap.isRecycled()) {
            currentBitmap.recycle();
        }
    }

    // 辅助方法：根据MIME类型获取文件扩展名
    private String getFileExtension(String mimeType) {
        switch (mimeType) {
            case "image/png":
                return ".png";
            case "image/jpeg":
                return ".jpg";
            default:
                return ".jpg"; // 默认保存为jpg
        }
    }

    // 辅助方法：获取原始文件名（不带扩展名）
    private String getOriginalFileName(Uri uri) {
        String fileName = "";
        Cursor cursor = getContentResolver().query(
                uri,
                new String[]{MediaStore.Images.Media.DISPLAY_NAME},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            fileName = cursor.getString(0);
            // 去除扩展名
            int dotIndex = fileName.lastIndexOf(".");
            if (dotIndex > 0) {
                fileName = fileName.substring(0, dotIndex);
            }
            cursor.close();
        }
        return fileName.isEmpty() ? "image" : fileName;
    }

    // 辅助方法：获取压缩格式
    private Bitmap.CompressFormat getCompressFormat(String mimeType) {
        return mimeType.equals("image/png") ?
                Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;
    }

    // 辅助方法：从Uri获取实际路径（仅用于Android 9及以下）
    private String getPathFromUri(Uri uri) {
        if (uri == null) return null;
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            return uri.getPath();
        }

        String[] projection = {MediaStore.Images.Media.DATA};
        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(columnIndex);
            }
        }
        return null;
    }
}

// Remove.bg API接口定义
interface RemoveBgService {
    @Multipart
    @POST("removebg")
    Call<ResponseBody> removeBackground(
        @Header("X-Api-Key") String apiKey,
        @Part MultipartBody.Part image
    );
}
