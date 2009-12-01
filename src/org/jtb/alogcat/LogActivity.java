package org.jtb.alogcat;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class LogActivity extends Activity {
	private static final SimpleDateFormat LOG_DATE_FORMAT = new SimpleDateFormat(
			"MMM d, yyyy HH:mm:ss ZZZZ");

	static final int LEVEL_DIALOG = 0;
	static final int FILTER_DIALOG = 1;
	static final int FORMAT_DIALOG = 2;

	private static final int MENU_LEVEL = 0;
	private static final int MENU_FILTER = 1;
	private static final int MENU_FORMAT = 2;
	private static final int MENU_AUTOSCROLL = 3;
	private static final int MENU_SEND = 4;
	private static final int MENU_PLAY = 5;

	private static final int WINDOW_SIZE = 1000;

	static final int CAT_WHAT = 0;
	static final int ENDSCROLL_WHAT = 1;
	static final int CLEAR_WHAT = 2;

	private AlertDialog mLevelDialog;
	private AlertDialog mFilterDialog;
	private AlertDialog mFormatDialog;

	private LinearLayout mCatLayout;
	private ScrollView mCatScroll;
	private Menu mMenu;
	private MenuItem mPlayItem;
	private MenuItem mLevelItem;
	private MenuItem mFilterItem;
	private MenuItem mFormatItem;
	private MenuItem mAutoScrollItem;

	private Level mLevel = Level.V;
	private Level mLastLevel = Level.V;
	private String mFilter = null;
	private Logcat mLogcat;
	private Format mFormat = Format.BRIEF;
	private Prefs mPrefs;
	private boolean mAutoScroll = true;

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
		entryText.setTextSize(10);
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

		reset();
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
				mLogcat = new Logcat(mHandler, mFormat, mLevel, mFilter,
						mAutoScroll);
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

		mFormatItem = menu.add(0, MENU_FORMAT, 0, getResources().getString(
				R.string.format_menu, mFormat.getTitle(this)));
		mFormatItem.setIcon(android.R.drawable.ic_menu_sort_by_size);

		mLevelItem = menu.add(0, MENU_LEVEL, 0, getResources().getString(
				R.string.level_menu, mLevel.getTitle(this)));
		mLevelItem.setIcon(android.R.drawable.ic_menu_agenda);

		mFilterItem = menu.add(0, MENU_FILTER, 0, getResources().getString(
				R.string.filter_menu, mFilter));
		mFilterItem.setIcon(android.R.drawable.ic_menu_search);

		mAutoScrollItem = menu.add(0, MENU_AUTOSCROLL, 0, getResources()
				.getString(R.string.autoscroll_menu, mAutoScroll));
		mAutoScrollItem.setIcon(android.R.drawable.ic_menu_more);

		MenuItem sendItem = menu.add(0, MENU_SEND, 0, R.string.send_menu);
		sendItem.setIcon(android.R.drawable.ic_menu_send);

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

		mFormatItem.setTitle(getResources().getString(R.string.format_menu,
				mFormat.getTitle(this)));

		mLevelItem.setTitle(getResources().getString(R.string.level_menu,
				mLevel.getTitle(this)));

		int filterMenuId = R.string.filter_menu;
		if (mFilter == null || mFilter.length() == 0) {
			filterMenuId = R.string.filter_menu_empty;
		}
		mFilterItem.setTitle(getResources().getString(filterMenuId, mFilter));

		mAutoScrollItem.setTitle(getResources().getString(
				R.string.autoscroll_menu, mAutoScroll));

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_LEVEL:
			showDialog(LEVEL_DIALOG);
			return true;
		case MENU_FILTER:
			showDialog(FILTER_DIALOG);
			return true;
		case MENU_FORMAT:
			showDialog(FORMAT_DIALOG);
			return true;
		case MENU_AUTOSCROLL:
			setAutoScroll(!mAutoScroll);
			return true;
		case MENU_SEND:
			send();
			return true;
		case MENU_PLAY:
			mLogcat.setPlay(!mLogcat.isPlay());
			return true;
		}

		return false;
	}

	public void send() {
		new Thread(new Runnable() {
			public void run() {
				Intent emailIntent = new Intent(
						android.content.Intent.ACTION_SEND);
				emailIntent.setType("plain/text");
				// emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, "");
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
						"Android Log: " + LOG_DATE_FORMAT.format(new Date()));
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, mLogcat
						.dumpLogText());
				startActivity(Intent.createChooser(emailIntent, "Send log ..."));
			}
		}).start();
	}

	public void setAutoScroll(boolean autoScroll) {
		mAutoScroll = autoScroll;
		mPrefs.setAutoScroll(autoScroll);
		reset();
	}

	public void setLevel(Level level) {
		mLevel = level;
		mPrefs.setLevel(level);
		reset();
	}

	public void setFilter(String filter) {
		mFilter = filter;
		mPrefs.setFilter(filter);
		reset();
	}

	public void setFormat(Format format) {
		mFormat = format;
		mPrefs.setFormat(format);
		reset();
	}

	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder;

		switch (id) {
		case LEVEL_DIALOG:
			builder = new LevelDialog.Builder(this);
			mLevelDialog = builder.create();
			return mLevelDialog;
		case FILTER_DIALOG:
			builder = new FilterDialog.Builder(this);
			mFilterDialog = builder.create();
			return mFilterDialog;
		case FORMAT_DIALOG:
			builder = new FormatDialog.Builder(this);
			mFormatDialog = builder.create();
			return mFormatDialog;
		}
		return null;
	}

}