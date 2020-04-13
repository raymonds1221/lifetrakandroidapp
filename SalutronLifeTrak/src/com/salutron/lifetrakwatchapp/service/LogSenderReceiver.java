package com.salutron.lifetrakwatchapp.service;

import java.io.File;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.LifeTrakApplication;
import com.salutron.lifetrakwatchapp.util.LifeTrakLogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;

public class LogSenderReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent intent) {
		//final File file = new File(LifeTrakApplication.PUBLIC_DIR, "logcat.txt");
		final File latestFile = LifeTrakLogger.lastFileModified(LifeTrakLogger.getLogPathDIR());
		// File file = new File(LifeTrakLogger.getLogPath());
		final String subject = ctx.getString(R.string.subject_title);

		emailWithIntent(ctx, subject, "", ctx.getString(R.string.app_name),
				Uri.fromFile(latestFile));
	}

	private void emailWithIntent(Context ctx, String subject, String body,
			String title, Uri attachment) {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);

		shareIntent.setType("message/rfc822");
		shareIntent.putExtra(Intent.EXTRA_EMAIL,
				new String[] { "appsupport@lifetrakusa.com" });
		shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		shareIntent.putExtra(Intent.EXTRA_TEXT, body);

		if (attachment != null) {
			shareIntent.putExtra(Intent.EXTRA_STREAM, attachment);
		}

		final Intent chooser = Intent.createChooser(shareIntent, title);
		chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(chooser);
	}

}
