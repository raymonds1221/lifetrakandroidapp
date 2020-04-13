package com.salutron.lifetrakwatchapp.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.common.base.Preconditions;

import java.util.HashSet;
import java.util.Set;

/**
 * Contains the boilerplate code for handling the resolution of connection failures.
 *
 * The behavior is similar to an automanaged client, except that the client is
 * not connected in onStart() and disconnected in onStop().
 *
 * This class can be used with any Activity or Fragment instance, provided that
 * the following are hooked to their corresponding methods:
 *
 * {@link #onCreate(Bundle)}
 * {@link #onSaveInstanceState(Bundle)}
 * {@link #onActivityResult(int, int, Intent)}
 *
 * @author Darwin Bautista
 */
public final class GoogleApiClientManager implements GoogleApiClient.OnConnectionFailedListener, DialogInterface.OnCancelListener {

	public interface Provider {
		GoogleApiClientManager getGoogleApiClientManager();
	}

	private static final String TAG = GoogleApiClientManager.class.getName();

	/**
	 * Track whether an authorization activity is stacking over the current activity, i.e. when
	 * a known auth error is being resolved, such as showing the account chooser or presenting a
	 * consent dialog. This avoids common duplications as might happen on screen rotations, etc.
	 */
	private static final String STATE_RESOLVING_ERROR = "resolving_error";
	private boolean resolvingError;

	private final GoogleApiClient client;
	private final Set<GoogleApiClient.OnConnectionFailedListener> connectionFailedListeners;
	private final Activity activity;

	public GoogleApiClientManager(Activity activity) {
		this.activity = Preconditions.checkNotNull(activity);
		this.client = GoogleFitHelper.configureClient(new GoogleApiClient.Builder(activity))
						.addOnConnectionFailedListener(this)
						.build();
		this.connectionFailedListeners = new HashSet<>();
	}

	/**
	 * Hook this to parent's onCreate() callback
	 * @param savedInstanceState
	 */
	public void onCreate(Bundle savedInstanceState) {
		// Restore error resolution state of Google API Client
		if (savedInstanceState != null) {
			resolvingError = savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
		}
	}

	/**
	 * Hook this to parent's onSaveInstanceState() callback
	 * @param outState
	 */
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(STATE_RESOLVING_ERROR, resolvingError);
	}

	/**
	 * Hook this to parent's onActivityResult() callback
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SalutronLifeTrakUtility.REQUEST_GOOGLE_FIT_OAUTH) {
			resolvingError = false;
			if (resultCode == Activity.RESULT_OK) {
				// Make sure the app is not already connected or attempting to connect
				if (!client.isConnecting() && !client.isConnected()) {
					// If connect() is called when there's no network connection, a dialog with message
					// "Unknown issue with Google Play Services" will be shown automatically.
					// However, this manager won't be notified that connect() failed.
					// As a workaround, prevent the said scenario and preemptively notify the listener
					// that the connection attempt failed.
					if (NetworkUtil.getInstance(client.getContext()).isNetworkAvailable()) {
						client.connect();
					} else {
						fireConnectionFailedEvent(new ConnectionResult(ConnectionResult.NETWORK_ERROR, null));
					}
				}
			} else if (resultCode == Activity.RESULT_CANCELED) {
				fireConnectionFailedEvent(new ConnectionResult(ConnectionResult.CANCELED, null));
			}
		}
	}

	/**
	 * Get the managed GoogleApiClient instance
	 * @return managed client instance
	 */
	public GoogleApiClient getClient() {
		return client;
	}

	public void registerConnectionCallbacks(GoogleApiClient.ConnectionCallbacks callbacks) {
		client.registerConnectionCallbacks(callbacks);
	}

	public boolean isConnectionCallbacksRegistered(GoogleApiClient.ConnectionCallbacks callbacks) {
		return client.isConnectionCallbacksRegistered(callbacks);
	}

	public void unregisterConnectionCallbacks(GoogleApiClient.ConnectionCallbacks callbacks) {
		client.unregisterConnectionCallbacks(callbacks);
	}

	public void registerConnectionFailedListener(GoogleApiClient.OnConnectionFailedListener listener) {
		connectionFailedListeners.add(listener);
	}

	public boolean isConnectionFailedListenerRegistered(GoogleApiClient.OnConnectionFailedListener listener) {
		return connectionFailedListeners.contains(listener);
	}

	public void unregisterConnectionFailedListener(GoogleApiClient.OnConnectionFailedListener listener) {
		connectionFailedListeners.remove(listener);
	}

	private void fireConnectionFailedEvent(ConnectionResult result) {
		for (final GoogleApiClient.OnConnectionFailedListener listener : connectionFailedListeners) {
			listener.onConnectionFailed(result);
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// If signed in user was changed, clear the Google Fit prefs
		if (result.getErrorCode() == CommonStatusCodes.SIGN_IN_REQUIRED) {
			PreferenceWrapper.getInstance(activity).clearGoogleFitPrefs();
		}
		Log.i(TAG, "Connection failed. Cause: " + result.toString());
		// Don't do anything if error is already being resolved
		if (resolvingError) {
			return;
		}
		resolvingError = true;

		if (result.hasResolution()) {
			try {
				// The failure has a resolution. Resolve it.
				// Called typically when the app is not yet authorized, and an
				// authorization dialog is displayed to the user.
				result.startResolutionForResult(activity, SalutronLifeTrakUtility.REQUEST_GOOGLE_FIT_OAUTH);
			} catch (IntentSender.SendIntentException e) {
				Log.e(TAG, "Exception while starting resolution activity", e);
				// There was an error with the resolution intent. Try again.
				resolvingError = false;
				client.connect();
			}
		} else if (GoogleApiAvailability.getInstance().isUserResolvableError(result.getErrorCode())) {
			// Show the localized error dialog
			GoogleApiAvailability.getInstance().showErrorDialogFragment(activity, result.getErrorCode(),
					SalutronLifeTrakUtility.REQUEST_GOOGLE_FIT_OAUTH, this);
		} else {
			fireConnectionFailedEvent(result);
		}
	}

	@Override
	public void onCancel(DialogInterface dialogInterface) {
		resolvingError = false;
		fireConnectionFailedEvent(new ConnectionResult(ConnectionResult.CANCELED, null));
	}
}
