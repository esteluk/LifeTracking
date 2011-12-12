package com.lifetracking.graphing;

import com.lifetracking.MyLife;
import com.lifetracking.SaveLoad;
import com.lifetracking.UtilHelper;
import com.lifetracking.tracks.Track;
import com.lifetracking.tracks.TrackValue;
import android.app.Dialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.text.TextPaint;

//TrackGraph is a Graph which can draw a graph for a given Track.
public class TrackGraph extends Graph {
	
	private Track m_track;
	private double m_minY, m_maxY;
	
	//Create new TrackGraph.
	public TrackGraph(Track track) {
		m_track = track;
	}
	
	//return - number of values in this graph.
	@Override public int getValuesSize(){
		return m_track.m_values.size();
	}
	
	//return - x coordinate for a graph value with the given index.
	@Override public long getX(int index){
		return m_track.m_values.get(index).m_dateOffset;
	}
	
	//return - zero. TrackGraph values don't have second X coordinate.
	@Override public long getX2(int index){
		return 0L;
	}
	
	//return - y coordinate for a graph value with the given index.
	@Override public double getY(int index){
		return getY(m_track.m_values.get(index));
	}
	
	//return - y coordinate for a graph value.
	private double getY(TrackValue value){
		if(MyLife.m_usePercentScale){
			return 100.0 * (value.m_value - m_minY) / (m_maxY - m_minY);
		} else {
			return value.m_value;
		}
	}
	
	//Update the indices that are active for this graph.
	//return - true if the graph should be rendered.
	@Override public boolean updateIndices(){
		m_minY = Double.MAX_VALUE; m_maxY = -Double.MAX_VALUE;
		for(TrackValue v : m_track.m_values){
			m_minY = Math.min(m_minY, v.m_value);
			m_maxY = Math.max(m_maxY, v.m_value);
		}
		return super.updateIndices();
	}
	
	//return - distance^2 from point (x,y) (in pixel coordinates) to the graph value at the given index. Double.MAX_VALUE if value isn't in selection range.
	@Override public double trySelect(int index, int x, int y){
		double selectionRadSq = 16.0f * m_circleRad * m_circleRad;//magic value
		
		TrackValue value = m_track.m_values.get(index);
		double dx = Math.abs(m_view.convertToPixelX(value.m_dateOffset) - x);
		double dy = Math.abs(m_view.convertToPixelY(getY(value)) - y);
		double distSq = dx * dx + dy * dy;
		return distSq <= selectionRadSq ? distSq : Double.MAX_VALUE;
	}
	
	//return - visible dialog for editing the graph value at the given index.
	@Override public Dialog showEditValueDialog(int index){
		return m_track.showEditTrackValueDialog(m_track.m_values.get(index));
	}
	
	//Remove the graph value at the given index.
	@Override public void deleteValue(int index){
		m_track.removeTrackValue(index);
		SaveLoad.saveTrack(m_track);
	}
	
	//return - full string representation of the graph value at the given index.
	@Override public String getValueString(int index){
		return m_track.m_values.get(index).toString(m_track);
	}
	
	//return - note for the graph value at the given index.
	@Override public String getNote(int index){
		return m_track.m_values.get(index).m_note;
	}
	
	//return - false. TrackGraph values don't have second X coordinate.
	@Override public boolean hasTwoXValues(){
		return false;
	}
	
	//return - true. TrackGraph values are (x,y) points.
	@Override public boolean hasYValues(){
		return true;
	}
	
	//return - name of this graph.
	@Override public String getName(){
		return m_track.m_name;
	}
	
	//return - graphing optiosn for this graph.
	@Override public GraphingOptions getGraphingOptions(){
		return m_track.m_graphingOptions;
	}
	
	///=================================
	/// Graphing functions.
	///=================================
	private final float m_circleRad = 8f;
	private final float m_circleWithNoteRad = 4f;
	
	static private Paint m_linePaint;
	static private Paint m_pointsPaint, m_pointsSelectedPaint, m_pointsWithNotePaint, m_pointsLinePaint;
	static private int m_selectionColor;

	//Render this track's values.
	@Override public void renderValues(Canvas canvas, int graphColor) {
		if(m_track.m_values.size() < 2) return;
		if(m_firstIndex == -1 || m_lastIndex == -1) return;
		
		m_pointsPaint.setColor(graphColor);
		m_linePaint.setColor(graphColor);
		
		drawTrackLines(canvas);
		drawTrackValues(canvas);
	}
	
	//Draw the lines connecting the track values.
	private void drawTrackLines(Canvas canvas){
		for(int i = m_firstIndex; i <= m_lastIndex; i++){
			if(i < 1) continue;
			Point p1 = convertToPixel(m_track.m_values.get(i - 1));
			Point p2 = convertToPixel(m_track.m_values.get(i));
			canvas.drawLine(p1.x, p1.y, p2.x, p2.y, m_linePaint);
		}
	}
	
	//Plot the track values.
	private void drawTrackValues(Canvas canvas){
		for(int n = m_firstIndex; n <= m_lastIndex; n++){
			TrackValue value = m_track.m_values.get(n);
			Point p = convertToPixel(value);
			canvas.drawCircle(p.x, p.y, m_circleRad, m_pointsPaint);
			if(value.m_note.length() > 0){
				canvas.drawCircle(p.x, p.y, m_circleWithNoteRad, m_pointsWithNotePaint);
			}
		}
	}
	
	//Render the graph value at the given index as a selected value.
	@Override public void renderSelectedValue(Canvas canvas, int index, float xAxisY, float yAxisX){
		TrackValue value = m_track.m_values.get(index);
		Point p = convertToPixel(value);
		canvas.drawCircle(p.x, p.y, m_circleRad, m_pointsSelectedPaint);
		if(value.m_note.length() > 0){
			canvas.drawCircle(p.x, p.y, m_circleWithNoteRad, m_pointsWithNotePaint);
		}
		//Draw lines from point to axes.
		canvas.drawLine(p.x, p.y, yAxisX, p.y, m_pointsLinePaint);
		canvas.drawLine(p.x, p.y, p.x, xAxisY, m_pointsLinePaint);
	}
	
	//Render the x axis label for selected graph value at the given index.
	//return - rectangle bounding the rendered text.
	@Override public Rect renderXAxisLabel(Canvas canvas, int index, float positionY, TextPaint textPaint){
		int oldColor = textPaint.getColor();
		textPaint.setColor(m_selectionColor);
		textPaint.setTextAlign(Align.CENTER);
		TrackValue value = m_track.m_values.get(index);
		Point p = convertToPixel(value);
		String date = value.getDateString(), time = value.getTimeString();
		Rect bounds1 = UtilHelper.getTextBounds(textPaint, time);
		Rect bounds2 = UtilHelper.getTextBounds(textPaint, date);
		float py1 = positionY + bounds1.height();
		float py2 = py1 + bounds2.height();
		canvas.drawText(time, p.x, py1, textPaint);
		canvas.drawText(date, p.x, py2, textPaint);
		textPaint.setColor(oldColor);
		bounds1.offset(p.x, (int)py1);
		bounds2.offset(p.x, (int)py2);
		bounds1.union(bounds2);
		return bounds1;
	}
	
	//Render the y axis label for selected graph value at the given index.
	//return - rectangle bounding the rendered text.
	@Override public Rect renderYAxisLabel(Canvas canvas, int index, float positionX, TextPaint textPaint){
		int oldColor = textPaint.getColor();
		textPaint.setColor(m_selectionColor);
		TrackValue value = m_track.m_values.get(index);
		Point p = convertToPixel(value);
		String valueString = UtilHelper.valueToString(value.m_value);
		Rect bounds = UtilHelper.getTextBounds(textPaint, valueString);
		float py = p.y + bounds.height() / 2.0f;
		canvas.drawText(valueString, positionX, py, textPaint);
		textPaint.setColor(oldColor);
		bounds.offset((int)positionX, (int)py);
		return bounds;
	}
	
	//return - Graph coordinate (value) converted to Canvas coordinate.
	private Point convertToPixel(TrackValue value){
		return new Point(m_view.convertToPixelX(value.m_dateOffset), m_view.convertToPixelY(getY(value)));
	}
	
	//Instantitate necessary Paint objects.
	static {
		m_selectionColor = Color.rgb(64, 64, 255);
		m_pointsPaint = new Paint();
		m_pointsPaint.setColor(Color.GREEN);
		m_pointsPaint.setAntiAlias(true);
		m_pointsSelectedPaint = new Paint(m_pointsPaint);
		m_pointsSelectedPaint.setColor(m_selectionColor);
		m_pointsWithNotePaint = new Paint(m_pointsPaint);
		m_pointsWithNotePaint.setColor(Color.WHITE);
		m_pointsLinePaint = new Paint();
		m_pointsLinePaint.setColor(m_pointsSelectedPaint.getColor());
		m_pointsLinePaint.setAntiAlias(true);
		m_pointsLinePaint.setPathEffect(new DashPathEffect(new float[]{2.0f, 3.0f}, 0));
		m_linePaint = new Paint(m_pointsPaint);
	}
}
