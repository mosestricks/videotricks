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
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class VideoReader {
    private final MediaExtractor mediaExtractor;
    private final HandlerThread handlerThread;
    private final Handler handler;
    private final MediaCodec codec;
    private final DataSource dataSource;
    private CompletableFuture<Void> job;

    public VideoReader(DataSource dataSource, CodecProvider codecProvider, int trackId,
                       Surface outputSurface) {
        this.dataSource = dataSource;

        handlerThread = new HandlerThread("VideoReader");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        mediaExtractor = new MediaExtractor();

        try {
            mediaExtractor.setDataSource(dataSource.getFileDescriptor(),
                    dataSource.getOffset(), dataSource.getLength());
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        MediaFormat inputFormat = mediaExtractor.getTrackFormat(trackId);
        final String mimeType = inputFormat.getString(MediaFormat.KEY_MIME);
        if (mimeType == null) {
            throw new RuntimeException("MIME type of the video asset could not be determined");
        }

        mediaExtractor.selectTrack(trackId);

        MediaCodecListener listener = new MediaCodecListener();
        codec = codecProvider.getConfiguredDecoder(inputFormat, outputSurface, listener, handler)
                .orElseThrow(() -> new RuntimeException("Could not create codec"));
    }

    public CompletableFuture<Void> start() {
        handler.post(codec::start);
        job = new CompletableFuture<>();
        return job;
    }

    public CompletableFuture<Void> dispose() {
        return CompletableFuture.runAsync(this::releaseResources, handler::post)
                .thenRun(handlerThread::quit);
    }

    /** Private methods */

    private void handleInputBuffer(int bufferIndex) {
        ByteBuffer buffer = codec.getInputBuffer(bufferIndex);
        if (buffer == null) {
            return;
        }

        int sampleSize = mediaExtractor.readSampleData(buffer, 0);
        if (sampleSize < 0) {
            codec.queueInputBuffer(bufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            return;
        }

        long ptsUs = mediaExtractor.getSampleTime();
        int sampleFlags = mediaExtractor.getSampleFlags();

        try {
            codec.queueInputBuffer(bufferIndex, 0, sampleSize, ptsUs, sampleFlags);
        } catch (MediaCodec.CodecException e) {
            throw new RuntimeException(e);
        }

        mediaExtractor.advance();
    }

    private void handleOutputBuffer(int bufferIndex, MediaCodec.BufferInfo bufferInfo) {
        boolean isEOS = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
        boolean isEmpty = bufferInfo.size == 0;

        //todo
        //codec.releaseOutputBuffer(bufferIndex, !isEmpty);
        codec.releaseOutputBuffer(bufferIndex, false);

        if (isEOS) {
            job.complete(null);
        }
    }

    private void handleCodecError(MediaCodec.CodecException e) {
        throw new RuntimeException(e);
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
