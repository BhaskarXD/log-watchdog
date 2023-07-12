package spr.graylog.analytics.logwatchdog.model;

public class PredictionData {
    private String ds;
    private double yhat;
    private double yhat_lower;
    private double yhat_upper;

    public PredictionData() {
    }

    public PredictionData(String ds, double yhat, double yhat_lower, double yhat_upper) {
        this.ds = ds;
        this.yhat = yhat;
        this.yhat_lower = yhat_lower;
        this.yhat_upper = yhat_upper;
    }

    public String getDs() {
        return ds;
    }

    public void setDs(String ds) {
        this.ds = ds;
    }

    public double getYhat() {
        return yhat;
    }

    public void setYhat(double yhat) {
        this.yhat = yhat;
    }

    public double getYhat_lower() {
        return yhat_lower;
    }

    public void setYhat_lower(double yhat_lower) {
        this.yhat_lower = yhat_lower;
    }

    public double getYhat_upper() {
        return yhat_upper;
    }

    public void setYhat_upper(double yhat_upper) {
        this.yhat_upper = yhat_upper;
    }

    @Override
    public String toString() {
        return "PredictionData{" +
                "ds=" + ds +
                ", yhat=" + yhat +
                ", yhat_lower=" + yhat_lower +
                ", yhat_upper=" + yhat_upper +
                '}';
    }
}
