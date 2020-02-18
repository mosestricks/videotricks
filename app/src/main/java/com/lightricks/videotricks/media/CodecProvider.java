package com.lightricks.videotricks.media;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Handler;
import android.view.Surface;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides media codecs for different media formats, keeping track of which ones were used.
 */
public class CodecProvider {
    private final List<MediaCodecInfo> infoList =
            Arrays.asList(new MediaCodecList(MediaCodecList.REGULAR_CODECS).getCodecInfos());

    /**
     * Get a configured decoder for the given format and surface if there's one available.
     *
     * @param format - media format that the encoder has to support
     * @param surface - used to configure the codec
     * @param callback - used to configure the codec in the async mode
     * @param handler - used to configure the codec in the async mode
     */
    public Optional<MediaCodec> getConfiguredDecoder(MediaFormat format,
                                                     @Nullable Surface surface,
                                                     @Nullable MediaCodec.Callback callback,
                                                     @Nullable Handler handler) {
        return getConfiguredCodec(format, surface, callback, handler, false);
    }

    /**
     * Get a configured encoder for the given format if there's one available.
     *
     * @param format - media format that the encoder has to support
     * @param callback - used to configure the codec in the async mode
     * @param handler - used to configure the codec in the async mode
     */
    public Optional<MediaCodec> getConfiguredEncoder(MediaFormat format,
                                                     @Nullable MediaCodec.Callback callback,
                                                     @Nullable Handler handler) {
        return getConfiguredCodec(format, null, callback, handler, true);
    }

    /**
     * Override this method to replace the format based on the actual codec info
     * after a suitable codec has been found.
     * @param format - original media format
     * @param codecInfo - media codec info of a suitable codec
     * @return new media format
     */
    protected MediaFormat transform(MediaFormat format, MediaCodecInfo codecInfo) {
        // There is no transformation by default.
        return format;
    }

    /**
     * Override this method if some codecs are preferred over the other.
     * @return comparator that assigns a higher value to preferred codecs
     */
    protected Comparator<MediaCodecInfo> getCodecComparator() {
        // All codecs are treated equal by default.
        return (info1, info2) -> 0;
    }

    /** Private methods */

    private Optional<MediaCodec> getConfiguredCodec(MediaFormat format,
                                                    @Nullable Surface surface,
                                                    @Nullable MediaCodec.Callback callback,
                                                    @Nullable Handler handler,
                                                    boolean isEncoder) {
        List<MediaCodecInfo> capableCodecs = findCodecs(format, isEncoder);
        if (capableCodecs.isEmpty()) {
            return Optional.empty();
        }

        return capableCodecs.stream()
                .map(info -> codecByName(info.getName()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(codec -> {
                    codec.setCallback(callback, handler);
                    MediaFormat finalFormat = transform(format, codec.getCodecInfo());

                    try {
                        codec.configure(finalFormat, surface, null, isEncoder ? MediaCodec.CONFIGURE_FLAG_ENCODE : 0);
                        return true;
                    } catch (MediaCodec.CodecException ce) {
                        codec.setCallback(null);
                        codec.release();
                        return false;
                    }
                }).findFirst();
    }

    private List<MediaCodecInfo> findCodecs(MediaFormat format, boolean isEncoder) {
        String mime = format.getString(MediaFormat.KEY_MIME);
        return infoList.stream()
                .filter(info -> info.isEncoder() == isEncoder)
                .filter(info -> Arrays.stream(info.getSupportedTypes())
                                      .anyMatch(type -> type.equalsIgnoreCase(mime)))
                .sorted(getCodecComparator())
                .collect(Collectors.toList());
    }

    private Optional<MediaCodec> codecByName(String name) {
        try {
            return Optional.of(MediaCodec.createByCodecName(name));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
