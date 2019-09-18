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
    private final String OK_METHOD = "ok";
    private final String SPLIT = "SEPARATOR-FOR-MESSAGES";

    public HouseMessages(){
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }


    public String IntroduceMethod() {
        return INTRODUCE_METHOD;
    }

    public String QuitMethod() {
        return QUIT_METHOD;
    }

    public String NewStatMethod() {
        return NEW_STAT_METHOD;
    }

    public String ElectMethod() {
        return ELECT_METHOD;
    }

    public String SendTokenMethod() {
        return TOKEN_METHOD;
    }

    public String OkMethod(){
        return OK_METHOD;
    }

    public String getSplit(){
        return SPLIT;
    }

    public void SendMessage(String message) throws IOException{         // gestisco nella classe house
        String[] input = message.split((SPLIT));
        String method = input[0];
        String targetJson = input[1];
        House target = gson.fromJson(targetJson, House.class);

        // alla fine uno switch non serviva, ma lo tengo giusto per rallentare il giro dei token per il debug
        switch (method){
            case TOKEN_METHOD:
//                System.out.println("sending to "+target.GetID());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
                break;
            default:    // negli altri non c'è bisogno di aspettare
                break;
        }

        Socket socket = new Socket("localhost", target.GetPort());
        DataOutputStream outToTarget = new DataOutputStream(socket.getOutputStream());
        outToTarget.writeBytes(message);
        socket.close();
    }
}
