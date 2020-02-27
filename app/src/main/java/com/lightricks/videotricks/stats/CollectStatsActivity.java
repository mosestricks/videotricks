package com.lightricks.videotricks.stats;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.lightricks.videotricks.R;
import com.lightricks.videotricks.databinding.ActivityCollectStatsBinding;
import com.lightricks.videotricks.util.UiHelper;

public class CollectStatsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);

        CollectStatsViewModel viewModel = ViewModelProviders.of(this)
                .get(CollectStatsViewModel.class);

        ActivityCollectStatsBinding dataBinding = DataBindingUtil.setContentView(this,
                R.layout.activity_collect_stats);

        dataBinding.setVm(viewModel);
        dataBinding.setLifecycleOwner(this);

        viewModel.setUiHelper(new UiHelper(this, dataBinding.coordinator));
        viewModel.setupVideoPipeline();
    }
}
