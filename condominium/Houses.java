package condominium;

import java.util.*;

public class Houses {

    private List<House> houseList;
    private static Houses instance;
    Map<Integer,List<Stat>> houseStats = new HashMap<>();
    private List<Stat> globalStats;
    private House oldest;

    public Houses() {
        houseList = new ArrayList<>();
        globalStats = new ArrayList<>();
    }

    //singleton
    public synchronized static Houses GetInstance(){
        if(instance==null)
            instance = new Houses();
        return instance;
    }

    public synchronized List<House> GetHouseList() { // prendo copia lista
        return new ArrayList<>(houseList);
    }

    public synchronized House GetOldest(){
        return oldest;
    }

    public House GetNewNext(int houseInNeed){
        // nel caso in cui la casa uscente è l'ultima, va bene 0 con indice del successore
        int index = 0;
        Iterator<House> iter = houseList.iterator();
        House newNext = houseList.get(0);
        while(iter.hasNext()){
            House h = iter.next();
            if(h.GetID() == houseInNeed){
                //se non stiamo parlando dell'ultima nell'anello
                if(houseList.indexOf(h) != houseList.size()-1)
                    index = houseList.indexOf(h)+1;
                newNext = houseList.get(index);
                break;
            }
        }
        return newNext;
    }

    public synchronized void AddHouse(House h){
        houseList.add(h);
        houseStats.put(h.GetID(), new ArrayList<>());
        if(houseList.size() == 1)
            oldest = h;
    }

    public synchronized void RemoveHouse(House h){
        houseList.remove(h);
        houseStats.remove(h);
        if(h.GetID() == oldest.GetID())
            if(houseList.size() > 0)
                // se la casa che ho appena rimosso era anche la più vecchia, la più vecchia ora è quella a indice 0
                oldest = houseList.get(0);
    }

    public void AddLocalStat(House h, Stat s){
        houseStats.get(h.GetID()).add(s);
    }

    public void AddGlobalStat(Stat s){
        globalStats.add(s);
    }

    public Stat[] GetLocalStats(int requestedQuantity, int houseID){
        Stat[] stats = new Stat[requestedQuantity];
        int lastIndex = houseStats.get(houseID).size()-1;
        if(lastIndex+1 >= requestedQuantity) {
            for (int i = 0; i < requestedQuantity; i++) {
                stats[i] = houseStats.get(houseID).get(lastIndex - i);
            }
            return stats;
        } else {
            return null;
        }
    }

    public Stat[] GetGlobalStats(int requestedQuantity){
        Stat[] stats = new Stat[requestedQuantity];
        int lastIndex = globalStats.size()-1;
        if(lastIndex+1 >= requestedQuantity) {
            for (int i = 0; i < requestedQuantity; i++) {
                stats[i] = globalStats.get(lastIndex - i);
            }
            return stats;
        } else {
            return null;
        }
    }

    public House GetByID(int id){
        List<House> usersCopy = GetHouseList();

        for(House h: usersCopy)
            if(h.GetID() == id)
                return h;
        return null;
    }

    public boolean ExistingID(int id){
        List<House> usersCopy = GetHouseList();

        for(House h: usersCopy)
            if(h.GetID() == id)
                return true;
        return false;
    }

}
