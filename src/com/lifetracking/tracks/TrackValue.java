package com.lifetracking.tracks;

import java.util.Calendar;
import com.lifetracking.LifeTracking;
import com.lifetracking.MyLife;
import com.lifetracking.UtilHelper;
import android.text.format.DateFormat;

//TrackValue stores one value in a Track, with additional information.
public class TrackValue {
	
	public long m_dateOffset;//offset from MyLife.m_originDate (in seconds)
	public double m_value;//value the user entered
	public String m_note;//any notes attached to this value
	
	//Create new TrackValue.
	public TrackValue(double value){
		m_dateOffset = MyLife.getOffsetFromCalendar(Calendar.getInstance());
		m_value = value;
		m_note = "";
	}
	
	//Create new TrackValue, but with present date.
	public TrackValue(double value, Calendar date){
		m_dateOffset = MyLife.getOffsetFromCalendar(date);
		m_value = value;
		m_note = "";
	}
	
	//return - date when this track value was entered.
	public Calendar getDate(){
		return MyLife.getCalendarFromOffset(m_dateOffset);
	}
	
	//return - track value formatted nicely.
	public String getValueString(){
		return UtilHelper.valueToString(m_value);
	}
	
	//return - date when this track was entered formated according to the given format.
	public String formatDate(String format){
		return DateFormat.format(format, getDate().getTime()).toString();
	}
	
	//return - string corresponding to the date when this value was entered.
	public String getDateString(){
		return DateFormat.getDateFormat(LifeTracking.g_globalContext).format(getDate().getTime());
	}
	
	//return - string corresponding to the time when this value was entered.
	public String getTimeString(){
		return DateFormat.getTimeFormat(LifeTracking.g_globalContext).format(getDate().getTime());
	}
	
	//return - string corresponding to the time and date when this interval started.
	public String getTimeDateString(){
		return getTimeString() + ", " + getDateString();
	}
	
	//return - a nice string representation of this track value.
	public String toString(Track myTrack){
		String note = m_note.length() > 0 ? (" Note: \"" + m_note + "\".") : "";
		return myTrack.m_name + " value of " + getValueString() + " added on " + getTimeDateString() + "." + note;
	}
}
