package condominium;

public class TokenThread extends Thread {
    private House house;
    private HouseMessages houseMessages = new HouseMessages();
    private static final int TOKEN_QUANTITY = 2;

    public TokenThread(House house){
        this.house = house;
    }

    public void run() {
        System.out.println("token thread started for house " + house.GetID());
        while (!house.IsQuitting()) {
            synchronized (this) {
                while (house.GetList().size() <= TOKEN_QUANTITY) {
                    try {
                        System.out.println("token will cycle with at least "+(TOKEN_QUANTITY+1)+" houses. current number: "+house.GetList().size());
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (house.HasToken()) {
                if (house.WantsBoost()) {
                    try {
                        System.out.println("boost started, keeping the token "+System.nanoTime());
                        System.out.println("\nit will last 3 seconds\n");
                        house.GetSmartMeter().boost();
                        house.SetWantsBoost(false);
                        System.out.println("boost ended, releasing the token "+System.nanoTime());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                boolean sent = false;
                while(!sent) {
                    try {
                        houseMessages.SendToken(house.GetNextInRing());
                        System.out.println("giving my token to " + house.GetNextInRing().GetID());
                        house.SetHasToken(false);
                        sent = true;
                    } catch (Exception e1) {
                        System.out.println("I wasn't unable to send the token. waiting for a new next in the ring");
                    }
                }
            }
        }
    }
}
