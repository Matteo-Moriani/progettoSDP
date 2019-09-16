package condominium;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HouseSocketThread extends Thread {

    private ServerSocket socket;
    private House house;
    private final Object quitLock = new Object();
    private boolean quitting = false;

    HouseSocketThread(ServerSocket socket, House house){
        this.socket = socket;
        this.house = house;

        start();
    }

    public void run() {
        System.out.println("\nSocket listening at port " + house.GetPort());
        while (!HouseQuitting()) {
            try {
                Socket messageSocket = socket.accept();
                new HouseMessageThread(messageSocket, house);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        System.out.println("house socket thread ended");
    }

    public boolean HouseQuitting(){
        synchronized (quitLock) {
            return quitting;
        }
    }

    public void setQuitting(boolean b){
        synchronized (quitLock){
            quitting = b;
        }
    }
}
