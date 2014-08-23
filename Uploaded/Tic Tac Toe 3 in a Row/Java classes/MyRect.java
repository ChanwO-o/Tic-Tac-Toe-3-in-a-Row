package com.tic.tac.toe.three.row;

import android.graphics.Color;
import android.graphics.RectF;

public class MyRect extends RectF {

	int color;
	String status;
	
	public MyRect(int left, int top, int right, int bottom) {
		super(left, top, right, bottom);
		color = Color.TRANSPARENT;
		status = "Blank";
	}
}
