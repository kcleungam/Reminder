//      Comp 4521
//      Leung Ka Chun       20125844        kcleungam@ust.hk
//      To Wun Yin            20112524        wytoaa@ust.hk
//      Leung Chun Fai      20113619        cfleungac@ust.hk

package hk.ust.cse.comp4521.reminder.util;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Merged from TimeConvert.java and ISO8601DateParser
 * @author Jeffrey
 *
 */
public final class DateTimeParser {
	
	public enum Format{
		DATE, DATE_COMPACT, TIME, ALL, SHORT, RFC3339, RFC3339_COMPACT, ISO8601
	}

	public static final String[] MONTH={"January","February","March","April","May","June","July","August","September","October","November","December"};
	
	/**
	 * This class is not meant to create an object
	 */
	private DateTimeParser(){
	}
	
	private final static int convert(long time, int field){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		return cal.get(field);	
	}
	
	/**
	 * Get year
	 * @param time
	 * @return		integer year
	 */
	public final static int toYear(long time){
		return convert(time, Calendar.YEAR);
	}
	
	/**
	 * Get month, 0 is Jan, 1 is Feb, etc...
	 * @param time
	 * @return		integer month
	 */
	public final static int toMonth(long time){
		return convert(time, Calendar.MONTH);
	}
	
	public final static int toDayOfWeek(long time){
		return convert(time, Calendar.DAY_OF_WEEK);
	}
	
	/**
	 * Get day of month
	 * @param time
	 * @return		integer day
	 */
	public final static int toDay(long time){
		return convert(time, Calendar.DATE);
	}
	
	/**
	 * Get hour in 24 hours
	 * @param time
	 * @return		integer hour
	 */
	public final static int toHour(long time){
		return convert(time, Calendar.HOUR_OF_DAY);
	}
	
	public final static int toMin(long time){
		return convert(time, Calendar.MINUTE);
	}

	public static String toDate(long time){
		return toString(time, Format.DATE);
	}
	
	public static String toRFC3339(Date time) {
		return toString(time, Format.RFC3339);
	}
	
	public static String toRFC3339(long time) {
		return toString(time, Format.RFC3339);
	}
	
	private static SimpleDateFormat getFormat(Format format){
		SimpleDateFormat sdf = null;
		switch(format){
		case DATE:
			sdf = new SimpleDateFormat("yyyy-MM-dd");
			break;
		case DATE_COMPACT:
			sdf = new SimpleDateFormat("yyyyMMdd");
			break;
		case SHORT:
			sdf = new SimpleDateFormat("HH:mm");
			break;
		case TIME:
			sdf = new SimpleDateFormat("HH:mm:ss");
			break;
		case ALL:
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			break;
		case RFC3339_COMPACT:
			sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
			TimeZone tz = TimeZone.getTimeZone("GMT+8");
			sdf.setTimeZone(tz);
			break;
		case ISO8601:
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			break;
		case RFC3339:
		default:
			sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			tz = TimeZone.getTimeZone("GMT+8");
			sdf.setTimeZone(tz);
		}
		return sdf;
	}

	public static final Time toTime(String time, Format format) throws ParseException{
		if(time == null || time.isEmpty())
			return null;
		return new Time(getFormat(format).parse(time).getTime());
	}
	
	public static final Long toLong(String time, Format format) throws ParseException{
		if(time == null || time.isEmpty())
			return null;
		return getFormat(format).parse(time).getTime();
	}
	
	public static final String toString(Date time, Format format){
		if(time == null)
			return null;
		return getFormat(format).format(time);
	}

	public static final String toString(Time time, Format format){
		if(time == null)
			return null;
		return getFormat(format).format(time);
	}
	
	public static final String toString(long time, Format format){
		return getFormat(format).format(time);
	}

	public static final boolean validate(String time, Format format){
		try{
			getFormat(format).parse(time).getTime();
			return true;
		}catch(ParseException e){
			return false;
		}
	}

	/**
	 * Convert the Calendar.Month to the corresponding full name, like January. The full time has the capital letter in the beginning only.
	 * @param month starting from 0 (January)
	 * @return The full name has the capital letter in the beginning only.
     */
	public static final String toMonth(int month) throws IllegalArgumentException{
		if(month<0||month>11) throw	new IllegalArgumentException();
		return MONTH[month];
	}

	public static final int fromMonth(String month) throws IllegalArgumentException{
		List<String> arrayList= Arrays.asList(MONTH);
		int position=arrayList.indexOf(month);
		if(position==-1) throw new IllegalArgumentException();
		return position;
	}
}
