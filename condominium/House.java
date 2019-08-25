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

    private static boolean coordinator;
    private static House nextInRing;
    private static String serverIP;
    private static List<House> condominiumHouses;
    private static HouseBuffer buffer;
    private static ServerMessages serverMessages;
    private static HouseMessages houseMessages;
    private static HouseSocketThread houseSocket;
    private static TokenThread tokenThread;
    private static SmartMeterSimulator smartMeter;
    private List<House> housesSendingStat = new ArrayList<>();
    private static boolean boosting;
    private static boolean quitting = false;
    private static final int TOTAL_TOKENS = 2;

    Gson gson;

    public House(int houseID, int housePort, int serverPort, String serverHost) throws IOException, InterruptedException{
        id = houseID;
        port = housePort;
        serverIP = "http://"+serverHost+":"+serverPort;
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        serverMessages = new ServerMessages();
        houseMessages = new HouseMessages();
        buffer = new HouseBuffer(this);
        boosting = false;

        System.out.println("\nHOUSE "+id+" (port "+port+")");

        // 1 - ricevo la lista di case appena il server è pronto
        boolean waitingForServer = true;
        while(waitingForServer){
            try{
                condominiumHouses = serverMessages.AskHouseList(serverIP);
                waitingForServer = false;
                System.out.println("connection with server established");
            } catch (IOException e){
                System.out.println("server isn't running, retrying...");
                Thread.sleep(1000);
            }
        }

        // 2 - mi registro se non ci sono case col mio id
        for(House h:condominiumHouses){
            if(h.GetID() == id) {
                System.out.println("there's already a house with my id");
                System.exit(0);
            }
        }
        Register();
        condominiumHouses.add(this);

        // 3 - avvio smart meter
        smartMeter = new SmartMeterSimulator(Integer.toString(id), buffer);
        smartMeter.setName("Smart Meter");
        smartMeter.start();
        System.out.println("smart meter running");

        // 4 - mi presento a tutte le altre case
        for(House h : condominiumHouses){
            if(h.GetID() != id) {
                System.out.println("introducing to house " + h.GetID());
                houseMessages.IntroduceTo(h.GetPort(), this);
            }
        }
        System.out.println("finished introducing to the condominium");

        // 5 - mi metto in ascolto
        ServerSocket socket = new ServerSocket(port);
        houseSocket = new HouseSocketThread(socket, this);

        // 6 - mi inserisco nell'anello
        if(condominiumHouses.size() == 1) {
            coordinator = true;
        } else if (condominiumHouses.size() > 1 && condominiumHouses.size() <= TOTAL_TOKENS){
            coordinator = false;
        } else {
            // da 3 case in su nel nostro caso
            coordinator = false;
        }
        // di base, la prossima nell'anello per l'ultima arrivata è la casa più vecchia
        nextInRing = serverMessages.AskOldest(serverIP);
        tokenThread = new TokenThread(this);
        tokenThread.start();
        printSituation();
    }

    public void SetBoosting(boolean b){
        boosting = b;
    }

    public int GetID() {
        return id;
    }

    public int GetPort() {
        return port;
    }

    public List<House> GetList(){
        return condominiumHouses;
    }

    public TokenThread GetTokenThread(){
        return tokenThread;
    }

    public SmartMeterSimulator GetSmartMeter(){
        return smartMeter;
    }

    public House GetNextInRing(){
        return nextInRing;
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

    public boolean IsQuitting(){
        return quitting;
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

        URL url = new URL( serverIP+"/server/add-house");
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
        House myself = new House(id, port, ServerREST.getPort(), ServerREST.getHost());

        Scanner scanner = new Scanner(System.in);
        while (!quitting) {
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
                    myself.Quit();
                    break;
                case "2":
                    if(!quitting) {
                        if (!tokenThread.BoostRequested()) {
                            System.out.println("requesting boost, waiting for a token");
                            tokenThread.SetWantsBoost(true);
                            serverMessages.BoostRequested(serverIP, id);
                            synchronized (tokenThread) {
                                tokenThread.notify();
                            }
                        } else {
                            System.out.println("boost request already pending");
                        }
                    } else {
                        System.out.println("I'm quitting, boost request refused");
                    }
                    break;
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

    public void Quit() throws IOException, InterruptedException{
        System.out.println("quitting...");

        // 1 - i thread token e houseSocket escono dai loro cicli while
        quitting = true;

        // 2 - se stavo aspettando un boost ma ancora non è iniziato, non lo voglio più
        if(tokenThread.BoostRequested()) {
            tokenThread.SetWantsBoost(false);
            System.out.println("deleting pending boost request");
        }

        // 3 - mi cancello ora dalle liste perché poi andrò in wait, inoltre gestisco coordinatore e token
        // voglio che si cancellino dalle liste una alla volta, così basta?
        serverMessages.Remove(serverIP, id);
        synchronized (this) {
            for (House h : condominiumHouses) {
                System.out.println("telling house " + h.GetID() + " to remove me from its list");
                houseMessages.Remove(h, id);
            }
            // se ero il coordinatore e non ci sono solo io eleggo il mio next
            if (coordinator && nextInRing.GetID() != id) {
                System.out.println("electing " + nextInRing.GetID() + " as the new coordinator");
                houseMessages.Elect(nextInRing);
            }
            // se avevo un token e rimangono almeno altre due case, lo mando al mio next
            if (tokenThread.HoldingToken() && condominiumHouses.size() >= TOTAL_TOKENS)
                houseMessages.SendToken(nextInRing);
        }

        //4 - se stavo boostando, faccio finire così poi mando il token al next
        while (boosting) {
            synchronized (this) {
                System.out.println("waiting boost to end to release and pass the token");
                wait();
                System.out.println("boost ended and token sent, now I can quit");
            }
        }
        smartMeter.stopMeGently();
        System.out.println("smart meter stopped");
        System.out.println("I quit successfully");
    }

    public void AddHouse(House h){
        condominiumHouses.add(h);
        // se io sono la penultima arrivata, allora l'ultima arrivata è la mia nuova next
        try {
            if (serverMessages.AskHouseList(serverIP).get(condominiumHouses.size() - 2).GetID() == id) {
                nextInRing = h;
                System.out.println("new next in ring: " + nextInRing.GetID());
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        System.out.println("\nHouse "+h.GetID()+" added to list");
        printSituation();
        if(condominiumHouses.size() > TOTAL_TOKENS) {
            synchronized (tokenThread) {
                tokenThread.notify();
                System.out.println("there are more than "+ TOTAL_TOKENS +" houses, tokens are cycling.");
            }
        }
    }

    public void RemoveHouse(int leavingID) throws IOException{
        Iterator<House> iter = condominiumHouses.iterator();
        while(iter.hasNext()){
            House h = iter.next();
            if(h.GetID() == leavingID){
                if(nextInRing.GetID() == leavingID && condominiumHouses.size() > 1){
                    nextInRing = serverMessages.AskNext(serverIP, h);
                    System.out.println("My next was removed, new next is "+nextInRing.GetID());
                }
                iter.remove();
                System.out.println("House "+h.GetID()+" removed from list");
                break;
            }
        }
        if(leavingID!=id)
            printSituation();
    }

    public void printSituation(){
        System.out.print("House list: ");
        for(House h: condominiumHouses){
            System.out.print(h.GetID()+" ");
        }
        System.out.print("\n");
        System.out.println("my next in the ring is "+ nextInRing.GetID());
        if(coordinator)
            System.out.println("I am the coordinator");
    }

    public void SetCoordinator(){
        coordinator = true;
        System.out.println("now I am the coordinator");
    }

}
