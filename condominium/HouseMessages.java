package condominium;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class HouseMessages {

    Gson gson;
    private final String INTRODUCE_METHOD = "introduce";
    private final String QUIT_METHOD = "quit";
    private final String NEW_STAT_METHOD = "new-stat";
    private final String ELECT_METHOD = "elect";
    private final String TOKEN_METHOD = "token";
    private final String SPLIT = "SEPARATOR-FOR-MESSAGES";

    public HouseMessages(){
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }


    public String getIntroduceMethod() {
        return INTRODUCE_METHOD;
    }

    public String getQuitMethod() {
        return QUIT_METHOD;
    }

    public String getNewStatMethod() {
        return NEW_STAT_METHOD;
    }

    public String getElectMethod() {
        return ELECT_METHOD;
    }

    public String getTokenMethod() {
        return TOKEN_METHOD;
    }

    public String getSplit(){
        return SPLIT;
    }

    public void SendMessage(String message) throws IOException{         // gestisco nella classe house
        String[] input = message.split((SPLIT));
        String method = input[0];
        String targetString = input[1];

        switch (method){
            case TOKEN_METHOD:
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
                break;
            default:    // negli altri non c'è bisogno di aspettare
                break;
        }

        House target = gson.fromJson(targetString, House.class);
        Socket socket = new Socket("localhost", target.GetPort());
        DataOutputStream outToTarget = new DataOutputStream(socket.getOutputStream());
        outToTarget.writeBytes(message);
        socket.close();
    }
}
