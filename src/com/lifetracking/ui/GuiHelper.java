package com.lifetracking.ui;

import java.util.Calendar;
import com.lifetracking.LifeTracking;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface.OnDismissListener;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

//GuiHelper contains helpful functions to manage GUI.
public class GuiHelper {

	//Setup time/date buttons that change the given Calendar value.
	public static void setupDateTimeButtons(View layout, int timeButtonId, int dateButtonId, final Calendar date){
		setupDateTimeButtons(layout, timeButtonId, dateButtonId, date, null);
	}
	
	//Setup time/date buttons that change the given Calendar value.
	//listener - will be called when date/time is dismissed. No guarantee the date/time is changed.
	public static void setupDateTimeButtons(View layout, int timeButtonId, int dateButtonId, final Calendar date, final OnDismissListener listener){
		final Button timeButton = (Button) layout.findViewById(timeButtonId);
		timeButton.setText(DateFormat.getTimeFormat(LifeTracking.g_globalContext).format(date.getTime()));
		timeButton.setOnClickListener(new OnClickListener() { public void onClick(View v) {
			TimePickerDialog dialog = new TimePickerDialog(LifeTracking.g_globalContext, new TimePickerDialog.OnTimeSetListener() {
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					date.set(Calendar.HOUR_OF_DAY, hourOfDay);
					date.set(Calendar.MINUTE, minute);
                	timeButton.setText(DateFormat.getTimeFormat(LifeTracking.g_globalContext).format(date.getTime()));
                }
            }, date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), DateFormat.is24HourFormat(LifeTracking.g_globalContext));
			dialog.show();
			if(listener != null) dialog.setOnDismissListener(listener);
		}});
		
		final Button dateButton = (Button) layout.findViewById(dateButtonId);
		dateButton.setText(DateFormat.getMediumDateFormat(LifeTracking.g_globalContext).format(date.getTime()));
		dateButton.setOnClickListener(new OnClickListener() { public void onClick(View v) {
			DatePickerDialog dialog = new DatePickerDialog(LifeTracking.g_globalContext, new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                	date.set(Calendar.YEAR, year);
                	date.set(Calendar.MONTH, monthOfYear);
                	date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                	dateButton.setText(DateFormat.getMediumDateFormat(LifeTracking.g_globalContext).format(date.getTime()));
                }
            }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
			dialog.show();
			if(listener != null) dialog.setOnDismissListener(listener);
		}});
	}
	
	//Update the text for time/date buttons with the given Calendar value.
	public static void updateDateTimeButtons(View layout, int timeButtonId, int dateButtonId, final Calendar date){
		final Button timeButton = (Button) layout.findViewById(timeButtonId);
		timeButton.setText(DateFormat.getTimeFormat(LifeTracking.g_globalContext).format(date.getTime()));
		
		final Button dateButton = (Button) layout.findViewById(dateButtonId);
		dateButton.setText(DateFormat.getMediumDateFormat(LifeTracking.g_globalContext).format(date.getTime()));
	}
}
