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
import com.lightricks.videotricks.util.LayoutUtils;
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
    private List<String> filenames = Arrays.asList("BaseballQuotClip.mp4", "BreakfastClip.mp4",
            "BusinessAdvisorsDefaultClip.mp4", "HairSalonDefaultClip.mp4",
            "KidsMusicLessonsClip.mp4", "LanguageStudyClip.mp4", "LightLeaksDefaultClip.mp4",
            "LuxuryCarsClip.mp4", "SkiClip.mp4", "Top5PlacesThisFallClip.mp4");

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
        filenames.forEach(filename -> {
            if (!FileUtils.fileExists(this, filename)) {
                FileUtils.copyAssetToFilesDir(this, filename);
            }
        });

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
        loadVideoFile(videoInfoList.get(6), binding.videoView41);
        loadVideoFile(videoInfoList.get(7), binding.videoView42);
        loadVideoFile(videoInfoList.get(8), binding.videoView51);
        loadVideoFile(videoInfoList.get(9), binding.videoView52);

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
                seekToRatio(binding.videoView41, ratio);
                seekToRatio(binding.videoView42, ratio);
                seekToRatio(binding.videoView51, ratio);
                seekToRatio(binding.videoView52, ratio);
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
        float ratio = info.getWidth() / (float) info.getHeight();
        LayoutUtils.applyDimensionRatio(binding.constraintLayout, view.getId(),
                String.valueOf(ratio));

        view.setVideoPath(info.getPath());
        view.pause();
        view.seekTo(1);
    }

    private void seekToRatio(VideoView view, double ratio) {
        long pos = Math.round(view.getDuration() * ratio);
        view.seekTo((int) pos);
    }
}
