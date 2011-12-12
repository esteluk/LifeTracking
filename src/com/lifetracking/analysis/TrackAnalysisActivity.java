package com.lifetracking.analysis;

import java.util.ArrayList;
import java.util.Calendar;
import com.lifetracking.LifeTracking;
import com.lifetracking.MyLife;
import com.lifetracking.R;
import com.lifetracking.UtilHelper;
import com.lifetracking.graphing.TimeAxis;
import com.lifetracking.tracks.Track;
import com.lifetracking.tracks.TrackValue;
import com.lifetracking.ui.GuiHelper;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

//TrackAnalysisActivity analyzes a Track.
public class TrackAnalysisActivity extends LifeTracking.LifeActivity {

	public static Track m_track;//this is the track we'll analyze

	private static Calendar m_startDate, m_endDate;

	//Called when the activity is first created.
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(m_track == null) return;

		setContentView(R.layout.track_analysis_screen);
		View topView = findViewById(R.id.TrackAnalysisScreen_Layout);
		getWindow().setTitle(m_track.m_name + " analysis");

		//Analysis text.
		final TextView analysisText = (TextView)findViewById(R.id.TrackAnalysisScreen_AnalysisText);

		OnDismissListener recalculateListener = new OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				analysisText.setText(getAnalysisText());
			}
		};

		m_startDate = MyLife.getCalendarFromOffset(m_track.m_values.get(0).m_dateOffset);
		m_endDate = Calendar.getInstance();
		GuiHelper.setupDateTimeButtons(topView, R.id.TrackAnalysisScreen_StartTimeButton, R.id.TrackAnalysisScreen_StartDateButton, m_startDate, recalculateListener);
		GuiHelper.setupDateTimeButtons(topView, R.id.TrackAnalysisScreen_EndTimeButton, R.id.TrackAnalysisScreen_EndDateButton, m_endDate, recalculateListener);

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
    		m_startDate = MyLife.getCalendarFromOffset(m_track.m_values.get(0).m_dateOffset);
    	}
    	
    	View topView = findViewById(R.id.TrackAnalysisScreen_Layout);
    	GuiHelper.updateDateTimeButtons(topView, R.id.TrackAnalysisScreen_StartTimeButton, R.id.TrackAnalysisScreen_StartDateButton, m_startDate);
		GuiHelper.updateDateTimeButtons(topView, R.id.TrackAnalysisScreen_EndTimeButton, R.id.TrackAnalysisScreen_EndDateButton, m_endDate);
    	((TextView)findViewById(R.id.TrackAnalysisScreen_AnalysisText)).setText(getAnalysisText());
    	return true;
    }

	//return - string with full analysis for the static track type.
	private String getAnalysisText() {
		ArrayList<TrackValue> trackValues = getTrackValues();
		StringBuilder sb = new StringBuilder();
		sb.append("Track values: " + trackValues.size() + "\n");
		if (trackValues.size() == 0) return sb.toString();

		sb.append("\n");
		//Track values
		TrackValue value = getHighestTrackValue(trackValues);
		String note = value.m_note.length() > 0 ? ("Note: \"" + value.m_note + "\".") : "";
		sb.append("Highest value of " + value.getValueString() + " is at " + value.getTimeDateString() + ". " + note + "\n");

		value = getLowestTrackValue(trackValues);
		note = value.m_note.length() > 0 ? ("Note: \"" + value.m_note + "\".") : "";
		sb.append("Lowest value of " + value.getValueString() + " is at " + value.getTimeDateString() + ". " + note + "\n");

		double meanValue = getMeanValue(trackValues);
		sb.append("Mean value: " + UtilHelper.valueToString(meanValue) + "\n");
		sb.append("Standard deviation: " + UtilHelper.valueToString(getStandardDeviation(trackValues, meanValue)) + "\n");
		double slope = getSlopeOfBestFitLine(trackValues, meanValue);
		sb.append("Slope of best fit line: " + UtilHelper.valueToString(slope) + " units per " + m_slopeUnits + "\n");
		
		return sb.toString();
	}

	//return - all track values that fall between the user-set dates.
	private ArrayList<TrackValue> getTrackValues() {
		long start = MyLife.getOffsetFromCalendar(m_startDate), end = MyLife.getOffsetFromCalendar(m_endDate);
		ArrayList<TrackValue> list = new ArrayList<TrackValue>();
		for (TrackValue v : m_track.m_values) {
			if (v.m_dateOffset < start || v.m_dateOffset > end) continue;
			list.add(v);
		}
		return list;
	}
	
	//return - track value with the highest value from the given track values.
	private TrackValue getHighestTrackValue(ArrayList<TrackValue> trackValues) {
		double value = -Double.MAX_VALUE;
		int maxIndex = -1;
		for (int n = 0; n < trackValues.size(); n++) {
			double curValue = trackValues.get(n).m_value;
			if (curValue > value) {
				value = curValue;
				maxIndex = n;
			}
		}
		return trackValues.get(maxIndex);
	}

	//return - track value with the lowest value from the given track values.
	private TrackValue getLowestTrackValue(ArrayList<TrackValue> trackValues) {
		double value = Double.MAX_VALUE;
		int minIndex = -1;
		for (int n = 0; n < trackValues.size(); n++) {
			double curValue = trackValues.get(n).m_value;
			if (curValue < value) {
				value = curValue;
				minIndex = n;
			}
		}
		return trackValues.get(minIndex);
	}

	//return - average/mean value for the given track values.
	private double getMeanValue(ArrayList<TrackValue> trackValues) {
		double total = 0;
		for (TrackValue v : trackValues) {
			total += v.m_value;
		}
		total /= trackValues.size();
		return total;
	}

	//return - standard deviation for the given track values.
	private double getStandardDeviation(ArrayList<TrackValue> trackValues, double average) {
		double sum = 0, size = (double) trackValues.size();
		for (TrackValue v : trackValues) {
			sum += (v.m_value - average) * (v.m_value - average) / size;
		}
		return Math.sqrt(sum);
	}
	
	//return - slope of the line of best fit for the given track values.
	private String m_slopeUnits;//will be set to units for the x-axis.
	private double getSlopeOfBestFitLine(ArrayList<TrackValue> trackValues, double avgY){
		long avgXLong = 0;
		for (TrackValue v : trackValues) {
			avgXLong += v.m_dateOffset;
		}
		avgXLong /= trackValues.size();
		int scaleIndex = TimeAxis.getSecondsToStringScaleIndex(trackValues.get(trackValues.size() - 1).m_dateOffset - trackValues.get(0).m_dateOffset);
		long scale = TimeAxis.m_timeIntervals[scaleIndex];
		double avgX = avgXLong / (double)scale;

		double topSum = 0.0, bottomSum = 0.0;
		for (TrackValue v : trackValues) {
			double dx = (v.m_dateOffset / (double)scale - avgX);
			topSum += dx * (v.m_value - avgY);
			bottomSum += dx * dx;
		}
		
		m_slopeUnits = TimeAxis.getLabel(scale);//can't return 2 vars so FUCK YOU, JAVA!
		return topSum / bottomSum;
	}
}
