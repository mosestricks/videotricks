package com.lightricks.videotricks.texture;

import android.os.Bundle;
import android.util.Size;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.lightricks.videotricks.R;
import com.lightricks.videotricks.databinding.ActivityTexturePlayerBinding;
import com.lightricks.videotricks.util.LayoutUtils;
import com.lightricks.videotricks.util.UiHelper;

public class TexturePlayerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);

        TexturePlayerViewModel viewModel = ViewModelProviders.of(this)
                .get(TexturePlayerViewModel.class);

        ActivityTexturePlayerBinding dataBinding = DataBindingUtil.setContentView(this,
                R.layout.activity_texture_player);

        dataBinding.setVm(viewModel);
        dataBinding.setLifecycleOwner(this);
        dataBinding.textureView.setSurfaceTextureListener(viewModel);

        viewModel.setUiHelper(new UiHelper(this, dataBinding.coordinator));
        viewModel.setupVideoPipeline();

        Size videoSize = viewModel.getVideoSize();
        float ratio = videoSize.getWidth() / (float) videoSize.getHeight();
        LayoutUtils.applyDimensionRatio(dataBinding.layout, R.id.texture_view,
                String.valueOf(ratio));
    }
}
