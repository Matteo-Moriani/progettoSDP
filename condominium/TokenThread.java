package condominium;

import java.io.IOException;
import java.util.Map;

public class TokenThread extends Thread {
    private House house;
    private HouseMessages houseMessages = new HouseMessages();
    private static final int TOTAL_TOKENS = 2;

    public TokenThread(House house){
        this.house = house;
    }

//    public void run() {
//        while (true) {
//            if (house.hasToken) {
//                if(house.GetList().size() > TOTAL_TOKENS)
//                    SendTokenTo(house.GetNextInRing());
//                else {
//                    synchronized (this) {
//                        try{
//                            System.out.println("(token thread) waiting for more houses");
//                            wait();
//                        } catch(InterruptedException e){
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        }
//    }

    public void run() {
        System.out.println("token thread started for house " + house.GetID());
        System.out.println(Thread.getAllStackTraces());

        while (!house.IsQuitting()) {
            // questa stampa inutile serve sennò non funziona nulla e non so perché
//            System.out.print("");
            if (house.HasToken()) {
                if (house.WantsBoost()) {
                    try {
                        System.out.println("boost started, keeping the token "+System.nanoTime());
                        System.out.println("\nit will last 3 seconds\n");
                        house.SetBoosting(true);
                        house.GetSmartMeter().boost();
                        house.SetWantsBoost(false);
                        System.out.println("boost ended, releasing the token "+System.nanoTime());
                        house.SetBoosting(false);
                        synchronized (house) {
                            house.notify();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(house.GetList().size() > TOTAL_TOKENS)
                    SendTokenTo(house.GetNextInRing());
                else {
                    synchronized (this) {
                        try{
                            System.out.println("(token thread) waiting for more houses");
                            wait();
                        } catch(InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void SendTokenTo(House h){
        boolean sent = false;
        while(!sent) {
            try {
                houseMessages.SendToken(h);
                System.out.println("giving my token to " + h.GetID());
                house.SetHasToken(false);
                sent = true;
            } catch (Exception e1) {
                System.out.println("I wasn't unable to send the token. waiting for a new next in the ring");
            }
        }
    }
    public void SetHasToken(boolean settingTo) throws IOException, InterruptedException{
        if(settingTo == true) {
//            if (!hasToken) {
//                hasToken = true;
            if(!token[0]){
                synchronized (token) {
                    token[0] = true;
                }
                System.out.println("received token");
            } else {
                if (condominiumHouses.size() > TOKEN_QUANTITY){
                    // l'inoltro ha senso solo da 3 case in su
                    System.out.println("I already have a token, sending this one to "+nextInRing.GetID());
                    houseMessages.SendToken(nextInRing);
                } else {
                    System.out.println("(house) waiting for more houses");
                }
            }
        } else {
//            hasToken = false;
            synchronized (token) {
                token[0] = false;
            }
            System.out.println("don't have a token anymore");
        }
    }

    public boolean HasToken(){
//        return hasToken;
        synchronized (token) {
            return token[0];
        }
    }

}
