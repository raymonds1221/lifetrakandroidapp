package com.salutron.lifetrakwatchapp.fragment.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * DialogFragment which wraps a simple AlertDialog
 *
 * @author Darwin Bautista
 */
public final class AlertDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

	public interface OnClickListener {
		int BUTTON_POSITIVE = DialogInterface.BUTTON_POSITIVE;
		int BUTTON_NEGATIVE = DialogInterface.BUTTON_NEGATIVE;

		void onClick(AlertDialogFragment dialogFragment, int which);
	}

	private static final String ARG_TITLE = "title";
	private static final String ARG_MESSAGE = "message";
	private static final String ARG_POSITIVE_BUTTON_TEXT = "positive_button_text";
	private static final String ARG_NEGATIVE_BUTTON_TEXT = "negative_button_text";
	private static final String ARG_DO_NOT_SHOW_PROMPT = "do_not_show_prompt";

	private OnClickListener onClickListener;

	public static AlertDialogFragment newInstance(int titleId, int messageId, int positiveButtonTextId) {
		return newInstance(titleId, messageId, positiveButtonTextId, 0);
	}

	public static AlertDialogFragment newInstance(int titleId, int messageId, int positiveButtonTextId, int negativeButtonTextId) {
		final Bundle args = new Bundle();
		args.putInt(ARG_TITLE, titleId);
		args.putInt(ARG_MESSAGE, messageId);
		args.putInt(ARG_POSITIVE_BUTTON_TEXT, positiveButtonTextId);
		args.putInt(ARG_NEGATIVE_BUTTON_TEXT, negativeButtonTextId);

		final AlertDialogFragment fragment = new AlertDialogFragment();
		fragment.setArguments(args);
		fragment.setCancelable(false);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Target fragment takes precedence over parent Activity
		final Fragment fragment = getTargetFragment();
		if (fragment instanceof OnClickListener) {
			onClickListener = (OnClickListener) fragment;
		} else {
			try {
				onClickListener = (OnClickListener) activity;
			} catch (ClassCastException e) {
				// Ignore, it means activity is not interested in getting feedback
			}
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		onClickListener = null;
	}

	/**
	 * Should be used by other Fragments in order to be set as listeners
	 * @param fragment
	 * @param tag
	 */
	public void show(Fragment fragment, String tag) {
		// Prevent duplicate dialogs
		if (fragment.getFragmentManager().findFragmentByTag(tag) != null) {
			return;
		}
		if (fragment instanceof OnClickListener) {
			setTargetFragment(fragment, 0);
		}
		show(fragment.getFragmentManager(), tag);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Bundle args = getArguments();
		if (args == null) {
			return super.onCreateDialog(savedInstanceState);
		}

		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
				.setMessage(args.getInt(ARG_MESSAGE))
				.setPositiveButton(args.getInt(ARG_POSITIVE_BUTTON_TEXT), this);

		final int titleId = args.getInt(ARG_TITLE);
		if (titleId != 0) {
			builder.setTitle(titleId);
		}

		final int negativeButtonTextId = args.getInt(ARG_NEGATIVE_BUTTON_TEXT);
		if (negativeButtonTextId != 0) {
			builder.setNegativeButton(negativeButtonTextId, this);
		}

		return builder.create();
	}

	@Override
	public void onClick(DialogInterface dialogInterface, int which) {
		if (onClickListener != null) {
			onClickListener.onClick(this, which);
		}
	}
}
