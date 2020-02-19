package com.lightricks.videotricks.stats;

import android.app.Application;
import android.content.res.AssetFileDescriptor;
import android.media.MediaMetadataRetriever;
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
import com.lightricks.videotricks.model.VideoMetadata;
import com.lightricks.videotricks.util.DataSource;
import com.lightricks.videotricks.util.MediaUtils;
import com.lightricks.videotricks.util.UiHelper;

public class CollectStatsViewModel extends AndroidViewModel {
    private MutableLiveData<String> outputText = new MutableLiveData<>();
    private MutableLiveData<Integer> progressVisibility = new MutableLiveData<>();
    private MutableLiveData<Integer> buttonVisibility = new MutableLiveData<>();
    private Handler uiThreadHandler = new Handler(Looper.getMainLooper());
    private StatsCollector statsCollector = new StatsCollector();
    private VideoWriter videoWriter;
    private VideoReader videoReader;
    private UiHelper uiHelper;

    public CollectStatsViewModel(@NonNull Application application) {
        super(application);

        reset();

        try {
            setupVideoPipeline(Constants.VIDEO_FILE_NAME);
        } catch (Exception e) {
            handleError(e.getMessage());
        }
    }

    public LiveData<String> getOutputText() {
        return outputText;
    }

    public LiveData<Integer> getProgressVisibility() {
        return progressVisibility;
    }

    public LiveData<Integer> getButtonVisibility() {
        return buttonVisibility;
    }

    @SuppressWarnings("unused")
    public void onButtonClick(View ignored) {
        collectStats();
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
        outputText.setValue(getApplication().getString(R.string.collect_stats_prompt,
                Constants.VIDEO_FILE_NAME));

        buttonVisibility.setValue(View.VISIBLE);
        progressVisibility.setValue(View.GONE);
    }

    @SuppressWarnings("SameParameterValue")
    private void setupVideoPipeline(String filename) throws Exception {
        AssetFileDescriptor fd = getApplication().getAssets().openFd(filename);
        DataSource dataSource = new DataSource(fd.getFileDescriptor(), fd.getStartOffset(),
                fd.getLength(), fd);

        VideoMetadata metadata = getMetadata(dataSource);
        Size videoSize = new Size(metadata.getWidth(), metadata.getHeight());
        CodecProvider encoderProvider = new VideoEncoderCodecProvider(videoSize,
                Constants.VIDEO_MIME, Constants.VIDEO_BIT_RATE, Constants.VIDEO_FPS,
                Constants.VIDEO_I_FRAME_INTERVAL);

        videoWriter = new VideoWriter(videoSize, Constants.VIDEO_MIME, encoderProvider);

        int trackId = MediaUtils.firstVideoTrack(dataSource)
                .orElseThrow(() -> new RuntimeException("Video track not found in " + filename));

        videoReader = new VideoReader(dataSource, new CodecProvider(), trackId,
                videoWriter.getSurface());

        videoReader.setSamplesHandler(statsCollector::acceptSample);
    }

    private VideoMetadata getMetadata(DataSource dataSource) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(dataSource.getFileDescriptor(), dataSource.getOffset(),
                dataSource.getLength());

        int width = Integer.parseInt(retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));

        int height = Integer.parseInt(retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

        int duration = Integer.parseInt(retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION));

        int rotation = Integer.parseInt(retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));

        return new VideoMetadata(width, height, duration, rotation);
    }

    private void collectStats() {
        progressVisibility.setValue(View.VISIBLE);
        buttonVisibility.setValue(View.GONE);
        videoReader.start().thenAcceptAsync(this::handleComplete, uiThreadHandler::post);
    }

    @SuppressWarnings("unused")
    private void handleComplete(Void nothing) {
        progressVisibility.setValue(View.GONE);
        showStats();
    }

    private void showStats() {
        int samplesCount = statsCollector.getSamplesCount();
        long actualDuration = statsCollector.getActualDuration();
        StatsCollector.Stats deltaStats = statsCollector.getTimeDeltaStats();
        double fps = 1e6 / deltaStats.avg;
        outputText.setValue(getApplication().getString(R.string.collect_stats_result,
                (long) samplesCount, actualDuration / 1000, (long) deltaStats.min / 1000,
                (long) deltaStats.max / 1000, (long) deltaStats.avg / 1000, (long) fps));
    }

    private void handleError(String message) {
        uiHelper.showLongSnackbar(message);
        reset();
    }

    static class Constants {
        static final String VIDEO_FILE_NAME = "SocialMediaLoveClip.mp4";
        static final String VIDEO_MIME = "video/avc";
        static final int VIDEO_BIT_RATE = 16_000_000;
        static final int VIDEO_FPS = 30;
        static final int VIDEO_I_FRAME_INTERVAL = 1;
    }
}
