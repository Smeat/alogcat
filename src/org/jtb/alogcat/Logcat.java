package org.jtb.alogcat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Logcat {
	private Level mLevel = null;
	private String mFilter = null;
	private Pattern mFilterPattern = null;
	private boolean mRunning = false;
	private BufferedReader mReader = null;
	private boolean mIsFilterPattern;
	private Handler mHandler;
	private Buffer mBuffer;
	private Process logcatProc;
	private Context mContext;
	private ArrayList<String> mLogCache = new ArrayList<String>();
	private boolean mPlay = true;

	Format mFormat;
	
	public Logcat(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;

		Prefs prefs = new Prefs(mContext);

		mLevel = prefs.getLevel();
		mIsFilterPattern = prefs.isFilterPattern();
		mFilter = prefs.getFilter();
		mFilterPattern = prefs.getFilterPattern();
		mFormat = prefs.getFormat();
		mBuffer = prefs.getBuffer();
	}

	public void start() {
		//Log.d("alogcat", "starting ...");
		mRunning = true;

		try {
			Message m = Message.obtain(mHandler, LogActivity.CLEAR_WHAT);
			mHandler.sendMessage(m);

			List<String> progs = new ArrayList<String>();

			progs.add("logcat");
			progs.add("-v");
			progs.add(mFormat.getValue());
			if (mBuffer != Buffer.MAIN) {
				progs.add("-b");
				progs.add(mBuffer.getValue());
			}
			progs.add("*:" + mLevel);

			logcatProc = Runtime.getRuntime()
					.exec(progs.toArray(new String[0]));

			mReader = new BufferedReader(new InputStreamReader(
					logcatProc.getInputStream()), 1024);

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
			//Log.d("alogcat", "stopped");

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

	private void cat(String line) {
		if (mIsFilterPattern) {
			if (mFilterPattern != null && !mFilterPattern.matcher(line).find()) {
				return;
			}
		} else {
			if (mFilter != null
					&& !line.toLowerCase().contains(mFilter.toLowerCase())) {
				return;
			}
		}

		Message m;

		m = Message.obtain(mHandler, LogActivity.CAT_WHAT);
		m.obj = line;
		mHandler.sendMessage(m);
	}

	private void cat(ArrayList<String> cache) {
		for (String s : cache) {
			cat(s);
		}
		cache.clear();
	}

	public void stop() {
		//Log.d("alogcat", "stopping ...");
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
