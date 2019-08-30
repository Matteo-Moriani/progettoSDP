package condominium;

import java.io.IOException;

public class TokenThread extends Thread {
    private House house;
    private HouseMessages houseMessages = new HouseMessages();
    private boolean hold;
    private boolean wantsBoost = false;
    private final Object tokenLock = new Object();
    private final Object boostLock = new Object();
    private static final int TOTAL_TOKENS = 2;

    public TokenThread(House house){
        this.house = house;
        if(house.GetList().size() > TOTAL_TOKENS)
            hold = false;
        else
            hold = true;
    }

    public void run() {
        System.out.println("token thread started");

        while (!house.IsQuitting()) {
            boolean alreadySent = false;

            if (HoldingToken()) {
                if (BoostRequested()) {
                    System.out.println("boost requested");
                    try {
                        System.out.println("boost started, keeping the token "+System.nanoTime());
                        System.out.println("\nit will last 3 seconds\n");
                        house.SetBoosting(true);
                        house.GetSmartMeter().boost();
                        SetWantsBoost(false);
                        System.out.println("boost ended, releasing the token "+System.nanoTime());
                        house.SetBoosting(false);
                        // non lo mando a me stesso che tanto se sto uscendo e sono l'ultimo rimasto non ha senso
                        if(house.GetNextInRing().GetID() != house.GetID()) {
                            SendTokenTo(house.GetNextInRing());
                            alreadySent = true;
                        }
                        synchronized (house) {
                            house.notify();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(house.GetList().size() > TOTAL_TOKENS && alreadySent == false) {
                    SendTokenTo(house.GetNextInRing());
                }
                else {
                    if(!house.IsQuitting()) {
                        synchronized (this) {
                            try {
                                System.out.println("waiting for more houses");
                                wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        System.out.println("token thread ended");
    }

    public void SendTokenTo(House h){
        boolean sent = false;
        while(!sent) {
            try {
                houseMessages.SendToken(h);
//                System.out.println("giving my token to " + h.GetID());
                SetHold(false);
                sent = true;
            } catch (Exception e1) {
                System.out.println("I wasn't unable to send the token. waiting for a new successor in the ring");
                if(house.GetList().size() <= TOTAL_TOKENS)
                    break;
            }
        }
    }

    public void SetHold(boolean settingTo) throws IOException, InterruptedException{
        if(settingTo == true) {
            if(!hold){
                synchronized (tokenLock) {
                    hold = true;
                }
//                System.out.println("received token");
            } else {
                if (house.GetList().size() > TOTAL_TOKENS){
                    // l'inoltro ha senso solo da 3 case in su
//                    System.out.println("I already have a token, sending this one to "+house.GetNextInRing().GetID());
                    houseMessages.SendToken(house.GetNextInRing());
                } else {
//                    System.out.println("(house) waiting for more houses");
                }
            }
        } else {
            synchronized (tokenLock) {
                hold = false;
            }
//            System.out.println("don't have a token anymore");
        }
    }

    public boolean HoldingToken(){
        synchronized (tokenLock) {
            return hold;
        }
    }

    public boolean BoostRequested(){
        synchronized (boostLock) {
            return wantsBoost;
        }
    }

    public void SetWantsBoost(boolean b){
        synchronized (boostLock) {
            wantsBoost = b;
        }
    }

}
