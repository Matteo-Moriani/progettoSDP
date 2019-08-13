package condominium;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HouseSocket extends Thread {

    private ServerSocket socket;
    private House house;
    private boolean quit;

    HouseSocket(ServerSocket socket, House house){
        this.socket = socket;
        this.house = house;
        quit = false;

        start();
    }

    public void run() {
        System.out.println("\nSocket listening at port " + house.getPort());
        while (!quit) {
            try {
                Socket messageSocket = socket.accept();
                new HouseMessageThread(messageSocket, house);
            } catch (IOException ex) {
                ex.printStackTrace();
                // Restart the socket.
            }
        }
        try {
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void Quit(){
        quit = true;
    }

}
