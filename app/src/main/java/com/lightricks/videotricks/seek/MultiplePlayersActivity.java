package com.lightricks.videotricks.seek;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.VideoView;

import com.lightricks.videotricks.R;
import com.lightricks.videotricks.databinding.ActivityMultiplePlayersBinding;
import com.lightricks.videotricks.model.VideoInfo;
import com.lightricks.videotricks.util.FileUtils;
import com.lightricks.videotricks.util.UiHelper;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MultiplePlayersActivity extends AppCompatActivity {

    private ActivityMultiplePlayersBinding binding;
    private UiHelper uiHelper;
    private MediaMetadataRetriever retriever = new MediaMetadataRetriever();
    private List<VideoInfo> videoInfoList;
    private List<String> filenames = Arrays.asList("Smoke01.mp4", "Smoke02.mp4",
            "SmokeOutro.mp4", "SwimmingClip.mp4", "LightLeakFXBackground.mp4",
            "LightLeaksDefaultClip.mp4");

    /** Activity methods */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_multiple_players);
        uiHelper = new UiHelper(this, binding.coordinator);
        setupFiles();
        setupViews();
    }

    /** Private methods */

    private void setupFiles() {
        filenames.forEach(filename -> FileUtils.copyAssetToFilesDir(this, filename));

        videoInfoList = filenames.stream()
                .map(filename -> new File(getFilesDir(), filename))
                .map(videoFile -> {
                    retriever.setDataSource(videoFile.getPath());
                    int width = Integer.parseInt(retriever.extractMetadata(
                            MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));

                    int height = Integer.parseInt(retriever.extractMetadata(
                            MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

                    int duration = Integer.parseInt(retriever.extractMetadata(
                            MediaMetadataRetriever.METADATA_KEY_DURATION));

                    return new VideoInfo(videoFile.getPath(), width, height, duration);
                }).collect(Collectors.toList());
    }

    private void setupViews() {
        if (videoInfoList == null || videoInfoList.size() != filenames.size()) {
            uiHelper.showLongSnackbar(R.string.err_copy_asset);
            return;
        }

        loadVideoFile(videoInfoList.get(0), binding.videoView11);
        loadVideoFile(videoInfoList.get(1), binding.videoView12);
        loadVideoFile(videoInfoList.get(2), binding.videoView21);
        loadVideoFile(videoInfoList.get(3), binding.videoView22);
        loadVideoFile(videoInfoList.get(4), binding.videoView31);
        loadVideoFile(videoInfoList.get(5), binding.videoView32);

        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double ratio = progress / 100.0;
                seekToRatio(binding.videoView11, ratio);
                seekToRatio(binding.videoView12, ratio);
                seekToRatio(binding.videoView21, ratio);
                seekToRatio(binding.videoView22, ratio);
                seekToRatio(binding.videoView31, ratio);
                seekToRatio(binding.videoView32, ratio);
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

    private void loadVideoFile(VideoInfo info, VideoView view) {
        view.setVideoPath(info.getPath());
        view.pause();
        view.seekTo(1);
    }

    private void seekToRatio(VideoView view, double ratio) {
        long pos = Math.round(view.getDuration() * ratio);
        view.seekTo((int) pos);
    }
}
