package condominium;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ServerMessages {

    Gson gson;
    private String serverIP;
    
    private final String SPLIT = "SEPARATOR-FOR-MESSAGES";
    private final String ASK_HOUSE_LIST_METHOD = "list";
    private final String ASK_OLDEST_METHOD = "oldest";
    private final String ASK_NEXT_METHOD = "new-next";
    private final String GET_HOUSE_STATS_METHOD = "get-local";
    private final String GET_GLOBAL_STATS_METHOD = "get-global";
    private final String ADD_CLIENT_METHOD = "add-client";
    private final String REMOVE_METHOD = "remove-house";
    private final String SEND_NEW_LOCAL_STAT_METHOD = "new-stat/local";
    private final String SEND_NEW_GLOBAL_STAT_METHOD = "new-stat/global";
    private final String BOOST_REQUESTED_METHOD = "boost";
    private final String REGISTER_METHOD = "add-house";

    public String AskHouseListMethod() {
        return ASK_HOUSE_LIST_METHOD;
    }
    public String AskOldestMethod() {
        return ASK_OLDEST_METHOD;
    }
    public String AskNextMethod() {
        return ASK_NEXT_METHOD;
    }
    public String GetHouseStatsMethod() {
        return GET_HOUSE_STATS_METHOD;
    }
    public String GetGlobalStatsMethod() {
        return GET_GLOBAL_STATS_METHOD;
    }
    public String AddClientMethod() {
        return ADD_CLIENT_METHOD;
    }
    public String RemoveMethod() {
        return REMOVE_METHOD;
    }
    public String SendNewLocalStatMethod() {
        return SEND_NEW_LOCAL_STAT_METHOD;
    }
    public String SendNewGlobalStatMethod() {
        return SEND_NEW_GLOBAL_STAT_METHOD;
    }
    public String BoostRequestedMethod() {
        return BOOST_REQUESTED_METHOD;
    }
    public String RegisterMethod() {
        return REGISTER_METHOD;
    }
    public String GetSplit(){
        return SPLIT;
    }


    public ServerMessages(String serverIP){
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        this.serverIP = serverIP;
    }

    public StringBuilder GetResponse(HttpURLConnection connection) throws IOException{
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        return response;
    }

    public void SendContent(String content, HttpURLConnection connection) throws IOException{
        connection.setDoOutput(true);
        DataOutputStream d = new DataOutputStream(connection.getOutputStream());
        d.writeBytes(content);
        d.flush();
        d.close();
        connection.getResponseCode();
    }

//    List<House> AskHouseList(String IP) throws IOException {
//
//        URL url = new URL( IP+"/server/list");
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("GET");
//        connection.setRequestProperty("Content-Type", "application/json");
//
//        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//        StringBuilder response = new StringBuilder();
//        String line;
//        while ((line = in.readLine()) != null) {
//            response.append(line);
//        }
//        in.close();
//
//        House[] array = gson.fromJson(response.toString(), House[].class);
//        List<House> list = new ArrayList();
//        for(int i = 0; i<array.length; i++){
//            list.add(array[i]);
//        }
//        return list;
//    }
//
//    House AskOldest(String IP) throws IOException {
//
//        URL url = new URL( IP+"/server/oldest");
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("GET");
//        connection.setRequestProperty("Content-Type", "application/json");
//
//        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//        StringBuilder response = new StringBuilder();
//        String line;
//        while ((line = in.readLine()) != null) {
//            response.append(line);
//        }
//        in.close();
//
//        return gson.fromJson(response.toString(), House.class);
//    }
//    House AskNext(String IP, House inNeed) throws IOException {
//
//        int leavingID = inNeed.GetID();
//        URL url = new URL( IP+"/server/new-next/"+leavingID);
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("GET");
//        connection.setRequestProperty("Content-Type", "application/json");
//
//        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//        StringBuilder response = new StringBuilder();
//        String line;
//        while ((line = in.readLine()) != null) {
//            response.append(line);
//        }
//        in.close();
//
//        return gson.fromJson(response.toString(), House.class);
//    }
//
//    Stat[] GetHouseStats(String serverIP, int quantity, int houseID) throws IOException {
//
//        URL url = new URL( serverIP+"/server/get-local/"+quantity+"/"+houseID);
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("GET");
//        connection.setRequestProperty("Content-Type", "application/json");
//
//        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//        StringBuilder response = new StringBuilder();
//        String line;
//        while ((line = in.readLine()) != null) {
//            response.append(line);
//        }
//        in.close();
//
//        if(response == null)
//            return null;
//        Stat[] stats = gson.fromJson(response.toString(), Stat[].class);
//        return stats;
//    }
//
//    Stat[] GetGlobalStats(String serverIP, int quantity) throws IOException {
//
//        URL url = new URL( serverIP+"/server/get-global/"+quantity);
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("GET");
//        connection.setRequestProperty("Content-Type", "application/json");
//
//        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//        StringBuilder response = new StringBuilder();
//        String line;
//        while ((line = in.readLine()) != null) {
//            response.append(line);
//        }
//        in.close();
//
//        Stat[] stats = gson.fromJson(response.toString(), Stat[].class);
//        return stats;
//    }
//
//    public void AddClient(String serverIP, String clientIP) throws IOException{
//        URL url = new URL( serverIP+"/server/add-client");
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("POST");
//        connection.setRequestProperty("Content-Type", "application/json");
//
//        connection.setDoOutput(true);
//        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
//        String jsonToServer = gson.toJson(clientIP);
//        wr.writeBytes(jsonToServer);
//        wr.flush();
//        wr.close();
//        connection.getResponseCode();               // non so se serve
//    }
//
//    public void Remove(String serverIP, int houseID) throws IOException{
//        URL url = new URL( serverIP+"/server/remove-house");
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("DELETE");
//        connection.setRequestProperty("Content-Type", "application/json");
//
//        connection.setDoOutput(true);
//        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
//        String jsonToServer = gson.toJson(houseID);
//        wr.writeBytes(jsonToServer);
//        wr.flush();
//        wr.close();
//        connection.getResponseCode();               // non so se serve
//    }
//
//    public void SendNewLocalStat(String serverIP, House h) throws IOException{
//        URL url = new URL( serverIP+"/server/new-stat/local");
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("POST");
//        connection.setRequestProperty("Content-Type", "application/json");
//
//        connection.setDoOutput(true);
//        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
//        String jsonToServer = gson.toJson(h);
//        wr.writeBytes(jsonToServer);
//        wr.flush();
//        wr.close();
//        connection.getResponseCode();               // non so se serve
//    }
//
//    public void SendNewGlobalStat(String serverIP, Stat s) throws IOException{
//        URL url = new URL( serverIP+"/server/new-stat/global");
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("POST");
//        connection.setRequestProperty("Content-Type", "application/json");
//
//        connection.setDoOutput(true);
//        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
//        String jsonToServer = gson.toJson(s);
//        wr.writeBytes(jsonToServer);
//        wr.flush();
//        wr.close();
//        connection.getResponseCode();               // non so se serve
//    }
//
//    public void BoostRequested(String serverIP, int houseRequestingID) throws IOException{
//        URL url = new URL( serverIP+"/server/boost");
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("POST");
//        connection.setRequestProperty("Content-Type", "application/json");
//
//        connection.setDoOutput(true);
//        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
//        String jsonToServer = gson.toJson(houseRequestingID);
//        wr.writeBytes(jsonToServer);
//        wr.flush();
//        wr.close();
//        connection.getResponseCode();               // non so se serve
//    }
//
//    public void Register(String serverIP, House h) throws IOException{
//
//        URL url = new URL( serverIP+"/server/add-house");
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("POST");
//        connection.setRequestProperty("Content-Type", "application/json");
//
//        connection.setDoOutput(true);
//        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
//        String jsonToServer = gson.toJson(h);
//        wr.writeBytes(jsonToServer);
//        wr.flush();
//        wr.close();
////        System.out.println(connection.getResponseCode());
//        if(connection.getResponseCode() == 500){
//            System.out.println("There was already a house with my same ID");
//            System.exit(0);
//        }
//    }

    public String MessageToServer(String message) throws IOException{
        String responseToHouse = "";

        String[] input = message.split((SPLIT));
        String method = input[0];
        String quantity;
        String id;
        String jsonHouse;
        String content;

        String urlBase = serverIP+"/server/"+method;
        URL url = new URL( urlBase);
        HttpURLConnection connection;
        StringBuilder response;
        DataOutputStream d;

        switch (method){
            case ASK_HOUSE_LIST_METHOD:
            case ASK_OLDEST_METHOD:
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("GET");
                response = GetResponse(connection);
                responseToHouse = response.toString();
                break;
            case ASK_NEXT_METHOD:
                id = input[1];
                url = new URL(urlBase+"/"+id);

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("GET");

                response = GetResponse(connection);
                responseToHouse = response.toString();
                break;
            case GET_GLOBAL_STATS_METHOD:
                quantity = input[1];
                url = new URL(urlBase+"/"+quantity);

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("GET");

                response = GetResponse(connection);
                responseToHouse = response.toString();
                break;
            case GET_HOUSE_STATS_METHOD:
                quantity = input[1];
                id = input[2];
                url = new URL(urlBase+"/"+quantity+"/"+id);

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("GET");

                response = GetResponse(connection);
                responseToHouse = response.toString();
                break;
            case ADD_CLIENT_METHOD:
            case BOOST_REQUESTED_METHOD:
            case SEND_NEW_GLOBAL_STAT_METHOD:
            case SEND_NEW_LOCAL_STAT_METHOD:
                content = input[1];

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("POST");

                SendContent(content,connection);
                break;
            case REMOVE_METHOD:
                id = input[1];

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("DELETE");

                SendContent(id,connection);
                break;
//            case SEND_NEW_GLOBAL_STAT_METHOD:
//                String jsonStat = input[1];
//
//                connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestProperty("Content-Type", "application/json");
//                connection.setRequestMethod("POST");
//
//                SendContent(jsonStat,connection);
//                break;
//            case SEND_NEW_LOCAL_STAT_METHOD:
//                jsonHouse = input[1];
//
//                connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestProperty("Content-Type", "application/json");
//                connection.setRequestMethod("POST");
//
//                SendContent(jsonHouse,connection);
//                break;
//            case BOOST_REQUESTED_METHOD:
//                id = input[1];
//
//                connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestProperty("Content-Type", "application/json");
//                connection.setRequestMethod("POST");
//
//                SendContent(id,connection);
//                break;
            case REGISTER_METHOD:
                jsonHouse = input[1];

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("POST");

                SendContent(jsonHouse,connection);
                if(connection.getResponseCode() == 500){
                    System.out.println("There was already a house with my same ID");
                    System.exit(0);
                }
                break;
            default:
                System.out.println("(debug): server message method not valid");
                break;
        }

        return responseToHouse;
    }
    
}
