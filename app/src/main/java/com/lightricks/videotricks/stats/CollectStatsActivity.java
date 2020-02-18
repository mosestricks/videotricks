package com.lightricks.videotricks.stats;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.lightricks.videotricks.R;
import com.lightricks.videotricks.databinding.ActivityCollectStatsBinding;
import com.lightricks.videotricks.model.VideoInfo;
import com.lightricks.videotricks.util.FileUtils;
import com.lightricks.videotricks.util.UiHelper;

import java.io.File;

public class CollectStatsActivity extends AppCompatActivity {
    private static final String VIDEO_FILE_NAME = "SocialMediaLoveClip.mp4";
    private CollectStatsViewModel viewModel;
    private ActivityCollectStatsBinding dataBinding;
    private UiHelper uiHelper;

    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);

        setupViewModel();
        setupVideoSource();
        setupViews();
        setupUiHelper();
    }

    /** Private methods */

    private void setupViewModel() {
        viewModel = ViewModelProviders.of(this).get(CollectStatsViewModel.class);
    }

    private void setupVideoSource() {
        if (!FileUtils.fileExists(this, VIDEO_FILE_NAME)) {
            if (!FileUtils.copyAssetToFilesDir(this, VIDEO_FILE_NAME)) {
                uiHelper.showLongSnackbar(R.string.err_copy_asset);
                return;
            }
        }

        File videoFile = new File(getFilesDir(), VIDEO_FILE_NAME);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoFile.getPath());

        int width = Integer.parseInt(retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));

        int height = Integer.parseInt(retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

        int duration = Integer.parseInt(retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION));

        viewModel.setVideoInfo(new VideoInfo(videoFile.getPath(), width, height, duration));
    }

    private void setupViews() {
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_collect_stats);
        dataBinding.setVm(viewModel);
        dataBinding.setLifecycleOwner(this);
    }

    private void setupUiHelper() {
        uiHelper = new UiHelper(this, dataBinding.coordinator);
        viewModel.setUiHelper(uiHelper);
    }
}
