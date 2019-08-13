package condominium;

import simulation_src_2019.Buffer;
import simulation_src_2019.Measurement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HouseBuffer implements Buffer {

    private final static int WINDOW = 24;
    private final static double OVERLAP = 0.5;
    private List<Measurement> lastMeasurements = new ArrayList<>();
    private int counter = 0;
    private House myHouse;
    private Stat lastStat = new Stat(0,0);

    public HouseBuffer(House h){
        myHouse = h;
    }

    public void addMeasurement(Measurement measure) {   // poi riguarda come funziona esattamente
        lastMeasurements.add(measure);                    // add lo mette alla fine
        counter++;

        if (lastMeasurements.size() > WINDOW)
            lastMeasurements.remove(0);             // quello con indice 0 è il meno recente

        if (lastMeasurements.size() == WINDOW) {
            if (counter >= WINDOW * OVERLAP) {              // il maggiore mi serve per la prima volta
                double tot = 0;
                for (Measurement m : lastMeasurements) {
                    tot = tot + m.getValue();
                }
                double mean = tot / WINDOW;
                lastStat.setMean(mean);
                lastStat.setTimestamp(measure.getTimestamp());
                try {
                    myHouse.setLastStat(lastStat);
                } catch (IOException e){
                    e.printStackTrace();
                }
                counter = 0;
            }
        }
    }
}
