package org.jtb.alogcat;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.text.Html;

public class LogSender {
	private static final SimpleDateFormat LOG_DATE_FORMAT = new SimpleDateFormat(
	"MMM d, yyyy HH:mm:ss ZZZZ");
	
	private Prefs mPrefs;
	private Context mContext;
	private LogDumper mLogDumper;
	
	public LogSender(Context context) {
		mPrefs = new Prefs(context);
		mContext = context;
		mLogDumper = new LogDumper(mContext);
	}
	
	public void send() {
		new Thread(new Runnable() {
			public void run() {
				Intent emailIntent = new Intent(
						android.content.Intent.ACTION_SEND);
				//emailIntent.setType(mPrefs.isEmailHtml() ? "text/html"
				//		: "text/plain");
				emailIntent.setType("message/rfc822");
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
						"Android Log: " + LOG_DATE_FORMAT.format(new Date()));
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
						mPrefs.isEmailHtml() ? Html.fromHtml(mLogDumper.dump(true)) : mLogDumper.dump(false));
				mContext.startActivity(Intent.createChooser(emailIntent, "Send log ..."));
			}
		}).start();
		
	}
	
}
