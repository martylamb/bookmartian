package com.martiansoftware.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author mlamb
 */
public class Dates {
    
    // Dates also contain time data.  When we compare dates (e.g. "give me all
    // Bookmarks created on 2016/09/17") it's convenient to drop the time data
    // from the Bookmark's creation timestamp to simplify the comparison.
    public static Date stripTime(Date d) {
        if (d == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
    public static Date today() {
        return stripTime(new Date());
    }
    
    public static Date yesterday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return stripTime(cal.getTime());
    }
    
}
