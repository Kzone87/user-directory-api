package dev.kzone.portfolio.userapi.dto;

public class AnalyticsBucket {
    private String label;
    private long count;

    public AnalyticsBucket() {
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
