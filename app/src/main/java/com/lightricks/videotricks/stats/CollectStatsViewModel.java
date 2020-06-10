package com.lightricks.videotricks.stats;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.res.AssetFileDescriptor;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
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

import java.util.concurrent.CompletableFuture;

public class CollectStatsViewModel extends AndroidViewModel {
    private MutableLiveData<String> promptText = new MutableLiveData<>();
    private MutableLiveData<Integer> promptVisibility = new MutableLiveData<>();
    private MutableLiveData<Integer> tableVisibility = new MutableLiveData<>();
    private MutableLiveData<Integer> progressVisibility = new MutableLiveData<>();
    private MutableLiveData<Integer> buttonVisibility = new MutableLiveData<>();
    private MutableLiveData<StatsTableData> tableData = new MutableLiveData<>();
    private Handler uiThreadHandler = new Handler(Looper.getMainLooper());
    private StatsCollector statsCollector = new StatsCollector();
    private VideoWriter videoWriter;
    private VideoReader videoReader;
    private UiHelper uiHelper;
    private long start;

    public CollectStatsViewModel(@NonNull Application application) {
        super(application);

        reset();
    }

    public LiveData<String> getPromptText() {
        return promptText;
    }

    public LiveData<Integer> getPromptVisibility() {
        return promptVisibility;
    }

    public LiveData<Integer> getTableVisibility() {
        return tableVisibility;
    }

    public LiveData<Integer> getProgressVisibility() {
        return progressVisibility;
    }

    public LiveData<Integer> getButtonVisibility() {
        return buttonVisibility;
    }

    public LiveData<StatsTableData> getTableData() {
        return tableData;
    }

    public void onButtonClick(@SuppressWarnings("unused") View ignored) {
        collectStats();
    }

    void setUiHelper(UiHelper uiHelper) {
        this.uiHelper = uiHelper;
    }

    void setupVideoPipeline() {
        try {
            setupVideoPipeline(Constants.VIDEO_FILE_NAME);
        } catch (Exception e) {
            handleError(e.getMessage());
        }
    }

    /** ViewModel methods */

    @Override
    protected void onCleared() {
        videoReader.dispose().join();
        videoWriter.dispose();
    }

    /** Private methods */

    private void reset() {
        showPrompt(getApplication().getString(R.string.collect_stats_prompt,
                Constants.VIDEO_FILE_NAME));
        showButton();
        hideProgress();
        hideTable();
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
                videoWriter.getSurface(), pts -> CompletableFuture.completedFuture(null));

        videoReader.setSamplesHandler(statsCollector::acceptSample);
        videoReader.setDryRun(true);
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
        showProgress();
        hideButton();
        start = SystemClock.uptimeMillis();
        videoReader.start().thenAcceptAsync(this::handleComplete, uiThreadHandler::post);
    }

    @SuppressWarnings("unused")
    private void handleComplete(Void nothing) {
        long elapsed = SystemClock.uptimeMillis() - start;
        start = 0;
        hideProgress();
        hidePrompt();
        showReadSpeed(elapsed);
        showTable();
    }

    private void handleError(String message) {
        uiHelper.showLongSnackbar(message);
        reset();
    }

    private void showPrompt(String prompt) {
        promptText.setValue(prompt);
        promptVisibility.setValue(View.VISIBLE);
    }

    private void hidePrompt() {
        promptVisibility.setValue(View.GONE);
    }

    private void showReadSpeed(long elapsed) {
        int sampleCount = statsCollector.getSamplesCount();
        long rate = sampleCount * 1000 / elapsed;
        @SuppressLint("DefaultLocale")
        String msg = String.format("Read speed: %d samples per second", rate);
        uiHelper.showLongSnackbar(msg);
    }

    private void showTable() {
        tableVisibility.setValue(View.VISIBLE);

        StatsCollector.Stats deltaStats = statsCollector.getTimeDeltaStats();
        @SuppressLint("DefaultLocale")
        StatsTableData data = new StatsTableData(Constants.VIDEO_FILE_NAME,
                String.format("%d ms", videoReader.getDurationUs() / 1000),
                String.format("%d", videoReader.getFrameRate()),
                String.format("%d", statsCollector.getSamplesCount()),
                String.format("%d us", statsCollector.getPtsStat().getMin()),
                String.format("%d us", statsCollector.getPtsStat().getMax()),
                String.format("%d ms", (statsCollector.getPtsStat().getMax() - statsCollector.getPtsStat().getMin()) / 1000),
                String.format("%d ms", (int) (deltaStats.min / 1000)),
                String.format("%d ms", (int) (deltaStats.max / 1000)),
                String.format("%d ms", (int) (deltaStats.avg / 1000)),
                String.format("%d", (int) (1e6 / deltaStats.avg)));

        tableData.setValue(data);
    }

    private void hideTable() {
        tableVisibility.setValue(View.GONE);
    }

    private void showProgress() {
        progressVisibility.setValue(View.VISIBLE);
    }

    private void hideProgress() {
        progressVisibility.setValue(View.GONE);
    }

    private void showButton() {
        buttonVisibility.setValue(View.VISIBLE);
    }

    private void hideButton() {
        buttonVisibility.setValue(View.GONE);
    }

    static class Constants {
        static final String VIDEO_FILE_NAME = "SocialMediaLoveClip.mp4";
        static final String VIDEO_MIME = "video/avc";
        static final int VIDEO_BIT_RATE = 16_000_000;
        static final int VIDEO_FPS = 30;
        static final int VIDEO_I_FRAME_INTERVAL = 1;
    }
}
