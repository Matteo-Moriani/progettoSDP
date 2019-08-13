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
    private static String serverIP;
    private static List<House> condominiumHouses;
    private static HouseBuffer buffer;
    private static ServerMessages serverMessages;
    private static HouseMessages houseMessages;
    private static HouseSocket houseSocket;
    private static SmartMeterSimulator smartMeter;
    private Stat lastStat;
    private List<House> HousesSendingStat = new ArrayList<>();

    Gson gson;

    public House(int houseID, int housePort, int serverPort, String serverHost) throws IOException{
        id = houseID;
        port = housePort;
        serverIP = "http://"+serverHost+":"+serverPort;
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        serverMessages = new ServerMessages();
        houseMessages = new HouseMessages();
        buffer = new HouseBuffer(this);

        System.out.println("\nHOUSE "+id+" (port "+port+")");

        // 1 - avvio smart meter
        smartMeter = new SmartMeterSimulator(Integer.toString(id), buffer);
        smartMeter.start();
        System.out.println("smart meter running");

        // 2 - mi registro nel condominio e ricevo la lista di case
        register();
        condominiumHouses = serverMessages.askHouseList(serverIP);
        printHouseList();

        // 3 - mi presento a tutte le altre case
        for(House h : condominiumHouses){
            if(h.getID() != id) {
                System.out.println("introducing to house " + h.getID());
                houseMessages.IntroduceTo(h.getPort(), this);
            }
        }
        System.out.print("finished introducing to the condominium");

        // 4 - mi metto in ascolto
        ServerSocket socket = new ServerSocket(port);
        houseSocket = new HouseSocket(socket, this);

    }

    public int getID() {
        return id;
    }

    public int getPort() {
        return port;
    }

    public void setLastStat(Stat lastStat) throws IOException{
        this.lastStat = lastStat;
        System.out.println("mean produced: "+lastStat.getMean()+" ("+lastStat.getTimestamp()+")");
        for(House h: condominiumHouses) {
            houseMessages.SendNewStat(h, this);
            System.out.println("sending "+id+" to "+h.getID());
        }
    }

    public Stat getLastStat(){
        return lastStat;
    }

    public void NewStatFromHouse(House sendingStat){

        // ogni casa entra una volta sola nella lista
        if(HousesSendingStat.contains(sendingStat))
            return;
        HousesSendingStat.add(sendingStat);

        double sum = 0;
        for(House h: condominiumHouses){
            // se una casa del condominio non ha ancora mandato la statistica, interrompo qui
            if(!HousesSendingStat.contains(h))
                return;
            else
                sum = sum + sendingStat.getLastStat().getMean();
        }
        // se ogni casa del condominio è in lista allora posso generare il dato finale
        System.out.println("total consumes: "+sum+" ("+sendingStat.getLastStat().getTimestamp()+")");
    }

    void register() throws IOException{

        URL url = new URL( serverIP+"/server/add/house");
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

    public static void main(String[] args) throws IOException{

        int id = (int)(Math.random()*900+101);          // sempre 3 cifre
        int port = (int)(Math.random()*64535+1001);     // sempre 4 cifre
        new House(id, port, ServerREST.getPort(), ServerREST.getHost());

        Scanner scanner = new Scanner(System.in);
        boolean quit = false;
        while (!quit) {
            System.out.println("\nCommands available:");
            System.out.println("1: quit");
            System.out.println("2: request boost");
            System.out.print("choose a command and press return: ");

            String input = "";
            input = scanner.nextLine();
            String[] command = input.split((" "));
            System.out.print("\n");
            switch (command[0]) {
                case "1":
                    System.out.println("quitting");
                    serverMessages.Leave(serverIP, id);
                    for(House h: condominiumHouses){
                        if(h.getID() != id)
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
        System.out.println("\nHouse "+h.getID()+" added to list");
        printHouseList();
    }

    public void RemoveHouse(int id) {
        Iterator<House> iter = condominiumHouses.iterator();
        while(iter.hasNext()){
            House h = iter.next();
            if(h.getID() == id){
                iter.remove();
                System.out.println("House "+h.getID()+" removed from list");

            }
        }
        printHouseList();
    }

    public void printHouseList(){
        System.out.println("House list:");
        for(House h: condominiumHouses){
            System.out.print(h.getID()+" ");
        }
        System.out.print("\n");
    }
}
