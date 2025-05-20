package com.example.pa.data.model;

// UploadResponse.java - 上传图片后的响应
public class UploadResponse {
    private String message;
    private String filename;
    private String path;

    public UploadResponse(String message, String filename, String path) {
        this.message = message;
        this.filename = filename;
        this.path = path;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

