package com.salutron.lifetrakwatchapp.fragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import roboguice.inject.InjectView;
import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.adapter.MenuAdapter;
import com.salutron.lifetrakwatchapp.adapter.MenuItem;
import com.salutron.lifetrakwatchapp.adapter.MenuItemAccount;
import com.salutron.lifetrakwatchapp.adapter.MenuItemPage;
import com.salutron.lifetrakwatchapp.adapter.MenuItemSeparator;
import com.salutron.lifetrakwatchapp.model.Menu;

public class MenuFragment extends BaseFragment {
	private ListView mListMenu;
	private MenuItemAccount mMenuItemAccount;
	private List<MenuItem> mMenus;
	@InjectView(R.id.tvwLastSyncDate)
	private TextView mLastSyncDate;
	@InjectView(R.id.tvwAppVersion)
	private TextView mAppVersion;
	@InjectView(R.id.tvwWatchFirmWare)
	private TextView mWatchFirmware;

	private final SimpleDateFormat mDateFormat = (SimpleDateFormat) DateFormat.getInstance();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_menu, null);
		mMenus = new ArrayList<MenuItem>() {
			private static final long serialVersionUID = 1L;
			{
				// add(new
				// MenuItemAccount(getLifeTrakApplication().getUserProfile().getFirstname(),
				// getLifeTrakApplication().getUserProfile().getLastname()));
				add(new MenuItemSeparator());
				add(new MenuItemPage(new Menu(R.drawable.lt_assets_menu_dash, getString(R.string.nav_dashboard), true, MENU_ITEM_DASHBOARD)));
				add(new MenuItemPage(new Menu(R.drawable.lt_assets_menu_goals, getString(R.string.nav_goals), false, MENU_ITEM_GOALS)));
				add(new MenuItemSeparator());
				add(new MenuItemPage(new Menu(R.drawable.lt_assets_menu_settings, getString(R.string.nav_settings), true, MENU_ITEM_SETTINGS)));
				add(new MenuItemPage(new Menu(R.drawable.assets_menu_partners, getString(R.string.nav_partners), true, MENU_ITEM_PARTNERS)));
				add(new MenuItemPage(new Menu(R.drawable.lt_assets_menu_help, getString(R.string.nav_help), true, MENU_ITEM_HELP)));
				add(new MenuItemPage(new Menu(R.drawable.lt_assets_menu_logout, getString(R.string.nav_signout), true, MENU_ITEM_LOGOUT)));
			}
		};
		
		
		mListMenu = (ListView) view.findViewById(R.id.lstMenu);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		try {
			if (null != getLifeTrakApplication() &&
					null != getLifeTrakApplication().getUserProfile() &&
					null != getLifeTrakApplication().getUserProfile().getProfileImageWeb() &&
					getLifeTrakApplication().getUserProfile().getProfileImageWeb().contains("normal")) {
				
				fetchFbImage(getLifeTrakApplication().getUserProfile().getProfileImageWeb());
			} else {
				initializeObjects();
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void fetchFbImage(String fbImageUrl) {
		AQuery aq = new AQuery(getActivity());
		aq.ajax(fbImageUrl, JSONObject.class, new AjaxCallback<JSONObject>(){
			@Override
			public void callback(String url, JSONObject object, AjaxStatus status) {
				super.callback(url, object, status);
				if(object.has("data")){
					try {
						getLifeTrakApplication().getUserProfile().setProfileImageWeb(object.getJSONObject("data").getString("url"));
						getLifeTrakApplication().getUserProfile().update();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					initializeObjects();
				} catch (NotFoundException e) {
					e.printStackTrace();
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
			}
		});
		
	}

	public void initializeObjects() throws NotFoundException, NameNotFoundException {
		updateLastSyncDate();

		mMenuItemAccount = new MenuItemAccount(getActivity(), getLifeTrakApplication().getUserProfile());

		if (mMenus.get(0) instanceof MenuItemAccount) {
			mMenus.remove(0);
		}

		mMenus.add(0, mMenuItemAccount);

		MenuAdapter adapter = new MenuAdapter(getActivity(), R.layout.adapter_menu, mMenus);
		mListMenu.setAdapter(adapter);

		final Activity activity = getActivity();

		if (activity instanceof AdapterView.OnItemClickListener) {
			final AdapterView.OnItemClickListener listener = (AdapterView.OnItemClickListener) getActivity();
			mListMenu.setOnItemClickListener(listener);
		}
	}

	public void updateLastSyncDate() throws NotFoundException, NameNotFoundException {
		switch (getLifeTrakApplication().getTimeDate().getHourFormat()) {
		case TIME_FORMAT_12_HR:
			mDateFormat.applyPattern("MMMM dd, yyyy hh:mm aa");
			break;
		case TIME_FORMAT_24_HR:
			mDateFormat.applyPattern("MMMM dd, yyyy HH:mm");
			break;
		}
		/*
		String appVersion = getResources().getString(R.string.app_version, getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName,
				mPreferenceWrapper.getPreferenceStringValue(SDK_VERSION));
		*/
		String appVersion = String.format("App Version: %s(%d)", getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName,
											getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionCode);
		
		if    (mPreferenceWrapper.getPreferenceStringValue(SDK_VERSION) != null) {
			appVersion += "| SDK Version: " + mPreferenceWrapper.getPreferenceStringValue(SDK_VERSION);
		}
		if (getLifeTrakApplication().getSelectedWatch() != null){
			String watchFirmWare =  ""  ;
			String firmWare = getLifeTrakApplication().getSelectedWatch().getWatchFirmWare();
			String softWare = getLifeTrakApplication().getSelectedWatch().getWatchSoftWare();
			if (firmWare != null && firmWare.length() > 0){
				watchFirmWare ="Version: "+ watchFirmWare + firmWare + " | ";
			}

			if (softWare != null && softWare.length() > 0){
				watchFirmWare = watchFirmWare  + "Software Version: "+ softWare;
			}
			else{
				watchFirmWare = watchFirmWare.replace("|", "");
			}
			mWatchFirmware.setText(watchFirmWare);


		}
		mLastSyncDate.setText(getResources().getString(R.string.last_sync_date, mDateFormat.format(getLifeTrakApplication().getSelectedWatch().getLastSyncDate())));
		mAppVersion.setText(appVersion);
	}
}
