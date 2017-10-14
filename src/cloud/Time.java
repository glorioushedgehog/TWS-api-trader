package cloud;

import com.sun.org.apache.xpath.internal.operations.Or;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

class Time {
    private static String marketOpen = "06:30:00.000";
    private static String marketClose = "13:00:00.000";
    static boolean outOfSync() {
        long n = System.currentTimeMillis();
        Calendar b = Calendar.getInstance();
        String d = dateFromCalendar(b);
        Date o = new Date();
        Date c = new Date();
        String os = d + " " + marketOpen;
        String cs = d + " " + marketClose;
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
        try {
            o = df.parse(os);
            c = df.parse(cs);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int dayOfWeek = b.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek != 1 && dayOfWeek != 7 && n > (o.getTime() - Parameters.prepMins * 60 * 1000) && n <= (c.getTime() - (Parameters.prepMins + Parameters.minsBeforeClose) * 60 * 1000);
    }


    static String getDay1(){
        Calendar b = Calendar.getInstance();
        int dayOfWeek = b.get(Calendar.DAY_OF_WEEK);
        if(dayOfWeek==6){
            String d = dateFromCalendar(b);
            Date n = b.getTime();
            Date o = new Date();
            String os = d+" "+Parameters.lastTradeTime+".000";
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
            try{
                o = df.parse(os);
            }catch(ParseException e){
                e.printStackTrace();
            }
            if(n.getTime()>o.getTime()){
                b.add(Calendar.DATE, 7);
            }
        }else if(dayOfWeek==7){
            b.add(Calendar.DATE, 6);
        }else{
            b.add(Calendar.DATE, 6-dayOfWeek);
        }
        return dateFromCalendar(b);
    }
    static String getDay2(String s){
        String sting = "";
        String day = "";
        BufferedReader fr = null;
        try{
            fr = new BufferedReader(new FileReader(OrderPlacer.dir+"expirations.csv"));
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
        String[] ex = sting.split(";");
        int i = 0;
        boolean boo = false;
        int currentMonth = Integer.parseInt(s.substring(4,6));
        int currentYear = Integer.parseInt(s.substring(0,4));
        while(i<ex.length&&!boo){
            if(Integer.parseInt(ex[i].substring(0,4))>currentYear){
                if(Integer.parseInt(ex[i].substring(4,6))==(currentMonth+Parameters.monthsApart-12)){
                    day = ex[i];
                    boo = true;
                }
            }else{
                if(Integer.parseInt(ex[i].substring(4,6))==(currentMonth+Parameters.monthsApart)){
                    day = ex[i];
                    boo = true;
                }
            }
            i++;
        }
        return day;
    }
    static String dateFromCalendar(Calendar c){
        int month = c.get(Calendar.MONTH)+1;
        int day = c.get(Calendar.DATE);
        String mo;
        String da;
        String d;
        if(month<10){
            mo = "0"+String.valueOf(month);
        }else{
            mo = String.valueOf(month);
        }
        if(day<10){
            da = "0"+String.valueOf(day);
        }else{
            da = String.valueOf(day);
        }
        d = c.get(Calendar.YEAR)+mo+da;
        return d;
    }
    static long getSleep(){
        long n = System.currentTimeMillis();
        Calendar b = Calendar.getInstance();
        String d = dateFromCalendar(b);
        Date o = new Date();
        Date c = new Date();
        String os = d+" "+ marketOpen;
        String cs = d+" "+ marketClose;
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
        try{
            o = df.parse(os);
            c = df.parse(cs);
        }catch(ParseException e){
            e.printStackTrace();
        }
        long sleep = 0;
        boolean night = false;
        int dayOfWeek = b.get(Calendar.DAY_OF_WEEK);
        if(n<=(o.getTime()-Parameters.prepMins*60*1000)){
            sleep = o.getTime()-Parameters.prepMins*60*1000-n;
        }else if(n<=(c.getTime()-(Parameters.prepMins+Parameters.minsBeforeClose)*60*1000)){
            sleep = c.getTime()-(Parameters.prepMins+Parameters.minsBeforeClose)*60*1000-n;
        }else{
            sleep = o.getTime()+(24*60-Parameters.prepMins)*60*1000-n;
            night = true;
        }
        if(dayOfWeek==1){
            sleep = o.getTime()+(24*60-Parameters.prepMins)*60*1000-n;
        }else if(dayOfWeek==7){
            sleep = o.getTime()+(48*60-Parameters.prepMins)*60*1000-n;
        }else if(dayOfWeek==6&&night){
            sleep = o.getTime()+(72*60-Parameters.prepMins)*60*1000-n;
        }
        return sleep;
    }
}
