package com.lightricks.videotricks.media;

import android.graphics.ImageFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Size;
import android.view.Surface;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class VideoPlayer implements Allocation.OnBufferAvailableListener, PlaybackControl {
    private final HandlerThread handlerThread;
    private final Handler handler;
    private final Allocation input, output;
    private final ScriptIntrinsicYuvToRGB yuvToRGB;
    private long lastPresentationTimeUs = -1, lastSystemTimeUs = -1;

    public VideoPlayer(Size videoSize, RenderScript renderScript) {
        handlerThread = new HandlerThread("VideoPlayer");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        Type yuvType = new Type.Builder(renderScript, Element.YUV(renderScript))
                .setX(videoSize.getWidth())
                .setY(videoSize.getHeight())
                .setYuvFormat(ImageFormat.YUV_420_888)
                .create();

        input = Allocation.createTyped(renderScript, yuvType,
                Allocation.USAGE_IO_INPUT | Allocation.USAGE_SCRIPT);

        input.setOnBufferAvailableListener(this);

        Type rgbaType = new Type.Builder(renderScript, Element.RGBA_8888(renderScript))
                .setX(videoSize.getWidth())
                .setY(videoSize.getHeight())
                .create();

        output = Allocation.createTyped(renderScript, rgbaType,
                Allocation.USAGE_IO_OUTPUT | Allocation.USAGE_SCRIPT);

        yuvToRGB = ScriptIntrinsicYuvToRGB.create(renderScript, Element.U8_4(renderScript));
        yuvToRGB.setInput(input);
    }

    public void surfaceCreated(Surface surface) {
        output.setSurface(surface);
    }

    public Surface getInputSurface() {
        return input.getSurface();
    }

    public void dispose() {
        input.destroy();
        output.destroy();
        yuvToRGB.destroy();
        handlerThread.quit();
    }

    /** OnBufferAvailableListener impl. */

    @Override
    public void onBufferAvailable(Allocation a) {
        input.ioReceive();
        yuvToRGB.forEach(output);
        output.ioSend();
    }

    /** PlaybackControl impl. */

    @Override
    public CompletableFuture<Void> completeAt(long presentationTimeUs) {
        long now = TimeUnit.MILLISECONDS.toMicros(SystemClock.elapsedRealtime());

        if (lastPresentationTimeUs == -1) {
            // This is the first frame, release it immediately.
            lastPresentationTimeUs = presentationTimeUs;
            lastSystemTimeUs = now;
            return CompletableFuture.completedFuture(null);
        }

        long elapsedUs = now - lastSystemTimeUs;
        long presentationTimeDeltaUs = presentationTimeUs - lastPresentationTimeUs;
        lastPresentationTimeUs = presentationTimeUs;

        if (elapsedUs >= presentationTimeDeltaUs) {
            // The time has elapsed enough, release the frame.
            lastSystemTimeUs = now;
            return CompletableFuture.completedFuture(null);
        }

        // Await your turn.
        long delayUs = presentationTimeDeltaUs - elapsedUs;
        lastSystemTimeUs = now + delayUs;
        CompletableFuture<Void> completeAfter = new CompletableFuture<>();
        handler.postDelayed(() -> completeAfter.complete(null),
                TimeUnit.MICROSECONDS.toMillis(delayUs));

        return completeAfter;
    }
}
