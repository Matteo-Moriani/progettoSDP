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

    //deve essere un singleton
    public synchronized static Houses getInstance(){
        if(instance==null)
            instance = new Houses();
        return instance;
    }

    public synchronized List<House> getHouseList() { // prendo copia lista
        return new ArrayList<>(houseList);
    }

    public synchronized House GetOldest(){
        return oldest;
    }

    public House GetNewNext(int leavingID){
        // nel caso in cui la casa uscente è l'ultima, va bene 0 con indice del successore
        int index = 0;
        Iterator<House> iter = houseList.iterator();
        House newNext = houseList.get(0);
        while(iter.hasNext()){
            House h = iter.next();
            System.out.println("checking if "+h.GetID()+" is equal to "+leavingID);
            if(h.GetID() == leavingID){
                System.out.println("found "+leavingID+" at index "+houseList.indexOf(h));
                //se non stiamo parlando dell'ultima nell'anello
                if(houseList.indexOf(h) != houseList.size()-1)
                    index = houseList.indexOf(h)+1;
                newNext = houseList.get(index);
//                removeHouse(h);
                break;
            }
        }
        return newNext;
    }

    public synchronized void addHouse(House h){
        houseList.add(h);
        houseStats.put(h.GetID(), new ArrayList<>());
        if(houseList.size() == 1)
            oldest = h;
    }

    public synchronized void removeHouse(House h){
        houseList.remove(h);
        houseStats.remove(h);
        if(h.GetID() == oldest.GetID())
            if(houseList.size() > 0)
                // se la casa che ho appena rimosso era anche la più vecchia, la più vecchia ora è quella a indice 0
                oldest = houseList.get(0);
    }

    public void addLocalStat(House h, Stat s){
        houseStats.get(h.GetID()).add(s);
    }

    public void addGlobalStat(Stat s){
        globalStats.add(s);
//        System.out.println("global stats available: "+globalStats.size());
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
