package com.lightricks.videotricks.util;

import android.media.MediaExtractor;
import android.media.MediaFormat;

import androidx.core.util.Pair;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

public class MediaUtils {

    public static Optional<Integer> firstVideoTrack(DataSource dataSource) throws IOException {
        return tracks(dataSource)
                .filter(format -> {
                    if (format.second != null) {
                        String mime = format.second.getString(MediaFormat.KEY_MIME);
                        if (mime != null) {
                            return mime.toLowerCase(Locale.ENGLISH).startsWith("video");
                        }
                    }

                    return false;
                })
                .map(format -> format.first)
                .findFirst();
    }

    private static Stream<Pair<Integer, MediaFormat>> tracks(DataSource dataSource) throws IOException {
        MediaExtractor mediaExtractor = new MediaExtractor();
        mediaExtractor.setDataSource(dataSource.getFileDescriptor(), dataSource.getOffset(),
                dataSource.getLength());

        int trackCount = mediaExtractor.getTrackCount();
        Stream.Builder<Pair<Integer, MediaFormat>> builder = Stream.builder();

        for (int trackIndex = 0; trackIndex < trackCount; ++trackIndex) {
            MediaFormat trackFormat = mediaExtractor.getTrackFormat(trackIndex);
            builder.add(new Pair<>(trackIndex, trackFormat));
        }

        mediaExtractor.release();
        return builder.build();
    }
}
