package cloud;

import java.awt.AWTException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import com.ib.controller.ApiController;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.NewOrderState;
import com.ib.controller.NewTickType;
import com.ib.controller.OrderStatus;
import com.ib.controller.OrderType;
import com.ib.controller.Position;
import com.ib.controller.ApiController.IAccountHandler;
import com.ib.controller.ApiController.IOptHandler;
import com.ib.controller.ApiController.IOrderHandler;
import com.ib.controller.ApiController.IPositionHandler;
import com.ib.controller.ApiController.TopMktDataAdapter;
import com.ib.controller.Types.Action;
import com.ib.controller.Types.MktDataType;
import com.ib.controller.Types.Right;
import com.ib.controller.Types.SecType;
import com.ib.controller.Types.TimeInForce;
import com.ib.controller.ApiConnection.ILogger;

public class OrderPlacer extends ConnectionHandlerAdapter implements ILogger
{
    private final ApiController m_controller = new ApiController(this,this,this);

    private String symbol = "SPY";

    static String dir = "C:\\mine\\";

    private String date;
    private String time;
    private String day1;
    private String day2;
    private double strike1;
    private double strike2;
    private boolean gotPos;
    private boolean gotStrike1;
    private boolean gotStrike2;
    private boolean placedOrders;
    private boolean twsOpen;

    private int nN = 0;
    private int fN = 0;
    private int sN = 0;
    private int xN = 0;

    private String val;

    private DataContract nC = new DataContract();
    private DataContract fC = new DataContract();
    private DataContract xC = new DataContract();
    private DataContract sC = new DataContract();

    private double tPrice;

    private int strikeX;

    static int robotDelay = 0;

    private boolean gotNewContracts = false;

    private ArrayList<Long> openOrders = new ArrayList<>();
    private ArrayList<OldContract> oldPos = new ArrayList<>();

    private boolean boo1;
    private boolean boo2;
    private boolean boo3;
    private boolean boo4;
    private boolean boo5;
    private boolean boo7;

    public static void main(String[] args){
        new OrderPlacer().run();
    }
    private void run(){
        System.out.println("hi, i am here");
        twsOpen = false;
        placedOrders = false;
        Parameters.getParameters();
        day1 = Time.getDay1();
        day2 = Time.getDay2(day1);
        if(Time.outOfSync()){
            System.out.println("out of sync");
            deepSleep(Time.getSleep()+Parameters.prepMins*60*1000);
            System.out.println("CONNECTING !!!!!!!11!!!!!");
            m_controller.connect("127.0.0.1",7496,0);
        }else{
            meta();
        }
    }
    private void meta(){
        robotDelay = 0;
        if(!twsOpen){
            twsOpen = true;
            deepSleep(Time.getSleep());
            InterfaceBot ib = new InterfaceBot();
            try{
                ib.openTWS();
            }catch(AWTException e){
                e.printStackTrace();
            }
            deepSleep(Parameters.prepMins*60*1000/2);
            try{
                ib.loginTWS();
            }catch(AWTException e){
                e.printStackTrace();
            }
            deepSleep(Parameters.prepMins*60*1000/4);
            if(!Parameters.realMoney){
                try{
                    ib.uAndE();
                }catch(AWTException e){
                    e.printStackTrace();
                }
            }
            deepSleep(Parameters.prepMins*60*1000/4-robotDelay);
            System.out.println("connecting...");
            m_controller.connect("127.0.0.1",7496,0);
        }else{
            twsOpen = false;
            deepSleep(Time.getSleep()+Parameters.prepMins*60*1000);
            getDateAndTime();
        }
    }
    @Override public void connected(){
        getDateAndTime();
    }
    @Override public void disconnected(){
        System.exit(0);
    }
    private void getDateAndTime(){
        Calendar c = Calendar.getInstance();
        date = Time.dateFromCalendar(c);
        time = String.valueOf(c.get(Calendar.HOUR_OF_DAY))+":"+String.valueOf(c.get(Calendar.MINUTE))+":"+String.valueOf(c.get(Calendar.SECOND));
        System.out.println("running at "+time);
        while(c.get(Calendar.HOUR_OF_DAY)<=6&&c.get(Calendar.MINUTE)<30){
            deepSleep(60*1000);
            c = Calendar.getInstance();
        }
        if(c.get(Calendar.DAY_OF_WEEK)== Calendar.FRIDAY &&c.get(Calendar.HOUR_OF_DAY)==12){
            if(gotNewContracts){
                gotNewContracts = false;
            }else{
                gotNewContracts = true;
                System.out.println("END OF WEEK DETECTED");
                nN = 0;
                fN = 0;
                xN = 0;
                day1 = Time.getDay1();
                day2 = Time.getDay2(day1);
            }
        }
        getPos();
    }
    private void getPos(){
        boo1 = true;
        gotPos = false;
        m_controller.reqPositions(new IPositionHandler(){
            public void position(String account,NewContract contract,int position,double avgCost){
                if(contract.symbol().equals(symbol)&&contract.expiry().equals(day1)){
                    nN = position;
                    nC.setNewCon(contract);
                }else if(contract.symbol().equals(symbol)&&contract.expiry().equals(day2)&&contract.right().equals(Right.Call)){
                    fN = position;
                    fC.setNewCon(contract);
                }else if(contract.symbol().equals(symbol)&&contract.expiry().equals(day2)&&contract.right().equals(Right.Put)){
                    xN = position;
                    xC.setNewCon(contract);
                }else if(contract.symbol().equals(symbol)&&contract.secType().equals(SecType.STK)){
                    sN = position;
                    sC.setNewCon(contract);
                }else if(position!=0){
                    oldPos.add(new OldContract(contract, position));
                }
            }
            @Override public void positionEnd(){
                if(boo1){
                    boo1 = false;
                    if(!gotPos){
                        gotPos = true;
                        cancelPos(this);
                    }
                }
            }
        });
    }
    private void cancelPos(IPositionHandler h){
        m_controller.cancelPositions(h);
        if(sN==0){
            defineStock();
        }else{
            getStock();
        }
    }
    private void defineStock(){
        NewContract c = new NewContract();
        c.currency("USD");
        c.exchange("SMART");
        c.secType(SecType.STK);
        c.symbol(symbol);
        sC.setNewCon(c);
        getStock();
    }
    private void getStock(){
        boo2 = true;
        sC.getNewCon().exchange("SMART");
        m_controller.reqTopMktData(sC.getNewCon(),"",true,new TopMktDataAdapter(){
            @Override public void tickPrice(NewTickType tickType,double price,int canAutoExecute){
                if(tickType==NewTickType.LAST){
                    sC.setPrice(price);
                }
            }
            @Override public void tickSnapshotEnd(){
                if(boo2){
                    boo2 = false;
                    if(nN==0){
                        gotStrike1 = false;
                        strikeX = (int)Math.round(sC.getPrice());
                        getStrike1();
                    }else{
                        gotStrike1 = true;
                        strike1 = nC.getNewCon().strike();
                        getNear();
                    }
                }
            }
        });
    }
    private void getStrike1(){
        strikeX--;
        nC.defineNewCon(strikeX, day1, symbol, Right.Call);
        getNear();
    }
    private void getNear(){
        boo3 = true;
        nC.getNewCon().exchange("SMART");
        m_controller.reqOptionMktData(nC.getNewCon(),"",true,new IOptHandler(){
            int volume;
            @Override public void tickPrice(NewTickType tickType,double price,int canAutoExecute){
                nC.setData(tickType, price);
            }
            @Override public void tickSize(NewTickType tickType, int size){
                if(tickType==NewTickType.VOLUME){
                    volume = size;
                }
            }
            @Override public void tickString(NewTickType tickType, String value){}
            @Override public void marketDataType(MktDataType marketDataType){}
            @Override public void tickOptionComputation(NewTickType tickType, double impliedVol, double delta, double optPrice,
                                                        double pvDividend, double gamma, double vega, double theta, double undPrice){
                if(tickType==NewTickType.LAST_OPTION){
                    nC.setOptionStats(impliedVol, delta, gamma, vega, theta);
                }
            }
            @Override public void tickSnapshotEnd(){
                if(boo3){
                    boo3 = false;
                    if(!gotStrike1){
                        if((nC.getAsk()-nC.getBid())>Parameters.maxSpread||volume<Parameters.minVolume){
                            gotStrike1 = true;
                            strike1 = strikeX+1;
                            strikeX = strikeX+2;
                        }
                        getStrike1();
                    }else{
                        if(fN==0){
                            gotStrike2 = false;
                            strikeX = (int)Math.round(sC.getPrice());
                            getStrike2();
                        }else{
                            gotStrike2 = true;
                            strike2 = fC.getNewCon().strike();
                            getFar();
                        }
                    }
                }
            }
        });
    }
    private void getStrike2(){
        strikeX--;
        fC.defineNewCon(strikeX, day2, symbol, Right.Call);
        getFar();
    }
    private void getFar(){
        boo4 = true;
        fC.getNewCon().exchange("SMART");
        m_controller.reqOptionMktData(fC.getNewCon(),"",true,new IOptHandler(){
            int volume;
            @Override public void tickPrice(NewTickType tickType,double price,int canAutoExecute){
                fC.setData(tickType, price);
            }
            @Override public void tickSize(NewTickType tickType, int size){
                if(tickType==NewTickType.VOLUME){
                    volume = size;
                }
            }
            @Override public void tickString(NewTickType tickType, String value){}
            @Override public void marketDataType(MktDataType marketDataType){}
            @Override public void tickOptionComputation(NewTickType tickType, double impliedVol, double delta, double optPrice,
                                                        double pvDividend, double gamma, double vega, double theta, double undPrice){
                if(tickType==NewTickType.LAST_OPTION){
                    fC.setOptionStats(impliedVol, delta, gamma, vega, theta);
                }
            }
            @Override public void tickSnapshotEnd(){
                if(boo4){
                    boo4 = false;
                    if(!gotStrike2){
                        if((fC.getAsk()-fC.getBid())>Parameters.maxSpread||volume<Parameters.minVolume){
                            gotStrike2 = true;
                            strike2 = strikeX+1;
                            strikeX = strikeX+2;
                        }
                        getStrike2();
                    }else{
                        if(xN==0){
                            fC.defineNewCon((int)strike2, day2, symbol, Right.Put);
                        }
                        getX();
                    }
                }
            }
        });
    }
    private void getX(){
        boo5 = true;
        xC.getNewCon().exchange("SMART");
        m_controller.reqOptionMktData(xC.getNewCon(),"",true,new IOptHandler(){
            @Override public void tickPrice(NewTickType tickType,double price,int canAutoExecute){
                if(tickType==NewTickType.LAST){
                    xC.setPrice(price);
                }
            }
            @Override public void tickSize(NewTickType tickType, int size){}
            @Override public void tickString(NewTickType tickType, String value){}
            @Override public void marketDataType(MktDataType marketDataType){}
            @Override public void tickOptionComputation(NewTickType tickType, double impliedVol, double delta, double optPrice,
                                                        double pvDividend, double gamma, double vega, double theta, double undPrice){
                if(tickType==NewTickType.LAST_OPTION){
                    xC.setOptionStats(impliedVol, delta, gamma, vega, theta);
                }
            }
            @Override public void tickSnapshotEnd(){
                if(boo5){
                    boo5 = false;
                    if(nC.getVega()==0||fC.getVega()==0){
                        System.out.println("HOLIDAY DETECTED; DOING NOTHING");
                        if(!twsOpen){
                            System.out.println("WE ARE INTENTIONALLY DISCONNECTING !!!!!!!11!11!11!!");
                            m_controller.disconnect();
                        }else{
                            meta();
                        }
                    }else{
                        getLiq();
                    }
                }
            }
        });
    }
    private void getLiq(){
        boo7 = true;
        m_controller.reqAccountUpdates(true, "", new IAccountHandler(){
            @Override public void accountValue(String account, String key, String value, String currency){
                if(key.equals("NetLiquidation")){
                    val = value;
                }
            }
            @Override public void accountTime(String timeStamp){}
            @Override public void accountDownloadEnd(String account){
                if(boo7){
                    boo7 = false;
                    cancelLiq();
                }
            }
            @Override public void updatePortfolio(Position position){}
        });
    }
    private void cancelLiq(){
        m_controller.reqAccountUpdates(false, "", new IAccountHandler(){
            @Override public void accountValue(String account, String key, String value, String currency){}
            @Override public void accountTime(String timeStamp){}
            @Override public void accountDownloadEnd(String account){}
            @Override public void updatePortfolio(Position position){}
        });
        calculate();
    }
    private void calculate(){
        double vR = nC.getVega()/fC.getVega();
        double delt = Math.abs(fC.getDelta()-nC.getDelta()/vR);
        int y = (int)(delt);
        int x;
        if(fC.getDelta()/(fC.getDelta()-nC.getDelta()/vR)>0){
            x = -y;
        }else{
            x = y;
        }
        tPrice = fC.getPrice()*Math.abs(1+x)*100+nC.getPrice()/vR*100+xC.getPrice()*y*100+sC.getPrice()*(delt-y)*100;
        System.out.println("total price: "+tPrice);
        double usedCap = Math.abs(nN*nC.getPrice()*100)+Math.abs(fN*fC.getPrice()*100)+Math.abs(xN*xC.getPrice()*100)+Math.abs(sN*sC.getPrice());
        double vE = nC.getVega()*nN*100+fC.getVega()*fN*100+xC.getVega()*xN*100;
        double dE = nC.getDelta()*nN*100+fC.getDelta()*fN*100+xC.getDelta()*xN*100+sN;
        double tE = nC.getTheta()*nN*100+fC.getTheta()*fN*100+xC.getTheta()*xN*100;
        double gE = nC.getGamma()*nN*100+fC.getGamma()*fN*100+xC.getGamma()*xN*100;
        System.out.println("near theta    : "+nC.getTheta());
        System.out.println("far theta     : "+fC.getTheta());
        System.out.println("near delta    : "+nC.getDelta());
        System.out.println("far delta     : "+fC.getDelta());
        System.out.println("vega ratio    : "+vR);
        System.out.println("delta exposure: "+dE);
        System.out.println("vega exposure : "+vE);
        System.out.println("theta exposure: "+tE);
        System.out.println("gamma exposure: "+gE);
        String[] v = {date,time,String.valueOf(nC.getPrice()),String.valueOf(fC.getPrice()),String.valueOf(xC.getPrice()),
                String.valueOf(strike1),String.valueOf(strike2),String.valueOf(tE),
                String.valueOf(sC.getPrice()),String.valueOf(nC.getDelta()),String.valueOf(fC.getDelta()),String.valueOf(xC.getDelta()),
                String.valueOf(nC.getVega()),String.valueOf(fC.getVega()),String.valueOf(xC.getVega()),String.valueOf(nC.getGamma()),
                String.valueOf(fC.getGamma()),String.valueOf(xC.getGamma()),String.valueOf(nC.getImpliedVolatility()),String.valueOf(fC.getImpliedVolatility()),
                String.valueOf(xC.getImpliedVolatility()),String.valueOf(vE),String.valueOf(dE),String.valueOf(gE),
                String.valueOf(tPrice),String.valueOf(usedCap),val,"0",
                String.valueOf(nN),String.valueOf(fN),String.valueOf(xN),String.valueOf(sN)};
        recordValues(v);
        if(!placedOrders){
            placedOrders = true;
            placeOrders();
        }else{
            placedOrders = false;
            if(!twsOpen){
                System.out.println("DISCONNECTING AND EXITING");
                m_controller.disconnect();
                deepSleep(30000);
                System.exit(0);
            }else{
                meta();
            }
        }
    }
    private void placeOrders(){
        double vR = nC.getVega()/fC.getVega();
        int f = (int)((Double.parseDouble(val)/tPrice)/4);
        int n = (int)(Math.round(f/vR));
        n = -n;
        double dEx = f*fC.getDelta()+n*nC.getDelta();
        int x = (int)(f*fC.getDelta()+n*nC.getDelta());
        f = f-x;
        int s = (int)(Math.round((dEx-x)*100));
        s = -s;
        boolean closingOld = false;
        while(oldPos.size()>0){
            closingOld = true;
            NewContract aCon = oldPos.get(0).getCon();
            int aPos = oldPos.get(0).getPos();
            aCon.exchange("SMART");
            ordor(aCon, -aPos);
            oldPos.remove(0);
        }
        if(closingOld){
            deepSleep(Parameters.secondsWaitForOrders*1000);
        }
        ordor(sC.getNewCon(),s-sN);
        ordor(nC.getNewCon(),n-nN);
        ordor(fC.getNewCon(),f-fN);
        ordor(xC.getNewCon(),x-xN);
        deepSleep(Parameters.secondsWaitForOrders*1000);
        while(openOrders.size()>0){
            long id = openOrders.get(0);
            int intId = (int)id;
            m_controller.cancelOrder(intId);
            openOrders.remove(0);
        }
        getDateAndTime();
    }
    private void ordor(NewContract c, int n){
        if(n!=0){
            NewOrder o = new NewOrder();
            o.action(n>0?Action.BUY:Action.SELL);
            o.totalQuantity(Math.abs(n));
            o.orderType(OrderType.MKT);
            o.tif(TimeInForce.DAY);
            show("placing order "+n+" of "+c);
            m_controller.placeOrModifyOrder(c,o,new IOrderHandler(){
                public void orderState(NewOrderState orderState){
                    show("Order status is "+orderState.status());
                }
                @Override public void handle(int errorCode,String errorMsg){
                    show("Error code and message: "+errorCode+" "+errorMsg+" for order "+n+" of "+c);
                }
                @Override public void orderStatus(OrderStatus status,int filled,int remaining,double avgFillPrice,long permId,int parentId,double lastFillPrice,
                                                  int clientId,String whyHeld){
                    if(remaining!=0){
                        if(!openOrders.contains((long)o.orderId())){
                            System.out.println("adding order id: "+o.orderId()+". perm id: "+permId+" parentId: "+parentId);
                            openOrders.add((long)o.orderId());
                        }
                    }else{
                        openOrders.remove((long)o.orderId());
                    }
                }
            });
        }
    }
    private void deepSleep(long mils){
        long now1 = System.currentTimeMillis();
        try{
            Thread.sleep(mils);
        }catch(InterruptedException e){
            System.out.println("sleep was interrupted");
            long now2 = System.currentTimeMillis();
            deepSleep(mils-(now2-now1));
        }
    }

    private void recordValues(String[] v){
        int i = 0;
        StringBuilder sting = new StringBuilder();
        String p = dir+"datas.csv";
        BufferedReader fr = null;
        try{
            fr = new BufferedReader(new FileReader(p));
            while(i<v.length){
                i++;
                sting.append(fr.readLine()).append('\n');
            }
        }
        catch (Exception e){
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
        i = 0;
        try{
            FileWriter writer = new FileWriter(p);
            String[] stingy = sting.toString().split("\n");
            while(i<v.length){
                writer.append(stingy[i]);
                writer.append(',');
                writer.append(v[i]);
                writer.append('\n');
                i++;
            }
            writer.flush();
            writer.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
    @Override public void log(String valueOf){}
}