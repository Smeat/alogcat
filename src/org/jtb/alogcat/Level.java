package org.jtb.alogcat;

import android.content.Context;
import android.graphics.Color;

public enum Level {
	V(0, Color.parseColor("#CCCCCC"), R.string.verbose_title), D(1, Color
			.parseColor("#0066ff"), R.string.debug_title), I(2, Color
			.parseColor("#00ff00"), R.string.info_title), W(3, Color
			.parseColor("#ffcc00"), R.string.warn_title), E(4, Color
			.parseColor("#ff3300"), R.string.error_title), F(5, Color
			.parseColor("#ff0066"), R.string.fatal_title);

	private static Level[] byOrder = new Level[6];

	static {
		byOrder[0] = V;
		byOrder[1] = D;
		byOrder[2] = I;
		byOrder[3] = W;
		byOrder[4] = E;
		byOrder[5] = F;
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

	public static Level getByOrder(int value) {
		return byOrder[value];
	}

	public String getTitle(Context context) {
		return context.getResources().getString(mTitleId);
	}
}
