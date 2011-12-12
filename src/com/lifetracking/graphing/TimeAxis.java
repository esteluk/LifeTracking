package com.lifetracking.graphing;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.text.format.DateFormat;
import com.lifetracking.LifeTracking;
import com.lifetracking.UtilHelper;

//TimeAxis deals with an axis measured in units of time.
public class TimeAxis {

	//Various time intervals measured in seconds.
	public static final long TIME_SECOND = 1L;
	public static final long TIME_MINUTE = 60L * TIME_SECOND;
	public static final long TIME_HOUR = 60L * TIME_MINUTE;
	public static final long TIME_DAY = 24L * TIME_HOUR;
	public static final long TIME_WEEK = 7L * TIME_DAY;
	public static final long TIME_MONTH = 30L * TIME_DAY;
	public static final long TIME_YEAR = 365L * TIME_DAY;
	
	//List of all time intervals used for displaying time.
	public static long m_timeIntervals[] = new long[] { TimeAxis.TIME_SECOND, TimeAxis.TIME_MINUTE, TimeAxis.TIME_HOUR, TimeAxis.TIME_DAY, TimeAxis.TIME_MONTH, TimeAxis.TIME_YEAR };
	
	//TimeScale holds the values associated with an axis.
	public static class TimeScale {
		public long m_hashDist;//distance between hash marks
		public String m_dateFormat;//format used for the date
		public String m_label;//axis label text
		private SimpleDateFormat m_formatter;
		
		//Create new TimeScale.
		public TimeScale(long hashX, String dateFormat, String dateFormat24, String label){
			m_hashDist = hashX;
			m_dateFormat = DateFormat.is24HourFormat(LifeTracking.g_globalContext) ? dateFormat24 : dateFormat;
			m_label = label;
			m_formatter = new SimpleDateFormat(m_dateFormat);
		}
		
		//return - string representation of the given date, formatted according to this TimeScale.
		public String formatDate(Calendar date){
			//String result = DateFormat.format(m_dateFormat, date.getTime()).toString();
			//if(m_hashDist == TIME_HOUR) result = result.toUpperCase();
			String result = m_formatter.format(date.getTime());
			return result;
		}
	}
	//These time scales help setup correct hash mark values for the axis.
	private final static TimeScale m_timeScales[] = new TimeScale[]{
		new TimeScale(10L * TIME_YEAR,   "yy",        "yy",      "years"),
		new TimeScale(TIME_YEAR,         "yy",        "yy",      "years"),
		new TimeScale(TIME_MONTH,        "MMM",       "MMM",     "months"),
		new TimeScale(TIME_DAY,          "E.d",       "E.d",     "days"),
		new TimeScale(TIME_HOUR,         "ha",        "H:00",    "hours"),
		new TimeScale(5L * TIME_MINUTE,  "h:mm a",    "H:mm",    "minutes"),
		new TimeScale(TIME_MINUTE,       "h:mm a",    "H:mm",    "minutes"),
		new TimeScale(5L * TIME_SECOND,  "h:mm:ss a", "H:mm:ss", "seconds"),
		new TimeScale(TIME_SECOND,       "h:ss:ss a", "H:mm:ss", "seconds")
	};
	
	//Compute the time scale for this axis.
	//axisLength - length of the axis.
	//return - TimeScale computed for this axis.
	public static TimeScale computeAxisVariables(long axisLength){
		for(int n = 0; n < m_timeScales.length; n++){
			if(axisLength >= 2L * m_timeScales[n].m_hashDist){
				return m_timeScales[n];
			}
		}
		return m_timeScales[0];
	}
	
	//return - label for one of the measurements (day, week, etc).
	public static String getLabel(long time){
		for(int n = 0; n < m_timeScales.length; n++){
			if(m_timeScales[n].m_hashDist == time) return m_timeScales[n].m_label;
		}
		return "";
	}

	//return - best date/time format to describe unchanging part of the time given the interval length. 
	public static String getMoreFormat(long hashDist) {
		if(hashDist <= TimeAxis.TIME_HOUR) return "d MMM yyyy";
		else if(hashDist <= TimeAxis.TIME_DAY) return "MMM yyyy";
		else if(hashDist <= TimeAxis.TIME_MONTH) return "yyyy";
		return "";
	}

	//return - index into m_timeIntervals that best fits the given number of seconds.
	public static int getSecondsToStringScaleIndex(long seconds) {
		int index = 0;
		for(; index < m_timeIntervals.length; index++) {
			if(seconds <= m_timeIntervals[index])
				break;
		}
		if(index > 0) index--;
		return index;
	}

	//return - a nice time/date representation for the given number of seconds.
	public static String secondsToString(long seconds) {
		return secondsToString(seconds, getSecondsToStringScaleIndex(seconds));
	}

	//return - a nice time/date representation for the given number of seconds. Forced to use the given scale.
	//scaleIndex - index into m_timeIntervals.
	public static String secondsToString(long seconds, int scaleIndex) {
		return UtilHelper.valueToString(seconds / (double) m_timeIntervals[scaleIndex]) + " " + TimeAxis.getLabel(m_timeIntervals[scaleIndex]);
	}
}
