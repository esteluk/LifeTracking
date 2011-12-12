package com.lifetracking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;
import com.lifetracking.events.Event;
import com.lifetracking.events.EventType;
import com.lifetracking.graphing.GraphingOptions;
import com.lifetracking.intervals.Interval;
import com.lifetracking.intervals.IntervalType;
import com.lifetracking.tracks.Track;
import com.lifetracking.tracks.TrackValue;
import com.lifetracking.tracks.Track.TrackType;
import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

//SaveLoad is the uber file for saving, loading, creating, and deleting files for *everything*.
public class SaveLoad {
	
	//=====================
	//== MyLife
	//=====================
	
	private static String m_myLifeFilename = "MyLife";

	//Save everything associated with MyLife.
	public static void saveMyLife(){
		new SaveDelegate(){ void doSave(DataOutputStream output) throws IOException {
			output.writeLong(MyLife.m_originDate.getTimeInMillis());
			output.writeByte(MyLife.m_showWelcomeScreen ? 1 : 0);
			output.writeByte(MyLife.m_useBigFont ? 1 : 0);
			output.writeByte(MyLife.m_usePercentScale ? 1 : 0);
		}}.save(m_myLifeFilename, "settings");
	}
	
	//Load everything associated with MyLife.
	public static void loadMyLife(){
		new LoadDelegate(){ Object doLoad(DataInputStream input, int versionCode) throws IOException {
			if(versionCode < 3) return null;
			return loadMyLife3(input);
			//else if(versionCode == 4) loadMyLife4(input);
		}}.load(m_myLifeFilename, null);
	}
	
	//Version 3: Load everything associated with MyLife static variables.
	private static Object loadMyLife3(DataInputStream input) throws IOException{
		MyLife.m_originDate = Calendar.getInstance();
		MyLife.m_originDate.setTimeInMillis(input.readLong());
		MyLife.m_showWelcomeScreen = input.readByte() != 0;
		MyLife.m_useBigFont = input.readByte() != 0;
		MyLife.m_usePercentScale = input.readByte() != 0;
		return MyLife.class;//eh...whatever
	}
	
	//=====================
	//== Tracks
	//=====================
	
	//return - full path to file corresponding to the given track.
	public static String getPathToTrack(Track track){
		return Track.m_tracksPrefix + Long.toString(track.m_id);
	}
	
	//Save the track to a file.
	public static void saveTrack(final Track track){
		new SaveDelegate(){ void doSave(DataOutputStream output) throws IOException {
			output.writeUTF(track.m_type.toString());//UTF:type
			output.writeUTF(track.m_name);//UTF:name
			int valuesSize = track.m_values.size();
			output.writeInt(valuesSize);//int:valuesSize
			for(int n = 0; n < valuesSize; n++){
				TrackValue value = track.m_values.get(n);
				output.writeDouble(value.m_value);//double:value
				output.writeLong(value.m_dateOffset);//long:dateOffset
				output.writeUTF(value.m_note);//utf:note
			}
			saveGraphingOptions(output, track.m_graphingOptions);
		}}.save(getPathToTrack(track), "track");
	}
	
	//Load the track from a file.
	public static Track loadTrack(final String filename){
		return (Track) new LoadDelegate(){ Object doLoad(DataInputStream input, int versionCode) throws IOException {
			if(versionCode < 3) return null;
			long id = Long.parseLong(filename.substring(Track.m_tracksPrefix.length()));
			return loadTrack3(input, id);
			//else if(versionCode == 4) return tracks.add(loadTrack4(input, id));
		}}.load(filename, "track");
	}
	
	//Version 3: Load the track from a file.
	private static Track loadTrack3(DataInputStream input, long id) throws IOException{
		Track track = Track.createNewTrack(TrackType.valueOf(input.readUTF()));//UTF:type
		track.m_id = id;
		track.m_name = input.readUTF();//UTF:name
		int valuesSize = input.readInt();//int:valuesSize
		for(int n = 0; n < valuesSize; n++){
			TrackValue value = new TrackValue(input.readDouble());//double:value
			value.m_dateOffset = input.readLong();//long:dateOffset
			value.m_note = input.readUTF();//UTF:note
			track.m_values.add(value);
		}
		loadGraphingOptions(input, track.m_graphingOptions);
		return track;
	}
	
	//Export the given track to a CSV file.
	public static void exportTrackToCsv(final Track track, String filename, boolean commas){
		final String c = commas ? "," : ";";
		new CsvSaveDelegate(){ void doSave(OutputStreamWriter writer) throws IOException {
			writer.write("Date" + c + "Value" + c + "Note" + "\n");
			for(int n = 0; n < track.m_values.size(); n++){
				TrackValue value = track.m_values.get(n);
				writer.write((value.getTimeString() + " " + value.getDateString()) + c + value.getValueString() + c + "\"" + value.m_note + "\"" + "\n");
			}
		}}.save(filename, "track CSV");
	}
	
	//Import a new track from a CSV file.
	public static Track importTrackFromCsv(String filename, boolean usesCommas, boolean hasHeader){
		final ArrayList<Track> trackWrapper = new ArrayList<Track>(1);//hacky way of adding a new Track().
		new CsvLoadDelegate(){ Object doLoad(InputStreamReader reader) throws IOException {
			//header?
			//trim spaces?
			//auto: comma separated?
			return null;
		}}.load(filename, "track CSV");
		return trackWrapper.size() > 0 ? trackWrapper.get(0) : null;
	}
	
	//Delete the save file corresponding to the track.
	public static void deleteTrack(Track track){
		LifeTracking.g_globalContext.deleteFile(getPathToTrack(track));
	}
	
	//=====================
	//== IntervalTypes
	//=====================
	
	//return - full path to the file corresponding to the given interval type.
	public static String getPathToIntervalType(IntervalType intervalType){
		return IntervalType.m_intervalTypesPrefix + Long.toString(intervalType.m_id);
	}
	
	//Save the interval type to a file.
	public static void saveIntervalType(final IntervalType intervalType){
		new SaveDelegate(){ void doSave(DataOutputStream output) throws IOException {
			output.writeUTF(intervalType.m_name);//UTF:name
			int intervalsSize = intervalType.m_intervals.size();
			output.writeInt(intervalsSize);//int:intervalsSize
			for(int n = 0; n < intervalsSize; n++){
				Interval value = intervalType.m_intervals.get(n);
				output.writeLong(value.m_start);//long:start
				output.writeLong(value.m_end);//long:end
				output.writeUTF(value.m_note);//UTF:note
			}
			saveGraphingOptions(output, intervalType.m_graphingOptions);
		}}.save(getPathToIntervalType(intervalType), "interval type");
	}
	
	//Load the interval type from a file.
	public static IntervalType loadIntervalType(final String filename){
		return (IntervalType) new LoadDelegate(){ Object doLoad(DataInputStream input, int versionCode) throws IOException {
			if(versionCode < 3) return null;
			long id = Long.parseLong(filename.substring(IntervalType.m_intervalTypesPrefix.length()));
			return loadIntervalType3(input, id);
			//else if(versionCode == 4) return intervalTypes.add(loadIntervalType4(input, id));
		}}.load(filename, "interval type");
	}
	
	//Version 3: Load the interval type from a file.
	private static IntervalType loadIntervalType3(DataInputStream input, long id) throws IOException{
		IntervalType intervalType = new IntervalType(input.readUTF());//UTF:name
		intervalType.m_id = id;
		boolean loadingEvents = (id == 0xFEEDFACE);//if we are loading an old interval type that's marked as events (by special id), then we will convert it to our normal events
		Interval inProgressInterval = null;
		int valuesSize = input.readInt();//int:valuesSize
		for(int n = 0; n < valuesSize; n++){
			Interval value = new Interval();
			value.m_intervalType = intervalType;
			value.m_start = input.readLong();//long:start
			value.m_end = input.readLong();//long:end
			if(loadingEvents) {
				String name = input.readUTF();//[UTF:name]
				EventType eventType = EventType.addEventType(name, true);
				eventType.addEvent(MyLife.getCalendarFromOffset(value.m_start)).m_note = input.readUTF();//UTF:note
				SaveLoad.saveEventType(eventType);
			} else {
				value.m_note = input.readUTF();//UTF:note
				if(value.isInProgress()) inProgressInterval = value;
				intervalType.addInterval(value);
			}
		}
		intervalType.m_lastAddedInterval = inProgressInterval;
		loadGraphingOptions(input, intervalType.m_graphingOptions);
		return loadingEvents ? null : intervalType;
	}
	
	//Export the given interval type to a CSV file.
	public static void exportIntervalTypeToCsv(final IntervalType intervalType, String filename, boolean commas){
		final String c = commas ? "," : ";";
		new CsvSaveDelegate(){ void doSave(OutputStreamWriter writer) throws IOException {
			writer.write("Start" + c + "End" + c + "Note" + "\n");
			for(int n = 0; n < intervalType.m_intervals.size(); n++){
				Interval value = intervalType.m_intervals.get(n);
				writer.write((value.getStartTimeString() + " " + value.getStartDateString()) + c + (value.getEndTimeString() + " " + value.getEndDateString()) + c + "\"" + value.m_note + "\"" + "\n");
			}
		}}.save(filename, "interval type CSV");
	}
	
	//Import a new interval type from a CSV file.
	public static IntervalType importIntervalTypeFromCsv(String filename, boolean usesCommas, boolean hasHeader){
		final ArrayList<IntervalType> intervalTypeWrapper = new ArrayList<IntervalType>(1);//hacky way of adding a new IntervalType().
		new CsvLoadDelegate(){ Object doLoad(InputStreamReader reader) throws IOException {
			//header?
			//trim spaces?
			//auto: comma separated?
			return null;
		}}.load(filename, "interval type CSV");
		return intervalTypeWrapper.size() > 0 ? intervalTypeWrapper.get(0) : null;
	}
	
	//Delete the save file corresponding to the interval type.
	public static void deleteIntervalType(IntervalType intervalType){
		LifeTracking.g_globalContext.deleteFile(getPathToIntervalType(intervalType));
	}
	
	//=====================
	//== EventTypes
	//=====================
	
	//return - full path to the file corresponding to the given event type.
	public static String getPathToEventType(EventType eventType){
		return EventType.m_eventTypesPrefix + Long.toString(eventType.m_id);
	}
	
	//Save the event type to a file.
	public static void saveEventType(final EventType eventType){
		new SaveDelegate(){ void doSave(DataOutputStream output) throws IOException {
			output.writeUTF(eventType.m_name);//UTF:name
			int eventsSize = eventType.m_events.size();
			output.writeInt(eventsSize);//int:eventsSize
			for(int n = 0; n < eventsSize; n++){
				Event value = eventType.m_events.get(n);
				output.writeLong(value.m_start);//long:start
				output.writeUTF(value.m_note);//UTF:note
			}
			saveGraphingOptions(output, eventType.m_graphingOptions);//GraphingOptions
		}}.save(getPathToEventType(eventType), "event type");
	}
	
	//Load the event type from a file.
	public static EventType loadEventType(final String filename){
		return (EventType) new LoadDelegate(){ Object doLoad(DataInputStream input, int versionCode) throws IOException {
			if(versionCode < 4) return null;
			long id = Long.parseLong(filename.substring(EventType.m_eventTypesPrefix.length()));
			return loadEventType4(input, id);
		}}.load(filename, "event type");
	}
	
	//Version 3: Load the event type from a file.
	private static EventType loadEventType4(DataInputStream input, long id) throws IOException{
		EventType eventType = new EventType(input.readUTF());//UTF:name
		eventType.m_id = id;
		int eventsSize = input.readInt();//eventsSize
		for(int n = 0; n < eventsSize; n++){
			Event value = new Event();
			value.m_eventType = eventType;
			value.m_start = input.readLong();//long:start
			value.m_note = input.readUTF();//UTF:note
			eventType.addEvent(value);
		}
		loadGraphingOptions(input, eventType.m_graphingOptions);//GraphingOptions
		return eventType;
	}
	
	//Export the given event type to a CSV file.
	public static void exportEventTypeToCsv(final EventType eventType, String filename, boolean commas){
		final String c = commas ? "," : ";";
		new CsvSaveDelegate(){ void doSave(OutputStreamWriter writer) throws IOException {
			writer.write("Date" + c + "Note" + "\n");
			for(int n = 0; n < eventType.m_events.size(); n++){
				Event value = eventType.m_events.get(n);
				writer.write((value.getStartTimeString() + " " + value.getStartDateString()) + c + "\"" + value.m_note + "\"" + "\n");
			}
		}}.save(filename, "event type CSV");
	}
	
	//Import a new event type from a CSV file.
	public static EventType importEventTypeFromCsv(String filename, boolean usesCommas, final boolean hasHeader){
		final String c = usesCommas ? "," : ";";
		final ArrayList<EventType> eventTypeWrapper = new ArrayList<EventType>(1);//hacky way of adding a new EventType().
		new CsvLoadDelegate(){ Object doLoad(InputStreamReader reader) throws IOException {
			String[] headers = null;
			if(hasHeader){
				StringBuffer sb = new StringBuffer();
				for(int c = 0; c > 0 && c != '\n'; c = reader.read()){}
				String header = sb.toString();
				StringTokenizer tokenizer = new StringTokenizer(header, c);
				headers = new String[tokenizer.countTokens()];
				int i = 0;
				for(String token = tokenizer.nextToken(); tokenizer.hasMoreTokens(); token = tokenizer.nextToken()){
					headers[i++] = token;
				}
			}
			
			//header?
			//trim spaces?
			//auto: comma separated?
			return null;
		}}.load(filename, "event type CSV");
		return eventTypeWrapper.size() > 0 ? eventTypeWrapper.get(0) : null;
	}
	
	//Delete the save file corresponding to the event type.
	public static void deleteEventType(EventType eventType){
		LifeTracking.g_globalContext.deleteFile(getPathToEventType(eventType));
	}
	
	//=====================
	//== GraphingOptions
	//=====================
	
	//Save the graphing options.
	public static void saveGraphingOptions(DataOutputStream output, GraphingOptions options) throws IOException{
		output.writeBoolean(options.m_autoAdjustY);
		output.writeBoolean(options.m_includeYZero);
	}
	
	//Load the graphing options.
	public static void loadGraphingOptions(DataInputStream input, GraphingOptions options) throws IOException{
		options.m_autoAdjustY = input.readBoolean();
		options.m_includeYZero = input.readBoolean();
	}
	
	//===== Normal save/load delegates
	
	//SaveDelegate is used to wrap stream creation and error checking for saving files.
	private static abstract class SaveDelegate {
		
		//Override this method to do the actual saving.
		abstract void doSave(DataOutputStream output) throws IOException;
		
		//Create output stream for saving a file with the given filename.
		//dataTypeName - name of the data being saved. Used for error messages.
		public void save(String filename, String dataTypeName) {
			try {
				FileOutputStream fos = LifeTracking.g_globalContext.openFileOutput(filename, Context.MODE_PRIVATE | Context.MODE_WORLD_WRITEABLE);
				DataOutputStream output = new DataOutputStream(fos);
				int versionCode = LifeTracking.g_globalContext.getPackageManager().getPackageInfo("com.lifetracking", 0).versionCode;
				output.writeInt(versionCode);
				doSave(output);
				fos.close();
			} catch (Exception e) {
				Toast.makeText(LifeTracking.g_globalContext, "Could not save " + dataTypeName + " data! :(", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	//LoadDelegate is used to wrap stream creation and error checking for loading files.
	private static abstract class LoadDelegate {
		
		//Override this method to do the actual loading.
		//return - loaded object if the parsing went OK, null otherwise.
		abstract Object doLoad(DataInputStream input, int versionCode) throws IOException;
		
		//Create output stream for loading a file with the given filename.
		//dataTypeName - name of the data being loaded. Used for error messages. If null then no error message will be displayed.
		//return - loaded object if the parsing went OK, null otherwise.
		public Object load(String filename, String dataTypeName) {
			try {
				FileInputStream fis = LifeTracking.g_globalContext.openFileInput(filename);
				DataInputStream input = new DataInputStream(fis);
				int versionCode = input.readInt();
				Object loadedObject = doLoad(input, versionCode);
				if(loadedObject == null){
					if(dataTypeName != null) Toast.makeText(LifeTracking.g_globalContext, "Tried loading " + dataTypeName + " data from a file with bad version number. Make sure you have the latest version.", Toast.LENGTH_LONG).show();
				}
				fis.close();
				return loadedObject;
			} catch (Exception e) {
				if(dataTypeName != null) Toast.makeText(LifeTracking.g_globalContext, "Could not load " + dataTypeName + " data! :(", Toast.LENGTH_LONG).show();
				return null;
			}
		}
	}
	
	//===== CSV save/load delegates
	
	//SaveDelegate is used to wrap stream creation and error checking for saving CSV files.
	private static abstract class CsvSaveDelegate {
		
		//Override this method to do the actual saving.
		abstract void doSave(OutputStreamWriter writer) throws IOException;
		
		//Create output stream for saving a file with the given filename.
		//dataTypeName - name of the data being saved. Used for error messages.
		public void save(String filename, String dataTypeName) {
			try {
				if(!filename.endsWith(".csv")) filename += ".csv";
				File dir = Environment.getExternalStorageDirectory();
				if(dir == null || !dir.exists()){
					Toast.makeText(LifeTracking.g_globalContext, "Could not save " + dataTypeName + " data! No external storage found.", Toast.LENGTH_LONG).show();
					return;
				}
				String sep = File.separator;
				File file = new File(dir.getAbsolutePath() + sep + "Android" + sep + "data" + sep + "com.lifetracking" + sep + "files", filename);
				file.getParentFile().mkdirs();
				file.createNewFile();
				FileOutputStream fos = new FileOutputStream(file);
				OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF8");
				doSave(writer);
				writer.flush();
				writer.close();
			} catch (Exception e) {
				Toast.makeText(LifeTracking.g_globalContext, "Could not save " + dataTypeName + " data! :(", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	//CsvLoadDelegate is used to wrap stream creation and error checking for loading CSV files.
	private static abstract class CsvLoadDelegate {
		
		//Override this method to do the actual loading.
		//return - loaded object if the parsing went OK, null otherwise.
		abstract Object doLoad(InputStreamReader reader) throws IOException;
		
		//Create output stream for loading a file with the given filename.
		//dataTypeName - name of the data being loaded. Used for error messages. If null then no error message will be displayed.
		//return - loaded object if the parsing went OK, null otherwise.
		public Object load(String filename, String dataTypeName) {
			try {
				FileInputStream fis = LifeTracking.g_globalContext.openFileInput(filename);
				InputStreamReader reader = new InputStreamReader(fis, "UTF8");
				Object loadedObject = doLoad(reader);
				if(loadedObject == null){
					if(dataTypeName != null) Toast.makeText(LifeTracking.g_globalContext, "Tried loading " + dataTypeName + " data from a file with bad version number. Make sure you have the latest version.", Toast.LENGTH_LONG).show();
				}
				reader.close();
				return loadedObject;
			} catch(FileNotFoundException e){
				if(!filename.endsWith(".csv")) return load(filename + ".csv", dataTypeName);//TODO: make obvious folder thing?
				return null;
			} catch (Exception e) {
				if(dataTypeName != null) Toast.makeText(LifeTracking.g_globalContext, "Could not load " + dataTypeName + " data! :(", Toast.LENGTH_LONG).show();
				return null;
			}
		}
	}
}
