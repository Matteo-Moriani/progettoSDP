package condominium;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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

    public String MessageToServer(String message) throws IOException{
        String responseToHouse = "";

        String[] input = message.split((SPLIT));
        String method = input[0];
        String quantity;
        String id;
        String jsonHouse;
        String jsonContent;

        String urlBase = serverIP+"/"+method;
        URL url = new URL( urlBase);
        HttpURLConnection connection;
        StringBuilder response;

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
                jsonContent = input[1];

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("POST");

                SendContent(jsonContent,connection);
                break;
            case REMOVE_METHOD:
                id = input[1];

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("DELETE");

                SendContent(id,connection);
                break;
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
