package org.jtb.alogcat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import org.jtb.alogcat.R;

public class LogActivity extends ListActivity {
	private static class State {
		List<LogEntry> entries = null;
		boolean play = true;
		int position = -1;
	}

	static final SimpleDateFormat LOG_DATE_FORMAT = new SimpleDateFormat(
			"MMM d, yyyy HH:mm:ss ZZZZ");

	static final int FILTER_DIALOG = 1;

	private static final int MENU_FILTER = 1;
	private static final int MENU_SEND = 5;
	private static final int MENU_PLAY = 6;
	private static final int MENU_CLEAR = 8;
	private static final int MENU_SAVE = 9;
	private static final int MENU_PREFS = 10;
	private static final int MENU_JUMP_TOP = 11;
	private static final int MENU_JUMP_BOTTPM = 12;

	private static final int WINDOW_SIZE = 1000;

	static final int CAT_WHAT = 0;
	static final int CLEAR_WHAT = 2;

	private AlertDialog mFilterDialog;

	private ListView mLogList;
	private LogEntryAdapter mLogEntryAdapter;
	private MenuItem mPlayItem;
	private MenuItem mFilterItem;

	private Level mLastLevel = Level.V;
	private Logcat mLogcat;
	private Prefs mPrefs;
	private LogActivity mThis;
	private boolean mPlay = true;

	private SaveScheduler mSaveScheduler;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CAT_WHAT:
				final String line = (String) msg.obj;
				cat(line);
				break;
			case CLEAR_WHAT:
				mLogList.post(new Runnable() {
					public void run() {
						mLogEntryAdapter.clear();
					}
				});
				break;
			}
		}
	};

	private LogActivity.State getState() {
		State state = new State();

		state.entries = mLogEntryAdapter.getLogEntries();
		state.play = mPlay;
		int x = mLogList.getScrollX();
		int y = mLogList.getScrollY();
		state.position = mLogList.pointToPosition(x, y);

		return state;
	}

	private void setState(final LogActivity.State state) {
		if (state != null) {
			if (!state.play) {
				mLogEntryAdapter = new LogEntryAdapter(this, R.layout.entry,
						state.entries);
				setListAdapter(mLogEntryAdapter);
				mPlay = false;
			}
			mLogList.post(new Runnable() {
				public void run() {
					mLogList.setSelection(state.position);
				}
			});
		}
	}

	private void jumpStart() {
		mLogList.post(new Runnable() {
			public void run() {
				mLogList.setSelection(0);
			}
		});
	}

	private void jumpBottom() {
		mLogList.post(new Runnable() {
			public void run() {
				mLogList.setSelection(mLogEntryAdapter.getCount() - 1);
			}
		});
	}

	private void cat(String s) {
		if (mLogEntryAdapter.getCount() > WINDOW_SIZE) {
			mLogList.post(new Runnable() {
				public void run() {
					mLogEntryAdapter.remove(0);
				}
			});
		}

		Format format = mPrefs.getFormat();
		Level level = format.getLevel(s);
		if (level == null) {
			level = mLastLevel;
		} else {
			mLastLevel = level;
		}

		final LogEntry entry = new LogEntry(s, level);

		mLogEntryAdapter.add(entry);
		if (mPrefs.isAutoScroll() && isNextLogBottom()) {
			mLogList.post(new Runnable() {
				public void run() {
					jumpBottom();
				}
			});
		}
	}

	private boolean isNextLogBottom() {
		return mLogList.getLastVisiblePosition() == -1
				|| mLogList.getLastVisiblePosition() + 1 == mLogList.getCount() - 1;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.log);

		mThis = this;
		mPrefs = new Prefs(this);

		mSaveScheduler = new SaveScheduler(this);

		mLogList = (ListView) findViewById(android.R.id.list);
		mLogList.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				MenuItem jumpTopItem = menu.add(0, MENU_JUMP_TOP, 0,
						R.string.jump_start_menu);
				jumpTopItem.setIcon(android.R.drawable.ic_media_previous);

				MenuItem jumpBottomItem = menu.add(0, MENU_JUMP_BOTTPM, 0,
						R.string.jump_end_menu);
				jumpBottomItem.setIcon(android.R.drawable.ic_media_next);
			}
		});

		Log.v("alogcat", "created");
	}

	@Override
	public void onStart() {
		super.onStart();
		mLogList.setBackgroundColor(mPrefs.getBackgroundColor().getColor());

		final State state = (State) getLastNonConfigurationInstance();
		if (state != null) {
			setState(state);
		} else {
			mLogEntryAdapter = new LogEntryAdapter(this, R.layout.entry,
					new ArrayList<LogEntry>());
			setListAdapter(mLogEntryAdapter);
			reset();
		}

		if (mPrefs.isPeriodicSave()) {
			mSaveScheduler.start();
		} else {
			mSaveScheduler.stop();
		}

		Log.v("alogcat", "started");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.v("alogcat", "resumed");
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.v("alogcat", "paused");
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (mPlay) {
			return null;
		} else {
			return getState();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mLogcat != null) {
			mLogcat.stop();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v("alogcat", "destroyed");
	}

	@Override
	protected void onSaveInstanceState(Bundle b) {
		Log.v("alogcat", "save instance");
	}

	@Override
	protected void onRestoreInstanceState(Bundle b) {
		Log.v("alogcat", "restore instance");
	}

	public void reset() {
		Toast.makeText(this, R.string.reading_logs, Toast.LENGTH_LONG).show();
		mLastLevel = Level.V;

		if (mLogcat != null) {
			mLogcat.stop();
		}

		mPlay = true;
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
		if (mPlay) {
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
			File f = save();
			String msg = getResources().getString(R.string.saving_log,
					f.toString());
			Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
			return true;
		case MENU_PLAY:
			if (mPlay) {
				mLogcat.setPlay(false);
				mPlay = false;
			} else {
				if (mLogcat != null) {
					mLogcat.setPlay(true);
					mPlay = true;
				} else {
					reset();
				}
			}
			return true;
		case MENU_CLEAR:
			clear();
			reset();
			return true;
		case MENU_PREFS:
			Intent intent = new Intent(this, PrefsActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_JUMP_TOP:
			Toast.makeText(this, "Jumping to top of log ...",
					Toast.LENGTH_SHORT).show();
			jumpStart();
			return true;
		case MENU_JUMP_BOTTPM:
			Toast.makeText(this, "Jumping to bottom of log ...",
					Toast.LENGTH_SHORT).show();
			jumpBottom();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void clear() {
		try {
			Runtime.getRuntime().exec(new String[] { "logcat", "-c" });
		} catch (IOException e) {
			Log.e("alogcat", "error clearing log", e);
		} finally {
		}
	}

	private String dump(boolean html) {
		StringBuilder sb = new StringBuilder();
		Level lastLevel = Level.V;

		// make copy to avoid CME
		List<LogEntry> entries = new ArrayList<LogEntry>(
				mLogEntryAdapter.getLogEntries());

		for (LogEntry le : entries) {
			if (!html) {
				sb.append(le.getText());
				sb.append('\n');
			} else {
				Level level = le.getLevel();
				if (level == null) {
					level = lastLevel;
				} else {
					lastLevel = level;
				}
				sb.append("<font color=\"");
				sb.append(level.getHexColor());
				sb.append("\" face=\"sans-serif\"><b>");
				sb.append(TextUtils.htmlEncode(le.getText()));
				sb.append("</b></font><br/>\n");
			}
		}

		return sb.toString();
	}

	private void send() {
		new Thread(new Runnable() {
			public void run() {
				boolean html = mPrefs.isEmailHtml();
				String content = dump(html);

				Intent emailIntent = new Intent(
						android.content.Intent.ACTION_SEND);
				// emailIntent.setType(mPrefs.isEmailHtml() ? "text/html"
				// : "text/plain");
				emailIntent.setType("message/rfc822");
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
						"Android Log: " + LOG_DATE_FORMAT.format(new Date()));
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
						html ? Html.fromHtml(content) : content);
				startActivity(Intent.createChooser(emailIntent, "Send log ..."));
			}
		}).start();

	}

	private File save() {
		final File path = new File("/sdcard/alogcat");
		final File file = new File(path + "/alogcat."
				+ LogSaver.LOG_FILE_FORMAT.format(new Date()) + ".txt");

		String msg = "saving log to: " + file.toString();
		Log.d("alogcat", msg);

		new Thread(new Runnable() {
			public void run() {
				String content = dump(false);

				if (!path.exists()) {
					path.mkdir();
				}

				BufferedWriter bw = null;
				try {
					file.createNewFile();
					bw = new BufferedWriter(new FileWriter(file), 1024);
					bw.write(content);
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

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case FILTER_DIALOG:
			mFilterDialog = new FilterDialog(this);
			return mFilterDialog;
		}
		return null;
	}

}