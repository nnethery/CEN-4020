package com.example.zanj.cen4020;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Convenience methods for getting timestamps.
 */
public class DateTimeUtil {
    public static String getTimeStampUtc() {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm", Locale.US);
        Date today = Calendar.getInstance().getTime();
        return df.format(today);
    }

    public static String getTimeStampUtc(long instant) {
        return ISODateTimeFormat.dateTime().withZoneUTC().print(instant);
    }
}
