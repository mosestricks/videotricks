package com.lightricks.videotricks.stats;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.lightricks.videotricks.R;
import com.lightricks.videotricks.media.VideoReader;
import com.lightricks.videotricks.model.VideoInfo;
import com.lightricks.videotricks.util.UiHelper;

import java.nio.file.Paths;

public class CollectStatsViewModel extends AndroidViewModel {
    private MutableLiveData<String> statusText = new MutableLiveData<>();
    private MutableLiveData<Integer> progressVisibility = new MutableLiveData<>();
    private MutableLiveData<Boolean> isButtonEnabled = new MutableLiveData<>();
    private MutableLiveData<String> buttonText = new MutableLiveData<>();
    private UiHelper uiHelper;
    private VideoInfo videoInfo;
    private VideoReader videoReader;
    private boolean statsAreReady;

    public CollectStatsViewModel(@NonNull Application application) {
        super(application);

        reset();
    }

    public LiveData<String> getStatusText() {
        return statusText;
    }

    public LiveData<Integer> getProgressVisibility() {
        return progressVisibility;
    }

    public LiveData<Boolean> getIsButtonEnabled() {
        return isButtonEnabled;
    }

    public LiveData<String> getButtonText() {
        return buttonText;
    }

    public void onButtonClick() {
        if (statsAreReady) {
            viewStats();
        } else {
            collectStats();
        }
    }

    void setUiHelper(UiHelper uiHelper) {
        this.uiHelper = uiHelper;
    }

    void setVideoInfo(VideoInfo videoInfo) {
        this.videoInfo = videoInfo;

        statusText.setValue(getApplication().getString(R.string.collect_stats_prompt,
                Paths.get(videoInfo.getPath()).getFileName().toString(),
                videoInfo.getDurationMs()));

        isButtonEnabled.setValue(true);
    }

    /** Private methods */

    private void reset() {
        progressVisibility.setValue(View.GONE);
        buttonText.setValue(getApplication().getString(R.string.go));
        isButtonEnabled.setValue(false);
    }

    private void collectStats() {
        isButtonEnabled.setValue(false);
        progressVisibility.setValue(View.VISIBLE);

        // todo
    }

    private void viewStats() {
        // todo
    }

    private void handleStats() {
        progressVisibility.setValue(View.GONE);
        buttonText.setValue(getApplication().getString(R.string.view));
        statsAreReady = true;
    }

    private void handleError(String message) {
        uiHelper.showLongSnackbar(message);
        reset();
    }
}
