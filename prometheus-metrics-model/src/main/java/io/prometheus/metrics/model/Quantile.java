package io.prometheus.metrics.model;

public class Quantile implements Comparable<Quantile> {
    private final double quantile;
    private final double value;

    public Quantile(double quantile, double value) {
        this.quantile = quantile;
        this.value = value;
    }

    public double getQuantile() {
        return quantile;
    }

    public double getValue() {
        return value;
    }

    @Override
    public int compareTo(Quantile o) {
        return Double.compare(quantile, o.quantile);
    }
}
