package com.lifetracking.graphing;

import android.app.Dialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.text.TextPaint;

import com.lifetracking.SaveLoad;
import com.lifetracking.UtilHelper;
import com.lifetracking.intervals.Interval;
import com.lifetracking.intervals.IntervalType;

//IntervalGraph is a Graph for an IntervalType.
public class IntervalGraph extends Graph {

	private IntervalType m_intervalType;
	
	//Create new TrackGraph.
	public IntervalGraph(IntervalType intervalType) {
		m_intervalType = intervalType;
	}
	
	//return - number of values in this graph.
	@Override public int getValuesSize(){
		return m_intervalType.m_intervals.size();
	}
	
	//return - x coordinate for a graph value with the given index.
	@Override public long getX(int index){
		return m_intervalType.m_intervals.get(index).m_start;
	}
	
	//return - second x coordinate for a graph value with the given index.
	@Override public long getX2(int index){
		return m_intervalType.m_intervals.get(index).getEndOrCurrent();
	}
	
	//return - 0.0f. IntervalGraph values don't have Y coordinate.
	@Override public double getY(int index){
		return 0.0f;
	}
	
	//return - distance^2 from point (x,y) (in pixel coordinates) to the graph value at the given index. Double.MAX_VALUE if value isn't in selection range.
	@Override public double trySelect(int index, int x, int y){
		Interval value = m_intervalType.m_intervals.get(index);
		int startX = m_view.convertToPixelX(value.m_start);
		int endX = m_view.convertToPixelX(value.getEndOrCurrent());
		double selectionRadSq = (endX - startX) / 2L;
		selectionRadSq = Math.max(selectionRadSq, 10.0f);//make sure we have at least 10px of selection
		selectionRadSq *= selectionRadSq;
		double dx = Math.abs((startX + endX) / 2L - x);
		double distSq = dx * dx;
		return distSq <= selectionRadSq ? distSq : Double.MAX_VALUE;
	}
	
	//return - visible dialog for editing the graph value at the given index.
	@Override public Dialog showEditValueDialog(int index){
		return m_intervalType.showEditIntervalDialog(m_intervalType.m_intervals.get(index));
	}
	
	//Remove the graph value at the given index.
	@Override public void deleteValue(int index){
		m_intervalType.removeInterval(index);
		SaveLoad.saveIntervalType(m_intervalType);
	}
	
	//return - full string representation of the graph value at the given index.
	@Override public String getValueString(int index){
		return m_intervalType.m_intervals.get(index).toString();
	}
	
	//return - note for the graph value at the given index.
	@Override public String getNote(int index){
		return m_intervalType.m_intervals.get(index).m_note;
	}
	
	//return - true. IntervalGraph has two X coodinates: start and end.
	@Override public boolean hasTwoXValues(){
		return true;
	}
	
	//return - false. IntervalGraph values don't have Y coordinate.
	@Override public boolean hasYValues(){
		return false;
	}
	
	//return - name of this graph.
	@Override public String getName(){
		return m_intervalType.m_name;
	}
	
	//return - graphing optiosn for this graph.
	@Override public GraphingOptions getGraphingOptions(){
		return m_intervalType.m_graphingOptions;
	}
	
	///=================================
	/// Graphing functions.
	///=================================
	private final static DashPathEffect m_noteLineDashPathEffect = new DashPathEffect(new float[]{15.0f, 20.0f}, 0);
	private final static float m_intervalLineThickness = 1.0f;
	
	private static Paint m_intervalLinePaint, m_intervalInsidePaint, m_inProgressIntervalPaint, m_noteLinePaint;
	private static int m_normalColor, m_selectionColor;
	private static int m_intervalAlpha = 96;
	
	//Render this graph's values.
	@Override public void renderValues(Canvas canvas, int graphColor) {
		if(m_intervalType.m_intervals.size() < 0) return;
		if(m_firstIndex == -1 || m_lastIndex == -1) return;
		
		m_normalColor = graphColor;
		m_intervalLinePaint.setColor(m_normalColor);
		m_intervalInsidePaint.setColor(UtilHelper.getColor(m_intervalAlpha, m_normalColor));

		Rect clipBounds = canvas.getClipBounds();
		for(int i = m_firstIndex; i <= m_lastIndex; i++){
			drawInterval(canvas, i, clipBounds.top, clipBounds.bottom);
		}
	}
	
	//Draw an interval (given its index).
	//top, bottom - Y values for the interval line.
	private void drawInterval(Canvas canvas, int index, float top, float bottom){
		Interval interval = m_intervalType.m_intervals.get(index);
		int x1 = m_view.convertToPixelX(getX(index));
		int x2 = m_view.convertToPixelX(getX2(index));
		boolean hasNote = (interval.m_note.compareTo("") != 0);
		if(interval.isInProgress()){
			m_inProgressIntervalPaint.setShader(getGradientForInProgressInterval(interval, x1, x2));
			canvas.drawRect(x1, top, x2, bottom, m_inProgressIntervalPaint);
		} else {
			canvas.drawRect(x1, top, x2, bottom, m_intervalInsidePaint);
			canvas.drawLine(x2, top, x2, bottom, m_intervalLinePaint);
			if(hasNote) canvas.drawLine(x2, top, x2, bottom, m_noteLinePaint);
		}
		canvas.drawLine(x1, top, x1, bottom, m_intervalLinePaint);
		if(hasNote) canvas.drawLine(x1, top, x1, bottom, m_noteLinePaint);
	}
	
	//Render the graph value at the given index as a selected value.
	@Override public void renderSelectedValue(Canvas canvas, int index, float xAxisY, float yAxisX){
		m_intervalLinePaint.setColor(m_selectionColor);
		m_intervalInsidePaint.setColor(UtilHelper.getColor(m_intervalAlpha, m_selectionColor));
		
		drawInterval(canvas, index, 0.0f, xAxisY);

		m_intervalLinePaint.setColor(m_normalColor);
		m_intervalInsidePaint.setColor(UtilHelper.getColor(m_intervalAlpha, m_normalColor));
	}
	
	//Render the x axis label for selected graph value at the given index.
	//return - rectangle bounding the rendered text.
	@Override public Rect renderXAxisLabel(Canvas canvas, int index, float positionY, TextPaint textPaint){
		Interval interval = m_intervalType.m_intervals.get(index);
		boolean renderEnd = !interval.isInProgress();
		
		//Calculate the bounding boxes for the time/date text for start/end and make sure start/end text doesn't overlap.
		int x1 = m_view.convertToPixelX(interval.m_start);
		int x2 = m_view.convertToPixelX(interval.getEndOrCurrent());
		String date1 = interval.getStartDateString(), time1 = interval.getStartTimeString();
		String date2 = interval.getEndDateString(), time2 = interval.getEndTimeString();
		Rect boundsTime1 = UtilHelper.getTextBounds(textPaint, time1);
		Rect boundsDate1 = UtilHelper.getTextBounds(textPaint, date1);
		Rect boundsTime2 = UtilHelper.getTextBounds(textPaint, time2);
		Rect boundsDate2 = UtilHelper.getTextBounds(textPaint, date2);
		float timeY = positionY + Math.max(boundsTime2.height(), boundsTime1.height());
		float dateY = timeY + Math.max(boundsDate1.height(), boundsDate2.height());
		boundsTime1.offset(x1, (int)timeY);
		boundsDate1.offset(x1, (int)dateY);
		boundsTime2.offset(x2, (int)timeY);
		boundsDate2.offset(x2, (int)dateY);
		Rect bounds1 = new Rect(boundsTime1); bounds1.union(boundsDate1);
		Rect bounds2 = new Rect(boundsTime2); bounds2.union(boundsDate2);
		if(renderEnd && Rect.intersects(bounds1, bounds2)){//fix intersection
			int dist = bounds1.width() / 2 + bounds2.width() / 2 - (bounds2.centerX() - bounds1.centerX()) + (int)textPaint.getTextSize();
			boundsTime1.offset(-dist / 2, 0);
			boundsDate1.offset(-dist / 2, 0);
			bounds1.offset(-dist / 2, 0);
			boundsTime2.offset(dist / 2, 0);
			boundsDate2.offset(dist / 2, 0);
			bounds2.offset(dist / 2, 0);
		}
		
		//Render the text.
		int oldColor = textPaint.getColor();
		textPaint.setColor(m_selectionColor);
		canvas.drawText(time1, boundsTime1.centerX(), timeY, textPaint);
		canvas.drawText(date1, boundsDate1.centerX(), dateY, textPaint);
		if(renderEnd) canvas.drawText(time2, boundsTime2.centerX(), timeY, textPaint);
		if(renderEnd) canvas.drawText(date2, boundsDate2.centerX(), dateY, textPaint);
		textPaint.setColor(oldColor);
		
		if(renderEnd) bounds1.union(bounds2);
		return bounds1;
	}
	
	//Render the y axis label for selected graph value at the given index.
	//return - null. IntervalGraph values don't have Y coordinate.
	@Override public Rect renderYAxisLabel(Canvas canvas, int index, float positionX, TextPaint textPaint){
		return null;
	}
	
	//return - linear gradient object to color and interval that's "in progress".
	private LinearGradient getGradientForInProgressInterval(Interval interval, int startX, int endX){
		return new LinearGradient(startX, 0, endX, 0, m_intervalInsidePaint.getColor(), Color.BLACK, TileMode.CLAMP);
	}
	
	//Instantitate necessary Paint objects.
	static {
		m_normalColor = Color.GREEN;
		m_selectionColor = Color.rgb(64, 64, 255);
		m_intervalLinePaint = new Paint();
		m_intervalLinePaint.setStrokeWidth(m_intervalLineThickness);
		m_intervalLinePaint.setColor(m_normalColor);
		m_intervalInsidePaint = new Paint();
		m_intervalInsidePaint.setColor(UtilHelper.getColor(m_intervalAlpha, m_normalColor));
		m_inProgressIntervalPaint = new Paint();
		m_noteLinePaint = new Paint(m_intervalLinePaint);
		m_noteLinePaint.setColor(Color.WHITE);
		m_noteLinePaint.setPathEffect(m_noteLineDashPathEffect);
	}
}
