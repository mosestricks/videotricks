package com.lightricks.videotricks.stats;

import android.app.Application;
import android.content.res.AssetFileDescriptor;
import android.os.Handler;
import android.os.Looper;
import android.util.Size;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.lightricks.videotricks.R;
import com.lightricks.videotricks.media.CodecProvider;
import com.lightricks.videotricks.media.VideoEncoderCodecProvider;
import com.lightricks.videotricks.media.VideoReader;
import com.lightricks.videotricks.media.VideoWriter;
import com.lightricks.videotricks.util.DataSource;
import com.lightricks.videotricks.util.MediaUtils;
import com.lightricks.videotricks.util.UiHelper;

public class CollectStatsViewModel extends AndroidViewModel {
    private MutableLiveData<String> statusText = new MutableLiveData<>();
    private MutableLiveData<Integer> progressVisibility = new MutableLiveData<>();
    private MutableLiveData<Integer> buttonVisibility = new MutableLiveData<>();
    private MutableLiveData<String> buttonText = new MutableLiveData<>();
    private Handler uiThreadHandler = new Handler(Looper.getMainLooper());
    private VideoWriter videoWriter;
    private VideoReader videoReader;
    private UiHelper uiHelper;
    private boolean statsAreReady;

    public CollectStatsViewModel(@NonNull Application application) {
        super(application);

        reset();

        try {
            setupVideoPipeline(Constants.VIDEO_FILE_NAME);
        } catch (Exception e) {
            handleError(e.getMessage());
        }
    }

    public LiveData<String> getStatusText() {
        return statusText;
    }

    public LiveData<Integer> getProgressVisibility() {
        return progressVisibility;
    }

    public LiveData<Integer> getButtonVisibility() {
        return buttonVisibility;
    }

    public LiveData<String> getButtonText() {
        return buttonText;
    }

    @SuppressWarnings("unused")
    public void onButtonClick(View ignored) {
        if (statsAreReady) {
            viewStats();
        } else {
            collectStats();
        }
    }

    void setUiHelper(UiHelper uiHelper) {
        this.uiHelper = uiHelper;
    }

    /** ViewModel methods */

    @Override
    protected void onCleared() {
        videoReader.dispose().join();
        videoWriter.dispose();
    }

    /** Private methods */

    private void reset() {
        statusText.setValue(getApplication().getString(R.string.collect_stats_prompt,
                Constants.VIDEO_FILE_NAME));

        buttonVisibility.setValue(View.VISIBLE);
        buttonText.setValue(getApplication().getString(R.string.go));
        progressVisibility.setValue(View.GONE);
    }

    @SuppressWarnings("SameParameterValue")
    private void setupVideoPipeline(String filename) throws Exception {
        CodecProvider encoderProvider = new VideoEncoderCodecProvider(Constants.VIDEO_SIZE,
                Constants.VIDEO_MIME, Constants.VIDEO_BIT_RATE, Constants.VIDEO_FPS,
                Constants.VIDEO_I_FRAME_INTERVAL);

        videoWriter = new VideoWriter(Constants.VIDEO_SIZE, Constants.VIDEO_MIME, encoderProvider);

        AssetFileDescriptor afd = getApplication().getAssets().openFd(filename);
        DataSource dataSource = new DataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                afd.getLength(), afd);

        int trackId = MediaUtils.firstVideoTrack(dataSource)
                .orElseThrow(() -> new RuntimeException("Video track not found in " + filename));

        videoReader = new VideoReader(dataSource, new CodecProvider(), trackId,
                videoWriter.getSurface());
    }

    private void collectStats() {
        progressVisibility.setValue(View.VISIBLE);
        buttonVisibility.setValue(View.GONE);
        videoReader.start().thenAcceptAsync(this::handleStats, uiThreadHandler::post);
    }

    private void viewStats() {
        // todo
    }

    @SuppressWarnings("unused")
    private void handleStats(Void nothing) {
        buttonVisibility.setValue(View.VISIBLE);
        progressVisibility.setValue(View.GONE);
        buttonText.setValue(getApplication().getString(R.string.view));
        statsAreReady = true;
    }

    private void handleError(String message) {
        uiHelper.showLongSnackbar(message);
        reset();
    }

    static class Constants {
        static final String VIDEO_FILE_NAME = "SocialMediaLoveClip.mp4";
        static final Size VIDEO_SIZE = new Size(1280, 720);
        static final String VIDEO_MIME = "video/avc";
        static final int VIDEO_BIT_RATE = 16_000_000;
        static final int VIDEO_FPS = 30;
        static final int VIDEO_I_FRAME_INTERVAL = 1;
    }
}