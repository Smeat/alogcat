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
	private static final String SEPARATOR = System
			.getProperty("line.separator");

	private Level mLevel = null;
	private String mFilter = null;
	private boolean mRunning = false;
	private BufferedReader mReader = null;
	private Format mFormat;
	private boolean mAutoScroll;
	private ArrayList<String> mLogCache = new ArrayList<String>();
	private boolean mPlay = true;
	private Handler mHandler;
	private Buffer mBuffer;
	private Process logcatProc;

	public Logcat(Handler handler, Format format, Level level, Buffer buffer,
			String filter, boolean autoScroll) {
		mHandler = handler;
		mLevel = level;
		mFilter = filter;
		mFormat = format;
		mBuffer = buffer;
		mAutoScroll = autoScroll;

	}

	public void start() {
		mRunning = true;

		try {
			Message m = Message.obtain(mHandler, LogActivity.CLEAR_WHAT);
			mHandler.sendMessage(m);

			logcatProc = Runtime.getRuntime().exec(
					new String[] { "logcat", "-v", mFormat.getValue(), "-b",
							mBuffer.getValue(), "*:" + mLevel });

			mReader = new BufferedReader(new InputStreamReader(logcatProc
					.getInputStream()), 1024);

			String line;
			while (mRunning && (line = mReader.readLine()) != null) {
				if (!mRunning) {
					break;
				}
				if (line.length() == 0) {
					continue;
				}
				if (mPlay) {
					cat(mLogCache);
					cat(line);
				} else {
					mLogCache.add(line);
				}
			}
		} catch (IOException e) {
			Log.e("alogcat", "error reading log", e);
			return;
		} finally {
			if (logcatProc != null) {
				logcatProc.destroy();
				logcatProc = null;
			}
			if (mReader != null) {
				try {
					mReader.close();
					mReader = null;
				} catch (IOException e) {
					Log.e("alogcat", "error closing stream", e);
				}
			}
		}
	}

	private void cat(ArrayList<String> cache) {
		for (int i = 0; i < cache.size(); i++) {
			cat(cache.get(i));
		}
		cache.clear();
	}

	private void cat(String line) {
		if (mFilter != null && mFilter.length() != 0 && !line.contains(mFilter)) {
			return;
		}

		Message m;

		m = Message.obtain(mHandler, LogActivity.CAT_WHAT);
		m.obj = line;
		mHandler.sendMessage(m);
		if (mAutoScroll) {
			m = Message.obtain(mHandler, LogActivity.ENDSCROLL_WHAT);
			mHandler.sendMessage(m);
		}
	}

	public String dump() {
		BufferedReader reader = null;

		try {
			Runtime.getRuntime().exec(
					new String[] { "logcat", "-d", "-v", mFormat.getValue(),
							"*:" + mLevel });

			reader = new BufferedReader(new InputStreamReader(logcatProc
					.getInputStream()));

			String line;
			StringBuilder sb = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append(SEPARATOR);
			}
			return sb.toString();
		} catch (IOException e) {
			Log.e("alogcat", "error reading log", e);
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					Log.e("alogcat", "error closing stream", e);
				}
			}
		}
	}

	public void clear() {
		try {
			Runtime.getRuntime().exec(new String[] { "logcat", "-c" });
		} catch (IOException e) {
			Log.e("alogcat", "error clearing log", e);
		} finally {
		}
	}

	public void stop() {
		Log.d("alogcat", "stopping ...");		
		mRunning = false;
	}

	public boolean isRunning() {
		return mRunning;
	}

	public boolean isPlay() {
		return mPlay;
	}

	public void setPlay(boolean play) {
		mPlay = play;
	}
}
