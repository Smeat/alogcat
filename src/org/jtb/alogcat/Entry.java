package org.jtb.alogcat;

public class Entry {
	private String mText = null;
	private Level mLevel;
	
	public Entry(String text) {
		mText = text;
		String lid = mText.substring(0, 1);
		mLevel = Level.valueOf(lid);
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
}
