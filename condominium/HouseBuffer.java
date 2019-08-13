package condominium;

import simulation_src_2019.Buffer;
import simulation_src_2019.Measurement;
import java.util.ArrayList;
import java.util.List;

public class HouseBuffer implements Buffer {

    private final static int MAX_SIZE = 24;
    private List<Measurement> lastMeasurements = new ArrayList<>();

    public void addMeasurement(Measurement measure) {   // poi riguarda come funziona esattamente
        lastMeasurements.add(measure);                    // add lo mette alla fine
        if(lastMeasurements.size() > MAX_SIZE) {
            double tot = 0;
            for(Measurement m:lastMeasurements){
                tot = tot+m.getValue();
            }
            double mean = tot/MAX_SIZE;
            // invia la media con il timestamp
            lastMeasurements.remove(0);
        }
    }
}
