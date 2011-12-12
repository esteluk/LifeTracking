package com.lifetracking.analysis;

import java.util.ArrayList;
import java.util.Calendar;
import com.lifetracking.LifeTracking;
import com.lifetracking.MyLife;
import com.lifetracking.R;
import com.lifetracking.UtilHelper;
import com.lifetracking.graphing.TimeAxis;
import com.lifetracking.intervals.Interval;
import com.lifetracking.intervals.IntervalType;
import com.lifetracking.ui.GuiHelper;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

//IntervalAnalysisActivity analyzes an IntervalType.
public class IntervalAnalysisActivity extends LifeTracking.LifeActivity {

	public static IntervalType m_intervalType;//this is the interval type we'll analyze

	private static Calendar m_startDate, m_endDate;

	//Called when the activity is first created.
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(m_intervalType == null) return;

		setContentView(R.layout.interval_analysis_screen);
		View topView = findViewById(R.id.IntervalAnalysisScreen_Layout);
		getWindow().setTitle(m_intervalType.m_name + " analysis");

		//Analysis text.
		final TextView analysisText = (TextView) findViewById(R.id.IntervalAnalysisScreen_AnalysisText);

		OnDismissListener recalculateListener = new OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				analysisText.setText(getAnalysisText());
			}
		};

		m_startDate = MyLife.getCalendarFromOffset(m_intervalType.m_intervals.get(0).m_start);
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
    		m_startDate = MyLife.getCalendarFromOffset(m_intervalType.m_intervals.get(0).m_start);
    	}
    	
    	View topView = findViewById(R.id.IntervalAnalysisScreen_Layout);
    	GuiHelper.updateDateTimeButtons(topView, R.id.IntervalAnalysisScreen_StartTimeButton, R.id.IntervalAnalysisScreen_StartDateButton, m_startDate);
		GuiHelper.updateDateTimeButtons(topView, R.id.IntervalAnalysisScreen_EndTimeButton, R.id.IntervalAnalysisScreen_EndDateButton, m_endDate);
    	((TextView)findViewById(R.id.IntervalAnalysisScreen_AnalysisText)).setText(getAnalysisText());
    	return true;
    }

	//return - string with full analysis for the static interval type.
	private String getAnalysisText() {
		ArrayList<Interval> intervals = getIntervals();
		StringBuilder sb = new StringBuilder();
		sb.append("Intervals: " + intervals.size() + "\n");
		if (intervals.size() == 0) return sb.toString();

		sb.append("\n");
		//Intervals
		Interval interval = getLongestInterval(intervals);
		String note = interval.m_note.length() > 0 ? ("Note: \"" + interval.m_note + "\".") : "";
		sb.append("Longest interval is from " + interval.getStartTimeDateString() + " to " + interval.getEndTimeDateString() + " (" + TimeAxis.secondsToString(interval.getDuration()) + "). " + note + "\n");

		interval = getShortestInterval(intervals);
		note = interval.m_note.length() > 0 ? ("Note: \"" + interval.m_note + "\".") : "";
		sb.append("Shortest interval is from " + interval.getStartTimeDateString() + " to " + interval.getEndTimeDateString() + " (" + TimeAxis.secondsToString(interval.getDuration()) + "). " + note + "\n");

		long duration = getMeanDuration(intervals);
		int scaleIndex = TimeAxis.getSecondsToStringScaleIndex(duration);
		sb.append("Mean duration: " + TimeAxis.secondsToString(duration, scaleIndex) + "\n");
		sb.append("Standard deviation: " + TimeAxis.secondsToString(getStandardDeviation(intervals, duration), scaleIndex) + "\n");
		
		sb.append("\n");
		//Breaks
		interval = getLongestBreak(intervals);
		if(interval != null) sb.append("Longest break is from " + interval.getStartTimeDateString() + " to " + interval.getEndTimeDateString() + " (" + TimeAxis.secondsToString(interval.getDuration()) + "). " + "\n");

		interval = getShortestBreak(intervals);
		if(interval != null) sb.append("Shortest break is from " + interval.getStartTimeDateString() + " to " + interval.getEndTimeDateString() + " (" + TimeAxis.secondsToString(interval.getDuration()) + "). " + "\n");

		if(intervals.size() > 1){
			long breakDuration = getMeanBreak(intervals);
			scaleIndex = TimeAxis.getSecondsToStringScaleIndex(breakDuration);
			sb.append("Mean break: " + TimeAxis.secondsToString(breakDuration, scaleIndex) + "\n");
			sb.append("Standard deviation: " + TimeAxis.secondsToString(getStandardDeviationForBreaks(intervals, breakDuration), scaleIndex) + "\n");
		}
		
		sb.append("\n");
		//More
		long startToEnd = MyLife.getOffsetFromCalendar(m_endDate) - MyLife.getOffsetFromCalendar(m_startDate);
		sb.append("Total time spent: " + TimeAxis.secondsToString(m_totalDuration) + " out of " + TimeAxis.secondsToString(startToEnd) + ". Which is " + UtilHelper.valueToString(m_totalDuration / (double)startToEnd * 100.0) + "%.");
		return sb.toString();
	}

	//return - all intervals that fall between the user-set dates.
	private ArrayList<Interval> getIntervals() {
		long start = MyLife.getOffsetFromCalendar(m_startDate), end = MyLife.getOffsetFromCalendar(m_endDate);
		ArrayList<Interval> list = new ArrayList<Interval>();
		for (Interval i : m_intervalType.m_intervals) {
			if(i.isInProgress()) continue;
			if(i.m_start < start || i.m_end > end) continue;
			list.add(i);
		}
		return list;
	}

	//return - interval with the shortest duration from the given intervals.
	private Interval getShortestInterval(ArrayList<Interval> intervals) {
		long time = Long.MAX_VALUE;
		int minIndex = -1;
		for (int n = 0; n < intervals.size(); n++) {
			long duration = intervals.get(n).getDuration();
			if (duration < time) {
				time = duration;
				minIndex = n;
			}
		}
		return intervals.get(minIndex);
	}

	//return - interval with the shortest time from the given intervals.
	private Interval getLongestInterval(ArrayList<Interval> intervals) {
		long time = -Long.MAX_VALUE;
		int maxIndex = -1;
		for (int n = 0; n < intervals.size(); n++) {
			long duration = intervals.get(n).getDuration();
			if (duration > time) {
				time = duration;
				maxIndex = n;
			}
		}
		return intervals.get(maxIndex);
	}

	//return - average/mean duration for the intervals.
	private long m_totalDuration = 0;//will be set to total duration for all intervals
	private long getMeanDuration(ArrayList<Interval> intervals) {
		m_totalDuration = 0;
		for (Interval i : intervals) {
			m_totalDuration += i.getDuration();
		}
		return m_totalDuration / intervals.size();
	}

	//return - standard deviation for the intervals.
	private long getStandardDeviation(ArrayList<Interval> intervals, long average) {
		double sum = 0, size = (double) intervals.size();
		for (Interval i : intervals) {
			sum += (i.getDuration() - average) * (i.getDuration() - average) / size;
		}
		return Math.round(Math.sqrt(sum));
	}

	//return - interval corresponding to the shortest break between the given intervals.
	private Interval getShortestBreak(ArrayList<Interval> intervals) {
		long time = Long.MAX_VALUE;
		int minIndex = -1;
		for (int n = 1; n < intervals.size(); n++) {
			long duration = intervals.get(n).m_start - intervals.get(n - 1).m_end;
			if (duration < time) {
				time = duration;
				minIndex = n;
			}
		}
		if(minIndex < 0) return null;
		Interval shortestBreak = new Interval();
		shortestBreak.m_start = intervals.get(minIndex - 1).m_end; shortestBreak.m_end = intervals.get(minIndex).m_start;
		return shortestBreak;
	}

	//return - interval corresponding to the shortest break between the given intervals.
	private Interval getLongestBreak(ArrayList<Interval> intervals) {
		long time = -Long.MAX_VALUE;
		int maxIndex = -1;
		for (int n = 1; n < intervals.size(); n++) {
			long duration = intervals.get(n).m_start - intervals.get(n - 1).m_end;
			if (duration > time) {
				time = duration;
				maxIndex = n;
			}
		}
		if(maxIndex < 0) return null;
		Interval shortestBreak = new Interval();
		shortestBreak.m_start = intervals.get(maxIndex - 1).m_end; shortestBreak.m_end = intervals.get(maxIndex).m_start;
		return shortestBreak;
	}

	//return - average/mean duration for breaks between the given intervals.
	private long getMeanBreak(ArrayList<Interval> intervals) {
		long totalTime = 0;
		for (int n = 1; n < intervals.size(); n++) {
			totalTime += intervals.get(n).m_start - intervals.get(n - 1).m_end;
		}
		totalTime /= intervals.size() - 1;
		return totalTime;
	}

	//return - standard deviation for the breaks between the given intervals.
	private long getStandardDeviationForBreaks(ArrayList<Interval> intervals, long average) {
		double sum = 0, size = (double) intervals.size();
		for (int n = 1; n < intervals.size(); n++) {
			long duration = intervals.get(n).m_start - intervals.get(n - 1).m_end;
			sum += (duration - average) * (duration - average) / size;
		}
		return Math.round(Math.sqrt(sum));
	}
}
