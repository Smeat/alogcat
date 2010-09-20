package org.jtb.alogcat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class LogActivity extends Activity {
	private static final SimpleDateFormat LOG_DATE_FORMAT = new SimpleDateFormat(
	"MMM d, yyyy HH:mm:ss ZZZZ");
	private static final SimpleDateFormat LOG_FILE_FORMAT = new SimpleDateFormat(
	"yyyy-MM-dd-HH-mm-ssZ");

	static final int FILTER_DIALOG = 1;

	private static final int MENU_FILTER = 1;
	private static final int MENU_SEND = 5;
	private static final int MENU_PLAY = 6;
	private static final int MENU_CLEAR = 8;
	private static final int MENU_SAVE = 9;
	private static final int MENU_PREFS = 10;

	private static final int WINDOW_SIZE = 1000;

	static final int CAT_WHAT = 0;
	static final int ENDSCROLL_WHAT = 1;
	static final int CLEAR_WHAT = 2;

	private AlertDialog mFilterDialog;

	private LinearLayout mCatLayout;
	private ScrollView mCatScroll;
	private Menu mMenu;
	private MenuItem mPlayItem;
	private MenuItem mFilterItem;

	private Level mLevel = Level.V;
	private Level mLastLevel = Level.V;
	private String mFilter = null;
	private Logcat mLogcat;
	private Format mFormat = Format.BRIEF;
	private Prefs mPrefs;
	private boolean mAutoScroll = true;
	private Buffer mBuffer = Buffer.MAIN;
	private Textsize mTextsize = Textsize.MEDIUM;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CAT_WHAT:
				String line = (String) msg.obj;
				cat(line);
				break;
			case ENDSCROLL_WHAT:
				mCatScroll.post(new Runnable() {
					public void run() {
						mCatScroll.fullScroll(ScrollView.FOCUS_DOWN);
					}
				});
				break;
			case CLEAR_WHAT:
				mCatLayout.removeAllViews();
				break;
			}
		}
	};

	private void cat(String s) {
		if (mCatLayout.getChildCount() > WINDOW_SIZE) {
			mCatLayout.removeViewAt(0);
		}

		TextView entryText = new TextView(this);
		entryText.setText(s);
		Level level = mFormat.getLevel(s);
		if (level == null) {
			level = mLastLevel;
		} else {
			mLastLevel = level;
		}
		entryText.setTextColor(level.getColor());
		entryText.setTextSize(mTextsize.getValue());
		entryText.setTypeface(Typeface.DEFAULT_BOLD);
		mCatLayout.addView(entryText);

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.log);

		mPrefs = new Prefs(this);

		mCatScroll = (ScrollView) findViewById(R.id.cat_scroll);
		mCatLayout = (LinearLayout) findViewById(R.id.cat_layout);

		mFormat = mPrefs.getFormat();
		mLevel = mPrefs.getLevel();
		mFilter = mPrefs.getFilter();
		mAutoScroll = mPrefs.isAutoScroll();
		mBuffer = mPrefs.getBuffer();
		mTextsize = mPrefs.getTextsize();

		reset();
	}

	@Override
	public void onResume() {
		super.onResume();
		
		mFormat = mPrefs.getFormat();
		mTextsize = mPrefs.getTextsize(); 
		mLevel = mPrefs.getLevel();
		mBuffer = mPrefs.getBuffer();
		mAutoScroll = mPrefs.isAutoScroll();
		
		mCatScroll.setBackgroundColor(mPrefs.getBackgroundColor());
		
		reset();
		Log.d("alogcat", "resumed");
	}

	@Override
	public void onPause() {
		super.onPause();
		mLogcat.stop();
		Log.d("alogcat", "paused");
	}

	private void reset() {
		Toast.makeText(this, R.string.reading_logs, Toast.LENGTH_LONG).show();
		mLastLevel = Level.V;

		new Thread(new Runnable() {
			public void run() {
				if (mLogcat != null) {
					mLogcat.stop();
				}
			}
		}).start();
		new Thread(new Runnable() {
			public void run() {
				mLogcat = new Logcat(mHandler, mFormat, mLevel, mBuffer,
						mFilter, mAutoScroll);
				mLogcat.start();
			}
		}).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		mMenu = menu;

		mPlayItem = menu.add(0, MENU_PLAY, 0, R.string.pause_menu);
		mPlayItem.setIcon(android.R.drawable.ic_media_pause);

		mFilterItem = menu.add(0, MENU_FILTER, 0, getResources().getString(
				R.string.filter_menu, mFilter));
		mFilterItem.setIcon(android.R.drawable.ic_menu_search);

		MenuItem clearItem = menu.add(0, MENU_CLEAR, 0, R.string.clear_menu);
		clearItem.setIcon(android.R.drawable.ic_menu_close_clear_cancel);

		MenuItem sendItem = menu.add(0, MENU_SEND, 0, R.string.send_menu);
		sendItem.setIcon(android.R.drawable.ic_menu_send);

		MenuItem saveItem = menu.add(0, MENU_SAVE, 0, R.string.save_menu);
		saveItem.setIcon(android.R.drawable.ic_menu_save);

		MenuItem prefsItem = menu.add(0, MENU_PREFS, 0, getResources().getString(
				R.string.prefs_menu));
		prefsItem.setIcon(android.R.drawable.ic_menu_preferences);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mLogcat.isPlay()) {
			mPlayItem.setTitle(R.string.pause_menu);
			mPlayItem.setIcon(android.R.drawable.ic_media_pause);
		} else {
			mPlayItem.setTitle(R.string.play_menu);
			mPlayItem.setIcon(android.R.drawable.ic_media_play);
		}

		int filterMenuId = R.string.filter_menu;
		if (mFilter == null || mFilter.length() == 0) {
			filterMenuId = R.string.filter_menu_empty;
		}
		mFilterItem.setTitle(getResources().getString(filterMenuId, mFilter));

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_FILTER:
			showDialog(FILTER_DIALOG);
			return true;
		case MENU_SEND:
			send();
			return true;
		case MENU_SAVE:
			save();
			return true;
		case MENU_PLAY:
			mLogcat.setPlay(!mLogcat.isPlay());
			return true;
		case MENU_CLEAR:
			mLogcat.clear();
			reset();
			return true;
		case MENU_PREFS:
			Intent intent = new Intent(this,
					PrefsActivity.class);
			startActivity(intent);			
			return true;
		}

		return false;
	}

	public void send() {
		new Thread(new Runnable() {
			public void run() {
				Intent emailIntent = new Intent(
						android.content.Intent.ACTION_SEND);
				emailIntent.setType("text/plain");
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
						"Android Log: " + LOG_DATE_FORMAT.format(new Date()));
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, dump());
				startActivity(Intent.createChooser(emailIntent, "Send log ..."));
			}
		}).start();
	}

	public void save() {
		final File path = new File("/sdcard/alogcat");
		final File file = new File(path + "/alogcat."
				+ LOG_FILE_FORMAT.format(new Date()) + ".txt");

		Toast.makeText(this,
				getResources().getString(R.string.saving_log, file.toString()),
				Toast.LENGTH_LONG).show();

		new Thread(new Runnable() {
			public void run() {
				String dump = dump();

				if (!path.exists()) {
					path.mkdir();
				}
				
				BufferedWriter bw = null;
				try {
					file.createNewFile();
					bw = new BufferedWriter(new FileWriter(file));
					bw.write(dump);
				} catch (IOException e) {
					Log.e("alogcat", "error saving log", e);
				} finally {
					if (bw != null) {
						try {
							bw.close();
						} catch (IOException e) {
						}
					}
				}
			}
		}).start();
	}

	private String dump() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mCatLayout.getChildCount(); i++) {
			TextView tv = (TextView) mCatLayout.getChildAt(i);
			CharSequence s = tv.getText();
			sb.append(s);
			sb.append("\n");
		}

		return sb.toString();
	}

	public void setFilter(String filter) {
		mFilter = filter;
		mPrefs.setFilter(filter);
		reset();
	}

	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder;

		switch (id) {
		case FILTER_DIALOG:
			builder = new FilterDialog.Builder(this);
			mFilterDialog = builder.create();
			return mFilterDialog;
		}
		return null;
	}

}