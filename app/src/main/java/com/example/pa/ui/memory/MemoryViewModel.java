package com.example.pa.ui.memory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MemoryViewModel extends ViewModel {
    private final MutableLiveData<MemoryData> memoryData = new MutableLiveData<>();

    public MemoryViewModel() {
        // Initialize with sample data
        MemoryData data = new MemoryData(
                "Memory Tracker",
                "Track and optimize your device memory usage",
                75 // percentage used
        );
        memoryData.setValue(data);
    }

    public LiveData<MemoryData> getMemoryData() {
        return memoryData;
    }

    // Data holder class
    public static class MemoryData {
        public final String title;
        public final String description;
        public final int usagePercentage;

        public MemoryData(String title, String description, int usagePercentage) {
            this.title = title;
            this.description = description;
            this.usagePercentage = usagePercentage;
        }
    }
}