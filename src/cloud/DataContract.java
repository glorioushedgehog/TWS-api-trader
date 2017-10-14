package cloud;

import com.ib.controller.NewContract;
import com.ib.controller.NewTickType;
import com.ib.controller.Types;
import com.ib.controller.Types.Right;

public class DataContract {
    private double price;
    private double bid;
    private double ask;

    private double delta;
    private double vega;
    private double gamma;
    private double theta;
    private double impliedVolatility;

    private NewContract newCon;


    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    double getBid() {
        return bid;
    }

    private void setBid(double bid) {
        this.bid = bid;
    }

    double getAsk() {
        return ask;
    }

    private void setAsk(double ask) {
        this.ask = ask;
    }

    public double getDelta() {
        return delta;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    double getVega() {
        return vega;
    }

    private void setVega(double vega) {
        this.vega = vega;
    }

    double getGamma() {
        return gamma;
    }

    private void setGamma(double gamma) {
        this.gamma = gamma;
    }

    double getTheta() {
        return theta;
    }

    private void setTheta(double theta) {
        this.theta = theta;
    }

    double getImpliedVolatility() {
        return impliedVolatility;
    }

    private void setImpliedVolatility(double impliedVolatility) {
        this.impliedVolatility = impliedVolatility;
    }

    NewContract getNewCon() {
        return newCon;
    }

    void setNewCon(NewContract newCon) {
        this.newCon = newCon;
    }
    void setData(NewTickType tickType, double price) {
        if (tickType == NewTickType.LAST) {
            this.setPrice(price);
        } else if (tickType == NewTickType.BID) {
            this.setBid(price);
        } else if (tickType == NewTickType.ASK) {
            this.setAsk(price);
        }
    }
    void setOptionStats(double impliedVol, double delta, double gamma, double vega, double theta){
        this.setDelta(delta);
        this.setVega(vega);
        this.setGamma(gamma);
        this.setTheta(theta);
        this.setImpliedVolatility(impliedVol);
    }

    void defineNewCon(int s, String day2, String symbol, Right call) {
        NewContract c = new NewContract();
        c.currency("USD");
        c.exchange("SMART");
        c.expiry(day2);
        c.tradingClass(symbol);
        c.multiplier("100");
        c.right(call);
        c.secType(Types.SecType.OPT);
        c.strike(s);
        c.symbol(symbol);
        this.setNewCon(c);
    }
}
