package com.benq.ifp.neolauncher.widget;

import android.content.Context;
import android.content.DialogInterface;

public class OptionsDialog {
	public static void show(Context context,
			int titleId,
			CharSequence[] items,
			DialogInterface.OnClickListener onClickListener) {
		Dialog.newDialog(context)
				.setTitle(titleId)
				.setItems(items, onClickListener)
				.show();
	}
}
