package condominium;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HouseSocketThread extends Thread {

    private ServerSocket socket;
    private House house;

    HouseSocketThread(ServerSocket socket, House house){
        this.socket = socket;
        this.house = house;

        start();
    }

    public void run() {
        System.out.println("\nSocket listening at port " + house.GetPort());
        while (true) {
            try {
                Socket messageSocket = socket.accept();
                new HouseMessageThread(messageSocket, house);
            } catch (IOException ex) {
                ex.printStackTrace();
                // Restart the socket.
            }
        }
//        try {
//            socket.close();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
    }
}
