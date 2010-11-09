package org.jtb.alogcat;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

public class LogActivity extends ListActivity {
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

	private ListView mLogList;
	private LogEntryAdapter mLogEntryAdapter;
	private MenuItem mPlayItem;
	private MenuItem mFilterItem;

	private Level mLastLevel = Level.V;
	private Logcat mLogcat;
	private LogDumper mLogDumper;
	private LogSender mLogSender;
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
				mLogEntryAdapter.clear();
				break;
			}
		}
	};

	private void cat(String s) {
		if (mLogEntryAdapter.getCount() > WINDOW_SIZE) {
			mLogEntryAdapter.remove(0);
		}

		Format format = mPrefs.getFormat();
		Level level = format.getLevel(s);
		if (level == null) {
			level = mLastLevel;
		} else {
			mLastLevel = level;
		}
		
		LogEntry entry = new LogEntry(s, level);
	
		mLogEntryAdapter.add(entry);
		if (mPrefs.isAutoScroll()) {
			mLogList.post(new Runnable() {
				public void run() {
					mLogList.setSelection(mLogEntryAdapter.getCount()-1);
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

		mSaveScheduler = new SaveScheduler(this);
		mLogDumper = new LogDumper(this);
		mLogSender = new LogSender(this);

		mLogList = (ListView) findViewById(android.R.id.list);
		mLogEntryAdapter = new LogEntryAdapter(this, R.layout.entry, new ArrayList<LogEntry>());
		setListAdapter(mLogEntryAdapter);		
	}

	@Override
	public void onResume() {
		super.onResume();

		mLogList.setBackgroundColor(mPrefs.getBackgroundColor().getColor());

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
		if (mLogcat != null) {
			mLogcat.stop();
		}
		Log.d("alogcat", "paused");
	}

	public void reset() {
		Toast.makeText(this, R.string.reading_logs, Toast.LENGTH_LONG).show();
		mLastLevel = Level.V;

		if (mLogcat != null) {
			mLogcat.stop();
		}
		
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

		mFilterItem = menu.add(
				0,
				MENU_FILTER,
				0,
				getResources().getString(R.string.filter_menu,
						mPrefs.getFilter()));
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
			mLogSender.send();
			return true;
		case MENU_SAVE:
			File f = new LogSaver(this).save();
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

	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case FILTER_DIALOG:
			mFilterDialog = new FilterDialog(this);
			return mFilterDialog;
		}
		return null;
	}

}