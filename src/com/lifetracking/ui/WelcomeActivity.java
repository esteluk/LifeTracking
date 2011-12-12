package com.lifetracking.ui;

import com.lifetracking.LifeTracking;
import com.lifetracking.MyLife;
import com.lifetracking.R;
import com.lifetracking.SaveLoad;
import com.lifetracking.events.EventType;
import com.lifetracking.intervals.IntervalType;
import com.lifetracking.tracks.Track;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

//WelcomeActivity is a screen that has welcoming information. It's shown only if the user hasn't disabled it.
public class WelcomeActivity extends LifeTracking.LifeActivity {

	//Called when the activity is first created.
    @Override public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
		setContentView(R.layout.welcome_screen);
		
		//"Don't show this again" checkbox.
		final CheckBox dontShowWelcomeScreenCheckBox = (CheckBox) findViewById(R.id.WelcomeScreen_DoNotShowAgainCheckBox);
		dontShowWelcomeScreenCheckBox.setChecked(!MyLife.m_showWelcomeScreen);

		//"Continue" button.
		final Button continueButton = (Button) findViewById(R.id.WelcomeScreen_ContinueButton);
		continueButton.setOnClickListener(new OnClickListener() { public void onClick(View v) {
			if(((CheckBox)findViewById(R.id.WelcomeScreen_AddCommonTracksCheckBox)).isChecked()){
				addCommonTracks();
			}
			MyLife.m_showWelcomeScreen = !dontShowWelcomeScreenCheckBox.isChecked();
			SaveLoad.saveMyLife();
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
        }});
    }
    
    //Add tracks most user will want to have.
    private void addCommonTracks(){
    	IntervalType.addIntervalType("Sleep", true);
    	IntervalType.addIntervalType("Work", true);
    	IntervalType.addIntervalType("Entertainment", true);
    	EventType.addEventType("Misc", true);
    	Track.addTrack("Weight", true);
    }
}
