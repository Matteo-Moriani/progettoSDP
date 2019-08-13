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
                    House h = gson.fromJson(input[0], House.class);
                    house.AddHouse(h);
                    break;
                case "quit":
                    int id = gson.fromJson(input[0], int.class);
                    house.RemoveHouse(id);
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
