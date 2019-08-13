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
    private final String SPLIT = "SEPARATOR-FOR-MESSAGES";

    public HouseMessages(){
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }

    public void IntroduceTo(int targetPort, House newHouse) throws IOException {
        Socket socket = new Socket("localhost", targetPort);
        DataOutputStream outToTarget = new DataOutputStream(socket.getOutputStream());
        String message = gson.toJson(newHouse)+SPLIT+INTRODUCE_METHOD;
        outToTarget.writeBytes(message);
        socket.close();
    }

    public void Quit(House target, int quittinHouseID) throws IOException{
        Socket socket = new Socket("localhost", target.getPort());
        DataOutputStream outToTarget = new DataOutputStream(socket.getOutputStream());
        String message = gson.toJson(quittinHouseID)+SPLIT+QUIT_METHOD;
        outToTarget.writeBytes(message);
        socket.close();
    }
}
