package org.jtb.alogcat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class TextsizeDialog extends AlertDialog {
	public static class Builder extends AlertDialog.Builder {
		private LogActivity mActivity;

		public Builder(LogActivity activity) {
			super(activity);

			this.mActivity = activity;

			setItems(R.array.textsize_entries, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					mActivity.setTextsize(Textsize.getByOrder(which));
				}
			});
			setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							mActivity.dismissDialog(LogActivity.TEXTSIZE_DIALOG);
						}
					});
		}
	}

	public TextsizeDialog(Context context) {
		super(context);
	}
}
