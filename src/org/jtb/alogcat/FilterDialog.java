package org.jtb.alogcat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class FilterDialog extends AlertDialog {
	public static class Builder extends AlertDialog.Builder {
		private LogActivity mActivity;

		public Builder(LogActivity activity) {
			super(activity);

			this.mActivity = activity;

			LayoutInflater factory = LayoutInflater.from(mActivity);
			final View view = factory.inflate(R.layout.filter_dialog, null);
			final EditText filterEdit = (EditText) view
					.findViewById(R.id.filter_edit);

			setView(view);
			setTitle(R.string.filter_dialog_title);
			setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							mActivity.setFilter(filterEdit.getText().toString());
							mActivity.dismissDialog(LogActivity.FILTER_DIALOG);
						}
					});
			setNeutralButton(R.string.clear,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							mActivity.setFilter(null);
							filterEdit.setText(null);
							mActivity.dismissDialog(LogActivity.FILTER_DIALOG);
						}
					});
			setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							mActivity.dismissDialog(LogActivity.FILTER_DIALOG);
						}
					});
		}
	}

	public FilterDialog(Context context) {
		super(context);
	}
}
