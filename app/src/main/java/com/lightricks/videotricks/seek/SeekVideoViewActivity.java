package com.lightricks.videotricks.seek;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.widget.SeekBar;

import com.lightricks.videotricks.R;
import com.lightricks.videotricks.databinding.ActivitySeekVideoViewBinding;
import com.lightricks.videotricks.model.VideoInfo;
import com.lightricks.videotricks.util.FileUtils;
import com.lightricks.videotricks.util.LayoutUtils;
import com.lightricks.videotricks.util.UiHelper;

import java.io.File;

public class SeekVideoViewActivity extends AppCompatActivity {
    private ActivitySeekVideoViewBinding binding;
    private UiHelper uiHelper;
    private VideoInfo videoInfo;

    /** Activity methods */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_seek_video_view);
        uiHelper = new UiHelper(this, binding.coordinator);
        setupFiles();
        setupViews();
    }

    /** Private methods */

    private void setupFiles() {
        final String filename = "toy-story.mp4";
        if (!FileUtils.copyAssetToFilesDir(this, filename)) {
            uiHelper.showLongSnackbar(R.string.err_copy_asset);
            return;
        }

        File videoFile = new File(getFilesDir(), filename);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoFile.getPath());

        int width = Integer.parseInt(retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));

        int height = Integer.parseInt(retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

        int duration = Integer.parseInt(retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION));

        videoInfo = new VideoInfo(videoFile.getPath(), width, height, duration);
    }

    private void setupViews() {
        if (videoInfo == null) {
            return;
        }

        float ratio = videoInfo.getWidth() / (float) videoInfo.getHeight();
        LayoutUtils.applyDimensionRatio(binding.constraintLayout, R.id.video_view,
                String.valueOf(ratio));

        binding.videoView.setVideoPath(videoInfo.getPath());
        binding.videoView.pause();
        binding.videoView.seekTo(1);

        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double ratio = progress / 100.0;
                long pos = Math.round(binding.videoView.getDuration() * ratio);
                binding.videoView.seekTo((int) pos);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No action
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // No action
            }
        });
    }

}
