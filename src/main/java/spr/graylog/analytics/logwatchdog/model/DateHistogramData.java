package spr.graylog.analytics.logwatchdog.model;

public class DateHistogramData {
    private String date;
    private long docCount;

    public DateHistogramData(String date, long docCount) {
        this.date = date;
        this.docCount = docCount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getDocCount() {
        return docCount;
    }

    public void setDocCount(long docCount) {
        this.docCount = docCount;
    }
}
