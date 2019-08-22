package condominium;

import java.io.IOException;

public class TokenThread extends Thread {
    private House house;
    private HouseMessages houseMessages = new HouseMessages();
    private boolean hold;
    private static final int TOTAL_TOKENS = 2;
    private final Object tokenLock = new Object();

    public TokenThread(House house){
        this.house = house;
        if(house.GetList().size() <= TOTAL_TOKENS)
            hold = false;
        else
            hold = true;
    }

    public void run() {
        System.out.println("token thread started for house " + house.GetID());

        while (!house.IsQuitting()) {
            if (HoldingToken()) {
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
                SetHold(false);
                sent = true;
            } catch (Exception e1) {
                System.out.println("I wasn't unable to send the token. waiting for a new next in the ring");
            }
        }
    }

    public void SetHold(boolean settingTo) throws IOException, InterruptedException{
        if(settingTo == true) {
            if(!hold){
                synchronized (tokenLock) {
                    hold = true;
                }
                System.out.println("received token");
            } else {
                if (house.GetList().size() > TOTAL_TOKENS){
                    // l'inoltro ha senso solo da 3 case in su
                    System.out.println("I already have a token, sending this one to "+house.GetNextInRing().GetID());
                    houseMessages.SendToken(house.GetNextInRing());
                } else {
                    System.out.println("(house) waiting for more houses");
                }
            }
        } else {
            synchronized (tokenLock) {
                hold = false;
            }
            System.out.println("don't have a token anymore");
        }
    }

    public boolean HoldingToken(){
        synchronized (tokenLock) {
            return hold;
        }
    }

}
