package org.jtb.alogcat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SaveReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("alogcat", "received intent for save");
		Lock.acquire(context);
		
		Intent svcIntent = new Intent(context, SaveService.class);
		context.startService(svcIntent);
	}
}
