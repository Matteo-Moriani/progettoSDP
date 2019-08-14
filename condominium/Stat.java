package condominium;

import com.google.gson.annotations.Expose;

public class Stat {
    @Expose
    private double mean;
    @Expose
    private long timestamp;

    public Stat(double mean, long timestamp){
        this.mean = mean;
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public double GetMean() {
        return mean;
    }
    public void setMean(double mean) {
        this.mean = mean;
    }

}
