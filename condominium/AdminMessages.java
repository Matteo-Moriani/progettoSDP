package condominium;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class AdminMessages {

    Gson gson;
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


    public AdminMessages(){
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }

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
