package com.lightricks.videotricks.media;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Size;
import android.view.Surface;

public class VideoWriter {
    private Surface inputSurface;
    private MediaCodec codec;

    public VideoWriter(Size videoSize, String mime, CodecProvider codecProvider) {
        MediaFormat format = MediaFormat.createVideoFormat(mime,
                videoSize.getWidth(), videoSize.getHeight());

        codec = codecProvider.getConfiguredEncoder(format, null, null)
                .orElseThrow(() -> new RuntimeException("Could not create codec"));

        inputSurface = codec.createInputSurface();
    }

    public Surface getSurface() {
        return inputSurface;
    }

    public void dispose() {
        codec.release();
        inputSurface.release();
    }
}
