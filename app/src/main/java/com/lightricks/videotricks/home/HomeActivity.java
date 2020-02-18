package com.lightricks.videotricks.home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.lightricks.videotricks.R;
import com.lightricks.videotricks.databinding.ActivityHomeBinding;
import com.lightricks.videotricks.seek.MultiplePlayersActivity;
import com.lightricks.videotricks.seek.SeekVideoViewActivity;
import com.lightricks.videotricks.stats.CollectStatsActivity;
import com.lightricks.videotricks.util.ClickableListAdapter;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class HomeActivity extends AppCompatActivity {
    private int[] labelResIds = new int[] {
            R.string.seek_video_view,
            R.string.multiple_players,
            R.string.collect_stats,
    };

    private Class[] activityClasses = new Class[] {
            SeekVideoViewActivity.class,
            MultiplePlayersActivity.class,
            CollectStatsActivity.class,
    };

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        setupViews();
    }

    private void setupViews() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(this,
                RecyclerView.VERTICAL));
        binding.recyclerView.setAdapter(makeAdapter());
    }

    private RecyclerView.Adapter makeAdapter() {
        ClickableListAdapter adapter = new ClickableListAdapter();
        adapter.setLabels(getLabels());
        adapter.setClickListener(((position, label) ->
                startActivityClass(activityClasses[position])));
        return adapter;
    }

    private List<String> getLabels() {
        return IntStream.of(labelResIds)
                .mapToObj(this::getString)
                .collect(toList());
    }

    private void startActivityClass(Class activityClass) {
        startActivity(new Intent(getApplicationContext(), activityClass));
    }
}
