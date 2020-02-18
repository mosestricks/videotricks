package com.lightricks.videotricks.media;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.lightricks.videotricks.util.DataSource;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class VideoReader {
    private final MediaExtractor mediaExtractor;
    private final HandlerThread handlerThread;
    private final Handler handler;
    private final MediaFormat inputFormat;
    private final MediaCodec codec;
    private final MediaCodecListener mediaCodecListener;
    private final DataSource dataSource;

    public VideoReader(DataSource dataSource, CodecProvider codecProvider, int trackId,
                       Surface outputSurface) {
        this.dataSource = dataSource;

        handlerThread = new HandlerThread("VideoReader");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        mediaExtractor = new MediaExtractor();
        mediaCodecListener = new MediaCodecListener();

        try {
            mediaExtractor.setDataSource(dataSource.getFileDescriptor(),
                    dataSource.getOffset(), dataSource.getLength());
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        inputFormat = mediaExtractor.getTrackFormat(trackId);
        final String mimeType = inputFormat.getString(MediaFormat.KEY_MIME);
        if (mimeType == null) {
            throw new RuntimeException("MIME type of the video asset could not be determined");
        }

        mediaExtractor.selectTrack(trackId);

        codec = codecProvider.getConfiguredDecoder(inputFormat, outputSurface, mediaCodecListener,
                handler).orElseThrow(() -> new RuntimeException("Could not create codec"));

        handler.post(codec::start);
    }

    public CompletableFuture dispose() {
        return CompletableFuture.runAsync(this::releaseResources, handler::post)
                .thenRun(handlerThread::quit);
    }

    /** Private methods */

    private void handleInputBuffer(int bufferIndex) {
        // todo
    }

    private void handleOutputBuffer(int bufferIndex, MediaCodec.BufferInfo bufferInfo) {
        // todo
    }

    private void handleCodecError(MediaCodec.CodecException exception) {
        // todo
    }

    private void releaseResources() {
        mediaExtractor.release();
        codec.release();
        dataSource.release();
    }

    private class MediaCodecListener extends MediaCodec.Callback {
        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int bufferIndex) {
            handleInputBuffer(bufferIndex);
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int bufferIndex,
                                            @NonNull MediaCodec.BufferInfo bufferInfo) {
            handleOutputBuffer(bufferIndex, bufferInfo);
        }

        @Override
        public void onError(@NonNull MediaCodec codec,
                            @NonNull MediaCodec.CodecException exception) {
            handleCodecError(exception);
        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec codec,
                                          @NonNull MediaFormat format) {
            // no action
        }
    }

}
