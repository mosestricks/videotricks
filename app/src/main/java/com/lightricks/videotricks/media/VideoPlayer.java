package com.lightricks.videotricks.media;

import android.graphics.ImageFormat;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;

public class VideoPlayer implements Allocation.OnBufferAvailableListener {
    private final Allocation input, output;
    private final ScriptIntrinsicYuvToRGB yuvToRGB;

    public VideoPlayer(Size videoSize, RenderScript renderScript) {
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

    public void surfaceCreated(SurfaceHolder holder) {
        output.setSurface(holder.getSurface());
    }

    public void surfaceDestroyed() {
        output.setSurface(null);
    }

    public Surface getInputSurface() {
        return input.getSurface();
    }

    public void dispose() {
        input.destroy();
        output.destroy();
        yuvToRGB.destroy();
    }

    /** OnBufferAvailableListener impl. */

    @Override
    public void onBufferAvailable(Allocation a) {
        input.ioReceive();
        yuvToRGB.forEach(output);
        output.ioSend();
    }
}
