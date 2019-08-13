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
    @Path("add/house")
    @POST
    @Consumes({"application/json"})
    public Response addHouse(String jsonHouse){
        House h = gson.fromJson(jsonHouse, House.class);
        if(houses.getInstance().idAlreadyPresent(h.getID())) {
            System.out.println("house "+h.getID()+" already present");
            return Response.serverError().build();                  // scegliere poi l'errore giusto
        }
        else{
            houses.getInstance().add(h);
            System.out.println("house "+h.getID()+" added");
            System.out.println("new list: ");
            for(House i:houses.getInstance().getHouseList()){
                System.out.print(i.getID()+" ");
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

    @Path("remove/house")
    @DELETE
    @Consumes({"application/json"})
    public Response removeHouse(String jsonHouseID) throws IOException{
        int houseID = gson.fromJson(jsonHouseID, int.class);
        House h = houses.getInstance().getByID(houseID);
        if(h!=null){
            houses.getInstance().remove(h);
            System.out.println("house "+h.getID()+" removed");
            System.out.println("new list: ");
            for(House i:houses.getInstance().getHouseList()){
                System.out.print(i.getID()+" ");
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

    @Path("get/{id}")
    @GET
    @Produces({"application/json"})
    public Response getByID(@PathParam("id") int id){
        House h = houses.getInstance().getByID(id);
        if(h!=null)
            return Response.ok(h).build();
        else
            return Response.status(Response.Status.NOT_FOUND).build();
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
}