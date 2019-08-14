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
import java.util.List;
import java.util.Scanner;

@Path("admin")
public class AdminClient {

    static ServerMessages messages = new ServerMessages();
    private static String serverIP = "http://"+ServerREST.getHost()+":"+ServerREST.getPort();
    private static final String clientIP = "http://localhost:111";
    private static final String HOST = "localhost";
    private static final int PORT = 111;
    private static Gson gson;

    public static void main(String[] args) throws InterruptedException, IOException {
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        // lo inizializzo come server per poter ricevere notifiche push
        HttpServer admin = HttpServerFactory.create("http://"+HOST+":"+PORT+"/");
        admin.start();

        // mi presento subito al server
        boolean waitingForServer = true;
        while(waitingForServer){
            try {
                messages.addClient(serverIP, clientIP);
                waitingForServer = false;
            } catch (IOException e){
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
            System.out.println("5 <N> <ID>:  get mean and standard deviation of the last N stats for condominium");
            System.out.println("6:           quit\n");
            System.out.print("choose a command and press return: ");

            String input = "";
            input = scanner.nextLine();
            String[] command = input.split((" "));
            System.out.print("\n");
            switch (command[0]) {
                case "1":
                    System.out.println("house list:");
                    List<House> houseList = messages.AskHouseList(serverIP);
                    for(House h:houseList){
                        System.out.print(h.GetID()+" ");
                    }
                    break;
                case "2":
                    Stat[] stats = messages.GetHouseStats(serverIP, Integer.parseInt(command[1]), Integer.parseInt(command[2]));
                    System.out.println("house "+Integer.parseInt(command[2])+" last "+Integer.parseInt((command[1]))+" stats:");
                    for(int i = 0; i<stats.length; i++) {
                        System.out.println("    "+(i+1)+": "+stats[i].GetMean()+" ("+stats[i].getTimestamp()+")");
                    }
                    break;
//                    case "3":
//                        System.out.println(SendRequest("GET", "/stats/" + cmd[1], ""));
//                        break;
//                    case "4":
//                        System.out.println(SendRequest("GET", "/mean-stddev/" + cmd[1] + "/" + cmd[2], ""));
//                        break;
//                    case "5":
//                        System.out.println(SendRequest("GET", "/mean-stddev/" + cmd[1], ""));
//                        break;
                case "6":
                    quit = true;
                    admin.stop(0);
                    break;
                default:
                    System.out.println("Input '" + input + "' not valid.");
                    break;
                }
                System.out.print("\n");
        }
        System.out.println("quitting");
    }

    @Path("house-joined")
    @POST
    @Consumes({"application/json"})
    public Response houseJoined(String jsonHouse){
        House h = gson.fromJson(jsonHouse, House.class);
        System.out.print("\n      NOTIFICATION: house "+h.GetID()+" joined the condominium");
        return Response.ok().build();
    }

    @Path("house-left")
    @POST
    @Consumes({"application/json"})
    public Response houseLeft(String jsonHouse){
        House h = gson.fromJson(jsonHouse, House.class);
        System.out.print("\n      NOTIFICATION: house "+h.GetID()+" left the condominium");
        return Response.ok().build();
    }

    @Path("boost-request")
    @POST
    @Consumes({"application/json"})
    public Response boostRequest(String jsonHouse){
        House h = gson.fromJson(jsonHouse, House.class);
        System.out.print("\n      NOTIFICATION: house "+h.GetID()+" has requested extra energy");
        return Response.ok().build();
    }
}
