package condominium;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

@Path("server")
public class ServerREST {

    private static final String HOST = "localhost";
    private static final int PORT = 222;
    private static Gson gson;
    private static Houses houses;
    private static String adminIP;
    private static AdminMessages adminMessages;
    private static final String NO_CLIENT_ERROR = "didn't send the notification: client isn't active";

    public static void main(String[] args) throws IOException {
        adminMessages = new AdminMessages();
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        houses = new Houses();

        HttpServer server = HttpServerFactory.create("http://"+HOST+":"+PORT+"/");
        server.start();

        System.out.println("\nServer running!");
        System.out.println("Server started on: http://"+HOST+":"+PORT);

        System.out.println("Hit return to stop");
        System.in.read();
        server.stop(0);
        System.out.println("Server stopped");
    }

    public static String GetHost(){
        return HOST;
    }

    public static int GetPort(){
        return PORT;
    }

    //restituisce la lista di case
    @Path("list")
    @GET
    @Produces({"application/json"})
    public Response GetHouseList(){
        List<House> list = houses.GetInstance().GetHouseList();
        House[] array = new House[list.size()];

        int i = 0;
        for(House h : list){
            array[i] = h;
            i++;
        }
        String jsonArray = gson.toJson(array);
        return Response.ok(jsonArray).build();
    }

    @Path("oldest")
    @GET
    @Produces({"application/json"})
    public Response GetOldestHouse(){
        House oldest = houses.GetInstance().GetOldest();
        String jsonOldest = gson.toJson(oldest);
        return Response.ok(jsonOldest).build();
    }

    @Path("new-next/{id}")
    @GET
    @Produces({"application/json"})
    public Response GetNewNextHouse(@PathParam("id") int id){
        House newNext = houses.GetInstance().GetNewNext(id);
        String jsonNewNext = gson.toJson(newNext);
        return Response.ok(jsonNewNext).build();
    }

    //permette di inserire una casa se non ce n'e gia una con lo stesso id
    @Path("add-house")
    @POST
    @Consumes({"application/json"})
    public synchronized Response AddHouse(String jsonHouse){
        House h = gson.fromJson(jsonHouse, House.class);
        if(houses.GetInstance().ExistingID(h.GetID())) {
            System.out.println("house "+h.GetID()+" already present");
            return Response.serverError().build();
        }
        else{
            houses.GetInstance().AddHouse(h);
            System.out.println("house "+h.GetID()+" added");
            System.out.println("new list: ");
            for(House i:houses.GetInstance().GetHouseList()){
                System.out.print(i.GetID()+" ");
            }
            System.out.print("\n");
            try {
                adminMessages.Notification(adminMessages.getHouseJoinedMethod(),adminIP, h.GetID());
            } catch (IOException e){
                System.out.println(NO_CLIENT_ERROR);
            }
            return Response.ok().build();
        }
    }

    @Path("remove-house")
    @DELETE
    @Consumes({"application/json"})
    public Response RemoveHouse(String jsonHouseID) {
        // per testare l'entrata nel ring di una casa durante l'uscita del next che gli sta venendo assegnato
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        int houseID = gson.fromJson(jsonHouseID, int.class);
        System.out.println("removing "+houseID);
        House h = houses.GetInstance().GetByID(houseID);
        if(h!=null){
            houses.GetInstance().RemoveHouse(h);
            System.out.println("house "+h.GetID()+" removed");
            System.out.println("new list: ");
            for(House i:houses.GetInstance().GetHouseList()){
                System.out.print(i.GetID()+" ");
            }
            System.out.print("\n");
            try {
                adminMessages.Notification(adminMessages.getHouseLeftMethod(),adminIP, h.GetID());
            } catch (IOException e){
                System.out.println(NO_CLIENT_ERROR);
            }
            return Response.ok().build();
        }
        else
            return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Path("get-local/{n}/{id}")
    @GET
    @Produces({"application/json"})
    public Response GetHouseStats(@PathParam("n") int n, @PathParam("id") int id){
        boolean idExist = houses.GetInstance().ExistingID(id);
        if(!idExist)
            return Response.ok(null).build();
        Stat[] lastStats = houses.GetInstance().GetLocalStats(n, id);
        if(lastStats != null) {
            String jsonStats = gson.toJson(lastStats);
            return Response.ok(jsonStats).build();
        } else {
            return Response.serverError().build();
        }
    }

    @Path("get-global/{n}")
    @GET
    @Produces({"application/json"})
    public Response GetGlobalStats(@PathParam("n") int n){
        Stat[] lastStats = houses.GetInstance().GetGlobalStats(n);
        if(lastStats != null) {
            String jsonStats = gson.toJson(lastStats);
            return Response.ok(jsonStats).build();
        } else {
            return Response.serverError().build();
        }
    }

    @Path("add-client")
    @POST
    @Consumes({"application/json"})
    public Response AddClient(String jsonClientIP){
        String ip = gson.fromJson(jsonClientIP, String.class);
        adminIP = ip;
        System.out.println("client has been initialized (IP "+adminIP+")");
        return Response.ok().build();
    }

    @Path("new-stat/local")
    @POST
    @Consumes({"application/json"})
    public Response NewLocalStat(String jsonHouse){
        House h = gson.fromJson(jsonHouse, House.class);
        houses.GetInstance().AddLocalStat(h,h.GetLastStat());
        return Response.ok().build();
    }

    @Path("new-stat/global")
    @POST
    @Consumes({"application/json"})
    public Response NewGlobalStat(String jsonStat){
        Stat s = gson.fromJson(jsonStat, Stat.class);
        houses.GetInstance().AddGlobalStat(s);
        return Response.ok().build();
    }

    @Path("boost")
    @POST
    @Consumes({"application/json"})
    public Response BoostRequested(String jsonID){
        int id = gson.fromJson(jsonID, int.class);
        try {
            adminMessages.Notification(adminMessages.getBoostRequestMethod(),adminIP, id);
        } catch (IOException e){
            System.out.println(NO_CLIENT_ERROR);
        }
        return Response.ok().build();
    }
}
