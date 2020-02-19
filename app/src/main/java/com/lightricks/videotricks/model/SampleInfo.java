package com.lightricks.videotricks.model;

public class SampleInfo implements Comparable<SampleInfo> {
    private final long presentationTimeUs;
    private final long size;
    private final int flags;

    public SampleInfo(long presentationTimeUs, long size, int flags) {
        this.presentationTimeUs = presentationTimeUs;
        this.size = size;
        this.flags = flags;
    }

    public long getPresentationTimeUs() {
        return presentationTimeUs;
    }

    public long getSize() {
        return size;
    }

    public int getFlags() {
        return flags;
    }

    @Override
    public int compareTo(SampleInfo other) {
        return Long.compare(this.presentationTimeUs, other.presentationTimeUs);
    }
}
