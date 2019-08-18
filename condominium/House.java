package condominium;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import simulation_src_2019.SmartMeterSimulator;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class House {

    @Expose
    private int id;
    @Expose
    private int port;
    @Expose
    private Stat lastStat;
    @Expose
    private boolean coordinator;
    private static House nextInRing;
    private static boolean isNewest;
    private static String serverIP;
    private static List<House> condominiumHouses;
    private static HouseBuffer buffer;
    private static ServerMessages serverMessages;
    private static HouseMessages houseMessages;
    private static HouseSocket houseSocket;
    private static SmartMeterSimulator smartMeter;
    private List<House> housesSendingStat = new ArrayList<>();

    Gson gson;

    public House(int houseID, int housePort, int serverPort, String serverHost) throws IOException, InterruptedException{
        id = houseID;
        port = housePort;
        serverIP = "http://"+serverHost+":"+serverPort;
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        serverMessages = new ServerMessages();
        houseMessages = new HouseMessages();
        buffer = new HouseBuffer(this);
        isNewest = true;

        System.out.println("\nHOUSE "+id+" (port "+port+")");

        // 1 - mi registro nel condominio e ricevo la lista di case
        boolean waitingForServer = true;
        while(waitingForServer){
            try{
                Register();
                waitingForServer = false;
            } catch (IOException e){
                System.out.println("server isn't running");
                Thread.sleep(1000);
            }
        }
        condominiumHouses = serverMessages.AskHouseList(serverIP);

        // 2 - avvio smart meter
        smartMeter = new SmartMeterSimulator(Integer.toString(id), buffer);
        smartMeter.start();
        System.out.println("smart meter running");

        // 3 - mi presento a tutte le altre case
        for(House h : condominiumHouses){
            if(h.GetID() != id) {
                System.out.println("introducing to house " + h.GetID());
                houseMessages.IntroduceTo(h.GetPort(), this);
            }
        }
        System.out.println("finished introducing to the condominium");

        // 4 - mi metto in ascolto
        ServerSocket socket = new ServerSocket(port);
        houseSocket = new HouseSocket(socket, this);

        // 5 - mi inserisco nell'anello
        if(condominiumHouses.size() == 1)
            coordinator = true;
        else
            coordinator = false;
        // di base, la prossima nell'anello per l'ultima arrivata è la casa più vecchia
        nextInRing = serverMessages.AskOldest(serverIP);
        printSituation();
    }

    public int GetID() {
        return id;
    }

    public int GetPort() {
        return port;
    }

    public void AskLastStat(Stat lastStat) throws IOException{
        this.lastStat = lastStat;
//        System.out.println("+++ mean produced: "+lastStat.GetMean()+" ("+lastStat.getTimestamp()+") +++");
        for(House h: condominiumHouses) {
            houseMessages.SendNewStat(h, this);
        }
        serverMessages.SendNewLocalStat(serverIP, this);
    }

    public Stat GetLastStat(){
        return lastStat;
    }

    public synchronized void NewStatFromHouse(House sendingStat) throws IOException{
        // se c'e' gia' la casa in questione, la sostituisco dalla lista mettendo la sua versione piu recente
        for(House h:housesSendingStat){
            if(h.GetID() == sendingStat.GetID()){
                housesSendingStat.remove(h);
                break;
            }
        }
        housesSendingStat.add(sendingStat);
        for(House h: condominiumHouses){
            boolean matchFound = false;
            for(House s:housesSendingStat){
                if(h.GetID() == s.GetID()) {
                    matchFound = true;
                    break;
                }
            }
            if(!matchFound){
                return;
            }
        }
        // se arrivo qui vuol dire che ogni casa del condominio è in lista e allora posso generare il dato finale
        double sum = 0;
        for(House h:housesSendingStat){
            sum = sum + h.GetLastStat().GetMean();
        }
//        System.out.println(">>> total consumes: "+sum+" ("+sendingStat.GetLastStat().getTimestamp()+") <<<\n");
        if(coordinator){
//            System.out.println("as a coordinator, I send the last global stat");
            Stat globalStat = new Stat(sum, sendingStat.GetLastStat().getTimestamp());
            serverMessages.SendNewGlobalStat(serverIP, globalStat);
        }
        housesSendingStat.clear();
    }

    void Register() throws IOException{

        URL url = new URL( serverIP+"/server/addHouse/house");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");

        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        String jsonToServer = gson.toJson(this);
        wr.writeBytes(jsonToServer);
        wr.flush();
        wr.close();
        connection.getResponseCode();               // non so se serve
    }

    public static void main(String[] args) throws IOException, InterruptedException{

        int id = (int)(Math.random()*900+101);          // sempre 3 cifre
        int port = (int)(Math.random()*64535+1001);     // sempre 4 cifre
        new House(id, port, ServerREST.getPort(), ServerREST.getHost());

        Scanner scanner = new Scanner(System.in);
        boolean quit = false;
        while (!quit) {
            System.out.println("\nCommands available:");
            System.out.println("1: quit");
            System.out.println("2: request boost");
            System.out.print("choose a command and press return:\n\n\n");

            String input = "";
            input = scanner.nextLine();
            String[] command = input.split((" "));
            System.out.print("\n");
            switch (command[0]) {
                case "1":
                    System.out.println("quitting");
                    for(House h: condominiumHouses){
                        if(h.GetID() != id)
                            // la casa che l'aveva come next comunicherà al server di cancellarla
                            houseMessages.Quit(h, id);
                    }
                    quit = true;
                    houseSocket.Quit();
                    smartMeter.stopMeGently();
                    break;
//                    case "2":
//                        System.out.println(SendRequest("GET", "/stats/" + cmd[1] + "/" + cmd[2], ""));
//                        break;
                default:
                    System.out.println("Input '" + input + "' not valid.");
                    break;
            }
            System.out.print("\n");
        }
        scanner.close();
        System.out.println("I left the condominium");
        System.exit(0);
    }

    public void AddHouse(House h){
        condominiumHouses.add(h);
        if(isNewest) {
            nextInRing = h;
            isNewest = false;
            System.out.println("new next in ring: "+ nextInRing.GetID());
        }
        System.out.println("\nHouse "+h.GetID()+" added to list");
        printSituation();
    }

    public void RemoveHouse(int id) throws IOException{
        Iterator<House> iter = condominiumHouses.iterator();
        while(iter.hasNext()){
            House h = iter.next();
            if(h.GetID() == id){
                if(nextInRing.GetID() == id){
                    nextInRing = serverMessages.AskNextAndRemove(serverIP, h);
                    System.out.println("My next was removed, new next is "+nextInRing.GetID());
                }
                iter.remove();
                System.out.println("House "+h.GetID()+" removed from list");
                break;
            }
        }
        if(serverMessages.AskOldest(serverIP).GetID() == id) {
            System.out.println("The coordinator was removed, now I am the coordinator");
            coordinator = true;
        }
        printSituation();
    }

    public void printSituation(){
        System.out.print("House list: ");
        for(House h: condominiumHouses){
            System.out.print(h.GetID()+" ");
        }
        System.out.print("\n");
        System.out.println("my next in the ring is "+ nextInRing.GetID());
        if(coordinator) System.out.println("I am the coordinator");
    }

}
