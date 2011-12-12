package com.lifetracking;

import java.util.Calendar;

//MyLife is essentially a collection of global/static settings.
public class MyLife {

	//User preferences.
	public static boolean m_showWelcomeScreen;
	
	//Graphing options.
	public static boolean m_useBigFont;
	public static boolean m_usePercentScale;
	
	//Other stuff.
	public static boolean m_loadedData;
	
	public static Calendar m_originDate;//all offsets are from this date
	static {
		m_originDate = Calendar.getInstance();
		m_originDate.set(1900, 1, 1);
		m_useBigFont = true;
		m_showWelcomeScreen = true;
	}

	//return - Calendar value for the point that is the given number of seconds away from the origin date.
	public static Calendar getCalendarFromOffset(long offset){
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(m_originDate.getTimeInMillis() + 1000L * offset);
		return calendar;
	}
	
	//return - number of seconds away from the origin date for the given calendar value.
	public static long getOffsetFromCalendar(Calendar calendar){
		return (calendar.getTimeInMillis() - m_originDate.getTimeInMillis()) / 1000L;
	}
}
