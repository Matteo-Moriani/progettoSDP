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
    private final String REMOVED_METHOD = "removed";

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

    public void Remove(House target, int quittingHouseID) throws IOException{
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

    public void Elect(House newCoordinator) throws IOException{
        Socket socket = new Socket("localhost", newCoordinator.GetPort());
        DataOutputStream outToTarget = new DataOutputStream(socket.getOutputStream());
        String message = " "+SPLIT+ELECT_METHOD;
        outToTarget.writeBytes(message);
        socket.close();
    }

    public void SendToken(House target) throws IOException, InterruptedException{
        Thread.sleep(1000);
        Socket socket = new Socket("localhost", target.GetPort());
        DataOutputStream outToTarget = new DataOutputStream(socket.getOutputStream());
        String message = " "+SPLIT+TOKEN_METHOD;
        outToTarget.writeBytes(message);
        socket.close();
    }

//    public void SendConfirm(int id, House removed) throws IOException{
//        Socket socket = new Socket("localhost", removed.GetPort());
//        DataOutputStream outToTarget = new DataOutputStream(socket.getOutputStream());
//        String message = gson.toJson(id)+SPLIT+REMOVED_METHOD;
//        outToTarget.writeBytes(message);
//        socket.close();
//    }
}
