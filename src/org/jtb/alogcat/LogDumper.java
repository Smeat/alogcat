package org.jtb.alogcat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class LogDumper {
	public static enum Type {
		PLAIN, HTML
	};

	private Prefs mPrefs;
	private Type mType;

	public LogDumper(Context context, Type type) {
		mPrefs = new Prefs(context);
		mType = type;
	}

	public String dump() {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		Process p = null;

		try {
			p = Runtime.getRuntime().exec(
					new String[] { "logcat", "-d", "-v",
							mPrefs.getFormat().getValue(), "-b",
							mPrefs.getBuffer().getValue(),
							"*:" + mPrefs.getLevel() });

			br = new BufferedReader(new InputStreamReader(p.getInputStream()),
					1024);

			String line;
			while ((line = br.readLine()) != null) {
				if (line.length() == 0) {
					continue;
				}
				switch (mType) {
				case PLAIN:
					sb.append(line);
					sb.append('\n');
					break;
				case HTML:
					Level level = mPrefs.getFormat().getLevel(line);
					sb.append("<font color=\"" + level.getHexColor()
							+ "\" face=\"sans-serif\"><b>");
					sb.append(TextUtils.htmlEncode(line));
					sb.append("</b></font><br/>\n");
					break;
				}
			}
			return sb.toString();
		} catch (IOException e) {
			Log.e("alogcat", "error reading log", e);
			return null;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					Log.e("alogcat", "error closing stream", e);
				}
			}
			if (p != null) {
				p.destroy();
			}
		}
	}
}
