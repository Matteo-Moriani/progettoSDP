package condominium;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

public class TokenThread extends Thread {
    private House house;
    private HouseMessages houseMessages = new HouseMessages();
    private boolean hold;
    private boolean wantsBoost = false;
    private final Object tokenLock = new Object();
    private final Object boostLock = new Object();
    private final Object quitLock = new Object();
    private static final int TOTAL_TOKENS = 2;
    private String split;
    private boolean cyclingDebugger = false;
    private boolean quitting = false;

    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public TokenThread(House house){
        this.house = house;
        split = house.getSplit();
        if(house.GetList().size() > TOTAL_TOKENS)
            hold = false;
        else
            hold = true;
    }

    public void run() {

        while (!HouseQuitting()){

            boolean alreadySent = false;

            if (HoldingToken()) {
                if (BoostRequested()) {
                    try {
                        System.out.println("\n      boost started, keeping the token "+house.GetTime(System.nanoTime())+"\n");
                        System.out.println("\n      boosting...\n");
                        house.SetBoosting(true);
                        house.GetSmartMeter().boost();
                        SetWantsBoost(false);
                        System.out.println("\n      boost ended, releasing the token "+house.GetTime(System.nanoTime())+"\n");
                        house.SetBoosting(false);

                        if (house.GetList().size() > TOTAL_TOKENS) {
                            int attempts = 0;
                            while(attempts < 3) {
                                try {
                                    SendTokenToNext();
                                    break;
                                } catch (IOException connect){
                                    System.out.println("I wasn't unable to send the token to "
                                            +house.GetNextInRing().GetID() + " after boost. trying again...");
                                    attempts++;
                                    Thread.sleep(500);
                                }
                            }
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
                    int attempts = 0;
                    while(attempts < 3) {
                        try {
                            SendTokenToNext();
                            break;
                        } catch (IOException connect){
                            System.out.println("I wasn't unable to send the token to "
                                    +house.GetNextInRing().GetID() + ". trying again...");
                            attempts++;
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
                else if(house.GetList().size() <= TOTAL_TOKENS){
                    if(!HouseQuitting()) {
                        synchronized (this) {
                            try {
                                System.out.println("waiting for more houses to cycle tokens");
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

    public void SendTokenToNext() throws IOException{
        if(cyclingDebugger)
            System.out.println("giving my token to " + house.GetNextInRing().GetID());
        houseMessages.SendMessage(houseMessages.SendTokenMethod() + split + gson.toJson(house.GetNextInRing()));
        SetHold(false);
    }


    public void SetHold(boolean settingTo) throws IOException{
        if(settingTo == true) {
            if(HouseQuitting()){
                System.out.println("I received a token while quitting, sending it to " + house.GetNextInRing().GetID());
                houseMessages.SendMessage(houseMessages.SendTokenMethod() + split + gson.toJson(house.GetNextInRing()));
            } else {
                if (!hold) {
                    synchronized (tokenLock) {
                        hold = true;
                    }
                    if(cyclingDebugger)
                        System.out.println("received token");
                } else {
                    if(cyclingDebugger)
                        System.out.println("I already have a token, sending this one to " + house.GetNextInRing().GetID());
                    boolean sent = false;
                    while(!sent) {
                        try {
                            houseMessages.SendMessage(houseMessages.SendTokenMethod() + split + gson.toJson(house.GetNextInRing()));
                            sent = true;
                        } catch (NullPointerException e) {
                            System.out.println("next not set yet, keeping also this token while waiting");
                            try {Thread.sleep(500);} catch (InterruptedException e1){e1.printStackTrace();}
                        }
                    }

                }
            }
        } else {
            synchronized (tokenLock) {
                hold = false;
            }
            if(cyclingDebugger)
                System.out.println("don't have a token anymore");
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

    public boolean HouseQuitting(){
        synchronized (quitLock) {
            return quitting;
        }
    }

    public void setQuitting(boolean b) {
        synchronized (quitLock){
            quitting = b;
        }
    }

}
