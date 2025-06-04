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
        // 首先设置为整个图像区域
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
        
        initialized = true;
        invalidate();
    }

    // 原有setImageDimensions方法保留，但现在优先使用新方法
    public void setImageDimensions(int width, int height) {
        this.imageWidth = width;
        this.imageHeight = height;

        // 计算图片在视图中的实际显示比例和位置
        calculateImageLayout();

        // 初始化裁剪框为图像的100%
        cropRect.set(offsetX, offsetY, 
                    offsetX + imageWidth * viewScale, 
                    offsetY + imageHeight * viewScale);
        
        initialized = true;
        invalidate();
    }

    // 计算图片在视图中的实际布局
    private void calculateImageLayout() {
        // 计算图片缩放以适应视图
        float scaleX = (float) getWidth() / imageWidth;
        float scaleY = (float) getHeight() / imageHeight;
        viewScale = Math.min(scaleX, scaleY);  // 保持宽高比

        // 计算居中显示的偏移量
        offsetX = (getWidth() - imageWidth * viewScale) / 2;
        offsetY = (getHeight() - imageHeight * viewScale) / 2;
        
        // 更新显示区域
        displayOffsetX = offsetX;
        displayOffsetY = offsetY;
        displayWidth = imageWidth * viewScale;
        displayHeight = imageHeight * viewScale;
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
        RectF imageRect = new RectF();
        
        // 转换为原始图像坐标
        imageRect.left = (cropRect.left - displayOffsetX) / viewScale;
        imageRect.top = (cropRect.top - displayOffsetY) / viewScale;
        imageRect.right = (cropRect.right - displayOffsetX) / viewScale;
        imageRect.bottom = (cropRect.bottom - displayOffsetY) / viewScale;
        
        // 确保坐标在图像范围内
        imageRect.left = Math.max(0, Math.min(imageRect.left, imageWidth));
        imageRect.top = Math.max(0, Math.min(imageRect.top, imageHeight));
        imageRect.right = Math.max(0, Math.min(imageRect.right, imageWidth));
        imageRect.bottom = Math.max(0, Math.min(imageRect.bottom, imageHeight));
        
        return imageRect;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // 绘制边界调试框，用于验证坐标系
        Paint debugPaint = new Paint();
        debugPaint.setColor(Color.GREEN);
        debugPaint.setStyle(Paint.Style.STROKE);
        debugPaint.setStrokeWidth(2);
        canvas.drawRect(displayOffsetX, displayOffsetY, 
                        displayOffsetX + displayWidth, displayOffsetY + displayHeight, 
                        debugPaint);

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
                return false;

            case MotionEvent.ACTION_MOVE:
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
                return true;
        }

        return super.onTouchEvent(event);
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
}