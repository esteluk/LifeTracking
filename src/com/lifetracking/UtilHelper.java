package com.lifetracking;

import java.text.DecimalFormat;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;

//UtilHelper has some miscellaneous helpful functions.
public class UtilHelper {

	//Nice format for double values.
	static final DecimalFormat m_valueFormatter = new DecimalFormat("#.##");
	
	//return - a double value converted to string.
	public static String valueToString(double value){
		return m_valueFormatter.format(value);
	}
	
	//return - text bounds for the string written using textPaint. This functions like Paint.getTextBounds(), but accounts for text alignment.
	public static Rect getTextBounds(Paint textPaint, String string){
		Rect bounds = new Rect();
		textPaint.getTextBounds(string, 0, string.length(), bounds);
		if(textPaint.getTextAlign() == Align.CENTER) bounds.offset(-bounds.width() / 2, 0);
		else if(textPaint.getTextAlign() == Align.RIGHT) bounds.offset(-bounds.width(), 0);
		bounds.bottom++;//seems like the bottom pixels don't get covered otherwise...
		return bounds;
	}
	
	//return - color with replaced alpha channel.
	public static int getColor(int alpha, int color){
		return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
	}
	
	//return - darker version of the given color.
	public static int getDarkerColor(int color){
		return Color.rgb(Color.red(color) / 2, Color.green(color) / 2, Color.blue(color) / 2);
	}
}
