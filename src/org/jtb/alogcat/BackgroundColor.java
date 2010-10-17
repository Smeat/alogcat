package org.jtb.alogcat;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;

public enum BackgroundColor {
	BLACK(R.string.black_title, "#000000"), WHITE(R.string.white_title,
			"#ffffff"), GRAY(R.string.gray_title, "#e0e0e0");

	private static final Map<String, BackgroundColor> byHexColor;

	static {
		byHexColor = new HashMap<String, BackgroundColor>() {
			{
				put("#000000", BLACK);
				put("#ffffff", WHITE);
				put("#e0e0e0", GRAY);
			}
		};
	}

	private String mHexColor;
	private int mId;

	private BackgroundColor(int id, String hexColor) {
		mId = id;
		mHexColor = hexColor;
	}

	public static BackgroundColor valueOfHexColor(String hexColor) {
		return byHexColor.get(hexColor);
	}

	public int getColor() {
		return Color.parseColor(mHexColor);
	}

	public String getTitle(Context context) {
		return context.getResources().getString(mId);
	}
}
