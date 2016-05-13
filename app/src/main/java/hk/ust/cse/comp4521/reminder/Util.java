package hk.ust.cse.comp4521.reminder;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
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
                sb.append(i);
        }
        return sb.toString();
    }

    public static boolean[] toRepeat(String s, int length){
        boolean[] bs = new boolean[length];
        for(int i=0; i<s.length(); i++){
            bs[Integer.parseInt("" + s.charAt(i))] = true;
        }
        return bs;
    }

}
