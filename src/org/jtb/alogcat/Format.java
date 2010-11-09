package org.jtb.alogcat;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;

public enum Format {
	BRIEF("brief", R.string.brief_title, Pattern.compile("^([VDIWEF])/")),
	PROCESS("process", R.string.process_title, Pattern.compile("^([VDIWEF])\\(")),
	TAG("tag", R.string.tag_title, Pattern.compile("^([VDIWEF])/")),
	THREAD("thread", R.string.thread_title, Pattern.compile("^([VDIWEF])\\(")),
	TIME("time", R.string.time_title, Pattern.compile(" ([VDIWEF])/")),
	THREADTIME("threadtime", R.string.threadtime_title, Pattern.compile(" ([VDIWEF]) ")),
	LONG("long", R.string.long_title, Pattern.compile("([VDIWEF])/")),
	RAW("raw", R.string.raw_title, null);
	
	private static Format[] byOrder = new Format[8];

	static {
		byOrder[0] = BRIEF;
		byOrder[1] = PROCESS;
		byOrder[2] = TAG;
		byOrder[3] = THREAD;
		byOrder[4] = TIME;
		byOrder[5] = THREADTIME;
		byOrder[6] = LONG;
		byOrder[7] = RAW;
	}
	
	private static final HashMap<String,Format> VALUE_MAP = new HashMap<String,Format>();
	
	static {
		VALUE_MAP.put(BRIEF.mValue, BRIEF); 
		VALUE_MAP.put(PROCESS.mValue, PROCESS); 
		VALUE_MAP.put(TAG.mValue, TAG); 
		VALUE_MAP.put(THREAD.mValue, THREAD); 
		VALUE_MAP.put(THREADTIME.mValue, THREAD); 
		VALUE_MAP.put(TIME.mValue, TIME); 
		VALUE_MAP.put(RAW.mValue, RAW); 
		VALUE_MAP.put(LONG.mValue, LONG); 
	}
		
	private String mValue;
	private int mTitleId;
	private Pattern mLevelPattern;
	
	private Format(String value, int titleId, Pattern levelPattern) {
		mValue = value;
		mTitleId = titleId;
		mLevelPattern = levelPattern;
	}
	
	public String getTitle(Context context) {
		return context.getResources().getString(mTitleId);
	}	
	
	public static final Format byValue(String value) {
		return VALUE_MAP.get(value);
	}
	
	public Level getLevel(String line) {
		if (mLevelPattern == null) {
			return null;
		}
		Matcher m = mLevelPattern.matcher(line);
		if (m.find()) {
			return Level.valueOf(m.group(1));
		}
		return null;
	}
	
	public static Format getByOrder(int order) {
		return byOrder[order];
	}
	
	public String getValue() {
		return mValue;
	}
}
