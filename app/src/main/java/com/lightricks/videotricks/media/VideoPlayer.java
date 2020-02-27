package com.lightricks.videotricks.media;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class VideoPlayer implements ImageReader.OnImageAvailableListener {
    private final RenderScript renderScript;
    private final ImageReader imageReader;
    private final HandlerThread handlerThread;
    private final Handler handler;
    private SurfaceHolder surfaceHolder;

    public VideoPlayer(Size videoSize, RenderScript renderScript) {
        this.renderScript = renderScript;

        handlerThread = new HandlerThread("VideoPlayer");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        imageReader = ImageReader.newInstance(videoSize.getWidth(), videoSize.getHeight(),
                ImageFormat.YUV_420_888, 1);

        imageReader.setOnImageAvailableListener(this, handler);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        surfaceHolder = holder;
    }

    public void surfaceDestroyed() {
        surfaceHolder = null;
    }

    public Surface getInputSurface() {
        return imageReader.getSurface();
    }

    public CompletableFuture<Void> dispose() {
        return CompletableFuture.runAsync(this::releaseResources, handler::post)
                .thenRun(handlerThread::quit);
    }

    /** OnImageAvailableListener impl. */

    @Override
    public void onImageAvailable(ImageReader reader) {
        Image image = reader.acquireNextImage();

        if (surfaceHolder != null) {
            convertImage(image, surfaceHolder.getSurface());
        } else {
            image.close();
        }
    }

    /** Private methods */

    private void releaseResources() {
        imageReader.close();
    }

    private void convertImage(Image image, Surface surface) {
        if (image == null) {
            return;
        }

        Image.Plane[] planes = image.getPlanes();
        if (planes.length == 0) {
            return;
        }

        byte[] yuvData = imageToByteBuffer(image).array();

        ScriptIntrinsicYuvToRGB yuvToRGB =
                ScriptIntrinsicYuvToRGB.create(renderScript, Element.U8_4(renderScript));

        Allocation input = Allocation.createSized(renderScript, Element.U8(renderScript), yuvData.length);
        input.copyFrom(yuvData);
        yuvToRGB.setInput(input);

        Type rgbaType = new Type.Builder(renderScript, Element.RGBA_8888(renderScript))
                .setX(image.getWidth())
                .setY(image.getHeight())
                .create();

        Allocation output = Allocation.createTyped(renderScript, rgbaType,
                Allocation.USAGE_IO_OUTPUT | Allocation.USAGE_SCRIPT);

        output.setSurface(surface);
        yuvToRGB.forEach(output);
        output.ioSend();

        //Bitmap bmp = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        //Allocation output = Allocation.createFromBitmap(renderScript, bmp);
        //yuvToRGB.forEach(output);
        //output.copyTo(bmp);
        //bmp.recycle();

        input.destroy();
        output.destroy();
        yuvToRGB.destroy();
        image.close();
    }

    private ByteBuffer imageToByteBuffer(final Image image) {
        final Rect crop = image.getCropRect();
        final int width = crop.width();
        final int height = crop.height();

        final Image.Plane[] planes = image.getPlanes();
        final byte[] rowData = new byte[planes[0].getRowStride()];
        final int bufferSize = width * height *
                ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8;
        final ByteBuffer output = ByteBuffer.allocateDirect(bufferSize);

        int channelOffset;
        int outputStride;

        for (int planeIndex = 0; planeIndex < 3; planeIndex++) {
            if (planeIndex == 0) {
                channelOffset = 0;
                outputStride = 1;
            } else if (planeIndex == 1) {
                channelOffset = width * height + 1;
                outputStride = 2;
            } else {
                channelOffset = width * height;
                outputStride = 2;
            }

            final ByteBuffer buffer = planes[planeIndex].getBuffer();
            final int rowStride = planes[planeIndex].getRowStride();
            final int pixelStride = planes[planeIndex].getPixelStride();
            final int shift = (planeIndex == 0) ? 0 : 1;
            final int widthShifted = width >> shift;
            final int heightShifted = height >> shift;

            buffer.position(rowStride * (crop.top >> shift) +
                    pixelStride * (crop.left >> shift));

            for (int row = 0; row < heightShifted; row++) {
                final int length;

                if (pixelStride == 1 && outputStride == 1) {
                    length = widthShifted;
                    buffer.get(output.array(), channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (widthShifted - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);

                    for (int col = 0; col < widthShifted; col++) {
                        output.array()[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }

                if (row < heightShifted - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }

        return output;
    }
}
