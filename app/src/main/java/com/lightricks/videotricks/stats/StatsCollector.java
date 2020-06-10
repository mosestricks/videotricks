package com.lightricks.videotricks.stats;

import com.lightricks.videotricks.model.SampleInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

class StatsCollector {
    private List<SampleInfo> inputSamples = new ArrayList<>(256);

    void acceptSample(SampleInfo info) {
        inputSamples.add(info);
    }

    int getSamplesCount() {
        return inputSamples.size();
    }

    LongSummaryStatistics getPtsStat() {
        return inputSamples.stream()
                .mapToLong(SampleInfo::getPresentationTimeUs)
                .summaryStatistics();
    }

    Stats getSizeStats() {
        OptionalLong minSize = inputSamples.stream()
                .mapToLong(SampleInfo::getSize)
                .min();

        OptionalDouble avgSize = inputSamples.stream()
                .mapToLong(SampleInfo::getSize)
                .average();

        OptionalLong maxSize = inputSamples.stream()
                .mapToLong(SampleInfo::getSize)
                .max();

        return new Stats(minSize.getAsLong(), maxSize.getAsLong(), avgSize.getAsDouble());
    }

    Stats getTimeDeltaStats() {
        if (inputSamples.size() < 2) {
            return new Stats(0.0, 0.0, 0.0);
        }

        List<SampleInfo> sortedSamples = inputSamples.stream()
                .sorted()
                .collect(Collectors.toList());

        long[] deltas = new long[sortedSamples.size() - 1];
        for (int i=1; i<sortedSamples.size(); i++) {
            SampleInfo current = sortedSamples.get(i);
            SampleInfo previous = sortedSamples.get(i-1);
            deltas[i-1] = current.getPresentationTimeUs() - previous.getPresentationTimeUs();
        }

        OptionalLong minDelta = LongStream.of(deltas).min();
        OptionalDouble avgDelta = LongStream.of(deltas).average();
        OptionalLong maxDelta = LongStream.of(deltas).max();

        return new Stats(minDelta.getAsLong(), maxDelta.getAsLong(), avgDelta.getAsDouble());
    }

    static class Stats {
        final double min;
        final double max;
        final double avg;

        Stats(double min, double max, double avg) {
            this.min = min;
            this.max = max;
            this.avg = avg;
        }
    }
}
