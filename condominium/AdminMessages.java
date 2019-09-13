package condominium;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class AdminMessages {

    private final String HOUSE_JOINED_METHOD = "house-joined";
    private final String HOUSE_LEFT_METHOD = "house-left";
    private final String BOOST_REQUEST_METHOD = "boost";

    public String getHouseJoinedMethod() {
        return HOUSE_JOINED_METHOD;
    }

    public String getHouseLeftMethod() {
        return HOUSE_LEFT_METHOD;
    }

    public String getBoostRequestMethod() {
        return BOOST_REQUEST_METHOD;
    }

    Gson gson;

    public AdminMessages(){
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }

//    public void HouseJoined(String adminIP, House newHouse) throws IOException {
//        URL url = new URL( adminIP+"/admin/house-joined");
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("POST");
//        connection.setRequestProperty("Content-Type", "application/json");
//
//        connection.setDoOutput(true);
//        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
//        String jsonToServer = gson.toJson(newHouse);
//        wr.writeBytes(jsonToServer);
//        wr.flush();
//        wr.close();
//        connection.getResponseCode();               // non so se serve
//    }
//
//    public void HouseLeft(String adminIP, House quittingHouse) throws IOException{
//        URL url = new URL( adminIP+"/admin/house-left");
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("POST");
//        connection.setRequestProperty("Content-Type", "application/json");
//
//        connection.setDoOutput(true);
//        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
//        String jsonToServer = gson.toJson(quittingHouse);
//        wr.writeBytes(jsonToServer);
//        wr.flush();
//        wr.close();
//        connection.getResponseCode();               // non so se serve
//    }
//
//    public void BoostNotification(String adminIP, int houseRequestingID) throws IOException{
//        URL url = new URL( adminIP+"/admin/boost");
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

    public void Notification(String method, String adminIP, int houseID) throws IOException{
        URL url = new URL( adminIP+"/admin/"+method);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");

        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        String jsonToServer = gson.toJson(houseID);
        wr.writeBytes(jsonToServer);
        wr.flush();
        wr.close();
        connection.getResponseCode();
    }
}
