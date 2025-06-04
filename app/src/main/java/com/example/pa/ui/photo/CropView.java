package com.example.pa.ui.photo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class CropView extends View {
    private RectF cropRect = new RectF();
    private Paint borderPaint;
    private Paint dimPaint;
    private Paint handlePaint;

    private float touchStartX, touchStartY;
    private boolean isMoving = false;
    private int touchHandle = -1; // -1表示没有，0-7表示8个控制点

    private final float HANDLE_SIZE = 40f;

    // 图像的实际尺寸
    private int imageWidth, imageHeight;

    // 比例控制
    private float aspectRatio = 0; // 0表示自由比例

    // 图像在视图中的实际显示区域
    private float displayOffsetX = 0;
    private float displayOffsetY = 0;
    private float displayWidth = 0;
    private float displayHeight = 0;

    // 添加视图尺寸和偏移量属性
    private float viewScale = 1.0f;
    private float offsetX = 0, offsetY = 0;
    
    // 标记是否已初始化裁剪区域
    private boolean initialized = false;

    public CropView(Context context) {
        super(context);
        init();
    }

    public CropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        borderPaint = new Paint();
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStrokeWidth(3);
        borderPaint.setStyle(Paint.Style.STROKE);

        handlePaint = new Paint();
        handlePaint.setColor(Color.WHITE);
        handlePaint.setStyle(Paint.Style.FILL);

        dimPaint = new Paint();
        dimPaint.setColor(Color.parseColor("#80000000")); // 半透明黑色
        dimPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * 设置图像显示信息
     * @param origWidth 原始图像宽度
     * @param origHeight 原始图像高度 
     * @param dispOffsetX 图像显示偏移X
     * @param dispOffsetY 图像显示偏移Y
     * @param dispWidth 图像显示宽度
     * @param dispHeight 图像显示高度
     */
    public void setImageDisplay(int origWidth, int origHeight, 
                               float dispOffsetX, float dispOffsetY,
                               float dispWidth, float dispHeight) {
        this.imageWidth = origWidth;
        this.imageHeight = origHeight;
        this.displayOffsetX = dispOffsetX;
        this.displayOffsetY = dispOffsetY;
        this.displayWidth = dispWidth;
        this.displayHeight = dispHeight;
        
        // 计算视图到原图的缩放比例
        this.viewScale = dispWidth / origWidth;
        
        // 使用实际图像显示区域设置裁剪框
        cropRect.set(
            displayOffsetX,
            displayOffsetY,
            displayOffsetX + displayWidth,
            displayOffsetY + displayHeight
        );
        
        // 添加日志输出帮助调试
        Log.d("CropView", String.format("setImageDisplay: offset=(%f,%f), size=(%f,%f), scale=%f", 
                                        dispOffsetX, dispOffsetY, dispWidth, dispHeight, viewScale));
        Log.d("CropView", String.format("cropRect: (%f,%f,%f,%f)", 
                                        cropRect.left, cropRect.top, cropRect.right, cropRect.bottom));
        
        // 确保使用自由比例模式 (0)
        this.aspectRatio = 0;
        
        initialized = true;
        invalidate();
    }

    // 原有setImageDimensions方法保留，但现在优先使用新方法
    public void setImageDimensions(int width, int height) {
        this.imageWidth = width;
        this.imageHeight = height;
        
        // 保存原始图像尺寸
        Log.d("CropView", "setImageDimensions: width=" + width + ", height=" + height);

        // 等待视图测量完成后再初始化裁剪框
        post(() -> {
            // 计算图片在视图中的实际显示比例和位置
            calculateImageLayout();
    
            // 初始化裁剪框，设置为图像实际显示区域的95%，确保覆盖大部分图像区域
            float cropWidth = displayWidth * 0.95f;
            float cropHeight = displayHeight * 0.95f;
            
            // 计算裁剪框的中心点位置，确保在图像中心
            float centerX = displayOffsetX + displayWidth / 2;
            float centerY = displayOffsetY + displayHeight / 2;
            
            // 设置裁剪矩形，确保对齐到显示的图像
            cropRect.set(
                centerX - cropWidth / 2, 
                centerY - cropHeight / 2,
                centerX + cropWidth / 2, 
                centerY + cropHeight / 2
            );
            
            Log.d("CropView", "初始化裁剪框:");
            Log.d("CropView", String.format("  - 显示区: x=%.1f, y=%.1f, w=%.1f, h=%.1f", 
                displayOffsetX, displayOffsetY, displayWidth, displayHeight));
            Log.d("CropView", String.format("  - 中心点: (%.1f, %.1f)", centerX, centerY));
            Log.d("CropView", String.format("  - 裁剪框: (%.1f, %.1f, %.1f, %.1f)", 
                cropRect.left, cropRect.top, cropRect.right, cropRect.bottom));
            
            // 设置自由比例模式
            aspectRatio = 0;
            
            initialized = true;
            invalidate();
        });
    }

    // 计算图片在视图中的实际布局
    private void calculateImageLayout() {
        if (getWidth() <= 0 || getHeight() <= 0 || imageWidth <= 0 || imageHeight <= 0) {
            Log.e("CropView", "calculateImageLayout: 无效尺寸");
            return;
        }
        
        try {
            // 获取视图属性
            int viewWidth = getWidth();
            int viewHeight = getHeight();
            
            // 使用虚拟屏幕尺寸计算比例
            float screenRatio = (float) viewHeight / viewWidth;
            float imageRatio = (float) imageHeight / imageWidth;
            
            // 计算图像大小调整以适应视图
            float scaleX = (float) viewWidth / imageWidth;
            float scaleY = (float) viewHeight / imageHeight;
            viewScale = Math.min(scaleX, scaleY);  // 保持宽高比
            
            // 计算图像实际显示尺寸
            displayWidth = imageWidth * viewScale;
            displayHeight = imageHeight * viewScale;
            
            // 图像会居中显示，计算居中显示的偏移量
            displayOffsetX = (viewWidth - displayWidth) / 2;
            displayOffsetY = (viewHeight - displayHeight) / 2;
            
            // 更新便于引用的变量
            offsetX = displayOffsetX;
            offsetY = displayOffsetY;
            
            Log.d("CropView", "视图布局计算:");
            Log.d("CropView", String.format("  - 视图尺寸: %d x %d", viewWidth, viewHeight));
            Log.d("CropView", String.format("  - 图像原始尺寸: %d x %d", imageWidth, imageHeight));
            Log.d("CropView", String.format("  - 计算比例: %f", viewScale));
            Log.d("CropView", String.format("  - 图像显示尺寸: %.1f x %.1f", displayWidth, displayHeight));
            Log.d("CropView", String.format("  - 图像偏移: (%.1f, %.1f)", displayOffsetX, displayOffsetY));
        } catch (Exception e) {
            Log.e("CropView", "计算布局错误", e);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (!initialized && imageWidth > 0 && imageHeight > 0) {
            // 视图大小变化时重新计算裁剪框
            calculateImageLayout();
            cropRect.set(offsetX, offsetY, 
                        offsetX + imageWidth * viewScale, 
                        offsetY + imageHeight * viewScale);
        }
    }

    public void setAspectRatio(float ratio) {
        this.aspectRatio = ratio;
        if (ratio > 0 && !cropRect.isEmpty()) {
            // 调整当前裁剪框以匹配新比例
            float width = cropRect.width();
            float newHeight = width / ratio;

            // 保持中心点不变
            float centerY = cropRect.centerY();
            cropRect.top = centerY - newHeight/2;
            cropRect.bottom = centerY + newHeight/2;

            // 确保不超出边界
            constrainRect();
            invalidate();
        }
    }

    // 转换视图坐标为图像坐标
    public RectF getCropRect() {
        if (!initialized || viewScale <= 0) {
            Log.e("CropView", "getCropRect: 视图未初始化或缩放比例无效");
            return new RectF(0, 0, imageWidth, imageHeight);
        }
        
        RectF imageRect = new RectF();
        
        // 计算相对于显示区域的偏移
        float relLeft = cropRect.left - displayOffsetX;
        float relTop = cropRect.top - displayOffsetY;
        float relRight = cropRect.right - displayOffsetX;
        float relBottom = cropRect.bottom - displayOffsetY;
        
        // 转换为原始图像坐标
        imageRect.left = relLeft / viewScale;
        imageRect.top = relTop / viewScale;
        imageRect.right = relRight / viewScale;
        imageRect.bottom = relBottom / viewScale;
        
        // 确保坐标在图像范围内
        imageRect.left = Math.max(0, Math.min(imageRect.left, imageWidth));
        imageRect.top = Math.max(0, Math.min(imageRect.top, imageHeight));
        imageRect.right = Math.max(0, Math.min(imageRect.right, imageWidth));
        imageRect.bottom = Math.max(0, Math.min(imageRect.bottom, imageHeight));
        
        // 记录日志以便调试
        Log.d("CropView", "转换裁剪框坐标:");
        Log.d("CropView", String.format("  - 视图裁剪框: (%.1f, %.1f, %.1f, %.1f)", 
                cropRect.left, cropRect.top, cropRect.right, cropRect.bottom));
        Log.d("CropView", String.format("  - 图像偏移: (%.1f, %.1f)", displayOffsetX, displayOffsetY));
        Log.d("CropView", String.format("  - 相对位置: (%.1f, %.1f, %.1f, %.1f)", 
                relLeft, relTop, relRight, relBottom));
        Log.d("CropView", String.format("  - 转换后图像裁剪区: (%.1f, %.1f, %.1f, %.1f)", 
                imageRect.left, imageRect.top, imageRect.right, imageRect.bottom));
        
        return imageRect;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (!initialized) {
            return;
        }
        
        // 输出绘制前的关键信息
        Log.d("CropView", String.format("绘制: 视图尺寸=%dx%d, 裁剪框=(%f,%f,%f,%f)",
            getWidth(), getHeight(), 
            cropRect.left, cropRect.top, cropRect.right, cropRect.bottom));

        // 绘制四周暗色区域
        canvas.drawRect(0, 0, getWidth(), cropRect.top, dimPaint);
        canvas.drawRect(0, cropRect.top, cropRect.left, cropRect.bottom, dimPaint);
        canvas.drawRect(cropRect.right, cropRect.top, getWidth(), cropRect.bottom, dimPaint);
        canvas.drawRect(0, cropRect.bottom, getWidth(), getHeight(), dimPaint);

        // 绘制裁剪框边界
        canvas.drawRect(cropRect, borderPaint);

        // 绘制四角控制点
        drawHandle(canvas, cropRect.left, cropRect.top, 0);
        drawHandle(canvas, cropRect.right, cropRect.top, 1);
        drawHandle(canvas, cropRect.right, cropRect.bottom, 2);
        drawHandle(canvas, cropRect.left, cropRect.bottom, 3);

        // 绘制中间控制点
        drawHandle(canvas, cropRect.centerX(), cropRect.top, 4);
        drawHandle(canvas, cropRect.right, cropRect.centerY(), 5);
        drawHandle(canvas, cropRect.centerX(), cropRect.bottom, 6);
        drawHandle(canvas, cropRect.left, cropRect.centerY(), 7);
    }

    private void drawHandle(Canvas canvas, float x, float y, int handleId) {
        canvas.drawCircle(x, y, HANDLE_SIZE/2, handlePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 确保触摸事件可以被处理，即使在裁剪框外部
        try {
            getParent().requestDisallowInterceptTouchEvent(true);
            
            float x = event.getX();
            float y = event.getY();
    
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchStartX = x;
                    touchStartY = y;
    
                    // 检测是否触摸到控制点
                    touchHandle = getTouchedHandle(x, y);
                    if (touchHandle >= 0) {
                        return true;
                    }
    
                    // 检测是否在裁剪框内部
                    if (cropRect.contains(x, y)) {
                        isMoving = true;
                        return true;
                    }
                    // 即使点击在裁剪框外部，仍然返回true以便我们能处理后续事件
                    return true;
    
                case MotionEvent.ACTION_MOVE:
                    if (!initialized) {
                        return true;
                    }
                    
                    float dx = x - touchStartX;
                    float dy = y - touchStartY;
    
                    if (isMoving) {
                        // 移动整个裁剪框
                        cropRect.offset(dx, dy);
                        constrainRect();
                        invalidate();
                    } else if (touchHandle >= 0) {
                        // 调整裁剪框大小
                        resizeRect(touchHandle, dx, dy);
                        constrainRect();
                        invalidate();
                    }
    
                    touchStartX = x;
                    touchStartY = y;
                    return true;
    
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isMoving = false;
                    touchHandle = -1;
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
            }
            
            return true;
        } catch (Exception e) {
            Log.e("CropView", "触摸事件处理错误", e);
            return false;
        }
    }

    private int getTouchedHandle(float x, float y) {
        // 检测四角控制点
        if (distance(x, y, cropRect.left, cropRect.top) < HANDLE_SIZE) return 0;
        if (distance(x, y, cropRect.right, cropRect.top) < HANDLE_SIZE) return 1;
        if (distance(x, y, cropRect.right, cropRect.bottom) < HANDLE_SIZE) return 2;
        if (distance(x, y, cropRect.left, cropRect.bottom) < HANDLE_SIZE) return 3;

        // 检测中间控制点
        if (distance(x, y, cropRect.centerX(), cropRect.top) < HANDLE_SIZE) return 4;
        if (distance(x, y, cropRect.right, cropRect.centerY()) < HANDLE_SIZE) return 5;
        if (distance(x, y, cropRect.centerX(), cropRect.bottom) < HANDLE_SIZE) return 6;
        if (distance(x, y, cropRect.left, cropRect.centerY()) < HANDLE_SIZE) return 7;

        return -1;
    }

    private float distance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private void resizeRect(int handle, float dx, float dy) {
        float left = cropRect.left;
        float top = cropRect.top;
        float right = cropRect.right;
        float bottom = cropRect.bottom;

        switch (handle) {
            case 0: // 左上角
                left += dx;
                top += dy;
                if (aspectRatio > 0) {
                    // 固定右下角
                    float width = right - left;
                    float height = width / aspectRatio;
                    top = bottom - height;
                }
                break;
            case 1: // 右上角
                right += dx;
                top += dy;
                if (aspectRatio > 0) {
                    // 固定左下角
                    float width = right - left;
                    float height = width / aspectRatio;
                    top = bottom - height;
                }
                break;
            case 2: // 右下角
                right += dx;
                bottom += dy;
                if (aspectRatio > 0) {
                    // 固定左上角
                    float width = right - left;
                    float height = width / aspectRatio;
                    bottom = top + height;
                }
                break;
            case 3: // 左下角
                left += dx;
                bottom += dy;
                if (aspectRatio > 0) {
                    // 固定右上角
                    float width = right - left;
                    float height = width / aspectRatio;
                    bottom = top + height;
                }
                break;
            case 4: // 上中
                top += dy;
                if (aspectRatio > 0) {
                    float width = right - left;
                    float height = width / aspectRatio;
                    top = bottom - height;
                }
                break;
            case 5: // 右中
                right += dx;
                if (aspectRatio > 0) {
                    float height = bottom - top;
                    float width = height * aspectRatio;
                    right = left + width;
                }
                break;
            case 6: // 下中
                bottom += dy;
                if (aspectRatio > 0) {
                    float width = right - left;
                    float height = width / aspectRatio;
                    bottom = top + height;
                }
                break;
            case 7: // 左中
                left += dx;
                if (aspectRatio > 0) {
                    float height = bottom - top;
                    float width = height * aspectRatio;
                    left = right - width;
                }
                break;
        }

        // 设置新的裁剪矩形
        cropRect.set(left, top, right, bottom);
    }

    private void constrainRect() {
        // 防止裁剪框超出图像边界(使用视图坐标系)
        float minLeft = displayOffsetX;
        float minTop = displayOffsetY;
        float maxRight = displayOffsetX + displayWidth;
        float maxBottom = displayOffsetY + displayHeight;
        
        // 限制左边界
        if (cropRect.left < minLeft) {
            cropRect.left = minLeft;
        }
        
        // 限制上边界
        if (cropRect.top < minTop) {
            cropRect.top = minTop;
        }
        
        // 限制右边界
        if (cropRect.right > maxRight) {
            cropRect.right = maxRight;
        }
        
        // 限制下边界
        if (cropRect.bottom > maxBottom) {
            cropRect.bottom = maxBottom;
        }

        // 最小尺寸限制(视图坐标系)
        float minSize = 50 * viewScale;
        
        // 宽度最小值限制
        if (cropRect.width() < minSize) {
            // 根据当前边界情况决定调整哪一边
            if (cropRect.right >= maxRight) {
                cropRect.left = cropRect.right - minSize;
            } else {
                cropRect.right = cropRect.left + minSize;
            }
        }

        // 高度最小值限制
        if (cropRect.height() < minSize) {
            if (cropRect.bottom >= maxBottom) {
                cropRect.top = cropRect.bottom - minSize;
            } else {
                cropRect.bottom = cropRect.top + minSize;
            }
        }
    }

    /**
     * 手动设置图像显示区域
     * 此方法允许外部直接指定图像的显示位置和大小
     * @param x 图像显示左上角的X坐标
     * @param y 图像显示左上角的Y坐标
     * @param width 图像显示宽度
     * @param height 图像显示高度
     */
    public void setImageDisplayManual(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            Log.e("CropView", "setImageDisplayManual: 无效的尺寸参数");
            return;
        }
        
        Log.d("CropView", String.format("手动设置图像区域: (%d,%d) %dx%d", x, y, width, height));
        
        // 直接设置显示区域
        this.displayOffsetX = x;
        this.displayOffsetY = y;
        this.displayWidth = width;
        this.displayHeight = height;
        
        // 更新引用变量
        this.offsetX = x;
        this.offsetY = y;
        
        // 计算显示比例
        this.viewScale = (float)width / imageWidth;
        
        // 重新初始化裁剪框
        float cropWidth = width * 0.95f;
        float cropHeight = height * 0.95f;
        float centerX = x + width / 2f;
        float centerY = y + height / 2f;
        
        cropRect.set(
            centerX - cropWidth / 2f,
            centerY - cropHeight / 2f,
            centerX + cropWidth / 2f,
            centerY + cropHeight / 2f
        );
        
        Log.d("CropView", "手动调整后的裁剪框:");
        Log.d("CropView", String.format("  - 中心点: (%.1f, %.1f)", centerX, centerY));
        Log.d("CropView", String.format("  - 裁剪框: (%.1f, %.1f, %.1f, %.1f)",
                cropRect.left, cropRect.top, cropRect.right, cropRect.bottom));
        
        initialized = true;
        invalidate();
    }
}