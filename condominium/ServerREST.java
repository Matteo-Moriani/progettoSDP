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
    private static ServerMessages messages;

    public static void main(String[] args) throws IOException {
        messages = new ServerMessages();
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

    public static String getHost(){
        return HOST;
    }

    public static int getPort(){
        return PORT;
    }

    //restituisce la lista di case
    @Path("list")
    @GET
    @Produces({"application/json"})
    public Response getHouseList(){
        List<House> list = houses.getInstance().getHouseList();
        House[] array = new House[list.size()];

        int i = 0;
        for(House h : list){
            array[i] = h;
            i++;
        }
        String jsonArray = gson.toJson(array);
        return Response.ok(jsonArray).build();
    }

    //permette di inserire una casa se non ce n'e gia una con lo stesso id
    @Path("addHouse/house")
    @POST
    @Consumes({"application/json"})
    public Response addHouse(String jsonHouse){
        House h = gson.fromJson(jsonHouse, House.class);
        if(houses.getInstance().idAlreadyPresent(h.GetID())) {
            System.out.println("house "+h.GetID()+" already present");
            return Response.serverError().build();                  // scegliere poi l'errore giusto
        }
        else{
            houses.getInstance().addHouse(h);
            System.out.println("house "+h.GetID()+" added");
            System.out.println("new list: ");
            for(House i:houses.getInstance().getHouseList()){
                System.out.print(i.GetID()+" ");
            }
            System.out.print("\n");
            try {
                messages.HouseJoined(adminIP, h);
            } catch (IOException e){
                System.out.println("client isn't active");
            }
            return Response.ok().build();
        }
    }

    @Path("removeHouse/house")
    @DELETE
    @Consumes({"application/json"})
    public Response removeHouse(String jsonHouseID) throws IOException{
        int houseID = gson.fromJson(jsonHouseID, int.class);
        House h = houses.getInstance().getByID(houseID);
        if(h!=null){
            houses.getInstance().removeHouse(h);
            System.out.println("house "+h.GetID()+" removed");
            System.out.println("new list: ");
            for(House i:houses.getInstance().getHouseList()){
                System.out.print(i.GetID()+" ");
            }
            System.out.print("\n");
            try {
                messages.HouseLeft(adminIP, h);
            } catch (IOException e){
                System.out.println("client isn't active");
            }
            return Response.ok().build();
        }
        else
            return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Path("get/{n}/{id}")
    @GET
    @Produces({"application/json"})
    public Response getHouseStats(@PathParam("n") int n, @PathParam("id") int id){
        Stat[] lastStats = houses.getInstance().getLocalStats(n, id);
        String jsonStats = gson.toJson(lastStats);
        return Response.ok(jsonStats).build();
    }

    @Path("get/{n}")
    @GET
    @Produces({"application/json"})
    public Response getGlobalStats(@PathParam("n") int n){
        Stat[] lastStats = houses.getInstance().getGlobalStats(n);
        String jsonStats = gson.toJson(lastStats);
        return Response.ok(jsonStats).build();
    }

    @Path("add/client")
    @POST
    @Consumes({"application/json"})
    public Response addClient(String jsonClientIP){
        String ip = gson.fromJson(jsonClientIP, String.class);
        adminIP = ip;
        System.out.println("client has been initialized (IP "+adminIP+")");
        return Response.ok().build();
    }

    @Path("new-stat/local")
    @POST
    @Consumes({"application/json"})
    public Response newLocalStat(String jsonHouse){
        House h = gson.fromJson(jsonHouse, House.class);
        houses.getInstance().addLocalStat(h,h.GetLastStat());
        return Response.ok().build();
    }

    @Path("new-stat/global")
    @POST
    @Consumes({"application/json"})
    public Response newGlobalStat(String jsonStat){
        Stat s = gson.fromJson(jsonStat, Stat.class);
        houses.getInstance().addGlobalStat(s);
        return Response.ok().build();
    }
}
