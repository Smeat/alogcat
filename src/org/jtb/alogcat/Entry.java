package org.jtb.alogcat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Entry {
	private static final Pattern[] LEVEL_PATTERNS = new Pattern[] {
			Pattern.compile("^([VDIWEF]+)/"), Pattern.compile("^([VDIWEF]+)\\("),
			Pattern.compile("([VDIWEF]+)/") };

	private String mText = null;
	private Level mLevel;

	public Entry(String text) {
		mText = text;
		parseLevel();
	}

	public String getText() {
		return mText;
	}

	public void setText(String text) {
		this.mText = text;
	}

	public Level getLevel() {
		return mLevel;
	}

	public void setLevel(Level level) {
		this.mLevel = level;
	}

	private void parseLevel() {
		for (int i = 0; i < LEVEL_PATTERNS.length; i++) {
			Matcher m = LEVEL_PATTERNS[i].matcher(mText);
			if (m.find()) {
				mLevel = Level.valueOf(m.group(1));
				return;
			}
		}
		throw new AssertionError("could not parse level from line: " + mText);
	}
}
