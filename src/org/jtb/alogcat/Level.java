package org.jtb.alogcat;

import android.content.Context;
import android.graphics.Color;

public enum Level {
	V(0, Color.parseColor("#999999"), R.string.verbose_title), D(1, Color
			.parseColor("#0066ff"), R.string.debug_title), I(2, Color
			.parseColor("#00ff00"), R.string.info_title), W(3, Color
			.parseColor("#ffcc00"), R.string.warn_title), E(4, Color
			.parseColor("#ff3300"), R.string.error_title), F(5, Color
			.parseColor("#ff0066"), R.string.fatal_title);

	private static Level[] byValue = new Level[6];

	static {
		byValue[0] = V;
		byValue[1] = D;
		byValue[2] = I;
		byValue[3] = W;
		byValue[4] = E;
		byValue[5] = F;
	}

	private int mColor;
	private int mValue;
	private int mTitleId;

	private Level(int value, int color, int titleId) {
		mValue = value;
		mColor = color;
		mTitleId = titleId;
	}

	public int getColor() {
		return mColor;
	}

	public int getValue() {
		return mValue;
	}

	public static Level getByValue(int value) {
		return byValue[value];
	}

	public String getTitle(Context context) {
		return context.getResources().getString(mTitleId);
	}
}
