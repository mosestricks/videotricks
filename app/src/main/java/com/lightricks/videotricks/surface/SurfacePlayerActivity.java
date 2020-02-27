package com.lightricks.videotricks.surface;

import android.os.Bundle;
import android.util.Size;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.lightricks.videotricks.R;
import com.lightricks.videotricks.databinding.ActivitySurfacePlayerBinding;
import com.lightricks.videotricks.util.LayoutUtils;
import com.lightricks.videotricks.util.UiHelper;

public class SurfacePlayerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);

        SurfacePlayerViewModel viewModel = ViewModelProviders.of(this)
                .get(SurfacePlayerViewModel.class);

        ActivitySurfacePlayerBinding dataBinding = DataBindingUtil.setContentView(this,
                R.layout.activity_surface_player);

        dataBinding.setVm(viewModel);
        dataBinding.setLifecycleOwner(this);
        dataBinding.videoSurface.getHolder().addCallback(viewModel);

        viewModel.setUiHelper(new UiHelper(this, dataBinding.coordinator));
        viewModel.setupVideoPipeline();

        Size videoSize = viewModel.getVideoSize();
        float ratio = videoSize.getWidth() / (float) videoSize.getHeight();
        LayoutUtils.applyDimensionRatio(dataBinding.layout, R.id.video_surface,
                String.valueOf(ratio));
    }
}
