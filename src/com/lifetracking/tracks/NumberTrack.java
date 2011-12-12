package com.lifetracking.tracks;

import java.util.Calendar;
import com.lifetracking.LifeTracking;
import com.lifetracking.R;
import com.lifetracking.SaveLoad;
import com.lifetracking.UtilHelper;
import com.lifetracking.ui.GuiHelper;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//NumberTrack is a Track that tracks number values.
public class NumberTrack extends Track {

	//Create new NumberTrack.
	public NumberTrack(String name) {
		super(TrackType.Number, name);
	}
	
	//Create a Dialog for adding a value to this track.
	@Override public Dialog showAddTrackValueDialog(){
		View layout = LifeTracking.g_inflater.inflate(R.layout.add_value_number_dialog, null, false);

		final NumberTrack track = this;
		final Calendar date = Calendar.getInstance();//Set to track value date
		final EditText valueText = (EditText) layout.findViewById(R.id.AddTrackValue_Number_ValueText);
		final EditText noteText = (EditText) layout.findViewById(R.id.AddTrackValue_Number_NoteText);
		final TextView lastValueText = (TextView) layout.findViewById(R.id.AddTrackValue_Number_LastValueText);
		valueText.setText("");
		noteText.setText("");
		if(m_values.size() > 0){
			TrackValue lastValue = m_values.get(m_values.size() - 1);
			lastValueText.setText("Last added value of " + lastValue.getValueString() + " added on " + lastValue.getTimeDateString());
		} else {
			lastValueText.setVisibility(View.GONE);
		}
		
		GuiHelper.setupDateTimeButtons(layout, R.id.AddTrackValue_Number_TimeButton, R.id.AddTrackValue_Number_DateButton, date);

		//Create dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(LifeTracking.g_globalContext);
		builder.setView(layout)
			.setMessage("Add new \"" + m_name + "\" value")
			.setPositiveButton("Add", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
				try {
					float valueNum = Float.parseFloat(valueText.getText().toString());//do this first because it might fail
					TrackValue trackValue = track.addTrackValue(valueNum, date);
					trackValue.m_note = noteText.getText().toString();
					SaveLoad.saveTrack(track);
					Toast.makeText(LifeTracking.g_globalContext, "Value " + valueText.getText() + " added to \"" + track.m_name + "\" at " + trackValue.getTimeString() + ", " + trackValue.getDateString(), Toast.LENGTH_LONG).show();
				} catch(NumberFormatException e){
					Toast.makeText(LifeTracking.g_globalContext, "Invalid value \"" + valueText.getText() + "\" entered.", Toast.LENGTH_LONG).show();
				}
			}})
			.setNegativeButton("Cancel", null);
		
		return builder.show();
	}
	
	//Create a Dialog for editing a value of this track.
	@Override public Dialog showEditTrackValueDialog(final TrackValue value){
		View layout = LifeTracking.g_inflater.inflate(R.layout.add_value_number_dialog, null, false);

		final NumberTrack track = this;
		final Calendar date = value.getDate();
		final EditText valueText = (EditText) layout.findViewById(R.id.AddTrackValue_Number_ValueText);
		final EditText noteText = (EditText) layout.findViewById(R.id.AddTrackValue_Number_NoteText);
		valueText.setText(UtilHelper.valueToString(value.m_value));
		noteText.setText(value.m_note);
		
		layout.findViewById(R.id.AddTrackValue_Number_LastValueText).setVisibility(View.GONE);
		GuiHelper.setupDateTimeButtons(layout, R.id.AddTrackValue_Number_TimeButton, R.id.AddTrackValue_Number_DateButton, date);

		AlertDialog.Builder builder = new AlertDialog.Builder(LifeTracking.g_globalContext);
		builder.setView(layout);
		builder.setMessage("Edit \"" + m_name + "\" value")
			.setPositiveButton("Edit", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) {
				try {
					float valueNum = Float.parseFloat(valueText.getText().toString());//do this first because it might fail
					m_values.remove(value);
					TrackValue newValue = track.addTrackValue(valueNum, date);
					newValue.m_note = noteText.getText().toString();
					SaveLoad.saveTrack(track);
				} catch(NumberFormatException e){
					Toast.makeText(LifeTracking.g_globalContext, "Invalid value \"" + valueText.getText() + "\" entered.", Toast.LENGTH_LONG).show();
				}
			}})
			.setNegativeButton("Cancel", null);

		return builder.show();
	}
}
