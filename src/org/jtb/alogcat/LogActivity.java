package org.jtb.alogcat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
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
	private MenuItem mPlayItem;
	private MenuItem mFilterItem;

	private Level mLastLevel = Level.V;
	private Logcat mLogcat;
	private LogDumper mLogDumper;
	private Prefs mPrefs;
	private LogActivity mThis;

	private SaveScheduler mSaveScheduler;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CAT_WHAT:
				String line = (String) msg.obj;
				cat(line);
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
		Format format = mPrefs.getFormat();
		Level level = format.getLevel(s);
		if (level == null) {
			level = mLastLevel;
		} else {
			mLastLevel = level;
		}
		entryText.setTextColor(level.getColor());
		entryText.setTextSize(mPrefs.getTextsize().getValue());
		entryText.setTypeface(Typeface.DEFAULT_BOLD);
		mCatLayout.addView(entryText);

		if (mPrefs.isAutoScroll()) {
			mCatScroll.post(new Runnable() {
				public void run() {
					mCatScroll.fullScroll(ScrollView.FOCUS_DOWN);
				}
			});			
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.log);

		mThis = this;
		mPrefs = new Prefs(this);

		mCatScroll = (ScrollView) findViewById(R.id.cat_scroll);
		mCatLayout = (LinearLayout) findViewById(R.id.cat_layout);

		mSaveScheduler = new SaveScheduler(this);
		mLogDumper = new LogDumper(this);

		reset();
	}

	@Override
	public void onResume() {
		super.onResume();

		mCatScroll.setBackgroundColor(mPrefs.getBackgroundColor().getColor());

		reset();
		Log.d("alogcat", "resumed");

		if (mPrefs.isPeriodicSave()) {
			mSaveScheduler.start();
		} else {
			mSaveScheduler.stop();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mLogcat.stop();
		Log.d("alogcat", "paused");
	}

	public void reset() {
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
				mLogcat = new Logcat(mThis, mHandler);
				mLogcat.start();
			}
		}).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		mPlayItem = menu.add(0, MENU_PLAY, 0, R.string.pause_menu);
		mPlayItem.setIcon(android.R.drawable.ic_media_pause);

		mFilterItem = menu.add(0, MENU_FILTER, 0,
				getResources().getString(R.string.filter_menu, mPrefs.getFilter()));
		mFilterItem.setIcon(android.R.drawable.ic_menu_search);

		MenuItem clearItem = menu.add(0, MENU_CLEAR, 0, R.string.clear_menu);
		clearItem.setIcon(android.R.drawable.ic_menu_close_clear_cancel);

		MenuItem sendItem = menu.add(0, MENU_SEND, 0, R.string.send_menu);
		sendItem.setIcon(android.R.drawable.ic_menu_send);

		MenuItem saveItem = menu.add(0, MENU_SAVE, 0, R.string.save_menu);
		saveItem.setIcon(android.R.drawable.ic_menu_save);

		MenuItem prefsItem = menu.add(0, MENU_PREFS, 0, getResources()
				.getString(R.string.prefs_menu));
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
		String filter = mPrefs.getFilter();
		if (filter == null || filter.length() == 0) {
			filterMenuId = R.string.filter_menu_empty;
		}
		mFilterItem.setTitle(getResources().getString(filterMenuId, filter));

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
			File f = new Saver(this).save();
			String msg = getResources().getString(R.string.saving_log,
					f.toString());
			Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
			return true;
		case MENU_PLAY:
			mLogcat.setPlay(!mLogcat.isPlay());
			return true;
		case MENU_CLEAR:
			mLogcat.clear();
			reset();
			return true;
		case MENU_PREFS:
			Intent intent = new Intent(this, PrefsActivity.class);
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
				//emailIntent.setType(mPrefs.isEmailHtml() ? "text/html"
				//		: "text/plain");
				emailIntent.setType("message/rfc822");
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
						"Android Log: " + LOG_DATE_FORMAT.format(new Date()));
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
						Html.fromHtml(mLogDumper.dump()));
				startActivity(Intent.createChooser(emailIntent, "Send log ..."));
			}
		}).start();
	}

	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case FILTER_DIALOG:
			mFilterDialog = new FilterDialog(this);
			return mFilterDialog;
		}
		return null;
	}

}