package com.lightricks.videotricks.media;

import java.util.concurrent.CompletableFuture;

public interface PlaybackControl {
    CompletableFuture<Void> completeAt(long presentationTimeUs);
}
