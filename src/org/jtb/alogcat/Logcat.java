package org.jtb.alogcat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Stack;

import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Logcat {
	private Level mLevel = null;
	private String mFilter = null;
	private boolean mRunning = false;
	private BufferedReader mReader = null;
	private Format mFormat;
	private boolean mAutoScroll;
	private StringBuilder mLogText = new StringBuilder();

	public Logcat(Format format, Level level, String filter, boolean autoScroll) {
		mLevel = level;
		mFilter = filter;
		mFormat = format;
		mAutoScroll = autoScroll;
	}

	public void cat(Handler handler) {
		Process logcatProc = null;
		mRunning = true;
		String separator = System.getProperty("line.separator");

		try {
			Message m = Message.obtain(handler, CatActivity.CLEAR_WHAT);
			handler.sendMessage(m);

			logcatProc = Runtime.getRuntime().exec(
					new String[] { "logcat", "-v", mFormat.toString(),
							"*:" + mLevel });

			mReader = new BufferedReader(new InputStreamReader(logcatProc
					.getInputStream()), 1024);

			String line;
			while (mRunning && (line = mReader.readLine()) != null) {
				if (!mRunning) {
					break;
				}
				if (mFilter != null && mFilter.length() != 0
						&& !line.contains(mFilter)) {
					continue;
				}
				m = Message.obtain(handler, CatActivity.CAT_WHAT);
				m.obj = line;
				handler.sendMessage(m);
				if (mAutoScroll) {
					m = Message.obtain(handler, CatActivity.ENDSCROLL_WHAT);
					handler.sendMessage(m);
				}

				mLogText.append(line);
				mLogText.append(separator);
			}
		} catch (IOException e) {
			Log.e("Logcat", "error reading log", e);
			return;
		} finally {
			if (mReader != null)
				try {
					mReader.close();
				} catch (IOException e) {
					Log.e("Logcat", "error closing stream", e);
				}

		}
	}

	public String getLogText() {
		return mLogText.toString();
	}

	public void stop() {
		mRunning = false;
		try {
			if (mReader != null) {
				mReader.close();
			}
		} catch (IOException e) {
			Log.e("Logcat", "error closing stream", e);
		}
	}

	public boolean isRunning() {
		return mRunning;
	}
}
