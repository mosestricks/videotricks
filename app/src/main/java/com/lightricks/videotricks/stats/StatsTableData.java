package com.lightricks.videotricks.stats;

public class StatsTableData {
    private final String filename;
    private final String reportedDuration;
    private final String reportedFps;
    private final String sampleCount;
    private final String minPts;
    private final String maxPts;
    private final String actualDuration;
    private final String minTimeDelta;
    private final String maxTimeDelta;
    private final String avgTimeDelta;
    private final String avgActualFps;

    public StatsTableData(String filename, String reportedDuration, String reportedFps,
                          String sampleCount, String minPts, String maxPts,
                          String actualDuration, String minTimeDelta, String maxTimeDelta,
                          String avgTimeDelta, String avgActualFps) {
        this.filename = filename;
        this.reportedDuration = reportedDuration;
        this.reportedFps = reportedFps;
        this.sampleCount = sampleCount;
        this.minPts = minPts;
        this.maxPts = maxPts;
        this.actualDuration = actualDuration;
        this.minTimeDelta = minTimeDelta;
        this.maxTimeDelta = maxTimeDelta;
        this.avgTimeDelta = avgTimeDelta;
        this.avgActualFps = avgActualFps;
    }

    public String getFilename() {
        return filename;
    }

    public String getReportedDuration() {
        return reportedDuration;
    }

    public String getReportedFps() {
        return reportedFps;
    }

    public String getSampleCount() {
        return sampleCount;
    }

    public String getMinPts() {
        return minPts;
    }

    public String getMaxPts() {
        return maxPts;
    }

    public String getActualDuration() {
        return actualDuration;
    }

    public String getMinTimeDelta() {
        return minTimeDelta;
    }

    public String getMaxTimeDelta() {
        return maxTimeDelta;
    }

    public String getAvgTimeDelta() {
        return avgTimeDelta;
    }

    public String getAvgActualFps() {
        return avgActualFps;
    }
}
