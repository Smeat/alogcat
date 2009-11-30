package org.jtb.alogcat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Stack;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Logcat {
	private Level mLevel = null;
	private String mFilter = null;
	private int mMax = 100;

	public Logcat(Level level, String filter, int max) {
		mLevel = level;
		mFilter = filter;
		mMax = max;
	}

	public void cat(Handler handler) {
		Process logcatProc = null;
		BufferedReader reader = null;

		try {
			logcatProc = Runtime.getRuntime().exec(
					new String[] { "logcat", "*:" + mLevel });

			reader = new BufferedReader(new InputStreamReader(logcatProc
					.getInputStream()));

			String line;
			final StringBuilder log = new StringBuilder();

			while ((line = reader.readLine()) != null) {
				Message m = Message.obtain(handler, CatActivity.CAT_WHAT);
				m.obj = line;
				handler.sendMessage(m);
				// TODO: this is optional, or togglable
				m = Message.obtain(handler, CatActivity.ENDSCROLL_WHAT);
				handler.sendMessage(m);
			}
		} catch (IOException e) {
			Log.e("Logcat", "error reading log", e);
			return;
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					Log.e("Logcat", "error closing stream", e);
				}

		}
	}
}
