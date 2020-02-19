package com.lightricks.videotricks.model;

public class VideoMetadata {
    private final int width;
    private final int height;
    private final int duration;
    private final int rotation;

    public VideoMetadata(int width, int height, int duration, int rotation) {
        this.width = width;
        this.height = height;
        this.duration = duration;
        this.rotation = rotation;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDuration() {
        return duration;
    }

    public int getRotation() {
        return rotation;
    }
}
