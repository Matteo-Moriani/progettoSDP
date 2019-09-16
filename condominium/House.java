package condominium;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import simulation_src_2019.SmartMeterSimulator;
import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.util.*;

public class House {

    @Expose
    private int id;
    @Expose
    private int port;
    @Expose
    private Stat lastStat;

    private static boolean coordinator;
    private House nextInRing;
    private static String serverIP;
    private List<House> houseList = new ArrayList<>();
    private static HouseBuffer buffer;
    private static ServerMessages serverMessages;
    private static HouseMessages houseMessages;
    private HouseSocketThread houseSocket;
    private TokenThread tokenThread;
    private static SmartMeterSimulator smartMeter;
    private List<House> housesSendingStat = new ArrayList<>();
    private static boolean boosting;
    private static boolean staticQuitting = false;
    private static final int TOTAL_TOKENS = 2;
    private boolean newStatReady = false;
    private static String split;
    private HashMap<Integer,Boolean> confirmations = new HashMap();
    Gson gson;

    public House(int houseID, int housePort, int serverPort, String serverHost) throws IOException, InterruptedException{
        id = houseID;
        port = housePort;
        serverIP = "http://"+serverHost+":"+serverPort;
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        serverMessages = new ServerMessages(serverIP);
        houseMessages = new HouseMessages();
        split = houseMessages.getSplit();
        buffer = new HouseBuffer(this);
        boosting = false;

        System.out.println("\nHOUSE "+id+" (port "+port+")");

        // 1 - mi registro appena in server è pronto, chiudo se il mio id è già presente
        boolean waitingForServer = true;
        while(waitingForServer){
            try{
                serverMessages.MessageToServer(serverMessages.RegisterMethod()+split+gson.toJson(this));
                waitingForServer = false;
                System.out.println("connection with server established");
            } catch (IOException e){
                System.out.println("server isn't running, retrying...");
                Thread.sleep(1000);
            }
        }

        // 2 - ricevo la lista di case
        String jsonArray = serverMessages.MessageToServer(serverMessages.AskHouseListMethod());
        House[] array = gson.fromJson(jsonArray, House[].class);
        for(int i = 0; i<array.length; i++){
            houseList.add(array[i]);
        }

        // 3 - avvio smart meter
        smartMeter = new SmartMeterSimulator(Integer.toString(id), buffer);
        smartMeter.setName("Smart Meter");
        smartMeter.start();
        System.out.println("smart meter running");

        // 4 - mi metto in ascolto, tra subito dopo l'arrivo della lista e subito prima che mi metto in ascolto
        // qualcuno esce, lo scoprirò nel presentarmi a chi è uscito
//        Thread.sleep(10000);
        ServerSocket socket = new ServerSocket(port);
        houseSocket = new HouseSocketThread(socket, this);

        // 5 - mi presento a tutte le altre case ed elimino chi è uscito mentre mi mettevo in ascolto
        for(House h : GetList()){
            if(h.GetID() != id) {
                System.out.println("introducing to house " + h.GetID());
                int attempt = 0;
                while(attempt < 10) {
                    try {
//                        houseMessages.IntroduceTo(h.GetPort(), this);
                        houseMessages.SendMessage(
                                houseMessages.IntroduceMethod()+split+gson.toJson(h)+split+gson.toJson(this)
                        );
                        break;
                    } catch (ConnectException e) {
                        System.out.println("Can't connect with house "+h.GetID()+". Trying again");
                        Thread.sleep(1000);
                        attempt++;
                    }
                }
                if(attempt == 3) {
                    System.out.println("House "+h.GetID()+" has left while introducing myself to it. Removing it from my list");
                    synchronized (houseList) {
                        houseList.remove(h);
                    }
                }
            }
        }
        System.out.println("finished introducing to the condominium");

        // 6 - mi inserisco nell'anello, la lista di case è sincronizzata quindi dovrebbe essere tutto ok
        if(GetList().size() == 1) {
            coordinator = true;
        } else if (GetList().size() > 1 && GetList().size() <= TOTAL_TOKENS){
            coordinator = false;
        } else {
            // da 3 case in su nel nostro caso
            coordinator = false;
        }
        // di base, la prossima nell'anello per l'ultima arrivata è la casa più vecchia
        // se askOldest avviene dopo che la mia imminente next è uscita e prima che quella casa riesca a comunicarlo al server,
        // controllo se il next che mi arriva sia effettivamente tra le case in lista, se non lo è ne chiedo un altro.
        boolean validNext = false;
        while(!validNext){
//            System.out.println("(DEBUG) fai uscire ora la casa");
//            Thread.sleep(5000);

            String nextInRingJson = serverMessages.MessageToServer(serverMessages.AskOldestMethod());
            nextInRing = gson.fromJson(nextInRingJson, House.class);

            for(House h:GetList()){
                if(h.GetID() == nextInRing.GetID()){
                    validNext = true;
                    break;
                }
            }
            if(!validNext) {
                System.out.println("next in ring received from server have left the condominium. asking again to server");
                Thread.sleep(500);
            }
        }

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

    public String getSplit(){return split;}

    public double GetTime(long timestamp){
        long r = timestamp/100000000;
        return (double)r/10.0;
    }

    public synchronized List<House> GetList(){
        return houseList;
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
        newStatReady = true;
        System.out.println("mean produced: "+lastStat.GetMean()+" ("+lastStat.getTimestamp()+")");
        // mando la comunicazione di nuova stat prodotta alle altre case
        for(House h: GetList()) {
            if(h.GetID() != id)
//                houseMessages.SendNewStat(h, this);
                houseMessages.SendMessage(
                        houseMessages.NewStatMethod()+split+gson.toJson(h)+split+gson.toJson(this)
                );
        }
        // lo dico anche a me stessa per triggerare la creazione della stat globale,
        // questo serve nel caso in cui io sia il coordinatore e l'ultima casa ad aver prodotto la stat
        NewStatFromHouse(this);
        // e poi mando al server
        serverMessages.MessageToServer(serverMessages.SendNewLocalStatMethod()+split+gson.toJson(this));
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
        if(sendingStat.GetID() != id)
            housesSendingStat.add(sendingStat);
        // controllo che siano arrivate le ultime stat da tutte le case (tranne che da me, lì uso un boolean)
        for(House h: GetList()){
            if(h.GetID() != id) {
                boolean matchFound = false;
                for (House s : housesSendingStat) {
                    if (h.GetID() == s.GetID()) {
                        matchFound = true;
                        break;
                    }
                }
                if (!matchFound) {
                    return;
                }
            }
        }
        if(!newStatReady)
            return;

        // se arrivo qui vuol dire che ogni casa del condominio è in lista e allora posso generare il dato finale
        double sum = 0;
        for(House h:housesSendingStat){
            sum = sum + h.GetLastStat().GetMean();
        }
        sum = sum + lastStat.GetMean();

        System.out.println("total consumes: "+sum+" ("+sendingStat.GetLastStat().getTimestamp()+")");
        if(coordinator){
            System.out.println(">>> sending the last global stat to the server <<<\n");
            Stat globalStat = new Stat(sum, sendingStat.GetLastStat().getTimestamp());
            serverMessages.MessageToServer(serverMessages.SendNewGlobalStatMethod()+split+gson.toJson(globalStat));
        } else {
            System.out.println();
        }
        housesSendingStat.clear();
        newStatReady = false;
    }

    public static void main(String[] args) throws IOException, InterruptedException{

        int id = (int)(Math.random()*900+100);          // sempre 3 cifre
        int port = (int)(Math.random()*64535+1000);     // sempre 4 cifre
        House myself = new House(id, port, ServerREST.GetPort(), ServerREST.GetHost());

        Scanner scanner = new Scanner(System.in);
        while (!staticQuitting) {
            System.out.println("\nCommands available:");
            System.out.println("1: quit");
            System.out.println("2: request boost");
            System.out.print("choose a command and press return:\n\n");

            String input = "";
            input = scanner.nextLine();
            String[] command = input.split((" "));
            System.out.print("\n");
            switch (command[0]) {
                case "1":
                    myself.Quit();
                    break;
                case "2":
                    myself.Boost();
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

    public void Boost() throws IOException{
        if(!staticQuitting) {
            if (!tokenThread.BoostRequested()) {
                System.out.println("boost requested");
                tokenThread.SetWantsBoost(true);
                serverMessages.MessageToServer(serverMessages.BoostRequestedMethod()+split+id);
                synchronized (tokenThread) {
                    tokenThread.notify();
                }
            } else {
                System.out.println("boost request already pending");
            }
        } else {
            System.out.println("I'm quitting, boost request refused");
        }
    }

    public void Quit() throws IOException, InterruptedException{
        // 1 - non accetto più input da parte dell'utente
        staticQuitting = true;

        System.out.println("quitting...");
        smartMeter.stopMeGently();
        System.out.println("smart meter stopped");

        // 2 - se stavo aspettando un boost ma ancora non è iniziato, non lo voglio più
        if(tokenThread.BoostRequested()) {
            tokenThread.SetWantsBoost(false);
            System.out.println("deleting pending boost request");
        }

        // 3 - gestisco coordinatore
        if (coordinator) {
            Elect();
        }

        // 4 - esco dalle liste, voglio che si cancellino dalle liste una alla volta
        synchronized (houseList) {
            houseList.remove(this);
        }
        List<House> temp = new ArrayList<>();
        temp.addAll(GetList());

        // mi serve rimuovere prima la casa dal server e poi dalle case per askNewNext
        serverMessages.MessageToServer(serverMessages.RemoveMethod()+split+id);
        for (House h : temp) {
//            Thread.sleep(5000);
            int attempt = 0;
            System.out.println("telling house " + h.GetID() + " to remove me from its list");
            while(attempt<3) {
                try {
                    houseMessages.SendMessage(
                            houseMessages.QuitMethod()+split+gson.toJson(h)+split+gson.toJson(id)
                    );
                    break;
                } catch (ConnectException e) {
                    System.out.println("House "+h.GetID()+" may have quit, retrying again...");
                    Thread.sleep(500);
                    attempt++;
                }
            }
            if(attempt==3)
                System.out.println("House "+h.GetID()+" have quit, moving on");
        }

        WaitForConfirmations();

        // 5 - i thread token e houseSocket escono dai loro cicli while, aspetto un paio di secondi
        // in caso di messaggi particolarmente in ritardo
        tokenThread.setQuitting(true);
        houseSocket.setQuitting(true);
        Thread.sleep(2000);

        // 6 - se avevo un token e rimangono almeno altre due case, lo mando al mio next
        if (tokenThread.HoldingToken() && GetList().size() >= TOTAL_TOKENS) {
            boolean sent = false;
            while(!sent) {
                try {
                    System.out.println("sending token before quitting");
                    houseMessages.SendMessage(
                            houseMessages.SendTokenMethod()+split+gson.toJson(nextInRing)
                    );
                    sent = true;
                } catch (ConnectException e) {
                    synchronized (this){
                        System.out.println("my next quit. waiting for a new next to send my token to");
                        wait();
                    }
                }
            }
        }

        // 7 - se stavo boostando, faccio finire così poi mando il token al next
        while (boosting) {
            synchronized (this) {
                System.out.println("waiting boost to end to release and pass the token");
                wait();
            }
        }
    }

    private void WaitForConfirmations(){
        for(House h : houseList){
            if(h.GetID() != id)
                confirmations.put(h.GetID(), false);
        }
        if(confirmations.size()!=0) {
            try {
                System.out.println("waiting for everyone to remove me from their list");
                synchronized (this) {
                    wait();
                }
                System.out.println("I was removed from everyone's list");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void CheckConfirmations(){
        if(confirmations.size() != 0){
            boolean everyoneConfirmed = true;
            for(boolean b : confirmations.values()){
                if(b == false){
                    everyoneConfirmed = false;
                }
            }
            if(everyoneConfirmed){
                synchronized (this){
                    notify();
                }
            }
        }
    }

    public void AddHouse(House h) throws IOException{
        // controllo non ci sia già la casa in questione. può capitare se si presenta dopo che ho ricevuto
        // una lista di case già aggiornata
        if(staticQuitting){
            if(!confirmations.containsKey(h.GetID())){
                confirmations.put(h.GetID(),false);
            }
            houseMessages.SendMessage(houseMessages.QuitMethod()+split+gson.toJson(h)+split+gson.toJson(id));
        }

        boolean alreadyPresent = false;
        for(House i:GetList()){
            if(i.GetID() == h.GetID())
                alreadyPresent = true;
        }
        if(!alreadyPresent) {
            synchronized (houseList) {
                houseList.add(h);
            }
            // se io sono la penultima arrivata, allora l'ultima arrivata è la mia nuova next
            try {
                String jsonArray = serverMessages.MessageToServer(serverMessages.AskHouseListMethod());
                House[] array = gson.fromJson(jsonArray, House[].class);
                List<House> temp = new ArrayList<>();
                for(int i = 0; i<array.length; i++){
                    temp.add(array[i]);
                }
                if (temp.get(GetList().size() - 2).GetID() == id) {
                    nextInRing = h;
                    System.out.println("new next in ring: " + nextInRing.GetID());
                }
            }catch(IOException e){
                e.printStackTrace();
            }
            System.out.println("\nHouse "+h.GetID()+" added to list");
            printSituation();
            if(GetList().size() > TOTAL_TOKENS) {
                synchronized (tokenThread) {
                    tokenThread.notify();
                    System.out.println("there are more than "+ TOTAL_TOKENS +" houses, tokens are cycling.");
                }
            }
        }
    }

    public void RemoveHouse(int leavingID) throws IOException{
        Iterator<House> iter = GetList().iterator();
        while(iter.hasNext()){
            House h = iter.next();
            if(h.GetID() == leavingID){
                // che succede se una casa va via quando non ho ancora un next? salto la ricerca del prossimo next
                try {
                    if (nextInRing.GetID() == leavingID && leavingID != id) {
                        // mettiamoci tanto a capire che il next sta uscendo
//                        try { Thread.sleep(10000); } catch(InterruptedException e){ e.printStackTrace(); }

                        int attempts = 0;
                        while(attempts<3) {
                            try {
                                String houseJson = serverMessages.MessageToServer(serverMessages.AskNextMethod() + split + id);
                                nextInRing = gson.fromJson(houseJson, House.class);
                                break;
                            } catch (IOException e) {
                                System.out.println("couldn't find a next in the ring, retrying...");
                                attempts++;
                                try { Thread.sleep(500); } catch(InterruptedException e1){ e1.printStackTrace(); }
                            }
                        }
                        if(attempts != 3){
                            System.out.println("My next was removed, new next is " + nextInRing.GetID());
                            synchronized (this) {
                                notify();
                            }
                        }
                    }
                } catch(NullPointerException e){
                    System.out.println("Next in the ring not yet initialized, ignoring the request for a new next");
                }
                iter.remove();
                System.out.println("House "+h.GetID()+" removed from my list");
                if(confirmations.size() != 0) {
                    confirmations.remove(leavingID);
                    CheckConfirmations();
                }

                houseMessages.SendMessage(houseMessages.OkMethod()+split+gson.toJson(h)+split+gson.toJson(id));
                if(h.GetID() != id)
                    System.out.println("Confirmed to house "+h.GetID()+" I removed it from my list");

                break;
            }
        }
    }

    public void printSituation(){
        System.out.print("House list: ");
        for(House h: GetList()){
            System.out.print(h.GetID()+" ");
        }
        System.out.print("\n");
        System.out.println("my next in the ring is "+ nextInRing.GetID());
        if(coordinator)
            System.out.println("I am the coordinator");
    }

    public void SetCoordinator(){
        if(!staticQuitting) {
            coordinator = true;
            System.out.println("\n+++ I was elected as the new coordinator +++\n");
        } else {
            Elect();
        }
    }

    public void ReceiveConfirmation(int id){
        confirmations.replace(id, true);
        CheckConfirmations();
        System.out.println("I was removed from "+id+" list.");
    }

    public void Elect(){
        // provare uscita contemporanea coordinatore e suo next
//            Thread.sleep(10000);
        boolean elected = false;
        while(!elected) {
            // se sarei io quello da eleggere, allora non importa
            if(nextInRing.GetID() == id)
                break;
            try {
                System.out.println("electing " + nextInRing.GetID() + " as the new coordinator");
                houseMessages.SendMessage(
                        houseMessages.ElectMethod()+split+gson.toJson(nextInRing)
                );
                elected = true;
            } catch (IOException e) {                   // prima era solo ConnectException
                synchronized (this){
                    System.out.println("my next quit. waiting for a new next to elect");
                    try {
                        wait();
                    } catch (InterruptedException e1){e1.printStackTrace();}
                }
            }
        }
    }

}
