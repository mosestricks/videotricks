package com.lightricks.videotricks.media;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Size;

import java.util.Locale;

import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
import static android.media.MediaFormat.COLOR_RANGE_FULL;
import static android.media.MediaFormat.KEY_BIT_RATE;
import static android.media.MediaFormat.KEY_COLOR_FORMAT;
import static android.media.MediaFormat.KEY_COLOR_RANGE;
import static android.media.MediaFormat.KEY_FRAME_RATE;
import static android.media.MediaFormat.KEY_I_FRAME_INTERVAL;

/**
 * Provides codecs for the VideoEncoder using the supplied parameters.
 */
public class VideoEncoderCodecProvider extends CodecProvider {
    private final Size videoSize;
    private final String mimeType;
    private final int videoBitRate;
    private final int framesPerSecond;
    private final int iFrameIntervalSeconds;

    /**
     * Public constructor.
     * All parameters are used to configure the video encoder.
     */
    public VideoEncoderCodecProvider(Size videoSize, String mimeType, int videoBitRate,
                                     int framesPerSecond, int iFrameIntervalSeconds) {
        this.videoSize = videoSize;
        this.mimeType = mimeType;
        this.videoBitRate = videoBitRate;
        this.framesPerSecond = framesPerSecond;
        this.iFrameIntervalSeconds = iFrameIntervalSeconds;
    }

    /** CodecProvider methods */

    @Override
    protected MediaFormat transform(MediaFormat format, MediaCodecInfo codecInfo) {
        MediaCodecInfo.VideoCapabilities videoCapabilities =
                codecInfo.getCapabilitiesForType(mimeType).getVideoCapabilities();

        int widthAlignment = videoCapabilities.getWidthAlignment();
        int heightAlignment = videoCapabilities.getHeightAlignment();
        int actualWidth = alignCeil(videoSize.getWidth(), widthAlignment);
        int actualHeight = alignCeil(videoSize.getHeight(), heightAlignment);

        if (!videoCapabilities.isSizeSupported(actualWidth, actualHeight)) {
            // Capabilities are inaccurately reported for vertical resolutions on some devices.
            // Try to swap width and height unless we know that this will cause issues on a particular codec / device.
            // noinspection SuspiciousNameCombination
            if (!canSwapWidthAndHeight(codecInfo.getName()) ||
                    !videoCapabilities.isSizeSupported(actualHeight, actualWidth)) {
                String msg = "Requested size is not supported by video codec\n" +
                        "codec=" + codecInfo.getName() + "\n" +
                        "widthAlignment=" + widthAlignment + "\n" +
                        "heightAlignment=" + heightAlignment + "\n" +
                        "videoSize.width=" + videoSize.getWidth() + "\n" +
                        "videoSize.height=" + videoSize.getHeight() + "\n" +
                        "alignedWidth=" + actualWidth + "\n" +
                        "alignedHeight=" + actualHeight + "\n";

                throw new RuntimeException(msg);
            }
        }

        MediaFormat newFormat = MediaFormat.createVideoFormat(mimeType, actualWidth, actualHeight);

        // Set some properties. Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        newFormat.setInteger(KEY_COLOR_FORMAT, COLOR_FormatSurface);
        newFormat.setInteger(KEY_BIT_RATE, videoBitRate);
        newFormat.setInteger(KEY_FRAME_RATE, framesPerSecond);
        newFormat.setInteger(KEY_I_FRAME_INTERVAL, iFrameIntervalSeconds);
        newFormat.setInteger(KEY_COLOR_RANGE, COLOR_RANGE_FULL);

        return newFormat;
    }

    /** Private methods */

    private int alignCeil(int num, int alignment) {
        int mod = num % alignment;
        return mod == 0 ? num : num + alignment - mod;
    }

    private boolean canSwapWidthAndHeight(String name) {
        // noinspection RedundantIfStatement
        if ("OMX.MTK.VIDEO.DECODER.HEVC".equals(name.toUpperCase(Locale.ENGLISH)) && "mcv5a".equals(Build.DEVICE)) {
            return false;
        }

        return true;
    }
}
