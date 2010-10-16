package org.jtb.alogcat;

import android.content.Context;
import android.graphics.Color;

public enum Level {
	V(0, "#121212", R.string.verbose_title), D(1, "#00006C",
			R.string.debug_title), I(2, "#20831B", R.string.info_title), W(3,
			"#FD7916", R.string.warn_title), E(4, "#FD0010",
			R.string.error_title), F(5, "#ff0066", R.string.fatal_title);

	private static Level[] byOrder = new Level[6];

	static {
		byOrder[0] = V;
		byOrder[1] = D;
		byOrder[2] = I;
		byOrder[3] = W;
		byOrder[4] = E;
		byOrder[5] = F;
	}

	private String mHexColor;
	private int mColor;
	private int mValue;
	private int mTitleId;

	private Level(int value, String hexColor, int titleId) {
		mValue = value;
		mHexColor = hexColor;
		mColor = Color.parseColor(hexColor);
		mTitleId = titleId;
	}

	public String getHexColor() {
		return mHexColor;
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
