package condominium;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class HouseMessageThread extends Thread{

    Socket socket;
    House house;
    Gson gson = new GsonBuilder().create();
    private final String SPLIT = "SEPARATOR-FOR-MESSAGES";

    public HouseMessageThread(Socket socket, House house){
        this.socket = socket;
        this.house = house;

        start();
    }

    public void run(){
        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String[] input = inFromClient.readLine().split((SPLIT));
            String method = input[1];
            switch (method){
                case "introduce":
                    House newHouse = gson.fromJson(input[0], House.class);
                    house.AddHouse(newHouse);
                    break;
                case "quit":
                    int quittingID = gson.fromJson(input[0], int.class);
                    house.RemoveHouse(quittingID);
                    break;
                case "new-stat":
                    House sendingStatHouse = gson.fromJson(input[0], House.class);
//                    System.out.println(house.GetID()+" is trying to receive last stat of "+sendingStatHouse.GetID());
                    house.NewStatFromHouse(sendingStatHouse);
                    break;
//                case SetAdmin:
//                    house.SetAdmin(gson.fromJson(mex.json, int.class));
//                    break;
//                case MorePowerToken:
//                    house.ReceivedMorePowerToken();
//                    break;
//                case MeasurementsMean:
//                    house.ReceivedMeasurementMean(gson.fromJson(mex.json, MeasurementMean.class));
//                    break;
                default:
                    System.out.println("there's something wrong with the message");
                    break;
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

}
