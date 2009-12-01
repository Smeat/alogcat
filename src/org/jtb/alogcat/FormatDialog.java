package org.jtb.alogcat;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class FormatDialog extends AlertDialog {
	public static class Builder extends AlertDialog.Builder {
		private CatActivity mActivity;
		
		public Builder(CatActivity activity) {
			super(activity);

			this.mActivity = activity;

			setItems(R.array.format_entries, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					mActivity.setFormat(Format.getByOrder(which));					
				}
			});
			setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							mActivity.dismissDialog(CatActivity.LEVEL_DIALOG);
						}
					});
		}
	}

	public FormatDialog(Context context) {
		super(context);
	}
}
