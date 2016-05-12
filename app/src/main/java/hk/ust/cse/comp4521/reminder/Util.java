package hk.ust.cse.comp4521.reminder;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by Jeffrey on 13/5/2016.
 */
public class Util {

    public static String toWkday(boolean[] bs){
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<bs.length; i++){
            if(bs[i]==true)
                sb.append(i+1);
        }
        return sb.toString();
    }

}
