package com.lifetracking.ui;

import com.lifetracking.LifeTracking;
import com.lifetracking.R;
import com.lifetracking.tracks.*;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

//AddTrackActivity allows the user to enter data for a new Track.
public class AddTrackActivity extends LifeTracking.LifeActivity {
	
	private EditText m_name;
	//private Spinner m_type;
	
	//Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

		setContentView(R.layout.add_track_menu);
		m_name = (EditText) findViewById(R.id.AddTrack_Name);
		//m_type = (Spinner) findViewById(R.id.AddTrack_Type);
		
		//Add all known track types to the type dropdown menu.
		/*ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getBaseContext(), android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for(int n = 0; n < Track.TrackType.values().length; n++){
			adapter.add(Track.TrackType.values()[n].toString());
		}
		m_type.setAdapter(adapter);*/
		
		//Setup "Done" button.
		Button button;
		button = (Button)findViewById(R.id.AddTrack_Done);
		button.setOnClickListener(new OnClickListener() { public void onClick(View v) {
        	Track.TrackType trackType = Track.TrackType.Number;
        	Track track = Track.createNewTrack(trackType);
        	track.m_name = m_name.getText().toString();
        	Track.addTrack(track);
        	startActivity(new Intent(AddTrackActivity.this, MainActivity.class));
        }});
		
		//Setup "Cancel" button.
		button = (Button)findViewById(R.id.AddTrack_Cancel);
		button.setOnClickListener(new OnClickListener() { public void onClick(View v) {
        	startActivity(new Intent(AddTrackActivity.this, MainActivity.class));
        }});
    }
}
