package com.salutron.lifetrakwatchapp.adapter;

import static com.salutron.lifetrakwatchapp.util.SalutronLifeTrakUtility.TAG;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import android.media.ExifInterface;
import android.os.Handler;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.salutron.lifetrak.R;
import com.salutron.lifetrakwatchapp.model.UserProfile;
import com.salutron.lifetrakwatchapp.util.NetworkUtil;
import com.salutron.lifetrakwatchapp.web.AsyncListener;
import com.salutron.lifetrakwatchapp.web.DownloadImageTask;
import com.salutron.lifetrakwatchapp.web.FBImageTask;
import com.salutron.lifetrakwatchapp.fragment.BaseFragment;

public class MenuItemAccount implements MenuItem {
	private Context mContext;
	private UserProfile mUserProfile;
	private final Handler mHandler = new Handler();
	private Bitmap mBitmap;

	public MenuItemAccount() {

	}

	public MenuItemAccount(Context context, UserProfile userProfile) {
		mContext = context;
		mUserProfile = userProfile;
	}

	@Override
	public int getItemViewType() {
		return MENU_VIEW_TYPE_ACCOUNT;
	}

	@Override
	public View getView(LayoutInflater inflater, View convertView) {
		View view = inflater.inflate(R.layout.adapter_menu_account, null);

		ImageView profilePic = (ImageView) view.findViewById(R.id.imgProfilePic);

		if (null != mUserProfile.getProfileImageLocal() && !"".equals(mUserProfile.getProfileImageLocal())) {
			File file = new File(mUserProfile.getProfileImageLocal());

			if (file.exists()) {
				showPictureFromLocal(profilePic, mUserProfile.getProfileImageLocal());
			}
		} else if (null != mUserProfile.getProfileImageWeb() && !mUserProfile.getProfileImageWeb().equals("null")) {
			showPictureFromWeb(profilePic, mUserProfile.getProfileImageWeb());
		}

		TextView name = (TextView) view.findViewById(R.id.tvwName);
		name.setText(mUserProfile.getFirstname() + " " + mUserProfile.getLastname());

		return view;
	}

	@Override
	public int getItemType() {
		return MENU_ITEM_ACCOUNT;
	}

	private void showPictureFromLocal(ImageView view, String path) {
		orientProfileImage(path);
		mBitmap = createSquareBitmap(mBitmap);
		view.setImageBitmap(makeRoundedBitmap(mBitmap, (int) dpToPx(100), (int) dpToPx(100)));
	}

	private void showPictureFromWeb(final ImageView view, final String path) {

		if (NetworkUtil.getInstance(mContext).isNetworkAvailable()) {
			DownloadImageTask imageTask = new DownloadImageTask() {
				@Override
				protected void onPostExecute(Bitmap result) {
					if(null != result){
						view.setImageBitmap(makeRoundedBitmap(result, (int) dpToPx(100), (int) dpToPx(100)));
					}
				}
			};
			view.setTag(path);
			imageTask.execute(view);
		}
	}

	private Bitmap makeRoundedBitmap(Bitmap bitmap, int width, int height) {
		Bitmap targetBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(targetBitmap);
		Path path = new Path();
		path.addCircle(width / 2, height / 2, height / 2, Path.Direction.CCW);
		canvas.clipPath(path);
		canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), new Rect(0, 0, targetBitmap.getWidth(), targetBitmap.getHeight()), null);
		return targetBitmap;
	}

	private int dpToPx(int dp) {
		return dp * (mContext.getResources().getDisplayMetrics().densityDpi / 160);
	}

	/*private void orientProfileImage(String imgPath) {
		try {
			ExifInterface exif = new ExifInterface(imgPath);
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
			Matrix matrix = new Matrix();
			if (orientation == 6) {
				matrix.postRotate(90);
			} else if (orientation == 3) {
				matrix.postRotate(180);
			} else if (orientation == 8) {
				matrix.postRotate(270);
			}
			mBitmap = BitmapFactory.decodeFile(imgPath);
			mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true); // rotatingbitmap
			mBitmap = createScaledBitmap(mBitmap, dpToPx(200), dpToPx(200));
		} catch (IOException e) {
			Log.e("image-rotation", e.getMessage());
		}
	}*/
	
	private void orientProfileImage(String imgPath) {
		try {
			ExifInterface exif = new ExifInterface(imgPath);
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			int rotateXDegrees = 0;
			if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
				rotateXDegrees = 90;
			} else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
				rotateXDegrees = 180;
			} else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
				rotateXDegrees = 270;
			}
			mBitmap = BitmapFactory.decodeFile(imgPath);
			mBitmap = BaseFragment.shrinkBitmap(mBitmap, 300, rotateXDegrees);
		} catch (IOException e) {
			Log.e("image-rotation", e.getMessage());
		}
	}

	protected Bitmap createScaledBitmap(Bitmap originalBitmap, float width, float height) {
		int originalWidth = originalBitmap.getWidth();
		int originalHeight = originalBitmap.getHeight();

		Bitmap bitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		float scale = width / originalWidth;
		float xtranslation = 0.0f;
		float ytranslation = (height - originalHeight * scale) / 2.0f;
		Matrix matrix = new Matrix();

		if (originalWidth > originalHeight)
			matrix.setRotate(90, originalWidth / 2, originalHeight / 2);

		matrix.postTranslate(xtranslation, ytranslation);
		matrix.postScale(scale, scale);

		Paint paint = new Paint();
		paint.setFilterBitmap(true);
		canvas.drawBitmap(originalBitmap, matrix, paint);

		return bitmap;
	}
	
	private Bitmap createSquareBitmap(Bitmap src) {
		Bitmap bitmap = null;
		
		if (src.getWidth() >= src.getHeight()) {
			bitmap = Bitmap.createBitmap(src, (src.getWidth() / 2) - (src.getHeight() / 2), 0, src.getHeight(), src.getHeight());
		} else {
			bitmap = Bitmap.createBitmap(src, 0, (src.getHeight() / 2) - (src.getWidth() / 2), src.getWidth(), src.getWidth());
		}
		
		return bitmap;
	}
}
