package com.lightricks.videotricks.texture;

import android.app.Application;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.os.Looper;
import android.renderscript.RenderScript;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
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

public class TexturePlayerViewModel extends AndroidViewModel implements
        TextureView.SurfaceTextureListener {

    private MutableLiveData<Integer> seekBarVisibility = new MutableLiveData<>();
    private MutableLiveData<Integer> buttonVisibility = new MutableLiveData<>();
    private Handler uiThreadHandler = new Handler(Looper.getMainLooper());
    private VideoReader videoReader;
    private VideoPlayer videoPlayer;
    private UiHelper uiHelper;
    private Size videoSize;

    public TexturePlayerViewModel(@NonNull Application application) {
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

    /** SurfaceTextureListener impl. */

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        Surface surface = new Surface(surfaceTexture);
        videoPlayer.surfaceCreated(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // no action
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // no action
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
                videoPlayer.getInputSurface(), videoPlayer);
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
