package com.lightricks.videotricks.model;

public class VideoInfo {
    private final String path;
    private final int width;
    private final int height;
    private final int durationMs;

    public VideoInfo(String path, int width, int height, int durationMs) {
        this.path = path;
        this.width = width;
        this.height = height;
        this.durationMs = durationMs;
    }

    public String getPath() {
        return path;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDurationMs() {
        return durationMs;
    }
}
