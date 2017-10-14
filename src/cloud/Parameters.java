package cloud;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

class Parameters {

    static String lastTradeTime;
    static int prepMins;
    static int minsBeforeClose;
    static int iconC1;
    static int iconC2;
    static int uAndEC1;
    static int uAndEC2;
    static String username;
    static String password;
    static boolean realMoney;
    static int monthsApart;
    static double maxSpread;
    static int secondsWaitForOrders;
    static int minVolume;
    static void getParameters(){
        String sting = "";
        BufferedReader fr = null;
        try{
            fr = new BufferedReader(new FileReader(OrderPlacer.dir+"parameters.csv"));
            sting = fr.readLine();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            try{
                if (fr != null) {
                    fr.close();
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        String[] parameters = sting.split(";");
        lastTradeTime = parameters[0];
        prepMins = Integer.parseInt(parameters[1]);
        minsBeforeClose = Integer.parseInt(parameters[2]);
        iconC1 = Integer.parseInt(parameters[3]);
        iconC2 = Integer.parseInt(parameters[4]);
        uAndEC1 = Integer.parseInt(parameters[5]);
        uAndEC2 = Integer.parseInt(parameters[6]);
        username = parameters[7];
        password = parameters[8];
        realMoney = parameters[9].equals("TRUE");
        monthsApart = Integer.parseInt(parameters[10]);
        maxSpread = Double.parseDouble(parameters[11]);
        secondsWaitForOrders = Integer.parseInt(parameters[12]);
        minVolume = Integer.parseInt(parameters[13]);
    }
}
