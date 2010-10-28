package org.jtb.alogcat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.Log;

public class LogSaver {
	private static final SimpleDateFormat LOG_FILE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd-HH-mm-ssZ");

	private Context mContext;
	private Prefs mPrefs;
	private LogDumper mLogDumper;

	public LogSaver(Context context) {
		mContext = context;
		mPrefs = new Prefs(mContext);

		mLogDumper = new LogDumper(mContext);
	}

	public File save() {
		final File path = new File("/sdcard/alogcat");
		final File file = new File(path + "/alogcat."
				+ LOG_FILE_FORMAT.format(new Date()) + ".txt");

		String msg = "saving log to: " + file.toString();
		Log.d("alogcat", msg);

		new Thread(new Runnable() {
			public void run() {
				String dump = mLogDumper.dump(false);

				if (!path.exists()) {
					path.mkdir();
				}

				BufferedWriter bw = null;
				try {
					file.createNewFile();
					bw = new BufferedWriter(new FileWriter(file), 1024);
					bw.write(dump);
				} catch (IOException e) {
					Log.e("alogcat", "error saving log", e);
				} finally {
					if (bw != null) {
						try {
							bw.close();
						} catch (IOException e) {
							Log.e("alogcat", "error closing log", e);
						}
					}
				}
			}
		}).start();
		
		return file;
	}

}
