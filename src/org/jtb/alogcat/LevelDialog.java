package org.jtb.alogcat;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class LevelDialog extends AlertDialog {
	public static class Builder extends AlertDialog.Builder {
		private LogActivity mActivity;

		public Builder(LogActivity activity) {
			super(activity);

			this.mActivity = activity;

			setItems(R.array.level_entries, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					mActivity.setLevel(Level.getByOrder(which));
				}
			});
			setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							mActivity.dismissDialog(LogActivity.LEVEL_DIALOG);
						}
					});
		}
	}

	public LevelDialog(Context context) {
		super(context);
	}
}
