package org.jtb.alogcat;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class SaveScheduler {
	private static final int NOTIFY_RUNNING = 100;

	private Context mContext = null;
	private Prefs mPrefs = null;

	public SaveScheduler(Context context) {
		this.mContext = context;
		mPrefs = new Prefs(mContext);
	}

	public void start() {
		Log.d("alogcat", "scheduling periodic saves");

		AlarmManager mgr = (AlarmManager) mContext
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(mContext, SaveReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, i,
				PendingIntent.FLAG_CANCEL_CURRENT);
		mgr.cancel(pi);
		mgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime(), mPrefs.getPeriodicFrequency()
						.getValue(), pi);
		// mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
		// SystemClock.elapsedRealtime(), 30000, pi);
		addNotification();
	}

	public void stop() {
		Log.d("alogcat", "canceling periodic saves");

		AlarmManager mgr = (AlarmManager) mContext
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(mContext, SaveReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, i,
				PendingIntent.FLAG_CANCEL_CURRENT);
		mgr.cancel(pi);
		removeNotification();
	}

	private void addNotification() {
		NotificationManager nm = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);

		int icon = android.R.drawable.presence_away;
		CharSequence tickerText = "aLogcat - periodically saving logs";

		Notification notification = new Notification(icon, tickerText,
				System.currentTimeMillis());
		notification.flags |= Notification.FLAG_ONGOING_EVENT
				| Notification.FLAG_NO_CLEAR;

		CharSequence contentTitle = tickerText;
		CharSequence contentText = "Click to open aLogcat";

		Intent notificationIntent = new Intent(mContext, LogActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(mContext, contentTitle, contentText,
				contentIntent);
		nm.notify(NOTIFY_RUNNING, notification);
	}

	private void removeNotification() {
		NotificationManager nm = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(NOTIFY_RUNNING);
	}
}
