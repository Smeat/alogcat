package org.jtb.alogcat;

import android.graphics.Color;

public enum Level {
	V(Color.parseColor("#999999")), D(Color.parseColor("#0066ff")), I(Color
			.parseColor("#00ff00")), W(Color.parseColor("#ffcc00")), E(Color
			.parseColor("#ff3300")), F(Color.parseColor("#ff0066"));

	private int mColor;

	private Level(int color) {
		mColor = color;
	}
	
	public int getColor() {
		return mColor;
	}
}
