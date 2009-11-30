package org.jtb.alogcat;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class CatActivity extends Activity {
	private static final int WINDOW_SIZE = 100;
	
	static final int CAT_WHAT = 0;
	static final int ENDSCROLL_WHAT = 1;

	private LinearLayout mCatLayout;
	private ScrollView mCatScroll;
	
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
			}
		}
	};

	private void cat(String s) {
		if (mCatLayout.getChildCount() > WINDOW_SIZE) {
			mCatLayout.removeViewAt(0);
		}

		Entry e = new Entry(s);
		TextView entryText = new TextView(this);
		entryText.setText(e.getText());
		entryText.setTextColor(e.getLevel().getColor());
		entryText.setTextSize(10);
		mCatLayout.addView(entryText);
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.log);

		mCatScroll = (ScrollView) findViewById(R.id.cat_scroll);
		mCatLayout = (LinearLayout) findViewById(R.id.cat_layout);
		
		reset();
	}

	private void reset() {
		new Thread(new Runnable() {
			public void run() {
				Logcat lc = new Logcat(Level.V, null);
				lc.cat(mHandler);
			}
		}).start();
	}
}