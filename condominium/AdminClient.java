package condominium;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Path("admin")
public class AdminClient {

    private static String serverIP = "http://"+ServerREST.GetHost()+":"+ServerREST.GetPort();
    private static final String clientIP = "http://localhost:111";
    private static final String HOST = "localhost";
    private static final int PORT = 111;
    private static Gson gson;
    private static final String NOT_ENOUGH_STATS_ERROR = "There aren't enough stats registered, please request a lower number.";
    private static final String FORMAT_ERROR = "Please follow the right command format";
    static ServerMessages messages = new ServerMessages(serverIP);
    private static String split = messages.GetSplit();

    public static void main(String[] args) throws InterruptedException, IOException {
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        // lo inizializzo come server per poter ricevere notifiche push
        HttpServer admin = HttpServerFactory.create("http://"+HOST+":"+PORT+"/");
        admin.start();

        // mi presento subito al server
        boolean waitingForServer = true;
        while(waitingForServer){
            try {
//                messages.AddClient(serverIP, clientIP);
                messages.MessageToServer(messages.AddClientMethod()+split+clientIP);
                waitingForServer = false;
            } catch (IOException e){
                System.out.println("waiting for server");
                Thread.sleep(1000);
            }
        }
        Scanner scanner = new Scanner(System.in);
        boolean quit = false;
        while (!quit) {
            System.out.println("\nCommands available:");
            System.out.println("1:           get houses list");
            System.out.println("2 <N> <ID>:  get last N stats for house with the given ID");
            System.out.println("3 <N>:       get last N stats for the whole condominium");
            System.out.println("4 <N> <ID>:  get mean and standard deviation of the last N stats for house ID");
            System.out.println("5 <N>:       get mean and standard deviation of the last N stats for condominium");
            System.out.println("6:           quit\n");
            System.out.print("choose a command and press return: ");

            String input = "";
            input = scanner.nextLine();
            String[] command = input.split((" "));
            System.out.print("\n");
            int n, id;
            double mean,standardDev;
            Stat[] stats;

            try {
                switch (command[0]) {
                    case "1":
                        System.out.println("house list:");
                        List<House> houseList = new ArrayList<>();
                        String jsonArray = messages.MessageToServer(messages.AskHouseListMethod()+split+"");
                        House[] array = gson.fromJson(jsonArray, House[].class);
                        for(int i = 0; i<array.length; i++){
                            houseList.add(array[i]);
                        }
                        for(House h:houseList){
                            System.out.print(h.GetID()+" ");
                        }
                        break;
                    case "2":
                        try {
                            n = Integer.parseInt(command[1]);
                            id = Integer.parseInt(command[2]);
//                            stats = messages.GetHouseStats(serverIP, n, id);
                            String jsonStats = messages.MessageToServer(messages.GetHouseStatsMethod()+split+n+split+id);
                            stats = gson.fromJson(jsonStats, Stat[].class);
                            if(stats == null){
                                System.out.println("house " +id+ " doesn't exist");
                                break;
                            }
                            System.out.println("house " + id + " last " + n + " stats:");
                            for (int i = 0; i < stats.length; i++) {
                                System.out.println("    " + (i + 1) + ": " + stats[i].GetMean() + " (" + stats[i].getTimestamp() + ")");
                            }
                        } catch(IOException e) {
                            System.out.println(NOT_ENOUGH_STATS_ERROR);
                        }
                        break;
                    case "3":
                        try {
                            n = Integer.parseInt(command[1]);
//                            stats = messages.GetGlobalStats(serverIP, n);
                            String jsonStats = messages.MessageToServer(messages.GetGlobalStatsMethod()+split+n);
                            stats = gson.fromJson(jsonStats, Stat[].class);
                            System.out.println("last " + n + " global stats:");
                            for (int i = 0; i < stats.length; i++) {
                                System.out.println("    " + (i + 1) + ": " + stats[i].GetMean() + " (" + stats[i].getTimestamp() + ")");
                            }
                        } catch (IOException e){
                            System.out.println(NOT_ENOUGH_STATS_ERROR);
                        }
                        break;
                    case "4":
                        try {
                            n = Integer.parseInt(command[1]);
                            id = Integer.parseInt(command[2]);
//                            stats = messages.GetHouseStats(serverIP, n, id);
                            String jsonStats = messages.MessageToServer(messages.GetHouseStatsMethod()+split+n+split+id);
                            stats = gson.fromJson(jsonStats, Stat[].class);
                            mean = 0;
                            for (int i = 0; i < n; i++) {
                                mean = mean + stats[i].GetMean();
                            }
                            mean = mean / n;
                            standardDev = 0;
                            for (int i = 0; i < n; i++) {
                                standardDev = standardDev + Math.pow(stats[i].GetMean() - mean, 2);
                            }
                            standardDev = Math.pow(standardDev / n, 0.5);
                            System.out.println("mean of house " + id + " last " + n + " stats: " + mean);
                            System.out.println("standard deviation of house " + id + " last " + n + " stats: " + standardDev);
                        } catch(IOException e) {
                            System.out.println(NOT_ENOUGH_STATS_ERROR);
                        }
                        break;
                    case "5":
                        try{
                            n = Integer.parseInt(command[1]);
//                            stats = messages.GetGlobalStats(serverIP, n);
                            String jsonStats = messages.MessageToServer(messages.GetGlobalStatsMethod()+split+n);
                            stats = gson.fromJson(jsonStats, Stat[].class);
                            mean = 0;
                            for(int i=0; i<n; i++){
                                mean = mean + stats[i].GetMean();
                            }
                            mean = mean/n;
                            standardDev = 0;
                            for(int i=0; i<n; i++){
                                standardDev = standardDev + Math.pow(stats[i].GetMean() - mean, 2);
                            }
                            standardDev = Math.pow(standardDev/n, 0.5);
                            System.out.println("mean of last "+n+" global stats: "+mean);
                            System.out.println("standard deviation of last "+n+" global stats: "+standardDev);
                        } catch(IOException e) {
                            System.out.println(NOT_ENOUGH_STATS_ERROR);
                        }
                        break;
                    case "6":
                        quit = true;
                        admin.stop(0);
                        break;
                    default:
                        System.out.println(FORMAT_ERROR);
                        break;
                }
                System.out.print("\n");
            } catch (NumberFormatException e){
                System.out.println(FORMAT_ERROR);
            }
        }
        System.out.println("quitting");
        System.exit(0);
    }

    @Path("house-joined")
    @POST
    @Consumes({"application/json"})
    public Response houseJoined(String jsonID){
        int id = gson.fromJson(jsonID, int.class);
        System.out.print("\n      NOTIFICATION: house "+id+" joined the condominium");
        return Response.ok().build();
    }

    @Path("house-left")
    @POST
    @Consumes({"application/json"})
    public Response houseLeft(String jsonID){
        int id = gson.fromJson(jsonID, int.class);
        System.out.print("\n      NOTIFICATION: house "+id+" left the condominium");
        return Response.ok().build();
    }

    @Path("boost")
    @POST
    @Consumes({"application/json"})
    public Response BoostRequested(String jsonID){
        int id = gson.fromJson(jsonID, int.class);
        System.out.print("\n      NOTIFICATION: house "+id+" requested a boost");
        return Response.ok().build();
    }
}
