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
    TokenThread tokenThread;
    Gson gson = new GsonBuilder().create();
    private String split;

    public HouseMessageThread(Socket socket, House house){
        this.socket = socket;
        this.house = house;
        split = house.getSplit();
        tokenThread = house.GetTokenThread();

        start();
    }

    public void run(){
        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String[] input = inFromClient.readLine().split((split));
            String method = input[0];
            switch (method){
                case "introduce":
                    House newHouse = gson.fromJson(input[2], House.class);
                    house.AddHouse(newHouse);
                    break;
                case "quit":
                    int quittingID = gson.fromJson(input[2], int.class);
                    house.RemoveHouse(quittingID);
                    break;
                case "new-stat":
                    House sendingStatHouse = gson.fromJson(input[2], House.class);
                    house.NewStatFromHouse(sendingStatHouse);
                    break;
                case "elect":
                    house.SetCoordinator();
                    break;
                case "token":
                    tokenThread.SetHold(true);
                    break;
                case "ok":
                    int ConfirmingID = gson.fromJson(input[2], int.class);
                    house.ReceiveConfirmation(ConfirmingID);
                    break;
                default:
                    System.out.println("there's something wrong with the message");
                    break;
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

}
