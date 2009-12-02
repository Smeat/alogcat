package org.jtb.alogcat;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;

public enum Textsize {
	SMALL(8, R.string.small_title),
	MEDIUM(10, R.string.medium_title),
	LARGE(12, R.string.large_title);
	
	private static Textsize[] byOrder = new Textsize[3];

	static {
		byOrder[0] = SMALL;
		byOrder[1] = MEDIUM;
		byOrder[2] = LARGE;
	}
	
	private static final HashMap<Integer,Textsize> VALUE_MAP = new HashMap<Integer,Textsize>();
	
	static {
		VALUE_MAP.put(SMALL.mValue, SMALL); 
		VALUE_MAP.put(MEDIUM.mValue, MEDIUM); 
		VALUE_MAP.put(LARGE.mValue, LARGE); 
	}
		
	private Integer mValue;
	private int mTitleId;
	
	private Textsize(Integer value, int titleId) {
		mValue = value;
		mTitleId = titleId;
	}
	
	public String getTitle(Context context) {
		return context.getResources().getString(mTitleId);
	}	
	
	public static final Textsize byValue(String value) {
		return VALUE_MAP.get(value);
	}
	
	public static Textsize getByOrder(int order) {
		return byOrder[order];
	}
	
	public Integer getValue() {
		return mValue;
	}
}
