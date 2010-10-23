package org.jtb.alogcat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class LogDumper {
	private Prefs mPrefs;
	private boolean html = false;
	
	public LogDumper(Context context) {
		mPrefs = new Prefs(context);
		html = mPrefs.isEmailHtml();
	}

	public LogDumper(Context context, boolean html) {
		mPrefs = new Prefs(context);
		this.html = html;
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
			Pattern filterPattern = mPrefs.getFilterPattern();
			Format format = mPrefs.getFormat();
			
			while ((line = br.readLine()) != null) {
				if (filterPattern != null && !filterPattern.matcher(line).find()) {
					continue;
				}

				if (!html) {
					sb.append(line);
					sb.append('\n');
				} else {
					Level level = format.getLevel(line);
					sb.append("<font color=\"" + level.getHexColor()
							+ "\" face=\"sans-serif\"><b>");
					sb.append(TextUtils.htmlEncode(line));
					sb.append("</b></font><br/>\n");

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
