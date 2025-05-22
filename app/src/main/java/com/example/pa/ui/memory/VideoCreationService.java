package com.example.pa.ui.memory;


import androidx.annotation.NonNull;

public interface VideoCreationService {
    interface VideoCreationCallback {
        void onSuccess(@NonNull String outputPath);

        void onFailure(@NonNull String errorMessage);

        void onProgress(float progress); // 0.0f to 1.0f (可选)
    }

    void createVideo(@NonNull VideoCreationOptions options, @NonNull VideoCreationCallback callback);

    void cancelCurrentTask(); // 可选的取消功能
}
