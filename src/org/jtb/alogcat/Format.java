package org.jtb.alogcat;

import java.util.HashMap;

import android.content.Context;

public enum Format {
	brief(R.string.brief_title),
	process(R.string.process_title),
	tag(R.string.tag_title),
	thread(R.string.thread_title),
	time(R.string.time_title);
	
	private int mTitleId;
	
	private Format(int titleId) {
		mTitleId = titleId;
	}
	
	public String getTitle(Context context) {
		return context.getResources().getString(mTitleId);
	}	
}
