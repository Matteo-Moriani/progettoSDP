package condominium;

import java.util.ArrayList;
import java.util.List;

public class Houses {

    private List<House> houseList;
    private static Houses instance;

    public Houses() {
        houseList = new ArrayList<House>();
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

    public synchronized void add(House h){
        houseList.add(h);
    }

    public synchronized void remove(House h){
        houseList.remove(h);
    }

    public House getByID(int id){
        List<House> usersCopy = getHouseList();

        for(House h: usersCopy)
            if(h.getID() == id)
                return h;
        return null;
    }

    public boolean idAlreadyPresent(int id){
        List<House> usersCopy = getHouseList();

        for(House h: usersCopy)
            if(h.getID() == id)
                return true;
        return false;
    }

}
