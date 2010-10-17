package org.jtb.alogcat;

import android.app.AlarmManager;
import android.content.Context;

public enum Frequency {
	HOUR(R.string.freq_60_title, AlarmManager.INTERVAL_HOUR),
	HALF_HOUR(R.string.freq_30_title, AlarmManager.INTERVAL_HALF_HOUR),
	FIFTEEN_MINUTES(R.string.freq_15_title, AlarmManager.INTERVAL_FIFTEEN_MINUTES);
	
	private long value;
	private int id;
	
	private Frequency(int id, long value) {
		this.id = id;
		this.value = value;
	}
	
	public long getValue() {
		return value;
	}
	
	public String getTitle(Context context) {
		return context.getResources().getString(id);
	}		
}
