package condominium;

public class Stat {
    private double mean;
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
    public double getMean() {
        return mean;
    }
    public void setMean(double mean) {
        this.mean = mean;
    }

}
