package org.jtb.alogcat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScheduleReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("alogcat", "received intent for schedule");

		org.jtb.alogcat.Intent.handleExtras(context, intent);

		Prefs prefs = new Prefs(context);

		SaveScheduler scheduler = new SaveScheduler(context);
		if (intent.getAction().equals(org.jtb.alogcat.Intent.SAVE_START_INTENT)) {
			prefs.setPeriodicSave(true);
			scheduler.start();
		} else if (intent.getAction().equals(
				org.jtb.alogcat.Intent.SAVE_STOP_INTENT)) {
			scheduler.stop();
		}
	}
}
