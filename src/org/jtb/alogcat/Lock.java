package org.jtb.alogcat;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class Lock {
	private static PowerManager.WakeLock lock;

	private static PowerManager.WakeLock getLock(Context context) {
		if (lock == null) {
			PowerManager mgr = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);

			lock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "org.jtb.alogcat.lock");
			lock.setReferenceCounted(true);
		}
		return lock;
	}

	public static synchronized void acquire(Context context) {
		WakeLock wakeLock = getLock(context);
		if (!wakeLock.isHeld()) {
			wakeLock.acquire();
			Log.d("alogcat", "wake lock acquired");
		}
	}

	public static synchronized void release() {
		if (lock == null) {
			Log
					.w(Lock.class.getSimpleName(),
							"release attempted, but wake lock was null");
		} else {
			if (lock.isHeld()) {
				lock.release();
				lock = null;
				Log.d("alogcat", "wake lock released");
			} else {
				Log.w("alogcat",
						"release attempted, but wake lock was not held");
			}
		}
	}
}
