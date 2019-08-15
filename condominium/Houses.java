package condominium;

import java.util.*;

public class Houses {

    private List<House> houseList;
    private static Houses instance;
    Map<Integer,List<Stat>> houseStats = new HashMap<>();
    private List<Stat> globalStats;

    public Houses() {
        houseList = new ArrayList<>();
        globalStats = new ArrayList<>();
    }

    //deve essere un singleton
    public synchronized static Houses getInstance(){
        if(instance==null)
            instance = new Houses();
        return instance;
    }

    public synchronized List<House> getHouseList() { // prendo copia lista
        return new ArrayList<>(houseList);
    }

    public synchronized void addHouse(House h){
        houseList.add(h);
        houseStats.put(h.GetID(), new ArrayList<>());
    }

    public synchronized void removeHouse(House h){
        houseList.remove(h);
        houseStats.remove(h);
    }

    public void addLocalStat(House h, Stat s){
        houseStats.get(h.GetID()).add(s);
        int lastIndex = houseStats.get(h.GetID()).size()-1;
        System.out.println("added "+houseStats.get(h.GetID()).get(lastIndex).GetMean());
    }

    public void addGlobalStat(Stat s){
        globalStats.add(s);
    }

    public Stat[] getLocalStats(int quantity, int houseID){
        Stat[] stats = new Stat[quantity];
        int lastIndex = houseStats.get(houseID).size()-1;
        for(int i = 0; i<quantity; i++){
            stats[i] = houseStats.get(houseID).get(lastIndex-i);
        }
        return stats;
    }

    public Stat[] getGlobalStats(int quantity){
        Stat[] stats = new Stat[quantity];
        int lastIndex = globalStats.size()-1;
        for(int i = 0; i<quantity; i++){
            stats[i] = globalStats.get(lastIndex-i);
        }
        return stats;
    }

    public House getByID(int id){
        List<House> usersCopy = getHouseList();

        for(House h: usersCopy)
            if(h.GetID() == id)
                return h;
        return null;
    }

    public boolean idAlreadyPresent(int id){
        List<House> usersCopy = getHouseList();

        for(House h: usersCopy)
            if(h.GetID() == id)
                return true;
        return false;
    }

}
