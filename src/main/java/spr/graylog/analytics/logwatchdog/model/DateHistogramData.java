package spr.graylog.analytics.logwatchdog.model;
import java.time.LocalDateTime;

public class DateHistogramData {
    private LocalDateTime date;
    private long docCount;

    public DateHistogramData(LocalDateTime date, long docCount) {
        this.date = date;
        this.docCount = docCount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public long getDocCount() {
        return docCount;
    }

    public void setDocCount(long docCount) {
        this.docCount = docCount;
    }
}
