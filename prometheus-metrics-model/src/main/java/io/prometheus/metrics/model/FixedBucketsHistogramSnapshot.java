package io.prometheus.metrics.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class FixedBucketsHistogramSnapshot extends MetricSnapshot {

    private final boolean gaugeHistogram;

    /**
     * To create a new {@link FixedBucketsHistogramSnapshot}, you can either call the constructor directly or use
     * the builder with {@link FixedBucketsHistogramSnapshot#newBuilder()}.
     *
     * @param metadata see {@link MetricMetadata} for more naming conventions.
     * @param data     the constructor will create a sorted copy of the collection.
     */
    public FixedBucketsHistogramSnapshot(MetricMetadata metadata, Collection<FixedBucketsHistogramData> data) {
        this(false, metadata, data);
    }

    /**
     * Use this with the first parameter {@code true} to create a Gauge Histogram.
     * The data model for Gauge Histograms is the same as for regular histograms, except that bucket values
     * are semantically gauges and not counters. See <a href="https://openmetrics.io">openmetrics.io</a> for more
     * info on Gauge Histograms.
     */
    public FixedBucketsHistogramSnapshot(boolean isGaugeHistogram, MetricMetadata metadata, Collection<FixedBucketsHistogramData> data) {
        super(metadata, data);
        this.gaugeHistogram = isGaugeHistogram;
    }

    public boolean isGaugeHistogram() {
        return gaugeHistogram;
    }

    @Override
    public List<FixedBucketsHistogramData> getData() {
        return (List<FixedBucketsHistogramData>) data;
    }

    public static final class FixedBucketsHistogramData extends DistributionMetricData {

        private final FixedBuckets buckets;

        /**
         * To create a new {@link FixedBucketsHistogramData}, you can either call the constructor directly
         * or use the Builder with {@link FixedBucketsHistogramData#newBuilder()}.
         *
         * @param count total number of observations. Optional, pass {@code -1} if not available.
         *              If the count is present, it must have the same value as the +Inf bucket.
         * @param sum                      sum of all observed values.
         *                                 Semantically the sum is a counter, so it should only be present if all
         *                                 observed values are positive (example: latencies are always positive).
         *                                 The sum is optional, pass {@link Double#NaN} if no sum is available.
         * @param buckets required, there must be at least the +Inf bucket.
         * @param labels                   must not be null. Use {@link Labels#EMPTY} if there are no labels.
         * @param exemplars                must not be null. Use {@link Exemplars#EMPTY} if there are no exemplars.
         * @param createdTimestampMillis   timestamp (as in {@link System#currentTimeMillis()}) when this histogram
         *                                 data (this specific set of labels) was created or reset to zero.
         *                                 Note that this refers to the creation of the timeseries,
         *                                 not the creation of the snapshot.
         *                                 It's optional. Use {@code 0L} if there is no created timestamp.
         */
        public FixedBucketsHistogramData(long count, double sum, FixedBuckets buckets, Labels labels, Exemplars exemplars, long createdTimestampMillis) {
            this(count, sum, buckets, labels, exemplars, createdTimestampMillis, 0);
        }

        /**
         * Constructor with an additional metric timestamp parameter. In most cases you should not need this,
         * as the timestamp of a Prometheus metric is set by the Prometheus server during scraping.
         * Exceptions include mirroring metrics with given timestamps from other metric sources.
         */
        public FixedBucketsHistogramData(long count, double sum, FixedBuckets buckets, Labels labels, Exemplars exemplars, long createdTimestampMillis, long timestampMillis) {
            super(count, sum, exemplars, labels, createdTimestampMillis, timestampMillis);
            this.buckets = buckets;
            validate();
        }

        public FixedBuckets getBuckets() {
            return buckets;
        }

        @Override
        protected void validate() {
            for (Label label : getLabels()) {
                if (label.getName().equals("le")) {
                    throw new IllegalArgumentException("le is a reserved label name for histograms");
                }
            }
            if (buckets.getCumulativeCount(buckets.size()-1) != getCount()) {
                throw new IllegalArgumentException("The +Inf bucket must have the same value as the count.");
            }
        }

        public static class Builder extends DistributionMetricData.Builder<FixedBucketsHistogramData.Builder> {

            private FixedBuckets buckets;

            private Builder() {}

            @Override
            protected Builder self() {
                return this;
            }

            public Builder withBuckets(FixedBuckets buckets) {
                this.buckets = buckets;
                return this;
            }

            public FixedBucketsHistogramSnapshot.FixedBucketsHistogramData build() {
                if (buckets == null) {
                    throw new IllegalArgumentException("buckets are required");
                }
                return new FixedBucketsHistogramData(count, sum, buckets, labels, exemplars, createdTimestampMillis, timestampMillis);
            }
        }

        public static Builder newBuilder() {
            return new Builder();
        }
    }

    public static class Builder extends MetricSnapshot.Builder<Builder> {

        private final List<FixedBucketsHistogramData> histogramData = new ArrayList<>();
        private boolean isGaugeHistogram = false;

        private Builder() {
        }

        public Builder addData(FixedBucketsHistogramData data) {
            histogramData.add(data);
            return this;
        }

        /**
         * Create a Gauge Histogram. The data model for Gauge Histograms is the same as for regular histograms,
         * except that bucket values are semantically gauges and not counters.
         * See <a href="https://openmetrics.io">openmetrics.io</a> for more info on Gauge Histograms.
         */
        public Builder asGaugeHistogram() {
            isGaugeHistogram = true;
            return this;
        }

        public FixedBucketsHistogramSnapshot build() {
            return new FixedBucketsHistogramSnapshot(isGaugeHistogram, buildMetadata(), histogramData);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }
}
