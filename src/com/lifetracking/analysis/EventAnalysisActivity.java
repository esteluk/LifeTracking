package com.lifetracking.analysis;

import java.util.ArrayList;
import java.util.Calendar;
import com.lifetracking.LifeTracking;
import com.lifetracking.MyLife;
import com.lifetracking.R;
import com.lifetracking.events.Event;
import com.lifetracking.events.EventType;
import com.lifetracking.graphing.TimeAxis;
import com.lifetracking.intervals.Interval;
import com.lifetracking.ui.GuiHelper;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

//IntervalAnalysisActivity analyzes an IntervalType.
public class EventAnalysisActivity extends LifeTracking.LifeActivity {

	public static EventType m_eventType;//this is the event type we'll analyze

	private static Calendar m_startDate, m_endDate;

	//Called when the activity is first created.
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(m_eventType == null) return;

		//Just reuse interval_analysis_screen.
		setContentView(R.layout.interval_analysis_screen);
		View topView = findViewById(R.id.IntervalAnalysisScreen_Layout);
		getWindow().setTitle(m_eventType.m_name + " analysis");

		//Analysis text.
		final TextView analysisText = (TextView) findViewById(R.id.IntervalAnalysisScreen_AnalysisText);

		OnDismissListener recalculateListener = new OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				analysisText.setText(getAnalysisText());
			}
		};

		m_startDate = MyLife.getCalendarFromOffset(m_eventType.m_events.get(0).m_start);
		m_endDate = Calendar.getInstance();
		GuiHelper.setupDateTimeButtons(topView, R.id.IntervalAnalysisScreen_StartTimeButton, R.id.IntervalAnalysisScreen_StartDateButton, m_startDate, recalculateListener);
		GuiHelper.setupDateTimeButtons(topView, R.id.IntervalAnalysisScreen_EndTimeButton, R.id.IntervalAnalysisScreen_EndDateButton, m_endDate, recalculateListener);

		recalculateListener.onDismiss(null);
	}
	
	//Create options menu (called once to create).
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		LifeTracking.g_menuInflater.inflate(R.layout.analysis_options_menu, menu);
		return true;
	}
    
	//Process option item selection event.
	@Override public boolean onOptionsItemSelected(MenuItem item) {
    	m_startDate = Calendar.getInstance();
    	m_endDate = Calendar.getInstance();
    	if(item.getItemId() == R.id.AnalysisOptionsMenu_LastWeekButton){
    		m_startDate.add(Calendar.DAY_OF_YEAR, -7);
    	} else if(item.getItemId() == R.id.AnalysisOptionsMenu_LastMonthButton){
    		m_startDate.add(Calendar.MONTH, -1);
    	} else if(item.getItemId() == R.id.AnalysisOptionsMenu_LastThreeMonthsButton){
    		m_startDate.add(Calendar.MONTH, -3);
    	} else if(item.getItemId() == R.id.AnalysisOptionsMenu_LastSixMonthsButton){
    		m_startDate.add(Calendar.MONTH, -6);
    	} else if(item.getItemId() == R.id.AnalysisOptionsMenu_LastYearButton){
    		m_startDate.add(Calendar.YEAR, -1);
    	} else if(item.getItemId() == R.id.AnalysisOptionsMenu_AllButton){
    		m_startDate = MyLife.getCalendarFromOffset(m_eventType.m_events.get(0).m_start);
    	}
    	
    	View topView = findViewById(R.id.IntervalAnalysisScreen_Layout);
    	GuiHelper.updateDateTimeButtons(topView, R.id.IntervalAnalysisScreen_StartTimeButton, R.id.IntervalAnalysisScreen_StartDateButton, m_startDate);
		GuiHelper.updateDateTimeButtons(topView, R.id.IntervalAnalysisScreen_EndTimeButton, R.id.IntervalAnalysisScreen_EndDateButton, m_endDate);
    	((TextView)findViewById(R.id.IntervalAnalysisScreen_AnalysisText)).setText(getAnalysisText());
    	return true;
    }

	//return - string with full analysis for the static interval type.
	private String getAnalysisText() {
		ArrayList<Event> events = getEvents();
		StringBuilder sb = new StringBuilder();
		sb.append("Events: " + events.size() + "\n");
		if (events.size() == 0) return sb.toString();
		
		sb.append("\n");
		//Breaks
		Interval interval = getLongestBreak(events);
		sb.append("Longest break is from " + interval.getStartTimeDateString() + " to " + interval.getEndTimeDateString() + " (" + TimeAxis.secondsToString(interval.getDuration()) + "). " + "\n");

		interval = getShortestBreak(events);
		sb.append("Shortest break is from " + interval.getStartTimeDateString() + " to " + interval.getEndTimeDateString() + " (" + TimeAxis.secondsToString(interval.getDuration()) + "). " + "\n");

		long breakDuration = getMeanBreak(events);
		int scaleIndex = TimeAxis.getSecondsToStringScaleIndex(breakDuration);
		sb.append("Mean break: " + TimeAxis.secondsToString(breakDuration, scaleIndex) + "\n");
		sb.append("Standard deviation: " + TimeAxis.secondsToString(getStandardDeviationForBreaks(events, breakDuration), scaleIndex) + "\n");
		
		return sb.toString();
	}

	//return - all events that fall between the user-set dates.
	private ArrayList<Event> getEvents() {
		long start = MyLife.getOffsetFromCalendar(m_startDate), end = MyLife.getOffsetFromCalendar(m_endDate);
		ArrayList<Event> list = new ArrayList<Event>();
		for (Event e : m_eventType.m_events) {
			if (e.m_start < start || e.m_start > end)
				continue;
			list.add(e);
		}
		return list;
	}

	//return - interval corresponding to the shortest break between the given events.
	private Interval getShortestBreak(ArrayList<Event> events) {
		long time = Long.MAX_VALUE;
		int minIndex = -1;
		for (int n = 1; n < events.size(); n++) {
			long duration = events.get(n).m_start - events.get(n - 1).m_start;
			if (duration < time) {
				time = duration;
				minIndex = n;
			}
		}
		Interval shortestBreak = new Interval();
		shortestBreak.m_start = events.get(minIndex - 1).m_start; shortestBreak.m_end = events.get(minIndex).m_start;
		return shortestBreak;
	}

	//return - interval corresponding to the shortest break between the given events.
	private Interval getLongestBreak(ArrayList<Event> events) {
		long time = -Long.MAX_VALUE;
		int maxIndex = -1;
		for (int n = 1; n < events.size(); n++) {
			long duration = events.get(n).m_start - events.get(n - 1).m_start;
			if (duration > time) {
				time = duration;
				maxIndex = n;
			}
		}
		Interval shortestBreak = new Interval();
		shortestBreak.m_start = events.get(maxIndex - 1).m_start; shortestBreak.m_end = events.get(maxIndex).m_start;
		return shortestBreak;
	}

	//return - average/mean duration for breaks between the given events.
	private long getMeanBreak(ArrayList<Event> events) {
		long totalTime = 0;
		for (int n = 1; n < events.size(); n++) {
			totalTime += events.get(n).m_start - events.get(n - 1).m_start;
		}
		totalTime /= events.size() - 1;
		return totalTime;
	}

	//return - standard deviation for the breaks between the given intervals.
	private long getStandardDeviationForBreaks(ArrayList<Event> events, long average) {
		double sum = 0, size = (double) events.size();
		for (int n = 1; n < events.size(); n++) {
			long duration = events.get(n).m_start - events.get(n - 1).m_start;
			sum += (duration - average) * (duration - average) / size;
		}
		return Math.round(Math.sqrt(sum));
	}
}
