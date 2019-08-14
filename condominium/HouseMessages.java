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

    public void Quit(House target, int quittingHouseID) throws IOException{
        Socket socket = new Socket("localhost", target.GetPort());
        DataOutputStream outToTarget = new DataOutputStream(socket.getOutputStream());
        String message = gson.toJson(quittingHouseID)+SPLIT+QUIT_METHOD;
        outToTarget.writeBytes(message);
        socket.close();
    }

    public void SendNewStat(House target, House sendingStatHouse) throws IOException{
        Socket socket = new Socket("localhost", target.GetPort());
        DataOutputStream outToTarget = new DataOutputStream(socket.getOutputStream());
        String message = gson.toJson(sendingStatHouse)+SPLIT+NEW_STAT_METHOD;
//        System.out.println("sending "+message+" from "+sendingStatHouse.GetID()+" to "+target.GetID());
        outToTarget.writeBytes(message);
        socket.close();
    }
}
