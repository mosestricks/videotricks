package com.lightricks.videotricks.surface;

import android.app.Application;
import android.content.res.AssetFileDescriptor;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.os.Looper;
import android.renderscript.RenderScript;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.lightricks.videotricks.media.CodecProvider;
import com.lightricks.videotricks.media.VideoPlayer;
import com.lightricks.videotricks.media.VideoReader;
import com.lightricks.videotricks.model.VideoMetadata;
import com.lightricks.videotricks.util.DataSource;
import com.lightricks.videotricks.util.MediaUtils;
import com.lightricks.videotricks.util.UiHelper;

public class SurfacePlayerViewModel extends AndroidViewModel implements SurfaceHolder.Callback {
    private MutableLiveData<Integer> seekBarVisibility = new MutableLiveData<>();
    private MutableLiveData<Integer> buttonVisibility = new MutableLiveData<>();
    private Handler uiThreadHandler = new Handler(Looper.getMainLooper());
    private VideoReader videoReader;
    private VideoPlayer videoPlayer;
    private UiHelper uiHelper;
    private Size videoSize;

    public SurfacePlayerViewModel(@NonNull Application application) {
        super(application);

        seekBarVisibility.setValue(View.GONE);
        buttonVisibility.setValue(View.VISIBLE);
    }

    public LiveData<Integer> getSeekBarVisibility() {
        return seekBarVisibility;
    }

    public LiveData<Integer> getButtonVisibility() {
        return buttonVisibility;
    }

    public void onButtonClick(@SuppressWarnings("unused") View ignored) {
        startPlayback();
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

    Size getVideoSize() {
        return videoSize;
    }

    /** ViewModel methods */

    @Override
    protected void onCleared() {
        videoReader.dispose().join();
        videoPlayer.dispose();
    }

    /** SurfaceHolder.Callback impl. */

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        videoPlayer.surfaceCreated(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // no action
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        videoPlayer.surfaceDestroyed();
    }

    /** Private methods */

    @SuppressWarnings("SameParameterValue")
    private void setupVideoPipeline(String filename) throws Exception {
        AssetFileDescriptor fd = getApplication().getAssets().openFd(filename);
        DataSource dataSource = new DataSource(fd.getFileDescriptor(), fd.getStartOffset(),
                fd.getLength(), fd);

        VideoMetadata metadata = getMetadata(dataSource);
        videoSize = new Size(metadata.getWidth(), metadata.getHeight());

        videoPlayer = new VideoPlayer(videoSize, RenderScript.create(getApplication()));

        int trackId = MediaUtils.firstVideoTrack(dataSource)
                .orElseThrow(() -> new RuntimeException("Video track not found in " + filename));

        videoReader = new VideoReader(dataSource, new CodecProvider(), trackId,
                videoPlayer.getInputSurface());
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

    private void startPlayback() {
        hideButton();
        videoReader.start().thenAcceptAsync(this::handleComplete, uiThreadHandler::post);
    }

    @SuppressWarnings("unused")
    private void handleComplete(Void nothing) {
        setupVideoPipeline();
        showButton();
    }

    private void handleError(String message) {
        uiHelper.showLongSnackbar(message);
    }

    private void showButton() {
        buttonVisibility.setValue(View.VISIBLE);
    }

    private void hideButton() {
        buttonVisibility.setValue(View.GONE);
    }

    static class Constants {
        static final String VIDEO_FILE_NAME = "SocialMediaLoveClip.mp4";
    }
}
